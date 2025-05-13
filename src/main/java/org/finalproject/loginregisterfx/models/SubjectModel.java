package org.finalproject.loginregisterfx.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a Subject in the system.
 * Based on the backend subjectSchema.
 */
public class SubjectModel {
    private String edpCode;
    private String subjectName;
    private int units;
    private List<String> prerequisites;
    private String teacherAssigned;
    private String department;
    private Map<String, GradeInfo> grades;
    
    /**
     * Inner class for storing grade information
     */
    public static class GradeInfo {
        private double midtermGrade;
        private double finalGrade;
        
        public GradeInfo(double midtermGrade, double finalGrade) {
            this.midtermGrade = midtermGrade;
            this.finalGrade = finalGrade;
        }
        
        public double getMidtermGrade() { return midtermGrade; }
        public double getFinalGrade() { return finalGrade; }
        public double getAverageGrade() { return (midtermGrade + finalGrade) / 2; }
    }
    
    /**
     * Constructor that initializes the model from a JsonObject
     */    public SubjectModel(JsonObject json) {
        this.edpCode = json.has("edpCode") ? json.get("edpCode").getAsString() : "";
        this.subjectName = json.has("subjectName") ? json.get("subjectName").getAsString() : "";
        this.units = json.has("units") ? json.get("units").getAsInt() : 0;
        
        // Try to get department from various possible field names
        if (json.has("department") && !json.get("department").isJsonNull()) {
            this.department = json.get("department").getAsString();
        } else if (json.has("dept") && !json.get("dept").isJsonNull()) {
            this.department = json.get("dept").getAsString();
        } else if (json.has("departmentId") && !json.get("departmentId").isJsonNull()) {
            this.department = json.get("departmentId").getAsString();
        } else {
            this.department = "N/A"; // Default value when no department is specified
        }
        
        // Initialize prerequisites list
        this.prerequisites = new ArrayList<>();
        if (json.has("prerequisites") && json.get("prerequisites").isJsonArray()) {
            JsonArray prereqArray = json.getAsJsonArray("prerequisites");
            for (int i = 0; i < prereqArray.size(); i++) {
                prerequisites.add(prereqArray.get(i).getAsString());
            }
        }
        
        // Initialize grades map
        this.grades = new HashMap<>();
        if (json.has("grades") && json.get("grades").isJsonObject()) {
            JsonObject gradesObj = json.getAsJsonObject("grades");
            for (String studentId : gradesObj.keySet()) {
                JsonObject gradeObj = gradesObj.getAsJsonObject(studentId);
                double midterm = gradeObj.has("midtermGrade") ? gradeObj.get("midtermGrade").getAsDouble() : 0;
                double finalGrade = gradeObj.has("finalGrade") ? gradeObj.get("finalGrade").getAsDouble() : 0;
                grades.put(studentId, new GradeInfo(midterm, finalGrade));
            }
        }
        
        // Get teacher if assigned
        if (json.has("teacherAssigned")) {
            if (json.get("teacherAssigned").isJsonObject()) {
                // If teacher details are included
                JsonObject teacherObj = json.getAsJsonObject("teacherAssigned");
                this.teacherAssigned = teacherObj.has("name") ? teacherObj.get("name").getAsString() : "";
            } else if (!json.get("teacherAssigned").isJsonNull()) {
                // If just the teacher ID is included
                this.teacherAssigned = json.get("teacherAssigned").getAsString();
            }
        }
    }
      /**
     * Empty constructor for creating new subjects
     */
    public SubjectModel() {
        this.prerequisites = new ArrayList<>();
        this.grades = new HashMap<>();
    }
    
    /**
     * Constructor with direct parameters for creating subjects
     */
    public SubjectModel(String edpCode, String subjectName, int units, String department, String[] prerequisites) {
        this.edpCode = edpCode;
        this.subjectName = subjectName;
        this.units = units;
        this.department = department;
        
        this.prerequisites = new ArrayList<>();
        if (prerequisites != null) {
            for (String prereq : prerequisites) {
                this.prerequisites.add(prereq);
            }
        }
        
        this.grades = new HashMap<>();
    }
    
    // Getters
    public String getEdpCode() { return edpCode; }
    public String getSubjectName() { return subjectName; }
    public int getUnits() { return units; }
    public String getDepartment() { return department; }
    public List<String> getPrerequisites() { return prerequisites; }
    public String getTeacherAssigned() { return teacherAssigned; }
    public Map<String, GradeInfo> getGrades() { return grades; }
    
    // Setters
    public void setEdpCode(String edpCode) { this.edpCode = edpCode; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setUnits(int units) { this.units = units; }
    public void setDepartment(String department) { this.department = department; }
    public void setTeacherAssigned(String teacherAssigned) { this.teacherAssigned = teacherAssigned; }
    
    // Add a prerequisite
    public void addPrerequisite(String prerequisite) {
        if (!prerequisites.contains(prerequisite)) {
            prerequisites.add(prerequisite);
        }
    }
    
    // Set a grade for a student
    public void setGrade(String studentId, double midterm, double finalGrade) {
        grades.put(studentId, new GradeInfo(midterm, finalGrade));
    }
    
    /**
     * Get all students who have grades in this subject
     * @param allStudents List of all students to filter from
     * @return List of students who have grades in this subject
     */
    public List<StudentModel> getStudentsWithGrades(List<StudentModel> allStudents) {
        List<StudentModel> result = new ArrayList<>();
        for (StudentModel student : allStudents) {
            if (grades.containsKey(student.getId())) {
                result.add(student);
            }
        }
        return result;
    }
    
    /**
     * Calculate the class average for this subject
     * @return The average grade of all students in this subject
     */
    public double calculateClassAverage() {
        if (grades.isEmpty()) {
            return 0.0;
        }
        
        double totalGrade = 0.0;
        for (GradeInfo grade : grades.values()) {
            totalGrade += grade.getAverageGrade();
        }
        
        return totalGrade / grades.size();
    }
    
    /**
     * Get a formatted string representation of the class average
     * @return Formatted class average string with two decimal places
     */
    public String getFormattedClassAverage() {
        return String.format("%.2f", calculateClassAverage());
    }
    
    /**
     * Convert this model to a JsonObject for API calls
     * @return JsonObject representation of this model
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        
        json.addProperty("edpCode", edpCode);
        json.addProperty("subjectName", subjectName);
        json.addProperty("units", units);
        if (department != null) json.addProperty("department", department);
        if (teacherAssigned != null) json.addProperty("teacherAssigned", teacherAssigned);
        
        // Add prerequisites
        JsonArray prereqArray = new JsonArray();
        for (String prereq : prerequisites) {
            prereqArray.add(prereq);
        }
        json.add("prerequisites", prereqArray);
        
        // Add grades if any
        if (!grades.isEmpty()) {
            JsonObject gradesObj = new JsonObject();
            for (Map.Entry<String, GradeInfo> entry : grades.entrySet()) {
                JsonObject gradeInfoObj = new JsonObject();
                gradeInfoObj.addProperty("midtermGrade", entry.getValue().getMidtermGrade());
                gradeInfoObj.addProperty("finalGrade", entry.getValue().getFinalGrade());
                gradesObj.add(entry.getKey(), gradeInfoObj);
            }
            json.add("grades", gradesObj);
        }
        
        return json;
    }
    
    @Override
    public String toString() {
        return subjectName + " (" + edpCode + ")";
    }
}
