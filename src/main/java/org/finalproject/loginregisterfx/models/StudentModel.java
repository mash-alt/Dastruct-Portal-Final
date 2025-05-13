package org.finalproject.loginregisterfx.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Model class representing a Student in the system.
 * Based on the backend studentSchema.
 */
public class StudentModel {
    private String id;
    private String name;
    private String email;
    private String studentId;
    private LocalDate birthday;
    private String idNumber;
    private String course;
    private int yearLevel;
    private String section;
    private String address;
    private String phoneNumber;
    private List<String> enrolledSubjectIds;
    private List<SubjectModel> enrolledSubjects;
    private boolean isEnrolled; // Added isEnrolled field
      /**
     * Constructor that initializes the model from a JsonObject
     */
    public StudentModel(JsonObject json) {
        System.out.println("Creating StudentModel from JSON: " + json.toString());
        
        this.id = json.has("_id") ? json.get("_id").getAsString() : "";
        this.name = json.has("name") ? json.get("name").getAsString() : "";
        this.email = json.has("email") ? json.get("email").getAsString() : "";
        this.studentId = json.has("studentId") ? json.get("studentId").getAsString() : "";
        this.idNumber = json.has("idNumber") ? json.get("idNumber").getAsString() : "";
        this.course = json.has("course") ? json.get("course").getAsString() : "";
        this.address = json.has("address") ? json.get("address").getAsString() : "";
        this.phoneNumber = json.has("phoneNumber") ? json.get("phoneNumber").getAsString() : "";
        this.isEnrolled = json.has("isEnrolled") ? json.get("isEnrolled").getAsBoolean() : false;
        
        // Default section if not provided
        this.section = json.has("section") ? json.get("section").getAsString() : "A";
        
        // Handle yearLevel that could be string or int in API response
        if (json.has("yearLevel")) {
            try {
                if (json.get("yearLevel").isJsonPrimitive()) {
                    if (json.get("yearLevel").getAsJsonPrimitive().isNumber()) {
                        this.yearLevel = json.get("yearLevel").getAsInt();
                    } else {
                        // Try to parse string to int
                        try {
                            this.yearLevel = Integer.parseInt(json.get("yearLevel").getAsString());
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse yearLevel as integer: " + e.getMessage());
                            this.yearLevel = 1;
                        }
                    }
                } else {
                    this.yearLevel = 1;
                }
            } catch (Exception e) {
                System.err.println("Error handling yearLevel: " + e.getMessage());
                this.yearLevel = 1;
            }
        } else {
            this.yearLevel = 1;
        }
        
        // Parse birthday if available
        if (json.has("bday") && !json.get("bday").isJsonNull()) {
            try {
                String bdayStr = json.get("bday").getAsString();
                // Handle different date formats
                if (bdayStr.contains("T")) {
                    bdayStr = bdayStr.split("T")[0]; // Remove time component if ISO format
                }
                this.birthday = LocalDate.parse(bdayStr);
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing birthday: " + e.getMessage());
                this.birthday = null;
            }
        }
        
        // Initialize enrolled subjects lists
        this.enrolledSubjectIds = new ArrayList<>();
        this.enrolledSubjects = new ArrayList<>();
        
        if (json.has("enrolledSubjects") && json.get("enrolledSubjects").isJsonArray()) {
            JsonArray subjectsArray = json.getAsJsonArray("enrolledSubjects");
            for (int i = 0; i < subjectsArray.size(); i++) {
                if (subjectsArray.get(i).isJsonObject()) {
                    // If full subject objects are included
                    JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                    enrolledSubjects.add(new SubjectModel(subjectObj));
                    if (subjectObj.has("_id")) {
                        enrolledSubjectIds.add(subjectObj.get("_id").getAsString());
                    }
                } else if (!subjectsArray.get(i).isJsonNull()) {
                    // If just the subject IDs are included
                    enrolledSubjectIds.add(subjectsArray.get(i).getAsString());
                }
            }
        }
    }
    
