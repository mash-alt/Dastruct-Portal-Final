package org.finalproject.loginregisterfx.models;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a Teacher in the system.
 * Based on the backend teacherSchema.
 */
public class TeacherModel {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private List<String> assignedSubjectIds;
    private List<SubjectModel> assignedSubjects;
    
    /**
     * Constructor that initializes the model from a JsonObject
     */
    public TeacherModel(JsonObject json) {
        this.id = json.has("_id") ? json.get("_id").getAsString() : "";
        this.name = json.has("name") ? json.get("name").getAsString() : "";
        this.email = json.has("email") ? json.get("email").getAsString() : "";
        this.phone = json.has("phone") ? json.get("phone").getAsString() : "";
        this.department = json.has("department") ? json.get("department").getAsString() : "";
          // Initialize assigned subjects lists
        this.assignedSubjectIds = new ArrayList<>();
        this.assignedSubjects = new ArrayList<>();
        
        if (json.has("assignedSubjects") && json.get("assignedSubjects").isJsonArray()) {
            JsonArray subjectsArray = json.getAsJsonArray("assignedSubjects");
            System.out.println("Teacher " + this.name + " has " + subjectsArray.size() + " assigned subjects");
            
            for (int i = 0; i < subjectsArray.size(); i++) {
                try {
                    if (subjectsArray.get(i).isJsonObject()) {                        // If full subject objects are included
                        // The new API format uses: id, edpCode, name, units, department
                        JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                        
                        // Create a subject model with the appropriate fields
                        String edpCode = "";
                        if (subjectObj.has("edpCode")) {
                            edpCode = subjectObj.get("edpCode").getAsString();
                        } else if (subjectObj.has("id")) {
                            edpCode = subjectObj.get("id").getAsString();
                        } else if (subjectObj.has("_id")) {
                            edpCode = subjectObj.get("_id").getAsString();
                        }
                        
                        String name = "Unknown Subject";
                        if (subjectObj.has("name")) {
                            name = subjectObj.get("name").getAsString();
                        } else if (subjectObj.has("subjectName")) {
                            name = subjectObj.get("subjectName").getAsString();
                        }
                        
                        int units = 0;
                        if (subjectObj.has("units")) {
                            units = subjectObj.get("units").getAsInt();
                        }
                        
                        String department = "";
                        if (subjectObj.has("department")) {
                            department = subjectObj.get("department").getAsString();
                        }
                        
                        SubjectModel subject = new SubjectModel(edpCode, name, units, department, new String[0]);
                        assignedSubjects.add(subject);
                        assignedSubjectIds.add(edpCode);
                        System.out.println("  - Added subject object with ID: " + edpCode + " and name: " + name);
                    } else if (!subjectsArray.get(i).isJsonNull()) {
                        // If just the subject IDs are included (MongoDB references)
                        String subjectId = subjectsArray.get(i).getAsString();
                        assignedSubjectIds.add(subjectId);
                        System.out.println("  - Added subject ID reference: " + subjectId);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing assigned subject at index " + i + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Processed " + assignedSubjectIds.size() + " subject IDs");
        }
    }
    
    /**
     * Empty constructor for creating new teachers
     */
    public TeacherModel() {
        this.assignedSubjectIds = new ArrayList<>();
        this.assignedSubjects = new ArrayList<>();
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public List<String> getAssignedSubjectIds() { return assignedSubjectIds; }
    public List<SubjectModel> getAssignedSubjects() { return assignedSubjects; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDepartment(String department) { this.department = department; }
    
    // Assign a subject by ID
    public void assignSubjectById(String subjectId) {
        if (!assignedSubjectIds.contains(subjectId)) {
            assignedSubjectIds.add(subjectId);
        }
    }
    
    // Assign a subject by object
    public void assignSubject(SubjectModel subject) {
        if (!assignedSubjects.contains(subject)) {
            assignedSubjects.add(subject);
        }
    }
    
    /**
     * Load full subject details for assigned subject IDs
     * @param allSubjects List of all subjects to look up from
     */
    public void loadAssignedSubjects(List<SubjectModel> allSubjects) {
        assignedSubjects.clear();
        for (SubjectModel subject : allSubjects) {
            if (assignedSubjectIds.contains(subject.getEdpCode())) {
                assignedSubjects.add(subject);
            }
        }
    }
    
    /**
     * Get the total units assigned to this teacher
     * @return Total number of units assigned
     */
    public int getTotalAssignedUnits() {
        int total = 0;
        for (SubjectModel subject : assignedSubjects) {
            total += subject.getUnits();
        }
        return total;
    }
    
    /**
     * Check if the teacher is already assigned to the given subject
     * @param subjectId The subject ID to check
     * @return True if already assigned, false otherwise
     */
    public boolean isAssignedToSubject(String subjectId) {
        return assignedSubjectIds.contains(subjectId);
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
        if (phone != null) json.addProperty("phone", phone);
        if (department != null) json.addProperty("department", department);
        
        // Add assigned subjects
        JsonArray subjectsArray = new JsonArray();
        for (String subjectId : assignedSubjectIds) {
            subjectsArray.add(subjectId);
        }
        json.add("assignedSubjects", subjectsArray);
        
        return json;
    }
    
    @Override
    public String toString() {
        return name;
    }

    // Clear all assigned subjects
    public void clearAssignedSubjects() {
        assignedSubjectIds.clear();
        assignedSubjects.clear();
        System.out.println("Cleared all assigned subjects for teacher: " + name);
    }
}
