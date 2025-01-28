package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class ShowStudentDetailsScreen extends Application {

    private DatabaseManager dbManager;

    public ShowStudentDetailsScreen() {
        dbManager = new DatabaseManager();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);

        // Table to display student details
        TableView<StudentDetail> studentTable = new TableView<>();

        // Create columns for the table
        TableColumn<StudentDetail, String> studentNameColumn = new TableColumn<>("Student Name");
        studentNameColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());

        TableColumn<StudentDetail, String> studentIdColumn = new TableColumn<>("Student ID");
        studentIdColumn.setCellValueFactory(cellData -> cellData.getValue().studentIdProperty());

        TableColumn<StudentDetail, String> totalMarksColumn = new TableColumn<>("Total Marks");
        totalMarksColumn.setCellValueFactory(cellData -> cellData.getValue().totalMarksProperty());

        TableColumn<StudentDetail, String> percentageColumn = new TableColumn<>("Percentage");
        percentageColumn.setCellValueFactory(cellData -> cellData.getValue().percentageProperty());

        TableColumn<StudentDetail, String> feedbackColumn = new TableColumn<>("Feedback");
        feedbackColumn.setCellValueFactory(cellData -> cellData.getValue().feedbackProperty());

        // Add columns to the table
        studentTable.getColumns().addAll(studentNameColumn, studentIdColumn, totalMarksColumn, percentageColumn, feedbackColumn);

        // Load student details
        loadStudentDetails(studentTable);

        root.getChildren().add(studentTable);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Student Details");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to load student details from the database
    private void loadStudentDetails(TableView<StudentDetail> studentTable) {
        try (Connection conn = dbManager.getConnection()) {
            String query = "SELECT s.student_name, s.student_id, SUM(m.marks) AS total_marks, " +
                           "AVG(m.marks) AS average_marks FROM students s " +
                           "JOIN marks m ON s.student_id = m.student_id " +
                           "GROUP BY s.student_id";

            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String studentName = rs.getString("student_name");
                    String studentId = rs.getString("student_id");
                    int totalMarks = rs.getInt("total_marks");
                    double averageMarks = rs.getDouble("average_marks");
                    double percentage = (averageMarks / 100) * 100;
                    String feedback = (percentage >= 50) ? "Pass" : "Fail";

                    // Add student data to table
                    StudentDetail studentDetail = new StudentDetail(studentName, studentId, totalMarks, percentage, feedback);
                    studentTable.getItems().add(studentDetail);
                }
            }
        } catch (SQLException e) {
            showErrorMessage("Failed to load student details.");
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
