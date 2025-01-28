package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class AddMarksScreen extends Application {

    private DatabaseManager dbManager;

    public AddMarksScreen() {
        dbManager = new DatabaseManager();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);

        // Student ComboBox
        Label studentLabel = new Label("Student Name:");
        ComboBox<String> studentComboBox = new ComboBox<>();

        // Populate ComboBox with student names dynamically from the database
        loadStudents(studentComboBox);

        // ListView to display subjects
        Label subjectsLabel = new Label("Subjects:");
        ListView<HBox> subjectsListView = new ListView<>();

        // Feedback and Marks
        Label totalMarksLabel = new Label("Total Marks:");
        TextField totalMarksField = new TextField();
        totalMarksField.setEditable(false);

        Label percentageLabel = new Label("Percentage:");
        TextField percentageField = new TextField();
        percentageField.setEditable(false);

        Label feedbackLabel = new Label("Feedback:");
        TextArea feedbackArea = new TextArea();
        feedbackArea.setEditable(false);

        // Add Marks Button
        Button addMarksButton = new Button("Add Marks");

        // Event handler for adding marks
        addMarksButton.setOnAction(e -> {
            String student = studentComboBox.getSelectionModel().getSelectedItem();
            if (student != null) {
                addMarksForStudent(student, subjectsListView, totalMarksField, percentageField, feedbackArea);
            } else {
                showErrorMessage("Please select a student.");
            }
        });

        // Set up student selection listener to load corresponding subjects
        setupStudentSelectionListener(studentComboBox, subjectsListView);

        root.getChildren().addAll(studentLabel, studentComboBox, subjectsLabel, subjectsListView, addMarksButton,
                                  totalMarksLabel, totalMarksField, percentageLabel, percentageField,
                                  feedbackLabel, feedbackArea);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Add Marks");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Load students from the database
    private void loadStudents(ComboBox<String> studentComboBox) {
        try (Connection conn = dbManager.getConnection()) {
            String query = "SELECT student_name FROM students";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    studentComboBox.getItems().add(rs.getString("student_name"));
                }
            }
        } catch (SQLException e) {
            showErrorMessage("Failed to load students.");
        }
    }

    // Load subjects for the selected student
    private void loadSubjectsForStudent(ComboBox<String> studentComboBox, ListView<HBox> subjectsListView) {
        String studentName = studentComboBox.getSelectionModel().getSelectedItem();
        if (studentName != null) {
            try (Connection conn = dbManager.getConnection()) {
                String studentQuery = "SELECT student_id FROM students WHERE student_name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
                    pstmt.setString(1, studentName);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int studentId = rs.getInt("student_id");
                            String query = "SELECT c.course_name FROM student_courses sc " +
                                           "JOIN courses c ON sc.course_id = c.course_id " +
                                           "WHERE sc.student_id = ?";
                            try (PreparedStatement pstmtCourses = conn.prepareStatement(query)) {
                                pstmtCourses.setInt(1, studentId);
                                try (ResultSet rsCourses = pstmtCourses.executeQuery()) {
                                    subjectsListView.getItems().clear();
                                    while (rsCourses.next()) {
                                        String courseName = rsCourses.getString("course_name");
                                        TextField marksField = new TextField();
                                        marksField.setPromptText("Enter Marks for " + courseName);
                                        HBox subjectBox = new HBox(new Label(courseName), marksField);
                                        subjectsListView.getItems().add(subjectBox);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                showErrorMessage("Failed to load subjects.");
            }
        }
    }

    // Set up event listener for student selection to load corresponding subjects
    private void setupStudentSelectionListener(ComboBox<String> studentComboBox, ListView<HBox> subjectsListView) {
        studentComboBox.setOnAction(e -> loadSubjectsForStudent(studentComboBox, subjectsListView));
    }

   private void addMarksForStudent(String studentName, ListView<HBox> subjectsListView,
            TextField totalMarksField, TextField percentageField, TextArea feedbackArea) {
    try (Connection conn = dbManager.getConnection()) {
        // Get student_id from student name
        String studentQuery = "SELECT student_id FROM students WHERE student_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
            pstmt.setString(1, studentName);
            ResultSet studentRs = pstmt.executeQuery();
            if (studentRs.next()) {
                int studentId = studentRs.getInt("student_id");

                int totalMarks = 0;
                int totalSubjects = 0;

                // Insert marks for each subject
                for (HBox subjectBox : subjectsListView.getItems()) {
                    Label subjectLabel = (Label) subjectBox.getChildren().get(0);
                    TextField marksField = (TextField) subjectBox.getChildren().get(1);
                    String marksText = marksField.getText();

                    if (!marksText.isEmpty()) {
                        int marks = Integer.parseInt(marksText);
                        totalMarks += marks;
                        totalSubjects++;

                        String courseQuery = "SELECT course_id FROM courses WHERE course_name = ?";
                        try (PreparedStatement pstmtCourse = conn.prepareStatement(courseQuery)) {
                            pstmtCourse.setString(1, subjectLabel.getText());
                            try (ResultSet courseRs = pstmtCourse.executeQuery()) {
                                if (courseRs.next()) {
                                    int courseId = courseRs.getInt("course_id");

                                    // Insert marks into the database
                                    String insertMarksQuery = "INSERT INTO marks (student_id, course_id, marks) VALUES (?, ?, ?)";
                                    try (PreparedStatement pstmtMarks = conn.prepareStatement(insertMarksQuery)) {
                                        pstmtMarks.setInt(1, studentId);
                                        pstmtMarks.setInt(2, courseId);
                                        pstmtMarks.setInt(3, marks);
                                        pstmtMarks.executeUpdate();
                                        System.out.println("Marks inserted for " + subjectLabel.getText() + ": " + marks);
                                    }
                                } else {
                                    System.out.println("Course not found: " + subjectLabel.getText());
                                }
                            }
                        }
                    }
                }

                // Calculate total percentage
                if (totalSubjects > 0) {
                    double percentage = (double) totalMarks / (totalSubjects * 100) * 100;
                    percentageField.setText(String.format("%.2f", percentage));

                    // Provide feedback
                    String feedback = (percentage >= 50) ? "Pass" : "Fail";
                    feedbackArea.setText(feedback);

                    // Update the total marks field
                    totalMarksField.setText(String.valueOf(totalMarks));  // This is where the total marks are updated
                }
            }
        }
    } catch (SQLException e) {
        showErrorMessage("Failed to add marks.");
        e.printStackTrace();  // This will log the detailed error to help with debugging
    }
}

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
