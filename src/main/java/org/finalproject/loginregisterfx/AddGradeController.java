package org.finalproject.loginregisterfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.models.StudentModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

/**
 * Controller for the AddGrade.fxml view that allows teachers to add grades for students.
 */
public class AddGradeController {

    @FXML private Label studentNameLabel;
    @FXML private Label subjectLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Button addGradeButton;
    @FXML private Button updateGradeButton;
    @FXML private TextField midtermGradeField;    @FXML private TextField finalGradeField;
    @FXML private Button testButton;

    private StudentModel student;
    private String subjectCode;
    private String subjectName;
    private Stage dialogStage;    @FXML
    private void initialize() {
        System.out.println("Initializing AddGradeController...");
        
        // Set current date time
        updateDateTime();
        
        // Set up button handlers
        if (addGradeButton != null) {
            addGradeButton.setOnAction(e -> handleAddGrade());
        }
        
        if (updateGradeButton != null) {
            updateGradeButton.setOnAction(e -> handleUpdateGrade());
        }
        
        // Enable test button for development
        if (testButton != null) {
            // Set to visible for testing (set to false for production)
            testButton.setVisible(true);
            System.out.println("Test button is visible for grade submission testing");
        }
    }
    
    /**
     * Set the student and subject data for this dialog
     * 
     * @param student The student model
     * @param subjectCode The subject code (EDP code)
     * @param subjectName The subject name
     */    public void setData(StudentModel student, String subjectCode, String subjectName) {
        this.student = student;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        
        // Update UI with the data
        if (studentNameLabel != null) {
            studentNameLabel.setText(student.getName());
        }
        
        if (subjectLabel != null) {
            subjectLabel.setText(subjectCode + " - " + subjectName);
        }
        
        // Check if the student already has grades and pre-populate fields
        loadExistingGrades();
        
        // Initialize debug test button
        try {
            javafx.application.Platform.runLater(() -> {
                Button testBtn = (Button) studentNameLabel.getScene().lookup("#testButton");
                if (testBtn != null) {
                    // Set to visible for testing (set to false for production)
                    testBtn.setVisible(true);
                    System.out.println("Test button is now visible for grade submission testing");
                } else {
                    System.out.println("Test button not found in the scene");
                }
            });
        } catch (Exception e) {
            System.out.println("Could not initialize test button: " + e.getMessage());
        }
    }
    
    /**
     * Set the stage of this dialog
     * 
     * @param dialogStage The stage for this dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Update the date time label with the current date and time
     */
    private void updateDateTime() {
        if (dateTimeLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dateTimeLabel.setText(now.format(formatter));
        }
    }
    
    /**
     * Load existing grades for this student and subject
     */
    private void loadExistingGrades() {
        if (student == null || subjectCode == null) {
            return;
        }
        
        // Query the API for the student's grades in this subject
        String endpoint = "/teacher/student/" + student.getId() + "/grades";
        
        AuthService.makeGetRequest(endpoint)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    try {
                        if (response != null && response.has("grades")) {
                            JsonObject gradesObj = response.getAsJsonObject("grades");
                            
                            // Check if midterm grade exists
                            if (gradesObj.has("midtermGrade") && !gradesObj.get("midtermGrade").isJsonNull()) {
                                String midtermGrade = gradesObj.get("midtermGrade").getAsString();
                                midtermGradeField.setText(midtermGrade);
                            }
                            
                            // Check if final grade exists
                            if (gradesObj.has("finalGrade") && !gradesObj.get("finalGrade").isJsonNull()) {
                                String finalGrade = gradesObj.get("finalGrade").getAsString();
                                finalGradeField.setText(finalGrade);
                            }
                            
                            // If grades exist, enable the update button and disable add button
                            if (gradesObj.has("midtermGrade") || gradesObj.has("finalGrade")) {
                                updateGradeButton.setDisable(false);
                                addGradeButton.setDisable(true);
                            } else {
                                updateGradeButton.setDisable(true);
                                addGradeButton.setDisable(false);
                            }
                        } else {
                            // No grades found, enable add button and disable update button
                            updateGradeButton.setDisable(true);
                            addGradeButton.setDisable(false);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading existing grades: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("Error fetching grades: " + ex.getMessage());
                // On error, enable add button as the default action
                javafx.application.Platform.runLater(() -> {
                    updateGradeButton.setDisable(true);
                    addGradeButton.setDisable(false);
                });
                return null;
            });
    }
    
