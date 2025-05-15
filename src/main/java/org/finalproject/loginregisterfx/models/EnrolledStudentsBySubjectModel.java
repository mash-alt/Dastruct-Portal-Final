package org.finalproject.loginregisterfx.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing subjects with their enrolled students.
 */
public class EnrolledStudentsBySubjectModel {
    private String subjectId;
    private String subjectCode;
    private String subjectName;
    private int enrolledStudentCount;
    private List<StudentModel> enrolledStudents;
    
    /**
     * Constructor that initializes the model from a JsonObject
     */
    public EnrolledStudentsBySubjectModel(JsonObject json) {        this.subjectId = json.has("_id") ? json.get("_id").getAsString() : "";
        this.subjectCode = json.has("edpCode") ? json.get("edpCode").getAsString() : 
                           (json.has("subjectCode") ? json.get("subjectCode").getAsString() : "");
        this.subjectName = json.has("name") ? json.get("name").getAsString() : 
                           (json.has("subjectName") ? json.get("subjectName").getAsString() : "");
        
        // Initialize enrolled students list
        this.enrolledStudents = new ArrayList<>();
        
        // Get student count directly from the JSON if available
        if (json.has("studentCount") && !json.get("studentCount").isJsonNull()) {
            this.enrolledStudentCount = json.get("studentCount").getAsInt();
            System.out.println("Retrieved studentCount from JSON for " + this.subjectCode + ": " + this.enrolledStudentCount);
        } else {
            // Default to 0
            this.enrolledStudentCount = 0;
        }
        
        if (json.has("enrolledStudents") && json.get("enrolledStudents").isJsonArray()) {
            JsonArray studentsArray = json.getAsJsonArray("enrolledStudents");
            
            // Process each student in the array
            for (int i = 0; i < studentsArray.size(); i++) {
                try {
                    JsonObject studentObj = studentsArray.get(i).getAsJsonObject();
                    StudentModel student = new StudentModel(studentObj);
                    enrolledStudents.add(student);
                } catch (Exception e) {
                    System.err.println("Error processing student data: " + e.getMessage());
                }
            }
            
            // Only override the count if we don't have a studentCount field but we do have enrolledStudents
            if (!json.has("studentCount") && !enrolledStudents.isEmpty()) {
                this.enrolledStudentCount = enrolledStudents.size();
            }
        }
    }
    
    /**
     * Empty constructor
     */
    public EnrolledStudentsBySubjectModel() {
        this.enrolledStudents = new ArrayList<>();
    }

    // Getters
    public String getSubjectId() { return subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public String getSubjectName() { return subjectName; }
    public int getEnrolledStudentCount() { return enrolledStudentCount; }
    public List<StudentModel> getEnrolledStudents() { return enrolledStudents; }
    
    // Setters
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setEnrolledStudentCount(int enrolledStudentCount) { this.enrolledStudentCount = enrolledStudentCount; }
    public void setEnrolledStudents(List<StudentModel> enrolledStudents) { 
        this.enrolledStudents = enrolledStudents;
        this.enrolledStudentCount = enrolledStudents.size();
    }
    
    @Override
    public String toString() {
        return subjectCode + " - " + subjectName + " (" + enrolledStudentCount + " students)";
    }
}
