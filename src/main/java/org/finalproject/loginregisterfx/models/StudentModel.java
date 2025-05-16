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
    
    /**
     * Inner class to represent an academic term (year and semester)
     * with enrolled subjects and grades
     */
    public static class AcademicTerm {
        private String academicYear;
        private String semester;
        private List<SubjectGrade> subjects;
        
        public AcademicTerm(String academicYear, String semester) {
            this.academicYear = academicYear;
            this.semester = semester;
            this.subjects = new ArrayList<>();
        }
        
        public void addSubject(SubjectGrade subject) {
            subjects.add(subject);
        }
        
        public String getAcademicYear() { return academicYear; }
        public String getSemester() { return semester; }
        public List<SubjectGrade> getSubjects() { return subjects; }
    }
    
    /**
     * Inner class to represent a subject with grade information
     */
    public static class SubjectGrade {
        private String subjectId;
        private String edpCode;
        private String subjectName;
        private int units;
        private Double midtermGrade;
        private Double finalGrade;
        private String remarks;
        
        public SubjectGrade(JsonObject json) {
            this.subjectId = json.has("subject") ? json.get("subject").getAsString() : "";
            this.edpCode = json.has("edpCode") ? json.get("edpCode").getAsString() : "";
            this.subjectName = json.has("subjectName") ? json.get("subjectName").getAsString() : "";
            this.units = json.has("units") ? json.get("units").getAsInt() : 0;
            this.remarks = json.has("remarks") ? json.get("remarks").getAsString() : "";
            
            // Handle grades that might be null
            this.midtermGrade = json.has("midtermGrade") && !json.get("midtermGrade").isJsonNull() ?
                                json.get("midtermGrade").getAsDouble() : null;
            this.finalGrade = json.has("finalGrade") && !json.get("finalGrade").isJsonNull() ?
                             json.get("finalGrade").getAsDouble() : null;
        }
        
        public String getSubjectId() { return subjectId; }
        public String getEdpCode() { return edpCode; }
        public String getSubjectName() { return subjectName; }
        public int getUnits() { return units; }
        public Double getMidtermGrade() { return midtermGrade; }
        public Double getFinalGrade() { return finalGrade; }
        public String getRemarks() { return remarks; }
        
        public String getMidtermGradeFormatted() {
            return midtermGrade != null ? String.format("%.2f", midtermGrade) : "N/A";
        }
        
        public String getFinalGradeFormatted() {
            return finalGrade != null ? String.format("%.2f", finalGrade) : "N/A";
        }
    }
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
    private String semester;     // Added semester field
    private String department;   // Added department field
    private List<String> enrolledSubjectIds;
    private List<SubjectModel> enrolledSubjects;
    private boolean isEnrolled; // Added isEnrolled field
    private List<AcademicTerm> academicHistory; // Academic history records
    // Fields for displaying grades in the UI
    private String midtermGrade;
    private String finalGrade;
    private double gpa;
    
    /**
     * Set midterm grade 
     * 
     * @param midtermGrade The midterm grade to set
     */
    public void setMidtermGrade(String midtermGrade) {
        this.midtermGrade = midtermGrade;
    }
    
    /**
     * Get midterm grade
     * 
     * @return The midterm grade
     */
    public String getMidtermGrade() {
        return midtermGrade;
    }
    
    /**
     * Set final grade
     * 
     * @param finalGrade The final grade to set
     */
    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }
    
    /**
     * Get final grade
     * 
     * @return The final grade
     */
    public String getFinalGrade() {
        return finalGrade;
    }
      /**
     * Constructor that initializes the model from a JsonObject
     */    public StudentModel(JsonObject json) {
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
        this.semester = json.has("semester") ? json.get("semester").getAsString() : "First";
        this.department = json.has("department") ? json.get("department").getAsString() : "";
        
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
        }        // Initialize enrolled subjects lists
        this.enrolledSubjectIds = new ArrayList<>();
        this.enrolledSubjects = new ArrayList<>();
        this.academicHistory = new ArrayList<>();
        
        if (json.has("enrolledSubjects") && json.get("enrolledSubjects").isJsonArray()) {
            JsonArray subjectsArray = json.getAsJsonArray("enrolledSubjects");
            System.out.println("Found " + subjectsArray.size() + " enrolled subjects in JSON");
            
            for (int i = 0; i < subjectsArray.size(); i++) {
                if (subjectsArray.get(i).isJsonObject()) {
                    // If full subject objects are included
                    JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                    enrolledSubjects.add(new SubjectModel(subjectObj));
                    if (subjectObj.has("_id")) {
                        enrolledSubjectIds.add(subjectObj.get("_id").getAsString());
                    }
                } else if (!subjectsArray.get(i).isJsonNull()) {
                    // If just the subject IDs are included (common case)
                    enrolledSubjectIds.add(subjectsArray.get(i).getAsString());
                }
            }
            System.out.println("Stored " + enrolledSubjectIds.size() + " subject IDs");
        }
        
        // Parse academic history       
        if (json.has("academicHistory") && json.get("academicHistory").isJsonArray()) {
            JsonArray historyArray = json.getAsJsonArray("academicHistory");
            System.out.println("Processing academic history with " + historyArray.size() + " terms");
            
            for (int i = 0; i < historyArray.size(); i++) {
                if (historyArray.get(i).isJsonObject()) {
                    JsonObject termObj = historyArray.get(i).getAsJsonObject();
                    String academicYear = termObj.has("academicYear") ? 
                                          termObj.get("academicYear").getAsString() : "";
                    String semester = termObj.has("semester") ? 
                                      termObj.get("semester").getAsString() : "";
                    
                    System.out.println("Processing term: " + academicYear + " " + semester);
                    AcademicTerm term = new AcademicTerm(academicYear, semester);
                    
                    // Process subjects in this term
                    if (termObj.has("subjects") && termObj.get("subjects").isJsonArray()) {
                        JsonArray termSubjectsArray = termObj.getAsJsonArray("subjects");
                        System.out.println("Term has " + termSubjectsArray.size() + " subjects");
                        
                        for (int j = 0; j < termSubjectsArray.size(); j++) {
                            if (termSubjectsArray.get(j).isJsonObject()) {
                                JsonObject subjectObj = termSubjectsArray.get(j).getAsJsonObject();
                                SubjectGrade subjectGrade = new SubjectGrade(subjectObj);
                                System.out.println("Added subject: " + subjectGrade.getSubjectName() + 
                                                  ", Midterm: " + subjectGrade.getMidtermGradeFormatted() + 
                                                  ", Final: " + subjectGrade.getFinalGradeFormatted());
                                term.addSubject(subjectGrade);
                            }
                        }
                    } else {
                        System.out.println("Term has no subjects array or it's empty");
                    }
                    
                    academicHistory.add(term);
                    System.out.println("Added term to academic history");
                }
            }
        } else {
            System.out.println("No academic history found in student data");
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
        this.semester = "First"; // Default to First semester
        this.department = course.length() > 2 ? course.substring(0, 2) : ""; // Default department from course prefix
        
        // Initialize empty lists
        this.enrolledSubjectIds = new ArrayList<>();
        this.enrolledSubjects = new ArrayList<>();
    }/**
     * Empty constructor for creating new students
     */
    public StudentModel() {
        this.yearLevel = 1;
        this.semester = "First"; // Default semester
        this.department = "";
        this.enrolledSubjectIds = new ArrayList<>();
        this.enrolledSubjects = new ArrayList<>();
        this.academicHistory = new ArrayList<>();
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
    public String getSemester() { return semester; } // Getter for semester
    public String getDepartment() { return department; } // Getter for department
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
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }    public void setSection(String section) { this.section = section; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setSemester(String semester) { this.semester = semester; } // Setter for semester
    public void setDepartment(String department) { this.department = department; } // Setter for department
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
     * Clear all enrolled subjects
     * Used when refreshing study load data
     */
    public void clearEnrolledSubjects() {
        enrolledSubjects.clear();
        enrolledSubjectIds.clear();
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
     */    /**
     * Get academic history data
     * @return The list of academic terms with grades
     */
    public List<AcademicTerm> getAcademicHistory() {
        return academicHistory;
    }
    
    /**
     * Get the current (most recent) academic term
     * @return The most recent academic term or null if no history exists
     */
    public AcademicTerm getCurrentAcademicTerm() {
        if (academicHistory == null || academicHistory.isEmpty()) {
            return null;
        }
        // Assume the first term is the most recent one
        // In a real app, you might want to sort by year/semester
        return academicHistory.get(0);
    }
    
    /**
     * Find a subject grade by EDP code in the current academic term
     * @param edpCode The EDP code to search for
     * @return The subject grade object or null if not found
     */
    public SubjectGrade findSubjectGradeByEdpCode(String edpCode) {
        AcademicTerm currentTerm = getCurrentAcademicTerm();
        if (currentTerm == null || currentTerm.getSubjects() == null) {
            return null;
        }
        
        for (SubjectGrade grade : currentTerm.getSubjects()) {
            if (grade.getEdpCode().equals(edpCode)) {
                return grade;
            }
        }
        
        return null;
    }
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
        if (semester != null) json.addProperty("semester", semester);
        if (department != null) json.addProperty("department", department);
        json.addProperty("isEnrolled", isEnrolled);
        
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
        return name + " (" + course + " - Year " + yearLevel + ", " + semester + " Semester)";
    }

    public double getGPA() {
        return gpa;
    }

    public void setGPA(double gpa) {
        this.gpa = gpa;
    }
}