    /**
     * Handle adding a new grade
     */
    private void handleAddGrade() {
        if (validateInputs()) {
            saveGrade(false);
        }
    }
    
    /**
     * Handle updating an existing grade
     */
    private void handleUpdateGrade() {
        if (validateInputs()) {
            saveGrade(true);
        }
    }
    
    /**
     * Validate the grade inputs according to Philippine standard grading (1.0-5.0)
     * 
     * @return true if inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        String midtermText = midtermGradeField.getText().trim();
        String finalText = finalGradeField.getText().trim();
        
        // Check that at least one field has a value
        if (midtermText.isEmpty() && finalText.isEmpty()) {
            showAlert("Error", "Please enter at least one grade value.");
            return false;
        }
        
        // Validate midterm grade if provided (Philippine standard: 1.0-5.0)
        if (!midtermText.isEmpty()) {
            try {
                double midtermGrade = Double.parseDouble(midtermText);
                if (midtermGrade < 1.0 || midtermGrade > 5.0) {
                    showAlert("Error", "Midterm grade must be between 1.0 and 5.0 (Philippine standard grading).");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Midterm grade must be a valid number.");
                return false;
            }
        }
        
        // Validate final grade if provided (Philippine standard: 1.0-5.0)
        if (!finalText.isEmpty()) {
            try {
                double finalGrade = Double.parseDouble(finalText);
                if (finalGrade < 1.0 || finalGrade > 5.0) {
                    showAlert("Error", "Final grade must be between 1.0 and 5.0 (Philippine standard grading).");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Final grade must be a valid number.");
                return false;
            }
        }
        
        return true;
    }
      /**
     * Save the grade to the server
     * 
     * @param isUpdate true if updating an existing grade, false if adding a new one
     */
    private void saveGrade(boolean isUpdate) {
        String midtermText = midtermGradeField.getText().trim();
        String finalText = finalGradeField.getText().trim();
          JsonObject gradeData = new JsonObject();
        gradeData.addProperty("studentName", student.getName());
        gradeData.addProperty("subjectName", subjectName);
        
        // Add student ID as optional information for the backend
        gradeData.addProperty("studentId", student.getId());
        
        // Add grade values if provided
        if (!midtermText.isEmpty()) {
            gradeData.addProperty("midtermGrade", Double.parseDouble(midtermText));
        }
        
        if (!finalText.isEmpty()) {
            gradeData.addProperty("finalGrade", Double.parseDouble(finalText));
        }
        
        // Use the API endpoint from the backend implementation
        String endpoint = "/teacher/subjects/grades";
        
        // Log the request details
        System.out.println("================== ADD GRADE REQUEST DETAILS ==================");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Action: " + (isUpdate ? "Update Grade" : "Add Grade"));
        System.out.println("Request payload: " + gradeData.toString());
        System.out.println("Student ID: " + student.getId());
        System.out.println("Student Name: " + student.getName());
        System.out.println("Subject Code: " + subjectCode);
        System.out.println("Subject Name: " + subjectName);
        System.out.println("Midterm Grade: " + midtermText);
        System.out.println("Final Grade: " + finalText);
        System.out.println("Request time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println("============================================================");
        
        AuthService.makePostRequest(endpoint, gradeData)
            .thenAccept(response -> {
                // Log the response details
                System.out.println("================== ADD GRADE RESPONSE ==================");
                System.out.println("Response received at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                System.out.println("Full response: " + (response != null ? response.toString() : "null"));
                  if (response != null) {
                    if (response.has("message")) {
                        System.out.println("Success message: " + response.get("message").getAsString());
                    }
                    if (response.has("error")) {
                        System.out.println("Error message: " + response.get("error").getAsString());
                    }
                    if (response.has("subject")) {
                        System.out.println("Subject data returned: " + response.get("subject").toString());
                        
                        // If we have subject data, try to extract more details about saved grades
                        try {
                            JsonObject subjectObj = response.getAsJsonObject("subject");
                            if (subjectObj.has("grades")) {
                                System.out.println("Grade details found in response:");
                                JsonObject grades = subjectObj.getAsJsonObject("grades");
                                for (String studentId : grades.keySet()) {
                                    System.out.println("  Student ID: " + studentId);
                                    JsonObject studentGrades = grades.getAsJsonObject(studentId);
                                    System.out.println("    Midterm: " + (studentGrades.has("midtermGrade") ? 
                                        studentGrades.get("midtermGrade").getAsString() : "Not set"));
                                    System.out.println("    Final: " + (studentGrades.has("finalGrade") ? 
                                        studentGrades.get("finalGrade").getAsString() : "Not set"));
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Could not parse grades from subject: " + e.getMessage());
                        }
                    }
                }
                System.out.println("============================================================");
                
                javafx.application.Platform.runLater(() -> {
                    if (response != null && response.has("message")) {
                        String successMsg = response.get("message").getAsString();
                        showAlert("Success", successMsg);
                        
                        // Close the dialog after successful operation
                        if (dialogStage != null) {
                            dialogStage.close();
                        }
                    } else {
                        String errorMsg = "Unknown error occurred";
                        if (response != null && response.has("error")) {
                            errorMsg = response.get("error").getAsString();
                        }
                        showAlert("Error", "Failed to add grade: " + errorMsg);
                    }
                });
            })
            .exceptionally(ex -> {
                System.out.println("================== ADD GRADE ERROR ==================");
                System.out.println("Error at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                System.out.println("Error message: " + ex.getMessage());
                if (ex.getCause() != null) {
                    System.out.println("Cause: " + ex.getCause().toString());
                }
                System.out.println("Stack trace: ");
                ex.printStackTrace();
                System.out.println("============================================================");
                
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Error adding grade: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Show an alert dialog with the given title and message
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Send a test grade to verify the API functionality
     * This can be triggered for debugging purposes
     */
    public void testGradeSubmission() {
        System.out.println("==================== TESTING GRADE SUBMISSION ====================");
        System.out.println("Time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
          // Create test data with a real student name and subject from your system
        // The test button should only be used when viewing a real student's grade page
        // otherwise we'll use fallback test data
        JsonObject testData = new JsonObject();
        
        // If we have actual student and subject data available, use it
        if (student != null && subjectName != null) {
            testData.addProperty("studentName", student.getName());
            testData.addProperty("subjectName", subjectName);
            testData.addProperty("studentId", student.getId());
        } else {
            // Fallback test data - these should be actual names in your database
            testData.addProperty("studentName", "John Doe");
            testData.addProperty("subjectName", "Data Structures");
        }
        
        // Use valid Philippine grading scale values (1.0-5.0)
        testData.addProperty("midtermGrade", 1.5); // 1.0 (Excellent) to 5.0 (Failed)
        testData.addProperty("finalGrade", 1.75);
        
        System.out.println("Sending test grade data: " + testData.toString());
        
        // Use the API endpoint
        String endpoint = "/teacher/subjects/grades";
        
        AuthService.makePostRequest(endpoint, testData)
            .thenAccept(response -> {
                System.out.println("==================== TEST GRADE RESPONSE ====================");
                System.out.println("Time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));                System.out.println("Full response: " + response.toString());
                
                if (response.has("message")) {
                    System.out.println("Success message: " + response.get("message").getAsString());
                }
                
                if (response.has("subject")) {
                    System.out.println("Subject data: " + response.get("subject").toString());
                    
                    // If we have subject data, try to extract more details about saved grades
                    try {
                        JsonObject subjectObj = response.getAsJsonObject("subject");
                        if (subjectObj.has("grades")) {
                            System.out.println("Grade details found in response:");
                            JsonObject grades = subjectObj.getAsJsonObject("grades");
                            for (String studentId : grades.keySet()) {
                                System.out.println("  Student ID: " + studentId);
                                JsonObject studentGrades = grades.getAsJsonObject(studentId);
                                System.out.println("    Midterm: " + (studentGrades.has("midtermGrade") ? 
                                    studentGrades.get("midtermGrade").getAsString() : "Not set"));
                                System.out.println("    Final: " + (studentGrades.has("finalGrade") ? 
                                    studentGrades.get("finalGrade").getAsString() : "Not set"));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Could not parse grades from subject: " + e.getMessage());
                    }
                }
                
                if (response.has("error")) {
                    System.out.println("Error message: " + response.get("error").getAsString());
                }
                
                System.out.println("=============================================================");
            })
            .exceptionally(ex -> {
                System.out.println("==================== TEST GRADE ERROR ====================");
                System.out.println("Time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                System.out.println("Error: " + ex.getMessage());
                System.out.println("=============================================================");
                return null;
            });
    }

    /**
     * Handle test button click
     * This is for debug purposes only and is connected to the hidden test button
     */
    @FXML
    private void handleTestButtonClick() {
        System.out.println("Test button clicked - executing test grade submission");
        testGradeSubmission();
    }
}