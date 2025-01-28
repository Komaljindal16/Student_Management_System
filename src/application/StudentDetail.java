package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentDetail {

    private final StringProperty studentName;
    private final StringProperty studentId;
    private final StringProperty totalMarks;
    private final StringProperty percentage;
    private final StringProperty feedback;

    public StudentDetail(String studentName, String studentId, int totalMarks, double percentage, String feedback) {
        this.studentName = new SimpleStringProperty(studentName);
        this.studentId = new SimpleStringProperty(studentId);
        this.totalMarks = new SimpleStringProperty(String.valueOf(totalMarks));
        this.percentage = new SimpleStringProperty(String.format("%.2f", percentage));
        this.feedback = new SimpleStringProperty(feedback);
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    public StringProperty totalMarksProperty() {
        return totalMarks;
    }

    public StringProperty percentageProperty() {
        return percentage;
    }

    public StringProperty feedbackProperty() {
        return feedback;
    }
}
