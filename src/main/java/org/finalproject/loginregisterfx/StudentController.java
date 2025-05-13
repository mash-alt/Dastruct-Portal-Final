package org.finalproject.loginregisterfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.models.StudentModel;
import org.finalproject.loginregisterfx.models.SubjectModel;
import org.finalproject.loginregisterfx.models.EnrolledSubjectModel;
import org.finalproject.loginregisterfx.Service.SessionManager;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.Service.EnrollmentService;

public class StudentController {
    
    @FXML
    private Label studentNameLabel;
    
    @FXML
    private Label studentIDLabel;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private Button enrollmentBtn;
    
    @FXML
    private Button studyLoadBtn;
    
    @FXML
    private Button eGradeBtn;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private VBox profileContent;
    
    @FXML
    private VBox enrollmentContent;
    
    @FXML
    private VBox studyLoadContent;
    
    @FXML
    private VBox eGradeContent;
    
    // Enrollment-related fields
    @FXML
    private Label enrollmentStatusLabel;
    
    @FXML
    private Button startEnrollmentBtn;
    
    @FXML
    private Button nextYearEnrollmentBtn;
    
    // Student information fields
    @FXML
    private Label fullNameValue;
    
    @FXML
    private Label studentIdValue;
    
    @FXML
    private Label emailValue;
    
    @FXML
    private Label courseValue;
    
    @FXML
    private Label yearLevelValue;
    
    @FXML
    private Label sectionValue;
    
    @FXML
    private Label gpaValue;
    
    @FXML
    private Label subjectsEnrolledValue;
    
    // Study load table fields
    @FXML
    private TableView<EnrolledSubjectModel> subjectsTable;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> subjectCodeCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> subjectNameCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, Integer> subjectUnitsCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> scheduleCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> instructorCol;
    
    @FXML
    private Label schoolYearLabel;
    
    @FXML
    private Label totalUnitsLabel;
    
    // Observable list for enrolled subjects
    private ObservableList<EnrolledSubjectModel> enrolledSubjectsData = FXCollections.observableArrayList();
    
    // Store student data
    private StudentModel studentData;    
      @FXML
    public void initialize() {
        // Set default content visibility
        profileContent.setVisible(true);
        enrollmentContent.setVisible(false);
        studyLoadContent.setVisible(false);
        eGradeContent.setVisible(false);
        
        // Set default active button style
        updateNavButtonStyles("profile");
        
        // Initialize the study load table columns
        subjectCodeCol.setCellValueFactory(cellData -> cellData.getValue().subjectCodeProperty());
        subjectNameCol.setCellValueFactory(cellData -> cellData.getValue().subjectNameProperty());
        subjectUnitsCol.setCellValueFactory(cellData -> cellData.getValue().unitsProperty().asObject());
        scheduleCol.setCellValueFactory(cellData -> cellData.getValue().scheduleProperty());
        instructorCol.setCellValueFactory(cellData -> cellData.getValue().instructorProperty());
        
        // Set the items source for the table
        subjectsTable.setItems(enrolledSubjectsData);
        
        // Check if there's an active session and initialize data
        if (SessionManager.getInstance().isAuthenticated() && 
            SessionManager.getInstance().getCurrentStudent() != null) {
            this.studentData = SessionManager.getInstance().getCurrentStudent();
            
            // Debug output
            System.out.println("Student data loaded from session: " + 
                (studentData != null ? studentData.getName() + " (" + studentData.getStudentId() + ")" : "NULL"));
            
            if (studentData != null) {
                updateStudentInfo();
                updateEnrollmentStatus();
            } else {
                System.err.println("ERROR: Student data is null even though session is authenticated");
            }
        } else {
            System.err.println("WARNING: No authenticated session found during initialization");
            // You might want to redirect to login page here
        }
        
        // Get current school year
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        
        // Set event handlers for navigation buttons
        // These are already set in FXML with onAction attributes
    }
    
