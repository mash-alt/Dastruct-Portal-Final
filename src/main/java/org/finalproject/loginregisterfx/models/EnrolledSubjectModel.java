package org.finalproject.loginregisterfx.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.finalproject.loginregisterfx.Service.SessionManager;

/**
 * Model class representing an enrolled subject for display in the UI.
 */
public class EnrolledSubjectModel {
    private final SimpleStringProperty subjectCode;
    private final SimpleStringProperty subjectName;
    private final SimpleIntegerProperty units;
    private final SimpleStringProperty schedule;
    private final SimpleStringProperty instructor;
    private final SimpleStringProperty subjectId; // Hidden field for reference
    private final SimpleStringProperty midtermGrade; // Midterm grade as string for formatting
    private final SimpleStringProperty finalGrade; // Final grade as string for formatting
    
    /**
     * Constructor for an enrolled subject
     * 
     * @param subjectCode The course code
     * @param subjectName The subject name
     * @param units The number of units
     * @param schedule The class schedule
     * @param instructor The instructor name
     * @param subjectId The subject ID from the database
     */    public EnrolledSubjectModel(String subjectCode, String subjectName, int units, 
                               String schedule, String instructor, String subjectId,
                               String midtermGrade, String finalGrade) {
        this.subjectCode = new SimpleStringProperty(subjectCode);
        this.subjectName = new SimpleStringProperty(subjectName);
        this.units = new SimpleIntegerProperty(units);
        this.schedule = new SimpleStringProperty(schedule);
        this.instructor = new SimpleStringProperty(instructor);
        this.subjectId = new SimpleStringProperty(subjectId);
        this.midtermGrade = new SimpleStringProperty(midtermGrade);
        this.finalGrade = new SimpleStringProperty(finalGrade);
    }
    
    /**
     * Constructor with default "N/A" grades
     */
    public EnrolledSubjectModel(String subjectCode, String subjectName, int units, 
                               String schedule, String instructor, String subjectId) {
        this(subjectCode, subjectName, units, schedule, instructor, subjectId, "N/A", "N/A");
    }    /**
     * Constructor that takes a SubjectModel
     * 
     * @param subject The SubjectModel to convert
     */
    public EnrolledSubjectModel(SubjectModel subject) {
        this.subjectCode = new SimpleStringProperty(subject.getEdpCode());
        this.subjectName = new SimpleStringProperty(subject.getSubjectName());
        this.units = new SimpleIntegerProperty(subject.getUnits());
        this.schedule = new SimpleStringProperty("TBA"); // Schedule not available in SubjectModel
        this.instructor = new SimpleStringProperty(subject.getTeacherAssigned() != null ? 
                                                subject.getTeacherAssigned() : "TBA");
        this.subjectId = new SimpleStringProperty(subject.getEdpCode()); // Using edpCode as ID
        
        // Get grades if available for the current student
        String midterm = "N/A";
        String finals = "N/A";
        
        // If the subject has grades for this student, use them
        if (SessionManager.getInstance().isAuthenticated() && 
            SessionManager.getInstance().getCurrentStudent() != null) {
            
            StudentModel currentStudent = SessionManager.getInstance().getCurrentStudent();
            
            // First check if subject has direct grade information
            if (subject.hasGrade(currentStudent)) {
                Double midtermGrade = subject.getMidtermGrade(currentStudent);
                Double finalGrade = subject.getFinalGrade(currentStudent);
                
                midterm = (midtermGrade != null) ? String.format("%.2f", midtermGrade) : "N/A";
                finals = (finalGrade != null) ? String.format("%.2f", finalGrade) : "N/A";
            } 
            // If not, check in the academic history
            else {
                StudentModel.SubjectGrade grade = currentStudent.findSubjectGradeByEdpCode(subject.getEdpCode());
                if (grade != null) {
                    midterm = grade.getMidtermGradeFormatted();
                    finals = grade.getFinalGradeFormatted();
                }
            }
        }
        
        this.midtermGrade = new SimpleStringProperty(midterm);
        this.finalGrade = new SimpleStringProperty(finals);
    }
    
    // Getters and setters for JavaFX properties
    public String getSubjectCode() {
        return subjectCode.get();
    }
    
    public SimpleStringProperty subjectCodeProperty() {
        return subjectCode;
    }
    
    public String getSubjectName() {
        return subjectName.get();
    }
    
    public SimpleStringProperty subjectNameProperty() {
        return subjectName;
    }
    
    public int getUnits() {
        return units.get();
    }
    
    public SimpleIntegerProperty unitsProperty() {
        return units;
    }
    
    public String getSchedule() {
        return schedule.get();
    }
    
    public SimpleStringProperty scheduleProperty() {
        return schedule;
    }
    
    public void setSchedule(String scheduleValue) {
        this.schedule.set(scheduleValue);
    }
    
    public String getInstructor() {
        return instructor.get();
    }
    
    public SimpleStringProperty instructorProperty() {
        return instructor;
    }
      public String getSubjectId() {
        return subjectId.get();
    }
    
    public SimpleStringProperty subjectIdProperty() {
        return subjectId;
    }
    
    public String getMidtermGrade() {
        return midtermGrade.get();
    }
    
    public SimpleStringProperty midtermGradeProperty() {
        return midtermGrade;
    }
    
    public String getFinalGrade() {
        return finalGrade.get();
    }
    
    public SimpleStringProperty finalGradeProperty() {
        return finalGrade;
    }
}