    /**
     * Constructor for creating a student with basic information
     * 
     * @param studentId The student ID
     * @param name The student's name
     * @param course The student's course
     * @param yearLevelStr The student's year level as a string (e.g., "3rd Year")
     * @param college The student's college
     */
    public StudentModel(String studentId, String name, String course, String yearLevelStr, String college) {
        this.studentId = studentId;
        this.name = name;
        this.course = course;
        
        // Parse year level from string
        try {
            String yearNumber = yearLevelStr.replaceAll("[^0-9]", "");
            this.yearLevel = Integer.parseInt(yearNumber);
        } catch (Exception e) {
            this.yearLevel = 1; // Default to 1st year if parsing fails
        }
        
        this.address = college;
        
        // Initialize empty lists
        this.enrolledSubjectIds = new ArrayList<>();
        this.enrolledSubjects = new ArrayList<>();
    }
    
    /**
     * Empty constructor for creating new students
     */
    public StudentModel() {
        this.yearLevel = 1;
        this.enrolledSubjectIds = new ArrayList<>();
        this.enrolledSubjects = new ArrayList<>();
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStudentId() { return studentId; }
    public LocalDate getBirthday() { return birthday; }
    public String getIdNumber() { return idNumber; }
    public String getCourse() { return course; }
    public int getYearLevel() { return yearLevel; }
    /**
     * Gets a user-friendly string representation of the year level
     * @return String representation of the year level (e.g., "1st Year", "2nd Year")
     */
    public String getYearLevelString() { 
        switch(yearLevel) {
            case 1: return "1st Year";
            case 2: return "2nd Year";
            case 3: return "3rd Year";
            case 4: return "4th Year";
            case 5: return "5th Year";
            default: return String.valueOf(yearLevel); 
        }
    } // For TableView compatibility
    public String getSection() { return section; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<String> getEnrolledSubjectIds() { return enrolledSubjectIds; }
    public List<SubjectModel> getEnrolledSubjects() { return enrolledSubjects; }
    public boolean isEnrolled() { return isEnrolled; } // Getter for isEnrolled
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setCourse(String course) { this.course = course; }
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }
    public void setSection(String section) { this.section = section; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEnrolled(boolean isEnrolled) { this.isEnrolled = isEnrolled; } // Setter for isEnrolled
    
    // Enroll in a subject by ID
    public void enrollSubjectById(String subjectId) {
        if (!enrolledSubjectIds.contains(subjectId)) {
            enrolledSubjectIds.add(subjectId);
        }
    }
    
    // Enroll in a subject by object
    public void enrollSubject(SubjectModel subject) {
        if (!enrolledSubjects.contains(subject)) {
            enrolledSubjects.add(subject);
        }
    }
    
    /**
     * Calculate the student's GPA based on enrolled subjects and grades
     * @return The calculated GPA or 0.0 if no grades are available
     */
    public double calculateGPA() {
        if (enrolledSubjects.isEmpty()) {
            return 0.0;
        }
        
        double totalPoints = 0.0;
        int totalUnits = 0;
        
        for (SubjectModel subject : enrolledSubjects) {
            if (subject.getGrades().containsKey(id)) {
                SubjectModel.GradeInfo grade = subject.getGrades().get(id);
                double average = grade.getAverageGrade();
                int units = subject.getUnits();
                
                totalPoints += (average * units);
                totalUnits += units;
            }
        }
        
        return totalUnits > 0 ? totalPoints / totalUnits : 0.0;
    }
    
    /**
     * Get a formatted string representation of the student's GPA
     * @return Formatted GPA string with two decimal places
     */
    public String getFormattedGPA() {
        return String.format("%.2f", calculateGPA());
    }
    
    /**
     * Convert this model to a JsonObject for API calls
     * @return JsonObject representation of this model
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        
        if (id != null && !id.isEmpty()) {
            json.addProperty("_id", id);
        }
        
        json.addProperty("name", name);
        json.addProperty("email", email);
        if (studentId != null) json.addProperty("studentId", studentId);
        if (idNumber != null) json.addProperty("idNumber", idNumber);
        if (course != null) json.addProperty("course", course);
        json.addProperty("yearLevel", yearLevel);
        if (section != null) json.addProperty("section", section);
        if (address != null) json.addProperty("address", address);
        if (phoneNumber != null) json.addProperty("phoneNumber", phoneNumber);
        
        if (birthday != null) {
            json.addProperty("bday", birthday.toString());
        }
        
        // Add enrolled subjects
        JsonArray subjectsArray = new JsonArray();
        for (String subjectId : enrolledSubjectIds) {
            subjectsArray.add(subjectId);
        }
        json.add("enrolledSubjects", subjectsArray);
        
        return json;
    }
    
    @Override
    public String toString() {
        return name + " (" + course + " - Year " + yearLevel + ")";
    }
}
