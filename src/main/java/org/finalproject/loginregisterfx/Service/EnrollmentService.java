package org.finalproject.loginregisterfx.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Service class to handle enrollment-related API calls.
 */
public class EnrollmentService {    // Use the same base URL and methods from AuthService
    // This BASE_URL isn't actually used because we're using AuthService.makePostRequest which already includes its own BASE_URL
    // private static final String BASE_URL = "http://localhost:5050/api"; 
    
    /**
     * Get the current academic year string in format "YYYY-YYYY"
     * @return Current academic year string
     */
    public static String getCurrentAcademicYear() {
        // As of May 14, 2025
        int currentYear = 2025;
        return currentYear + "-" + (currentYear + 1);
    }
    
    /**
     * Get available semesters as an array
     * @return Array of semester strings
     */
    public static String[] getSemesters() {
        return new String[]{"First", "Second", "Summer"};
    }/**
     * Enroll a student in the current academic year
     * 
     * @param studentId The ID of the student to enroll
     * @param subjectIds Optional list of subject IDs for enrollment (can be null for first-year students)
     * @param academicYear Optional academic year in format "YYYY-YYYY" (defaults to current year-next year)
     * @param semester Optional semester (defaults to "First")
     * @return CompletableFuture with API response
     */    public static CompletableFuture<JsonObject> enrollStudent(String studentId, 
                                                             List<String> subjectIds, 
                                                             String academicYear, 
                                                             String semester) {
        // The endpoint based on the backend route structure
        String endpoint = "/student/enroll";
        
        // Create the request body with required fields based on backend implementation
        Map<String, Object> requestData = new HashMap<>();
        // This is critical - the studentId must be included in the request body
        // The backend validates this field in req.body.studentId
        requestData.put("studentId", studentId);
        
        // Add optional parameters only if they're provided
        if (subjectIds != null && !subjectIds.isEmpty()) {
            requestData.put("subjectIds", subjectIds);
        }
        
        if (academicYear != null && !academicYear.isEmpty()) {
            requestData.put("academicYear", academicYear);
        }
        
        if (semester != null && !semester.isEmpty()) {
            requestData.put("semester", semester);
        }        // Log the enrollment request for debugging - enhanced for troubleshooting
        System.out.println("============ ENROLLMENT REQUEST ============");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Student ID: " + studentId);
        System.out.println("Full studentId format: " + studentId);  // Verify format (should be ucb-XXXXX)
        System.out.println("Subject IDs: " + (subjectIds != null ? subjectIds.toString() : "null"));
        System.out.println("Subject IDs count: " + (subjectIds != null ? subjectIds.size() : 0));
        System.out.println("Academic Year: " + academicYear);
        System.out.println("Semester: " + semester);
        System.out.println("Request data: " + requestData);
        System.out.println("Auth token: " + (AuthService.getAuthToken() != null ? "Present (length: " + AuthService.getAuthToken().length() + ")" : "Missing"));
        System.out.println("Full URL: " + "http://localhost:5050/api" + endpoint);
        System.out.println("============================================");
        
        // Make the API call with the properly formatted request body
        return AuthService.makePostRequest(endpoint, requestData);
    }
      /**
     * Enroll a student in the current academic year with default settings
     * For first-year students, this will automatically assign first-year subjects based on department
     * 
     * @param studentId The ID of the student to enroll
     * @return CompletableFuture with API response
     */
    public static CompletableFuture<JsonObject> enrollStudent(String studentId) {
        // For existing students, an empty list won't work - the API expects an array of subject IDs
        // We'll provide a list of commonly available subject IDs as a fallback
        List<String> defaultSubjectsList = new ArrayList<>();
        defaultSubjectsList.add("646c124512b8e255c9e1aaac"); // Placeholder IDs
        defaultSubjectsList.add("646c124512b8e255c9e1aaad");
        defaultSubjectsList.add("646c124512b8e255c9e1aaae");
        
        // Pass these default IDs - if they're invalid, the API will validate and return appropriate errors
        return enrollStudent(studentId, defaultSubjectsList, null, null);
    }
    
    /**
     * Enroll a student in specific subjects for the current academic year and semester
     * 
     * @param studentId The ID of the student to enroll
     * @param subjectIds List of subject IDs to enroll in
     * @return CompletableFuture with API response
     */
    public static CompletableFuture<JsonObject> enrollStudentInSubjects(String studentId, List<String> subjectIds) {
        return enrollStudent(studentId, subjectIds, null, null);
    }
    
