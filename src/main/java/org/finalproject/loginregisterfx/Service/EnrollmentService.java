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
public class EnrollmentService {
    // Use the same base URL and methods from AuthService
    private static final String BASE_URL = "http://localhost:5050/api"; 

    /**
     * Enroll a student in the current academic year
     * 
     * @param studentId The ID of the student to enroll
     * @return CompletableFuture with API response
     */
    public static CompletableFuture<JsonObject> enrollStudent(String studentId) {
        String endpoint = "/enrollment/enroll";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
    
    /**
     * Pre-enroll a student for the next academic year
     * 
     * @param studentId The ID of the student to pre-enroll
     * @return CompletableFuture with API response
     */
    public static CompletableFuture<JsonObject> preEnrollForNextYear(String studentId) {
        String endpoint = "/enrollment/pre-enroll";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("nextYear", true);
        
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
     * @param course Student's course
     * @return CompletableFuture with API response containing available subjects
     */
    public static CompletableFuture<JsonObject> getAvailableSubjects(int yearLevel, String course) {
        String endpoint = "/subjects/available";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("yearLevel", yearLevel);
        requestData.put("course", course);
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
    
    /**
     * Enroll a student in specific subjects
     * 
     * @param studentId The ID of the student
     * @param subjectIds List of subject IDs to enroll in
     * @return CompletableFuture with API response
     */
    public static CompletableFuture<JsonObject> enrollInSubjects(String studentId, List<String> subjectIds) {
        String endpoint = "/enrollment/subjects";
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("subjectIds", subjectIds);
        
        return AuthService.makePostRequest(endpoint, requestData);
    }
    
    /**
     * Get enrolled subjects for a student
     * 
     * @param studentId The ID of the student
     * @return CompletableFuture with API response containing enrolled subjects
     */
    public static CompletableFuture<JsonObject> getEnrolledSubjects(String studentId) {
        String endpoint = "/enrollment/subjects/" + studentId;
        
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
