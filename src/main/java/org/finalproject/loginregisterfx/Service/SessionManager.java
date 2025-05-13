package org.finalproject.loginregisterfx.Service;

import com.google.gson.JsonObject;
import org.finalproject.loginregisterfx.models.StudentModel;

/**
 * Session manager to maintain authentication state and current user data
 * across the application.
 */
public class SessionManager {
    private static SessionManager instance;
    private String authToken;
    private StudentModel currentStudent;
    private String userRole;
    private boolean isAuthenticated = false;

    // Private constructor for singleton pattern
    private SessionManager() {
        // Initialize with no session
    }
    
    /**
     * Get the singleton instance of SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Start a new session with authentication data
     * 
     * @param token JWT token from login
     * @param userData User data from API
     * @param role Role of the user (student, faculty, admin)
     */
    public void startSession(String token, JsonObject userData, String role) {
        this.authToken = token;
        this.userRole = role;
        this.isAuthenticated = true;
        
        // If student role, initialize student data
        if ("student".equalsIgnoreCase(role)) {
            this.currentStudent = new StudentModel(userData);
        }
    }
    
    /**
     * End the current session (logout)
     */
    public void endSession() {
        this.authToken = null;
        this.currentStudent = null;
        this.userRole = null;
        this.isAuthenticated = false;
    }
    
    /**
     * Update the current student data
     * 
     * @param userData Updated user data from API
     */
    public void updateStudentData(JsonObject userData) {
        if (this.currentStudent != null) {
            this.currentStudent = new StudentModel(userData);
        }
    }
    
    /**
     * Update the enrollment status of the current student
     * 
     * @param isEnrolled New enrollment status
     */
    public void updateEnrollmentStatus(boolean isEnrolled) {
        if (this.currentStudent != null) {
            this.currentStudent.setEnrolled(isEnrolled);
        }
    }
    
    // Getters
    public String getAuthToken() {
        return authToken;
    }
    
    public StudentModel getCurrentStudent() {
        return currentStudent;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}
