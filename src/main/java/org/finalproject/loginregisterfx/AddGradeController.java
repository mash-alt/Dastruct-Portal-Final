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

/**
 * Controller for the AddGrade.fxml view that allows teachers to add grades for students.
 */
public class AddGradeController {

    @FXML private Label studentNameLabel;
    @FXML private Label subjectLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Button addGradeButton;
    @FXML private Button updateGradeButton;
    @FXML private TextField midtermGradeField;
    @FXML private TextField finalGradeField;

    private StudentModel student;
    private String subjectCode;
    private String subjectName;
    private Stage dialogStage;

    @FXML
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
    }
    
    /**
     * Set the student and subject data for this dialog
     * 
     * @param student The student model
     * @param subjectCode The subject code (EDP code)
     * @param subjectName The subject name
     */
    public void setData(StudentModel student, String subjectCode, String subjectName) {
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
        String endpoint = "/teacher/subject/" + subjectCode + "/student/" + student.getId() + "/grades";
        
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
        
        // Add grade values if provided
        if (!midtermText.isEmpty()) {
            gradeData.addProperty("midtermGrade", Double.parseDouble(midtermText));
        }
        
        if (!finalText.isEmpty()) {
            gradeData.addProperty("finalGrade", Double.parseDouble(finalText));
        }
        
        // Use the API endpoint from the backend implementation
        String endpoint = "/teacher/subjects/grades";
        
        AuthService.makePostRequest(endpoint, gradeData)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (response != null && response.has("message")) {
                        showAlert("Success", response.get("message").getAsString());
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
}