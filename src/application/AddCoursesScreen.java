package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class AddCoursesScreen extends Application {

    private DatabaseManager dbManager;

    public AddCoursesScreen() {
        dbManager = new DatabaseManager();
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);

        // Section to Add New Course
        Label courseNameLabel = new Label("Course Name:");
        TextField courseNameField = new TextField();
        
        Label courseDescLabel = new Label("Course Description:");
        TextArea courseDescField = new TextArea();
        
        Button addCourseButton = new Button("Add Course");

        // Section to Update Course
        Label courseIdLabel = new Label("Enter Course ID for Update:");
        TextField courseIdField = new TextField();
        Button updateCourseButton = new Button("Update Course");
        Button validateCourseButton = new Button("Validate Course");

        // Section to Delete Course
        Button deleteCourseButton = new Button("Delete Course");
        
        Label updateCourseNameLabel = new Label("New Course Name:");
        TextField updateCourseNameField = new TextField();
        
        Label updateCourseDescLabel = new Label("New Course Description:");
        TextArea updateCourseDescField = new TextArea();
        
        // Show All Courses Button
        Button showCoursesButton = new Button("Show All Courses");

        // Add Course Action
        addCourseButton.setOnAction(e -> {
            addCourse(courseNameField.getText(), courseDescField.getText());
            courseNameField.clear();
            courseDescField.clear();
        });

        // Validate Course Action
        validateCourseButton.setOnAction(e -> {
            verifyCourseAndPopulateFields(courseIdField.getText(), updateCourseNameField, updateCourseDescField);
        });

        // Update Course Action
        updateCourseButton.setOnAction(e -> {
            updateCourse(courseIdField.getText(), updateCourseNameField.getText(), updateCourseDescField.getText());
            courseIdField.clear();
            updateCourseNameField.clear();
            updateCourseDescField.clear();
        });

        // Delete Course Action
        deleteCourseButton.setOnAction(e -> {
            deleteCourse(courseIdField.getText());
            courseIdField.clear();
            updateCourseNameField.clear();
            updateCourseDescField.clear();
        });

        // Show All Courses Action
        showCoursesButton.setOnAction(e -> showAllCourses());
        
        
        Button backButton = new Button("Back to Home");
        backButton.setOnAction(e -> {
            Main mainScreen = new Main();
            mainScreen.start(new Stage()); // Open the HomeScreen
            primaryStage.close(); // Close the current stage
        });

        root.getChildren().addAll(
            courseNameLabel, courseNameField, courseDescLabel, courseDescField,
            addCourseButton, courseIdLabel, courseIdField, validateCourseButton,
            updateCourseNameLabel, updateCourseNameField, updateCourseDescLabel, updateCourseDescField, updateCourseButton, deleteCourseButton, showCoursesButton, backButton
        );
        
       

        Scene scene = new Scene(root);
        primaryStage.setTitle("Course Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to Add Course
    private void addCourse(String name, String description) {
        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Both Course Name and Description are required.");
            return;
        }
        String query = "INSERT INTO courses (course_name, course_description) VALUES ('" + name + "', '" + description + "')";
        dbManager.executeUpdate(query);
        showAlert("Success", "Course added successfully.");
    }

 // Method to Update Course Details
    private void updateCourse(String courseId, String newName, String newDescription) {
        if (courseId.isEmpty() || newName.isEmpty() || newDescription.isEmpty()) {
            showAlert("Error", "Course ID, Name, and Description are required.");
            return;
        }

        // Check if the course exists
        String query = "SELECT * FROM courses WHERE course_id = '" + courseId + "'";
        List<List<String>> course = dbManager.executeQuery(query);
        
        if (course.isEmpty()) {
            showAlert("Error", "Course ID not found.");
        } else {
            // Update the course details if it exists
            query = "UPDATE courses SET course_name = '" + newName + "', course_description = '" + newDescription + "' WHERE course_id = '" + courseId + "'";
            dbManager.executeUpdate(query);
            showAlert("Success", "Course updated successfully.");
        }
    }


 // Method to Verify if Course Exists and Populate Fields for Update
    private void verifyCourseAndPopulateFields(String courseId, TextField updateCourseNameField, TextArea updateCourseDescField) {
        String query = "SELECT * FROM courses WHERE course_id = '" + courseId + "'";
        List<List<String>> course = dbManager.executeQuery(query);

        if (course.isEmpty()) {
            showAlert("Error", "Course ID not found.");
        } else {
            // Assuming the first row contains the course details
            List<String> courseDetails = course.get(0); // First row (course details)

            if (courseDetails.size() >= 2) {
                updateCourseNameField.setText(courseDetails.get(1));  // Assuming first column is course_name
                updateCourseDescField.setText(courseDetails.get(2));  // Assuming second column is course_description
            }
        }
    }



 // Method to Delete Course
    private void deleteCourse(String courseId) {
        if (courseId.isEmpty()) {
            showAlert("Error", "Course ID is required.");
            return;
        }
        
        String query = "SELECT * FROM courses WHERE course_id = '" + courseId + "'";
        List<List<String>> course = dbManager.executeQuery(query); // Change to List<List<String>>

        if (course.isEmpty()) {
            showAlert("Error", "Course ID not found.");
        } else {
            query = "DELETE FROM courses WHERE course_id = '" + courseId + "'";
            dbManager.executeUpdate(query);
            showAlert("Success", "Course deleted successfully.");
        }
    }


 // Method to Show All Courses
    private void showAllCourses() {
        String query = "SELECT * FROM courses";
        List<List<String>> courses = dbManager.executeQuery(query); // Change to List<List<String>>

        if (courses.isEmpty()) {
            showAlert("Error", "No courses available.");
        } else {
            StringBuilder courseDetails = new StringBuilder();
            for (List<String> course : courses) { // Loop over each row (course)
                // Assuming first column is course_id, second is course_name, and third is course_description
                if (course.size() >= 3) {
                    courseDetails.append("ID: ").append(course.get(0)) // Course ID
                            .append(", Name: ").append(course.get(1)) // Course Name
                            .append(", Description: ").append(course.get(2)) // Course Description
                            .append("\n");
                }
            }
            TextArea courseTextArea = new TextArea(courseDetails.toString());
            courseTextArea.setEditable(false);
            Stage courseStage = new Stage();
            VBox courseRoot = new VBox(10, new Label("All Courses:"), courseTextArea);
            Scene courseScene = new Scene(courseRoot, 500, 400);
            courseStage.setTitle("All Courses");
            courseStage.setScene(courseScene);
            courseStage.show();
        }
    }


    // Helper method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