    /**
     * Pre-enroll a student for the next academic year
     * 
     * @param studentId The ID of the student to pre-enroll
     * @return CompletableFuture with API response
     */    public static CompletableFuture<JsonObject> preEnrollForNextYear(String studentId) {
        // Use the same enrollment endpoint with next year's academic year
        String endpoint = "/student/enroll";
        
        // Calculate next academic year
        int currentYear = java.time.LocalDate.now().getYear();
        String nextAcademicYear = (currentYear + 1) + "-" + (currentYear + 2);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("academicYear", nextAcademicYear);
        requestData.put("semester", "First"); // Default to first semester for next year
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
    
    /**
     * Get the enrollment status of a student
     * 
     * @param studentId The ID of the student
     * @return CompletableFuture with API response containing enrollment status
     */
    public static CompletableFuture<JsonObject> getEnrollmentStatus(String studentId) {
        String endpoint = "/enrollment/status/" + studentId;
        
        return AuthService.makeGetRequest(endpoint);
    }
      /**
     * Get available subjects for enrollment
     * 
     * @param yearLevel Student's year level
     * @param department Student's department/course (e.g., "BSIT", "BSCS")
     * @param semester Semester ("First", "Second", "Summer")
     * @return CompletableFuture with API response containing available subjects
     */
    public static CompletableFuture<JsonObject> getAvailableSubjects(int yearLevel, String department, String semester) {
        String endpoint = "/subjects/available";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("yearLevel", yearLevel);
        requestData.put("department", department);
        
        if (semester != null && !semester.isEmpty()) {
            requestData.put("semester", semester);
        }
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
    
    /**
     * Get available subjects for enrollment (without specifying semester)
     * 
     * @param yearLevel Student's year level
     * @param department Student's department/course
     * @return CompletableFuture with API response containing available subjects
     */
    public static CompletableFuture<JsonObject> getAvailableSubjects(int yearLevel, String department) {
        return getAvailableSubjects(yearLevel, department, null);
    }
    
    /**
     * Validate if subjects match a student's department
     * 
     * @param studentId The ID of the student
     * @param subjectIds List of subject IDs to validate
     * @return CompletableFuture with API response containing validation results
     */
    public static CompletableFuture<JsonObject> validateSubjectsForStudent(String studentId, List<String> subjectIds) {
        String endpoint = "/enrollment/validate-subjects";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("subjectIds", subjectIds);
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
      /**
     * Enroll a student in specific subjects (older endpoint - deprecated)
     * Use enrollStudent or enrollStudentInSubjects instead.
     * 
     * @param studentId The ID of the student
     * @param subjectIds List of subject IDs to enroll in
     * @return CompletableFuture with API response
     * @deprecated Use {@link #enrollStudentInSubjects(String, List)} instead
     */
    @Deprecated
    public static CompletableFuture<JsonObject> enrollInSubjects(String studentId, List<String> subjectIds) {
        return enrollStudentInSubjects(studentId, subjectIds);
    }
    
    /**
     * Get a student's academic history
     * 
     * @param studentId The ID of the student
     * @return CompletableFuture with API response containing academic history
     */
    public static CompletableFuture<JsonObject> getAcademicHistory(String studentId) {
        String endpoint = "/student/" + studentId + "/academic-history";
        
        return AuthService.makeGetRequest(endpoint);
    }
    
    /**
     * Get enrolled subjects for a student
     * 
     * @param studentId The ID of the student
     * @return CompletableFuture with API response containing enrolled subjects
     */
    public static CompletableFuture<JsonObject> getEnrolledSubjects(String studentId) {
        String endpoint = "/student/" + studentId + "/studyload";
        
        return AuthService.makeGetRequest(endpoint);
    }
    
    /**
     * Drop a subject from a student's enrollment
     * 
     * @param studentId The ID of the student
     * @param subjectId The ID of the subject to drop
     * @return CompletableFuture with API response
     */
    public static CompletableFuture<JsonObject> dropSubject(String studentId, String subjectId) {
        String endpoint = "/enrollment/drop";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("subjectId", subjectId);
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
}