    /**
     * Initialize student data from login response
     * @param userData JsonObject containing student data from API
     */
    public void initializeStudentData(JsonObject userData) {
        try {
            // Create StudentModel from the response
            System.out.println("Initializing student data from: " + (userData != null ? userData.toString() : "null JSON"));
            
            // Sample data format:
            // {"_id":"6822f1c0db950f6a1b200f41","name":"test account","email":"test@test.com","studentId":"ucb-62476646",
            // "bday":"2025-05-12T00:00:00.000Z","course":"BSIT","address":"test","phoneNumber":"0912345678",
            // "isEnrolled":false,"enrolledSubjects":[],"academicHistory":[],"createdAt":"2025-05-13T07:16:16.769Z",
            // "updatedAt":"2025-05-13T07:16:16.769Z","__v":0}
            
            this.studentData = new StudentModel(userData);
            
            // Save student in session for persistence
            SessionManager.getInstance().setCurrentStudent(studentData);
            
            // Make sure UI updates happen on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                try {
                    // Update UI with student data
                    updateStudentInfo();
                    
                    // Update enrollment status based on isEnrolled property
                    updateEnrollmentStatus();
                    
                    System.out.println("UI update completed successfully!");
                } catch (Exception e) {
                    System.err.println("ERROR updating UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("ERROR initializing student data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update UI with student information
     */
    private void updateStudentInfo() {
        if (studentData == null) {
            System.err.println("ERROR: studentData is null in updateStudentInfo()");
            return;
        }
        
        System.out.println("DEBUG - Student Data Details:");
        System.out.println("Name: " + studentData.getName());
        System.out.println("Student ID: " + studentData.getStudentId());
        System.out.println("Email: " + studentData.getEmail());
        System.out.println("Course: " + studentData.getCourse());
        System.out.println("Year Level: " + studentData.getYearLevelString());
        System.out.println("Section: " + studentData.getSection());
        System.out.println("Address: " + studentData.getAddress());
        System.out.println("Phone: " + studentData.getPhoneNumber());
        System.out.println("Enrolled: " + studentData.isEnrolled());
        System.out.println("Enrolled Subjects Count: " + studentData.getEnrolledSubjects().size());
        
        // Update sidebar profile
        studentNameLabel.setText(studentData.getName());
        studentIDLabel.setText(studentData.getStudentId());
        
        // Update profile content
        if (fullNameValue != null) {
            fullNameValue.setText(studentData.getName());
        } else {
            System.err.println("ERROR: fullNameValue label is null");
        }
        
        if (studentIdValue != null) {
            studentIdValue.setText(studentData.getStudentId());
        } else {
            System.err.println("ERROR: studentIdValue label is null");
        }
        
        if (emailValue != null) {
            emailValue.setText(studentData.getEmail());
        } else {
            System.err.println("ERROR: emailValue label is null");
        }
        
        if (courseValue != null) {
            courseValue.setText(studentData.getCourse());
        } else {
            System.err.println("ERROR: courseValue label is null");
        }
        
        if (yearLevelValue != null) {
            yearLevelValue.setText(studentData.getYearLevelString());
        } else {
            System.err.println("ERROR: yearLevelValue label is null");
        }
        
        if (sectionValue != null) {
            sectionValue.setText(studentData.getSection());
        } else {
            System.err.println("ERROR: sectionValue label is null");
        }
        
        // Update GPA and enrolled subjects
        if (gpaValue != null) {
            gpaValue.setText(studentData.getFormattedGPA());
        } else {
            System.err.println("ERROR: gpaValue label is null");
        }
        
        int enrolledSubjectsCount = studentData.getEnrolledSubjects().size();
        if (subjectsEnrolledValue != null) {
            subjectsEnrolledValue.setText(String.valueOf(enrolledSubjectsCount));
        } else {
            System.err.println("ERROR: subjectsEnrolledValue label is null");
        }
        
        System.out.println("Student info update completed");
    }
    
    /**
     * Switch to profile content
     */
    @FXML
    public void switchToProfile() {
        profileContent.setVisible(true);
        enrollmentContent.setVisible(false);
        studyLoadContent.setVisible(false);
        eGradeContent.setVisible(false);
        
        updateNavButtonStyles("profile");
    }
    
    /**
     * Switch to enrollment content
     */
    @FXML
    public void switchToEnrollment() {        
        profileContent.setVisible(false);
        enrollmentContent.setVisible(true);
        studyLoadContent.setVisible(false);
        eGradeContent.setVisible(false);
        
        updateNavButtonStyles("enrollment");
    }
    
    /**
     * Switch to study load content
     */
    @FXML
    public void switchToStudyLoad() {
        profileContent.setVisible(false);
        enrollmentContent.setVisible(false);
        studyLoadContent.setVisible(true);
        eGradeContent.setVisible(false);
        
        updateNavButtonStyles("studyLoad");
        loadEnrolledSubjects();
    }
    
    /**
     * Switch to e-grade content
     */
    @FXML
    public void switchToEGrade() {
        profileContent.setVisible(false);
        enrollmentContent.setVisible(false);
        studyLoadContent.setVisible(false);
        eGradeContent.setVisible(true);
        
        updateNavButtonStyles("eGrade");
    }
    
    /**
     * Update the styles of navigation buttons based on selection
     * @param activeSection The currently active section
     */
    private void updateNavButtonStyles(String activeSection) {
        // Reset all to default nav-button style
        dashboardBtn.getStyleClass().clear();
        enrollmentBtn.getStyleClass().clear();
        studyLoadBtn.getStyleClass().clear();
        eGradeBtn.getStyleClass().clear();
        
        dashboardBtn.getStyleClass().add("nav-button");
        enrollmentBtn.getStyleClass().add("nav-button");
        studyLoadBtn.getStyleClass().add("nav-button");
        eGradeBtn.getStyleClass().add("nav-button");
        
        // Set active style for selected button
        switch (activeSection) {
            case "profile":
                dashboardBtn.getStyleClass().clear();
                dashboardBtn.getStyleClass().add("nav-button-active");
                break;
            case "enrollment":
                enrollmentBtn.getStyleClass().clear();
                enrollmentBtn.getStyleClass().add("nav-button-active");
                break;
            case "studyLoad":
                studyLoadBtn.getStyleClass().clear();
                studyLoadBtn.getStyleClass().add("nav-button-active");
                break;
            case "eGrade":
                eGradeBtn.getStyleClass().clear();
                eGradeBtn.getStyleClass().add("nav-button-active");
                break;
        }
    }
    
    /**
     * Handle logout button click
     */
    @FXML
    public void handleLogout() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to logout?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // End session
                    SessionManager.getInstance().endSession();
                    
                    // Call logout endpoint if needed
                    AuthService.logout()
                        .exceptionally(ex -> {
                            System.err.println("Error during logout: " + ex.getMessage());
                            return null; // Continue with local logout even if API call fails
                        });
                    
                    // Load login form
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("LoginForm.fxml"));
                    javafx.scene.Parent loginView = loader.load();
                    
                    // Get current stage
                    Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
                    currentStage.setScene(new javafx.scene.Scene(loginView));
                    currentStage.setTitle("Student Portal Login");
                    currentStage.setResizable(false);
                    currentStage.show();
                    currentStage.centerOnScreen();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to login screen.");
                }
            }
        });
    }
    
    /**
     * Handle the start enrollment button click
     */
    @FXML
    public void handleStartEnrollment() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Enrollment");
        confirmAlert.setHeaderText("Start Enrollment Process");
        confirmAlert.setContentText("Are you sure you want to begin the enrollment process for the 2025-2026 school year?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Update UI to show process is starting
                    enrollmentStatusLabel.setText("Status: Enrollment in Progress");
                    enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f39c12;");
                    
                    // Disable start button
                    startEnrollmentBtn.setDisable(true);
                    startEnrollmentBtn.setText("Enrollment in Progress...");
                    
                    // Get session manager and current student ID
                    String studentId = studentData.getId();

                    // Make the API call using EnrollmentService
                    org.finalproject.loginregisterfx.Service.EnrollmentService.enrollStudent(studentId)
                        .thenAccept(response2 -> {
                            // Update on JavaFX application thread
                            javafx.application.Platform.runLater(() -> {
                                // Check if enrollment was successful
                                if (response2.has("success") && response2.get("success").getAsBoolean()) {
                                    // Update student model
                                    studentData.setEnrolled(true);
                                    
                                    // Update session manager
                                    org.finalproject.loginregisterfx.Service.SessionManager.getInstance().updateEnrollmentStatus(true);
                                    
                                    // Update UI to reflect enrollment status
                                    updateEnrollmentStatus();
                                    
                                    // Show success message and offer to view enrolled subjects
                                    showEnrollmentSuccessDialog();
                                } else {
                                    // Show error message
                                    String errorMessage = response2.has("message") ? 
                                        response2.get("message").getAsString() : "An unknown error occurred";
                                    showAlert(Alert.AlertType.ERROR, "Enrollment Failed", errorMessage);
                                    
                                    // Reset UI
                                    enrollmentStatusLabel.setText("Status: Not Enrolled");
                                    enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                                    startEnrollmentBtn.setDisable(false);
                                    startEnrollmentBtn.setText("Start Enrollment Process");
                                }
                            });
                        })
                        .exceptionally(ex -> {
                            // Handle exceptions
                            javafx.application.Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Error", 
                                    "Could not complete enrollment: " + ex.getMessage());
                                
                                // Reset UI
                                enrollmentStatusLabel.setText("Status: Not Enrolled");
                                enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                                startEnrollmentBtn.setDisable(false);
                                startEnrollmentBtn.setText("Start Enrollment Process");
                            });
                            return null;
                        });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not start enrollment process: " + e.getMessage());
                    
                    // Reset UI
                    enrollmentStatusLabel.setText("Status: Not Enrolled");
                    enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    startEnrollmentBtn.setDisable(false);
                    startEnrollmentBtn.setText("Start Enrollment Process");
                }
            }
        });
    }
    
    /**
     * Shows a success dialog after enrollment with an option to view subjects
     */
