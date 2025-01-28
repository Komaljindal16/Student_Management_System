package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);

        Button addCoursesButton = new Button("Add Courses");
        Button addStudentsButton = new Button("Add Students");
        Button addMarksButton = new Button("Add Marks");
        Button showStudentDetailsButton = new Button("Show Student Details");

        // Set actions for the buttons
        addCoursesButton.setOnAction(e -> openAddCoursesScreen(primaryStage));
        addStudentsButton.setOnAction(e -> openAddStudentsScreen(primaryStage));
        addMarksButton.setOnAction(e -> openAddMarksScreen(primaryStage));
        showStudentDetailsButton.setOnAction(e -> openShowStudentDetailsScreen(primaryStage));

        root.getChildren().addAll(addCoursesButton, addStudentsButton, addMarksButton, showStudentDetailsButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("School Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openAddCoursesScreen(Stage primaryStage) {
        AddCoursesScreen addCoursesScreen = new AddCoursesScreen();
        addCoursesScreen.start(primaryStage);
    }

    private void openAddStudentsScreen(Stage primaryStage) {
        AddStudentsScreen addStudentsScreen = new AddStudentsScreen();
        addStudentsScreen.start(primaryStage);
    }

    private void openAddMarksScreen(Stage primaryStage) {
        AddMarksScreen addMarksScreen = new AddMarksScreen();
        addMarksScreen.start(primaryStage);
    }

    private void openShowStudentDetailsScreen(Stage primaryStage) {
        ShowStudentDetailsScreen showStudentDetailsScreen = new ShowStudentDetailsScreen();
        showStudentDetailsScreen.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
