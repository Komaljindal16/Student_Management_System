package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.ArrayList;

public class AddStudentsScreen extends Application {

    private DatabaseManager dbManager;

    public AddStudentsScreen() {
        dbManager = new DatabaseManager();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);

        // Create the UI components
        Label studentNameLabel = new Label("Student Name:");
        TextField studentNameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();

        Label registrationDateLabel = new Label("Registration Date:");
        DatePicker registrationDatePicker = new DatePicker();

        Label coursesLabel = new Label("Courses to Enroll:");

        // This will hold checkboxes for each course
        VBox coursesCheckBoxContainer = new VBox(5);
        List<CheckBox> courseCheckBoxes = new ArrayList<>();

        // Populate checkboxes with courses fetched from the database
        populateCoursesCheckBoxes(coursesCheckBoxContainer, courseCheckBoxes);

        Button addStudentButton = new Button("Add Student");

        addStudentButton.setOnAction(e -> {
            // Collect the inputs and call the addStudent method
            addStudent(studentNameField.getText(), emailField.getText(),
                    phoneField.getText(), registrationDatePicker.getValue(), courseCheckBoxes);
        });
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> {
            Main mainScreen = new Main();
            mainScreen.start(new Stage()); // Open the HomeScreen
            primaryStage.close(); // Close the current stage
        });
        // Add UI components to the layout
        root.getChildren().addAll(studentNameLabel, studentNameField, emailLabel, emailField,
                phoneLabel, phoneField, registrationDateLabel, registrationDatePicker,
                coursesLabel, coursesCheckBoxContainer, addStudentButton, backButton);

        // Create the scene and show the stage
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Add Student");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void populateCoursesCheckBoxes(VBox container, List<CheckBox> courseCheckBoxes) {
        // Query to get the courses from the database
        String query = "SELECT course_name FROM courses";
        List<List<String>> result = dbManager.executeQuery(query);

        // Add each course as a checkbox to the VBox container
        for (List<String> row : result) {
            String courseName = row.get(0);
            CheckBox courseCheckBox = new CheckBox(courseName);
            container.getChildren().add(courseCheckBox);
            courseCheckBoxes.add(courseCheckBox);
        }
    }

    private void addStudent(String name, String email, String phone, java.time.LocalDate registrationDate, List<CheckBox> courseCheckBoxes) {
        // Validate the input
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || registrationDate == null || courseCheckBoxes.isEmpty()) {
            showAlert("Error", "Please fill all fields and select at least one course.");
            return;
        }

        // SQL query to insert the student into the database
        String query = "INSERT INTO students (student_name, email, phone_number, registration_date) VALUES ('" + name + "', '" + email + "', '" + phone + "', '" + registrationDate + "')";
        try {
            // Insert the student into the students table
            dbManager.executeUpdate(query);

            // Get the last inserted student ID (auto-incremented)
            String studentIdQuery = "SELECT LAST_INSERT_ID()";
            List<List<String>> result = dbManager.executeQuery(studentIdQuery);
            if (result.isEmpty()) {
                showAlert("Error", "Failed to retrieve student ID.");
                return;
            }
            String studentId = result.get(0).get(0);

            // Loop through each selected course and insert the enrollment
            for (CheckBox courseCheckBox : courseCheckBoxes) {
                if (courseCheckBox.isSelected()) {
                    String courseName = courseCheckBox.getText();
                    
                    // Ensure the course exists
                    String courseIdQuery = "SELECT course_id FROM courses WHERE course_name = '" + courseName + "'";
                    List<List<String>> courseResult = dbManager.executeQuery(courseIdQuery);
                    
                    if (courseResult.isEmpty()) {
                        showAlert("Error", "Course " + courseName + " not found.");
                        return;
                    }
                    
                    String courseId = courseResult.get(0).get(0);

                    // Insert into student_courses table to link student and course
                    String enrollmentQuery = "INSERT INTO student_courses (student_id, course_id) VALUES ('" + studentId + "', '" + courseId + "')";
                    dbManager.executeUpdate(enrollmentQuery);
                }
            }

            // Show success message
            showAlert("Success", "Student added and enrolled in selected courses.");

        } catch (Exception ex) {
            // Show an error message if something goes wrong with the database
            showAlert("Error", "An error occurred while adding the student: " + ex.getMessage());
        }
    }

    // Method to show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        // Close database connection when the application stops
        dbManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