private void showEnrollmentSuccessDialog() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Enrollment Approved");
    alert.setHeaderText("Enrollment Successful");
    alert.setContentText("Your enrollment has been approved. You can now view your study load in the Study Load section.");
    
    // Add button to view study load directly
    ButtonType viewStudyLoad = new ButtonType("View Study Load");
    ButtonType okButton = new ButtonType("OK", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
    
    alert.getButtonTypes().setAll(viewStudyLoad, okButton);
    
    alert.showAndWait().ifPresent(type -> {
        if (type == viewStudyLoad) {
            // Switch to study load tab
            switchToStudyLoad();
        }
    });
}

// Remove this redundant method as the functionality is already implemented in handleStartEnrollment
    
    /**
     * Update enrollment status based on isEnrolled property from API
     */
    private void updateEnrollmentStatus() {
        if (studentData == null) {
            return;
        }
        
        boolean isEnrolled = studentData.isEnrolled();
        
        // Update enrollment status and button text
        if (isEnrolled) {
            enrollmentStatusLabel.setText("Status: Enrolled");
            enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");  // Green color
            startEnrollmentBtn.setDisable(true);
            startEnrollmentBtn.setText("Already Enrolled");
            
            // Enable next year enrollment button when student is enrolled for current year
            if (nextYearEnrollmentBtn != null) {
                nextYearEnrollmentBtn.setDisable(false);
            }
        } else {
            enrollmentStatusLabel.setText("Status: Not Enrolled");
            enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");  // Red color
            startEnrollmentBtn.setDisable(false);
            startEnrollmentBtn.setText("Start Enrollment Process");
            
            // Disable next year enrollment button when student is not enrolled for current year
            if (nextYearEnrollmentBtn != null) {
                nextYearEnrollmentBtn.setDisable(true);
            }
        }
    }
    
    /**
     * Show an alert dialog
     * @param type Alert type
     * @param title Alert title
     * @param message Alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Handle next year enrollment button click
     */
    @FXML
    public void handleNextYearEnrollment() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Next Year Enrollment");
        confirmAlert.setHeaderText("Start Next Year Enrollment Process");
        confirmAlert.setContentText("Are you sure you want to begin the enrollment process for the 2026-2027 school year?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Disable button while processing
                    nextYearEnrollmentBtn.setDisable(true);
                    nextYearEnrollmentBtn.setText("Processing...");
                    
                    // Get student ID from current session
                    String studentId = studentData.getId();
                    
                    // Make the API call using EnrollmentService
                    org.finalproject.loginregisterfx.Service.EnrollmentService.preEnrollForNextYear(studentId)
                        .thenAccept(response2 -> {
                            // Update on JavaFX application thread
                            javafx.application.Platform.runLater(() -> {
                                // Check if pre-enrollment was successful
                                if (response2.has("success") && response2.get("success").getAsBoolean()) {
                                    // Show success message
                                    showAlert(Alert.AlertType.INFORMATION, "Next Year Enrollment Confirmed", 
                                        "Your enrollment for the 2026-2027 school year has been confirmed. " +
                                        "You will receive more information before the start of the next school year.");
                                    
                                    // Update button to show success
                                    nextYearEnrollmentBtn.setText("Enrolled for Next Year");
                                    nextYearEnrollmentBtn.setDisable(true);
                                } else {
                                    // Show error message
                                    String errorMessage = response2.has("message") ? 
                                        response2.get("message").getAsString() : "An unknown error occurred";
                                    showAlert(Alert.AlertType.ERROR, "Pre-Enrollment Failed", errorMessage);
                                    
                                    // Reset button
                                    nextYearEnrollmentBtn.setDisable(false);
                                    nextYearEnrollmentBtn.setText("Enroll for Next Year");
                                }
                            });
                        })
                        .exceptionally(ex -> {
                            // Handle exceptions
                            javafx.application.Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Error", 
                                    "Could not complete next year enrollment: " + ex.getMessage());
                                
                                // Reset button
                                nextYearEnrollmentBtn.setDisable(false);
                                nextYearEnrollmentBtn.setText("Enroll for Next Year");
                            });
                            return null;
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not complete next year enrollment: " + e.getMessage());
                    
                    // Reset button
                    nextYearEnrollmentBtn.setDisable(false);
                    nextYearEnrollmentBtn.setText("Enroll for Next Year");
                }
            }
        });
    }
    
    /**
     * Load enrolled subjects when switching to the study load tab
     */
    private void loadEnrolledSubjects() {
        if (studentData == null) {
            System.err.println("ERROR: studentData is null in loadEnrolledSubjects()");
            return;
        }
        
        // Show message if not enrolled
        if (!studentData.isEnrolled()) {
            showAlert(Alert.AlertType.INFORMATION, "Not Enrolled", 
                "You are not currently enrolled. Please complete the enrollment process first.");
            
            // Switch back to enrollment tab
            switchToEnrollment();
            return;
        }

        // Set school year
        if (schoolYearLabel != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            int currentYear = now.getYear();
            schoolYearLabel.setText("School Year " + currentYear + "-" + (currentYear + 1));
        } else {
            System.err.println("ERROR: schoolYearLabel is null");
        }

        // Load enrolled subjects from API
        String studentId = studentData.getId();
        System.out.println("Loading enrolled subjects for student ID: " + studentId);
        
        EnrollmentService.getEnrolledSubjects(studentId)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    try {
                        if (response != null && response.has("subjects") && response.get("subjects").isJsonArray()) {
                            System.out.println("Received subjects response successfully");
                            // Process the enrolled subjects
                            processEnrolledSubjects(response.getAsJsonArray("subjects"));
                        } else {
                            System.err.println("Invalid response format or empty subjects array");
                            if (response != null) {
                                System.err.println("Response: " + response.toString());
                            }
                            showAlert(Alert.AlertType.WARNING, "No Data", 
                                "No enrolled subjects found. Please check your enrollment status.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", 
                            "Could not load enrolled subjects: " + e.getMessage());
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not load enrolled subjects: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Process enrolled subjects from the API response
     * 
     * @param subjectsArray JsonArray of enrolled subjects
     */
    private void processEnrolledSubjects(JsonArray subjectsArray) {
        // Clear previous data
        enrolledSubjectsData.clear();
        
        // Process each subject and add to the observable list
        int count = subjectsArray.size();
        int totalUnits = 0;
        
        System.out.println("Processing " + count + " enrolled subjects");
        
        for (int i = 0; i < subjectsArray.size(); i++) {
            try {
                com.google.gson.JsonObject subjectJson = subjectsArray.get(i).getAsJsonObject();
                
                // Create a SubjectModel first
                SubjectModel subjectModel = new SubjectModel(
                    subjectJson.has("edpCode") ? subjectJson.get("edpCode").getAsString() : "N/A",
                    subjectJson.has("subjectName") ? subjectJson.get("subjectName").getAsString() : "Unknown Subject",
                    subjectJson.has("units") ? subjectJson.get("units").getAsInt() : 0,
                    subjectJson.has("department") ? subjectJson.get("department").getAsString() : "",
                    new String[0]  // No prerequisites needed for display
                );
                
                // Set teacher if available
                if (subjectJson.has("instructor")) {
                    subjectModel.setTeacherAssigned(subjectJson.get("instructor").getAsString());
                }
                
                // Create EnrolledSubjectModel from SubjectModel
                EnrolledSubjectModel enrolledSubject = new EnrolledSubjectModel(subjectModel);
                
                // Add schedule if available
                if (subjectJson.has("schedule")) {
                    enrolledSubject.setSchedule(subjectJson.get("schedule").getAsString());
                    System.out.println("Setting schedule: " + subjectJson.get("schedule").getAsString());
                }
                
                // Add to the observable list
                enrolledSubjectsData.add(enrolledSubject);
                
                // Add to total units
                totalUnits += subjectModel.getUnits();
                
            } catch (Exception e) {
                System.err.println("Error processing subject: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Set the table items
        subjectsTable.setItems(enrolledSubjectsData);
        
        // Set up cell value factories if not already set
        if (subjectCodeCol.getCellValueFactory() == null) {
            subjectCodeCol.setCellValueFactory(cellData -> cellData.getValue().subjectCodeProperty());
            subjectNameCol.setCellValueFactory(cellData -> cellData.getValue().subjectNameProperty());
            subjectUnitsCol.setCellValueFactory(cellData -> cellData.getValue().unitsProperty().asObject());
            scheduleCol.setCellValueFactory(cellData -> cellData.getValue().scheduleProperty());
            instructorCol.setCellValueFactory(cellData -> cellData.getValue().instructorProperty());
        }
          // Update count and total units labels
        subjectsEnrolledValue.setText(String.valueOf(count));
        if (totalUnitsLabel != null) {
            totalUnitsLabel.setText(String.valueOf(totalUnits));
        }
    }
    
    
    /**
     * Handle print study load button click
     */
    @FXML
    public void handlePrintStudyLoad() {
        showAlert(Alert.AlertType.INFORMATION, "Print Function", 
            "Print functionality will be implemented in a future update.");
    }
    
    /**
     * Handle export study load button click
     */
    @FXML
    public void handleExportStudyLoad() {
        showAlert(Alert.AlertType.INFORMATION, "Export Function", 
            "Export to PDF functionality will be implemented in a future update.");
    }
}