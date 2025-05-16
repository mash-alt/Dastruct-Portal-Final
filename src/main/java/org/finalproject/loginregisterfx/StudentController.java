package org.finalproject.loginregisterfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
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
    private Button refreshStudyLoadBtn;
    
    @FXML
    private Button refreshGradesBtn;
    
    @FXML
    private Button printGradeReportBtn;
    
    @FXML
    private Button exportGradeReportBtn;
    
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
    private Label phoneNumberValue;
    
    @FXML
    private Label addressValue;
    
    @FXML
    private Label courseValue;
    
    @FXML
    private Label semesterValue;
    
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
    
    // E-Grade table fields
    @FXML
    private TableView<EnrolledSubjectModel> gradesTable;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> gradeSubjectCodeCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> gradeSubjectNameCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, Integer> gradeSubjectUnitsCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> midtermGradeCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> finalGradeCol;
    
    @FXML
    private Label gradesSchoolYearLabel;
    
    @FXML
    private Label gpaLabel;
    
    // Observable list for enrolled subjects
    private ObservableList<EnrolledSubjectModel> enrolledSubjectsData = FXCollections.observableArrayList();
    
    // Store student data
    private StudentModel studentData;    @FXML
    public void initialize() {
        System.out.println("Initializing StudentController...");
        
        // Set default content visibility
        profileContent.setVisible(true);
        enrollmentContent.setVisible(false);
        studyLoadContent.setVisible(false);
        
        // Make sure eGradeContent is properly initialized
        if (eGradeContent != null) {
            eGradeContent.setVisible(false);
        } else {
            System.out.println("Note: eGradeContent not loaded yet, this is normal if the tab isn't created");
        }
        
        // Set default active button style
        updateNavButtonStyles("profile");
        
        // Verify that all FXML elements are properly loaded
        verifyFXMLElements();
          // Initialize the study load table columns
        if (subjectCodeCol != null && subjectNameCol != null && subjectUnitsCol != null && 
            scheduleCol != null && instructorCol != null) {
            
            // Set cell value factories
            subjectCodeCol.setCellValueFactory(cellData -> cellData.getValue().subjectCodeProperty());
            subjectNameCol.setCellValueFactory(cellData -> cellData.getValue().subjectNameProperty());
            subjectUnitsCol.setCellValueFactory(cellData -> cellData.getValue().unitsProperty().asObject());
            scheduleCol.setCellValueFactory(cellData -> cellData.getValue().scheduleProperty());
            
            // Enhanced instructor cell factory with formatting logic
            instructorCol.setCellValueFactory(cellData -> {
                String instructorName = cellData.getValue().getInstructor();
                // Ensure instructor name is displayed properly
                if (instructorName == null || instructorName.trim().isEmpty() || 
                    instructorName.equals("null") || instructorName.equalsIgnoreCase("undefined")) {
                    return new SimpleStringProperty("Not Assigned");
                }
                // Check if it needs to be capitalized or formatted
                if (instructorName.equals(instructorName.toLowerCase()) || instructorName.equals(instructorName.toUpperCase())) {
                    // Convert JOHN DOE or john doe to John Doe
                    String[] nameParts = instructorName.split(" ");
                    StringBuilder formattedName = new StringBuilder();
                    for (String part : nameParts) {
                        if (part.length() > 0) {
                            formattedName.append(part.substring(0, 1).toUpperCase());
                            if (part.length() > 1) {
                                formattedName.append(part.substring(1).toLowerCase());
                            }
                            formattedName.append(" ");
                        }
                    }
                    return new SimpleStringProperty(formattedName.toString().trim());
                }
                return new SimpleStringProperty(instructorName);
            });
            
            // Apply column styling for visibility
            String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
            subjectCodeCol.setStyle(columnStyle);
            subjectNameCol.setStyle(columnStyle);
            subjectUnitsCol.setStyle(columnStyle);
            scheduleCol.setStyle(columnStyle);
            instructorCol.setStyle(columnStyle);
            
            // Set the items source for the table
            if (subjectsTable != null) {
                subjectsTable.setItems(enrolledSubjectsData);
                
                // Make sure table has proper styling
                subjectsTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
                
                // Apply row styling
                subjectsTable.setRowFactory(tv -> {
                    javafx.scene.control.TableRow<EnrolledSubjectModel> row = new javafx.scene.control.TableRow<>();
                    row.setStyle("-fx-text-fill: #333333;");
                    return row;
                });
                
                System.out.println("Study load table initialized successfully with text styling");
            } else {
                System.err.println("ERROR: subjectsTable is null");
            }
        } else {
            System.err.println("ERROR: One or more table columns are null");
        }
          // Check if there's an active session and initialize data
        if (SessionManager.getInstance().isAuthenticated() && 
            SessionManager.getInstance().getCurrentStudent() != null) {
            this.studentData = SessionManager.getInstance().getCurrentStudent();
            
            // Debug output
            System.out.println("Student data loaded from session: " + 
                (studentData != null ? studentData.getName() + " (" + studentData.getStudentId() + ")" : "NULL"));
            
            if (studentData != null) {
                // Set some initial placeholder values to check visibility
                if (fullNameValue != null) {
                    fullNameValue.setText("Loading data...");
                    fullNameValue.setStyle("-fx-text-fill: #000000;"); // Force black text
                }
                
                // Make sure UI elements are properly loaded before updating
                Platform.runLater(() -> {
                    updateStudentInfo();
                    updateEnrollmentStatus();
                    System.out.println("Student data successfully displayed");
                    
                    // Double check the text color after update
                    if (fullNameValue != null) {
                        fullNameValue.setStyle("-fx-text-fill: #000000;"); // Ensure black text
                    }
                    if (studentIdValue != null) {
                        studentIdValue.setStyle("-fx-text-fill: #000000;");
                    }
                    if (emailValue != null) {
                        emailValue.setStyle("-fx-text-fill: #000000;");
                    }
                    if (courseValue != null) {
                        courseValue.setStyle("-fx-text-fill: #000000;");
                    }
                    if (yearLevelValue != null) {
                        yearLevelValue.setStyle("-fx-text-fill: #000000;");
                    }
                    if (sectionValue != null) {
                        sectionValue.setStyle("-fx-text-fill: #000000;");
                    }
                    if (subjectsEnrolledValue != null) {
                        subjectsEnrolledValue.setStyle("-fx-text-fill: #000000;");
                    }
                });
            } else {
                System.err.println("ERROR: Student data is null even though session is authenticated");
                // You might want to redirect to login page here
                showAlert(Alert.AlertType.ERROR, "Session Error", "Could not load student data. Please log in again.");
            }
        } else {
            System.err.println("WARNING: No authenticated session found during initialization");
            // You might want to redirect to login page here
            showAlert(Alert.AlertType.WARNING, "Session Expired", "Your session has expired. Please log in again.");
        }
          // Get current school year
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        System.out.println("Current academic year: " + currentYear + "-" + (currentYear + 1));
          // Set current academic year in enrollment view if the label exists
        if (schoolYearLabel != null) {
            schoolYearLabel.setText("School Year " + currentYear + "-" + (currentYear + 1));
        }
        
        // If we have a refresh study load button, set its styling
        if (refreshStudyLoadBtn != null) {
            refreshStudyLoadBtn.getStyleClass().add("export-button");
        }
        
        // Style enrollment and study load elements - ensure text is visible
        if (enrollmentStatusLabel != null) {
            enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
        }
        
        if (schoolYearLabel != null) {
            schoolYearLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;");
        }
        
        if (totalUnitsLabel != null) {
            totalUnitsLabel.setStyle("-fx-text-fill: #333333;");
        }
        
        System.out.println("StudentController initialization complete");
    }
    
    /**
     * Verify that all required FXML elements are properly loaded
     * This helps with debugging UI issues
     */
    private void verifyFXMLElements() {
        StringBuilder missingElements = new StringBuilder();
          // Check profile view elements
        if (fullNameValue == null) missingElements.append("fullNameValue, ");
        if (studentIdValue == null) missingElements.append("studentIdValue, ");
        if (emailValue == null) missingElements.append("emailValue, ");
        if (phoneNumberValue == null) missingElements.append("phoneNumberValue, ");
        if (addressValue == null) missingElements.append("addressValue, ");
        if (courseValue == null) missingElements.append("courseValue, ");
        if (semesterValue == null) missingElements.append("semesterValue, ");
        if (yearLevelValue == null) missingElements.append("yearLevelValue, ");
        if (sectionValue == null) missingElements.append("sectionValue, ");
        
        // Check enrollment elements
        if (enrollmentStatusLabel == null) missingElements.append("enrollmentStatusLabel, ");
        if (startEnrollmentBtn == null) missingElements.append("startEnrollmentBtn, ");
        if (nextYearEnrollmentBtn == null) missingElements.append("nextYearEnrollmentBtn, ");
        
        // Check sidebar elements
        if (studentNameLabel == null) missingElements.append("studentNameLabel, ");
        if (studentIDLabel == null) missingElements.append("studentIDLabel, ");
        
        // Check if any elements are missing
        if (missingElements.length() > 0) {
            System.err.println("WARNING: The following FXML elements were not loaded: " + 
                               missingElements.toString());
        } else {
            System.out.println("All essential FXML elements are successfully loaded");
        }
    }
      /**
     * Initialize student data from login response
     * @param userData JsonObject containing student data from API
     */    public void initializeStudentData(JsonObject userData) {
        try {
            // Create StudentModel from the response
            System.out.println("Initializing student data from: " + (userData != null ? userData.toString() : "null JSON"));
            
            // Extract actual user data - the API now returns user data nested in a "user" object
            JsonObject actualUserData = userData;
            if (userData != null && userData.has("user") && userData.get("user").isJsonObject()) {
                System.out.println("Found user object in response, extracting student data");
                actualUserData = userData.getAsJsonObject("user");
            }
              
            // Check for academic history and enrolled subjects
            if (actualUserData != null) {
                System.out.println("Processing student data: " + actualUserData);
                
                if (actualUserData.has("academicHistory")) {
                    JsonArray academicHistory = actualUserData.getAsJsonArray("academicHistory");
                    System.out.println("Found academic history with " + academicHistory.size() + " terms");
                    
                    if (academicHistory.size() > 0 && academicHistory.get(0).isJsonObject()) {
                        JsonObject currentTerm = academicHistory.get(0).getAsJsonObject();
                        if (currentTerm.has("subjects")) {
                            JsonArray subjects = currentTerm.getAsJsonArray("subjects");
                            System.out.println("Current term has " + subjects.size() + " subjects");
                            
                            // Log details about each subject to help with debugging
                            for (int i = 0; i < subjects.size() && i < 5; i++) { // Log up to first 5 subjects
                                if (subjects.get(i).isJsonObject()) {
                                    JsonObject subject = subjects.get(i).getAsJsonObject();
                                    String name = subject.has("subjectName") ? subject.get("subjectName").getAsString() : "Unknown";
                                    String code = subject.has("edpCode") ? subject.get("edpCode").getAsString() : "Unknown";
                                    
                                    System.out.println("Subject: " + name + " (Code: " + code + ")");
                                    
                                    // Check if grades exist
                                    if (subject.has("midtermGrade") && !subject.get("midtermGrade").isJsonNull()) {
                                        System.out.println("  Midterm grade: " + subject.get("midtermGrade").getAsString());
                                    }
                                    if (subject.has("finalGrade") && !subject.get("finalGrade").isJsonNull()) {
                                        System.out.println("  Final grade: " + subject.get("finalGrade").getAsString());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("No academic history found in student data");
                }
                
                if (actualUserData.has("enrolledSubjects")) {
                    System.out.println("Found enrolledSubjects list with " + 
                        actualUserData.getAsJsonArray("enrolledSubjects").size() + " subject IDs");
                }
            }
            
            // Create the student model from the actual user data
            this.studentData = new StudentModel(actualUserData);
              // Extract the actual user data if needed before saving to session
            JsonObject dataToSave = userData;
            if (userData != null && userData.has("user") && userData.get("user").isJsonObject()) {
                dataToSave = userData.getAsJsonObject("user");
            }
            
            // Save student in session for persistence
            SessionManager.getInstance().updateStudentData(dataToSave);
            
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
    }    /**
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
        
        // Make sure this runs on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                // Update sidebar profile
                if (studentNameLabel != null) {
                    studentNameLabel.setText(studentData.getName());
                }
                
                if (studentIDLabel != null) {
                    studentIDLabel.setText(studentData.getStudentId());
                }
                
                // Update profile content with explicit text color                
                if (fullNameValue != null) {
                    fullNameValue.setText(studentData.getName());
                    fullNameValue.setStyle("-fx-text-fill: #000000;");
                    
                    // Double-check that the text is visible
                    System.out.println("Setting fullNameValue to: " + studentData.getName());
                    System.out.println("fullNameValue style: " + fullNameValue.getStyle());
                    System.out.println("fullNameValue parent background: " + 
                                      (fullNameValue.getParent() != null ? 
                                       fullNameValue.getParent().getStyle() : "null parent"));
                } else {
                    System.err.println("ERROR: fullNameValue label is null");
                }
                
                if (studentIdValue != null) {
                    studentIdValue.setText(studentData.getStudentId());
                    studentIdValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting studentIdValue to: " + studentData.getStudentId());
                } else {
                    System.err.println("ERROR: studentIdValue label is null");
                }
                  if (emailValue != null) {
                    emailValue.setText(studentData.getEmail());
                    emailValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting emailValue to: " + studentData.getEmail());
                } else {
                    System.err.println("ERROR: emailValue label is null");
                }
                
                // Set phone number
                if (phoneNumberValue != null) {
                    phoneNumberValue.setText(studentData.getPhoneNumber());
                    phoneNumberValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting phoneNumberValue to: " + studentData.getPhoneNumber());
                } else {
                    System.err.println("ERROR: phoneNumberValue label is null");
                }
                
                // Set address
                if (addressValue != null) {
                    addressValue.setText(studentData.getAddress());
                    addressValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting addressValue to: " + studentData.getAddress());
                } else {
                    System.err.println("ERROR: addressValue label is null");
                }
                
                // Set phone number
                if (phoneNumberValue != null) {
                    phoneNumberValue.setText(studentData.getPhoneNumber());
                    phoneNumberValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting phoneNumberValue to: " + studentData.getPhoneNumber());
                } else {
                    System.err.println("ERROR: phoneNumberValue label is null");
                }
                
                // Set address
                if (addressValue != null) {
                    addressValue.setText(studentData.getAddress());
                    addressValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting addressValue to: " + studentData.getAddress());
                } else {
                    System.err.println("ERROR: addressValue label is null");
                }
                  if (courseValue != null) {
                    courseValue.setText(studentData.getCourse());
                    courseValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting courseValue to: " + studentData.getCourse());
                } else {
                    System.err.println("ERROR: courseValue label is null");
                }
                  // Set semester
                if (semesterValue != null) {
                    // Get semester directly from the student model
                    String semester = studentData.getSemester();
                    semesterValue.setText(semester);
                    semesterValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting semesterValue to: " + semester);
                } else {
                    System.err.println("ERROR: semesterValue label is null");
                }
                
                if (yearLevelValue != null) {
                    yearLevelValue.setText(studentData.getYearLevelString());
                    yearLevelValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting yearLevelValue to: " + studentData.getYearLevelString());
                } else {
                    System.err.println("ERROR: yearLevelValue label is null");
                }
                
                if (sectionValue != null) {
                    sectionValue.setText(studentData.getSection());
                    sectionValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting sectionValue to: " + studentData.getSection());
                } else {
                    System.err.println("ERROR: sectionValue label is null");
                }
                  // Update GPA and enrolled subjects
                if (gpaValue != null) {
                    gpaValue.setText(studentData.getFormattedGPA());
                    // GPA already has a custom color in the FXML
                    System.out.println("Setting gpaValue to: " + studentData.getFormattedGPA());
                } else {
                    System.err.println("ERROR: gpaValue label is null");
                }                  // Count enrolled subjects - try all available sources and use the maximum count
                int enrolledSubjectsCount = 0;
                
                // Check all possible sources and use the highest number found
                // 1. Academic history (most reliable source)
                if (studentData.getCurrentAcademicTerm() != null && 
                    studentData.getCurrentAcademicTerm().getSubjects() != null &&
                    !studentData.getCurrentAcademicTerm().getSubjects().isEmpty()) {
                    int historyCount = studentData.getCurrentAcademicTerm().getSubjects().size();
                    enrolledSubjectsCount = Math.max(enrolledSubjectsCount, historyCount);
                    System.out.println("Academic history subject count: " + historyCount);
                }
                
                // 2. Check enrolled subject IDs
                if (studentData.getEnrolledSubjectIds() != null && !studentData.getEnrolledSubjectIds().isEmpty()) {
                    int idsCount = studentData.getEnrolledSubjectIds().size();
                    enrolledSubjectsCount = Math.max(enrolledSubjectsCount, idsCount);
                    System.out.println("Enrolled subject IDs count: " + idsCount);
                }
                
                // 3. Check enrolled subjects objects
                if (studentData.getEnrolledSubjects() != null && !studentData.getEnrolledSubjects().isEmpty()) {
                    int subjectsCount = studentData.getEnrolledSubjects().size();
                    enrolledSubjectsCount = Math.max(enrolledSubjectsCount, subjectsCount);
                    System.out.println("Enrolled subjects objects count: " + subjectsCount);
                }
                
                System.out.println("Final subjects count determined: " + enrolledSubjectsCount);
                
                if (subjectsEnrolledValue != null) {
                    subjectsEnrolledValue.setText(String.valueOf(enrolledSubjectsCount));
                    subjectsEnrolledValue.setStyle("-fx-text-fill: #000000;");
                    System.out.println("Setting subjectsEnrolledValue to: " + enrolledSubjectsCount);
                } else {
                    System.err.println("ERROR: subjectsEnrolledValue label is null");
                }
                
                // Also update enrollment status since it's related to student info
                updateEnrollmentStatus();
                
                System.out.println("Student info update completed");
            } catch (Exception e) {
                System.err.println("Error updating student info UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
        loadGrades();
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
     */    @FXML    public void handleLogout() {
        try {
            System.out.println("Opening logout confirmation dialog...");
            
            // Load the Logout.fxml dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Logout.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the owner stage
            LogoutDialogController controller = loader.getController();
            controller.setOwnerStage((Stage) logoutBtn.getScene().getWindow());
            
            // End student session if needed (this will be managed in LogoutDialogController)
            if (SessionManager.getInstance() != null) {
                SessionManager.getInstance().endSession();
            }
            
            // Create and configure dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Logout");
            dialogStage.initOwner(logoutBtn.getScene().getWindow());
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Failed to open logout dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Failed to open logout dialog: " + e.getMessage());
        }
    }
      /**
     * Handle the start enrollment button click
     */
    @FXML
public void handleStartEnrollment() {
    // Get current year for the enrollment process
    java.time.LocalDate now = java.time.LocalDate.now();
    int currentYear = now.getYear();
    String academicYear = currentYear + "-" + (currentYear + 1);

    // Show confirmation dialog with academic year details
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Confirm Enrollment");
    confirmAlert.setHeaderText("Start Enrollment Process");
    confirmAlert.setContentText("Are you sure you want to begin the enrollment process for the " +
            academicYear + " school year?\n\n" +
            "This will register you for classes based on your program requirements.");

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
                // Make sure to use the studentId field (ucb-XXXXX) instead of the MongoDB _id field
                String studentId = studentData.getStudentId();

                System.out.println("Student ID for enrollment (original): " + studentId);

                // Ensure the student ID has the proper format - the backend expects ucb-XXXXX
                if (studentId != null && !studentId.startsWith("ucb-")) {
                    studentId = "ucb-" + studentId;
                    System.out.println("Added ucb- prefix to student ID: " + studentId);
                } else {
                    System.out.println("Student ID already has correct format: " + studentId);
                }

                // Verify the studentId is valid and well-formed
                if (studentId == null || studentId.trim().isEmpty() || !studentId.matches("ucb-\\d+")) {
                    System.err.println("ERROR: Invalid student ID format: " + studentId);
                    showAlert(Alert.AlertType.ERROR, "Invalid Student ID",
                            "Your student ID appears to be invalid or missing. Please contact IT support.");
                    return;
                }

                // Show processing indicator
                showAlert(Alert.AlertType.INFORMATION, "Processing",
                        "Your enrollment request is being processed. This may take a moment...");

                // Debug logging to confirm the values being sent
                System.out.println("ENROLLMENT REQUEST DETAILS:");
                System.out.println("- Student ID: " + studentId);
                System.out.println("- Academic Year: " + academicYear);
                System.out.println("- Semester: First");
                System.out.println("- Auth token length: " +
                        (org.finalproject.loginregisterfx.Service.AuthService.getAuthToken() != null ?
                                org.finalproject.loginregisterfx.Service.AuthService.getAuthToken().length() : "null"));                // First fetch available subjects based on student's department and year level
                int yearLevel = studentData.getYearLevel();
                String department = studentData.getCourse();
                System.out.println("Fetching available subjects for year level: " + yearLevel + ", department: " + department);
                
                // First, get the available subjects
                org.finalproject.loginregisterfx.Service.EnrollmentService.getAvailableSubjects(yearLevel, department, "First")
                .thenAccept(subjectsResponse -> {
                    List<String> subjectIds = new ArrayList<>();
                    
                    if (subjectsResponse != null && subjectsResponse.has("subjects") && 
                        subjectsResponse.get("subjects").isJsonArray()) {
                        
                        JsonArray availableSubjects = subjectsResponse.getAsJsonArray("subjects");
                        System.out.println("Found " + availableSubjects.size() + " available subjects");
                        
                        // Extract subject IDs from available subjects
                        for (int i = 0; i < availableSubjects.size(); i++) {
                            JsonObject subject = availableSubjects.get(i).getAsJsonObject();
                            if (subject.has("_id")) {
                                subjectIds.add(subject.get("_id").getAsString());
                            }
                        }
                    } else {
                        System.out.println("No subjects found in response or unexpected format");
                        // Adding some placeholder subjects - these will be validated by the backend
                        // They're just placeholder IDs in case we can't fetch real ones
                        subjectIds.add("646c124512b8e255c9e1aaac");
                        subjectIds.add("646c124512b8e255c9e1aaad");
                        subjectIds.add("646c124512b8e255c9e1aaae");
                    }
                    
                    System.out.println("Proceeding with enrollment using " + subjectIds.size() + " subject IDs");
                    
                    // Now proceed with enrollment using the fetched subject IDs
                    org.finalproject.loginregisterfx.Service.EnrollmentService.enrollStudent(
                            studentId, subjectIds, academicYear, "First"
                    ).thenAccept(response2 -> {
                    // Update on JavaFX application thread
                    javafx.application.Platform.runLater(() -> {
                        // Check if enrollment was successful (response has a message and no error field)
                        if (response2.has("message") && !response2.has("error")) {
                            // Update student model
                            studentData.setEnrolled(true);

                            // If the response contains the updated student data, update our local model
                            if (response2.has("student")) {
                                // Update enrollment status from the response
                                JsonObject studentObj = response2.getAsJsonObject("student");
                                if (studentObj.has("isEnrolled")) {
                                    studentData.setEnrolled(studentObj.get("isEnrolled").getAsBoolean());
                                }

                                // Print received student data for debugging
                                System.out.println("Received student data from successful enrollment response: " + studentObj.toString());
                            }                            // Update session manager
                            org.finalproject.loginregisterfx.Service.SessionManager.getInstance().updateEnrollmentStatus(true);

                            // Instead of refreshing, directly update UI with current data
                            updateStudentInfo();

                            // Update UI to reflect enrollment status
                            updateEnrollmentStatus();

                            // Show success message and offer to view enrolled subjects
                            showEnrollmentSuccessDialog();
                        } else {
                            // Show error message with details and suggestions
                            String errorMessage = response2.has("error") ?
                                    response2.get("error").getAsString() : "An unknown error occurred";

                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Enrollment Failed");
                            errorAlert.setHeaderText("Could not complete enrollment");
                            errorAlert.setContentText(errorMessage + "\n\n" +
                                    "Possible reasons:\n" +
                                    "- You may have outstanding requirements\n" +
                                    "- There might be a problem with course availability\n" +
                                    "- System connectivity issues\n\n" +
                                    "Please contact the registrar's office for assistance.");
                            errorAlert.showAndWait();

                            // Reset UI
                            enrollmentStatusLabel.setText("Status: Not Enrolled");
                            enrollmentStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                            startEnrollmentBtn.setDisable(false);
                            startEnrollmentBtn.setText("Start Enrollment Process");
                        }
                    });
                }).exceptionally(ex -> {
                    // Handle exceptions with more detailed information
                    javafx.application.Platform.runLater(() -> {
                        String errorMessage = ex.getMessage();
                        System.err.println("Enrollment error: " + errorMessage);

                        // Check if it's a connection error
                        boolean isConnectionError = errorMessage != null &&
                                (errorMessage.contains("Connection refused") ||
                                        errorMessage.contains("connect timed out") ||
                                        errorMessage.contains("Unable to connect"));

                        if (isConnectionError) {
                            showAlert(Alert.AlertType.ERROR, "Connection Error",
                                    "Could not connect to the enrollment server.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Enrollment Error",
                                    "Could not complete enrollment: " + errorMessage);
                        }

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
     * This method manages the pre-enrollment process for the next academic year
     */
    @FXML
    public void handleNextYearEnrollment() {
        // Check current enrollment status first
        if (!studentData.isEnrolled()) {
            showAlert(Alert.AlertType.WARNING, "Not Eligible", 
                "You must be enrolled in the current academic year before you can pre-enroll for next year.");
            return;
        }
        
        // Calculate next academic year for display
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        String nextAcademicYear = (currentYear + 1) + "-" + (currentYear + 2);
        
        // Show confirmation dialog with next year information
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Next Year Enrollment");
        confirmAlert.setHeaderText("Start Next Year Enrollment Process");
        confirmAlert.setContentText("Are you sure you want to begin the enrollment process for the " + 
                                   nextAcademicYear + " school year?\n\n" +
                                   "This will pre-register you for next year's subjects based on your current academic progress.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {                try {
                    // Disable button while processing
                    nextYearEnrollmentBtn.setDisable(true);
                    nextYearEnrollmentBtn.setText("Processing...");
                    
                    // Get student ID from current session
                    String studentId = studentData.getStudentId();
                    
                    // Show processing indicator
                    showAlert(Alert.AlertType.INFORMATION, "Processing", 
                        "Your pre-enrollment request is being processed. This may take a moment...");
                      // Make the API call using EnrollmentService
                    org.finalproject.loginregisterfx.Service.EnrollmentService.preEnrollForNextYear(studentId)
                        .thenAccept(response2 -> {
                            // Update on JavaFX application thread
                            javafx.application.Platform.runLater(() -> {
                                // Check if pre-enrollment was successful (response has message and no error field)
                                if (response2.has("message") && !response2.has("error")) {
                                    // Show detailed success message with next steps
                                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                    successAlert.setTitle("Next Year Enrollment Confirmed");
                                    successAlert.setHeaderText("Pre-Enrollment Successful");
                                    successAlert.setContentText(
                                        "Your enrollment for the " + nextAcademicYear + " school year has been confirmed.\n\n" +
                                        "Next Steps:\n" +
                                        "1. Check your email for confirmation details\n" +
                                        "2. Complete any outstanding financial requirements\n" +
                                        "3. Update your personal information if needed\n\n" +
                                        "Your subject schedule will be available before the start of the next academic year."
                                    );
                                    successAlert.showAndWait();
                                      // Update button to show success
                                    nextYearEnrollmentBtn.setText("Enrolled for Next Year");
                                    nextYearEnrollmentBtn.setDisable(true);
                                    nextYearEnrollmentBtn.setStyle("-fx-text-fill: white;"); // Ensure text is visible
                                    
                                    // Store pre-enrollment status somewhere if needed
                                    // This could be stored in StudentModel if needed                                } else {
                                    // Show error message with details
                                    String errorMessage = response2.has("error") ? 
                                        response2.get("error").getAsString() : "An unknown error occurred";
                                    
                                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                    errorAlert.setTitle("Pre-Enrollment Failed");
                                    errorAlert.setHeaderText("Could not complete pre-enrollment");
                                    errorAlert.setContentText(errorMessage + "\n\nPlease contact the registrar's office for assistance.");
                                    errorAlert.showAndWait();
                                    
                                    // Reset button
                                    nextYearEnrollmentBtn.setDisable(false);
                                    nextYearEnrollmentBtn.setText("Enroll for Next Year");
                                }
                            });
                        })
                        .exceptionally(ex -> {
                            // Handle exceptions
                            javafx.application.Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Connection Error", 
                                    "Could not complete next year enrollment: " + ex.getMessage());
                                
                                // Reset button
                                nextYearEnrollmentBtn.setDisable(false);
                                nextYearEnrollmentBtn.setText("Enroll for Next Year");
                            });
                            return null;
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "System Error", 
                        "Could not complete next year enrollment due to a system error: " + e.getMessage() + 
                        "\n\nPlease contact technical support for assistance.");
                    
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
            schoolYearLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;"); // Ensure visible text
        } else {
            System.err.println("ERROR: schoolYearLabel is null");
        }
        
        // Clear previous data
        enrolledSubjectsData.clear();

        // First, try to use data from the student's academic history (similar to loadGrades method)
        StudentModel.AcademicTerm currentTerm = studentData.getCurrentAcademicTerm();
        
        if (currentTerm != null && currentTerm.getSubjects() != null && !currentTerm.getSubjects().isEmpty()) {
            System.out.println("Loading enrolled subjects from academic history. Found " + 
                currentTerm.getSubjects().size() + " subjects.");
            
            int totalUnits = 0;
            
            // Convert academic history subjects to EnrolledSubjectModel
            for (StudentModel.SubjectGrade subjectGrade : currentTerm.getSubjects()) {
                // Create EnrolledSubjectModel directly from SubjectGrade
                EnrolledSubjectModel enrolledSubject = new EnrolledSubjectModel(
                    subjectGrade.getEdpCode(),
                    subjectGrade.getSubjectName(),
                    subjectGrade.getUnits(),
                    "TBA", // Schedule not available in academic history
                    "TBA", // Instructor not available in academic history
                    subjectGrade.getSubjectId(),
                    subjectGrade.getMidtermGradeFormatted(),
                    subjectGrade.getFinalGradeFormatted()
                );
                
                // Add to the observable list
                enrolledSubjectsData.add(enrolledSubject);
                
                // Add to total units
                totalUnits += subjectGrade.getUnits();
            }
            
            // Set the table items
            subjectsTable.setItems(enrolledSubjectsData);
            
            // Always update cell value factories to ensure proper formatting
            subjectCodeCol.setCellValueFactory(cellData -> cellData.getValue().subjectCodeProperty());
            subjectNameCol.setCellValueFactory(cellData -> cellData.getValue().subjectNameProperty());
            subjectUnitsCol.setCellValueFactory(cellData -> cellData.getValue().unitsProperty().asObject());
            scheduleCol.setCellValueFactory(cellData -> cellData.getValue().scheduleProperty());
            
            // Special handling for instructor column
            instructorCol.setCellValueFactory(cellData -> {
                String name = cellData.getValue().getInstructor();
                // Ensure instructor name is displayed properly
                if (name == null || name.trim().isEmpty() || 
                    name.equals("null") || name.equalsIgnoreCase("undefined")) {
                    return new SimpleStringProperty("Not Assigned");
                }
                return new SimpleStringProperty(name);
            });
            
            instructorCol.setVisible(true);
            
            // Update count and total units labels
            if (subjectsEnrolledValue != null) {
                subjectsEnrolledValue.setText(String.valueOf(currentTerm.getSubjects().size()));
                subjectsEnrolledValue.setStyle("-fx-text-fill: #333333;"); // Ensure visible text
            }
            
            if (totalUnitsLabel != null) {
                totalUnitsLabel.setText(String.valueOf(totalUnits));
                totalUnitsLabel.setStyle("-fx-text-fill: #333333;"); // Ensure visible text
            }
            
            return;
        }
    
        // If no data in academic history, load from API
        String studentId = studentData.getStudentId();
        System.out.println("Loading enrolled subjects for student ID: " + studentId);
          EnrollmentService.getEnrolledSubjects(studentId)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    try {
                        // Debug the full response to see its structure
                        System.out.println("Full study load response: " + response.toString());
                
                // The backend can return different structures, handle all possible formats
                if (response != null && response.has("studyLoad") && response.get("studyLoad").isJsonArray()) {
                    // Format: { studyLoad: [...] }
                    System.out.println("Received subjects response with direct studyLoad array");
                    processEnrolledSubjects(response.getAsJsonArray("studyLoad"));
                } 
                // Format: { data: { studyLoad: [...] } }
                else if (response != null && response.has("data") && 
                         response.get("data").isJsonObject() && 
                         response.getAsJsonObject("data").has("studyLoad") && 
                         response.getAsJsonObject("data").get("studyLoad").isJsonArray()) {
                    System.out.println("Found studyLoad in nested data object");
                    processEnrolledSubjects(response.getAsJsonObject("data").getAsJsonArray("studyLoad"));
                }
                // Format: { message: "...", student: {...}, studyLoad: [...] }
                else if (response != null && response.has("message") && response.has("studyLoad") && 
                         response.get("studyLoad").isJsonArray()) {
                    System.out.println("Found studyLoad array with message");
                    processEnrolledSubjects(response.getAsJsonArray("studyLoad"));
                    
                    // If there's updated student data, update student model
                    if (response.has("student") && response.get("student").isJsonObject()) {
                        JsonObject studentObj = response.getAsJsonObject("student");
                        SessionManager.getInstance().updateStudentData(studentObj);
                        // Update student data model
                        studentData = new StudentModel(studentObj);
                        // Update UI
                        updateStudentInfo();
                    }
                }
                else {
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
                
                // Debug the raw JSON for this subject
                System.out.println("Processing subject: " + subjectJson);
                
                // Create a SubjectModel first
                SubjectModel subjectModel = new SubjectModel(
                    subjectJson.has("edpCode") ? subjectJson.get("edpCode").getAsString() : "N/A",
                    subjectJson.has("name") && !subjectJson.get("name").isJsonNull() ? 
                        subjectJson.get("name").getAsString() : 
                        (subjectJson.has("subjectName") ? subjectJson.get("subjectName").getAsString() : "Unknown Subject"),
                    subjectJson.has("units") ? subjectJson.get("units").getAsInt() : 0,
                    subjectJson.has("department") ? subjectJson.get("department").getAsString() : "",
                    new String[0]  // No prerequisites needed for display
                );
                  // Set teacher if available - handle all possible formats from the API
                String instructorName = "Not Assigned";
                
                // First check for teacherName field (string) from the updated backend
                if (subjectJson.has("teacherName") && !subjectJson.get("teacherName").isJsonNull()) {
                    instructorName = subjectJson.get("teacherName").getAsString();
                    System.out.println("Setting teacher from teacherName: " + instructorName);
                } 
                // Check for teacherFullName field (another possible format)
                else if (subjectJson.has("teacherFullName") && !subjectJson.get("teacherFullName").isJsonNull()) {
                    instructorName = subjectJson.get("teacherFullName").getAsString();
                    System.out.println("Setting teacher from teacherFullName: " + instructorName);
                }
                // Check for instructor object with different name properties
                else if (subjectJson.has("instructor") && !subjectJson.get("instructor").isJsonNull()) {
                    if (subjectJson.get("instructor").isJsonObject()) {
                        JsonObject instructor = subjectJson.get("instructor").getAsJsonObject();
                        
                        // Try fullName first
                        if (instructor.has("fullName") && !instructor.get("fullName").isJsonNull()) {
                            instructorName = instructor.get("fullName").getAsString();
                            System.out.println("Setting teacher from instructor.fullName: " + instructorName);
                        }
                        // Try name property
                        else if (instructor.has("name") && !instructor.get("name").isJsonNull()) {
                            instructorName = instructor.get("name").getAsString();
                            System.out.println("Setting teacher from instructor.name: " + instructorName);
                        }
                        // Try firstName + lastName combination
                        else if (instructor.has("firstName") && !instructor.get("firstName").isJsonNull() &&
                                 instructor.has("lastName") && !instructor.get("lastName").isJsonNull()) {
                            String firstName = instructor.get("firstName").getAsString();
                            String lastName = instructor.get("lastName").getAsString();
                            instructorName = firstName + " " + lastName;
                            System.out.println("Setting teacher from instructor.firstName + lastName: " + instructorName);
                        }
                    } else if (subjectJson.get("instructor").isJsonPrimitive()) {
                        // Handle case where instructor might be a direct string value
                        instructorName = subjectJson.get("instructor").getAsString();
                        System.out.println("Setting teacher from instructor string: " + instructorName);
                    }
                }
                
                // Set the teacher name in the model
                subjectModel.setTeacherAssigned(instructorName);
                System.out.println("Final instructor name set: " + instructorName);
                
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
        
        // Always update cell value factories to ensure proper formatting
        subjectCodeCol.setCellValueFactory(cellData -> cellData.getValue().subjectCodeProperty());
        subjectNameCol.setCellValueFactory(cellData -> cellData.getValue().subjectNameProperty());
        subjectUnitsCol.setCellValueFactory(cellData -> cellData.getValue().unitsProperty().asObject());
        scheduleCol.setCellValueFactory(cellData -> cellData.getValue().scheduleProperty());
        
        // Special handling for instructor column to format it nicely
        instructorCol.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getInstructor();
            // Ensure instructor name is displayed properly
            if (name == null || name.trim().isEmpty() || 
                name.equals("null") || name.equalsIgnoreCase("undefined")) {
                return new SimpleStringProperty("Not Assigned");
            }
            return new SimpleStringProperty(name);
        });
        
        // Make sure instructor column is visible
        instructorCol.setVisible(true);
        
        // Update count and total units labels
        if (subjectsEnrolledValue != null) {
            subjectsEnrolledValue.setText(String.valueOf(count));
            subjectsEnrolledValue.setStyle("-fx-text-fill: #333333;"); // Ensure visible text
        }
        
        if (totalUnitsLabel != null) {
            totalUnitsLabel.setText(String.valueOf(totalUnits));
            totalUnitsLabel.setStyle("-fx-text-fill: #333333;"); // Ensure visible text
        }
    }
      /**
     * Refresh student data from the backend API
     * This can be called after enrollment or any other action that might change student data
     */
    
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
      /**
     * Handle refresh study load button click
     * This method is called when the user clicks the Refresh Study Load button
     */    
    @FXML
    public void handleRefreshStudyLoad() {
        if (studentData == null || studentData.getStudentId() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "No student data available to refresh study load.");
            return;
        }
        
        // Show loading indicator
        showAlert(Alert.AlertType.INFORMATION, "Refreshing Data", "Loading your latest study load information...");
        
        // Get the student ID for API calls
        String studentId = studentData.getStudentId();
        System.out.println("Refreshing study load data for student ID: " + studentId);
        
        // First refresh student profile data to potentially update academic history
        AuthService.getStudentProfile(studentId)
            .thenAccept(response -> {
                if (response != null) {
                    System.out.println("Updated student profile data received");
                    
                    // Extract student data
                    JsonObject studentJson = null;
                    if (response.has("student")) {
                        studentJson = response.getAsJsonObject("student");
                    } else {
                        // If the student data is at the root level
                        studentJson = response;
                    }
                    
                    if (studentJson != null) {
                        // Update student model with fresh data
                        final JsonObject finalStudentJson = studentJson;
                        Platform.runLater(() -> {
                            studentData = new StudentModel(finalStudentJson);
                            SessionManager.getInstance().updateStudentData(finalStudentJson);
                            
                            // Now directly fetch enrolled subjects using the EnrollmentService API
                            // The API will give us the latest data regardless of what's in the student model
                            EnrollmentService.getEnrolledSubjects(studentId)
                                .thenAccept(studyLoadResponse -> {
                                    Platform.runLater(() -> {
                                        try {
                                            System.out.println("Received fresh study load data from API");
                                            
                                            if (studyLoadResponse != null) {
                                                // Check if we got data in the various possible formats
                                                if (studyLoadResponse.has("studyLoad") && studyLoadResponse.get("studyLoad").isJsonArray()) {
                                                    processEnrolledSubjects(studyLoadResponse.getAsJsonArray("studyLoad"));
                                                } 
                                                else if (studyLoadResponse.has("data") && 
                                                        studyLoadResponse.get("data").isJsonObject() && 
                                                        studyLoadResponse.getAsJsonObject("data").has("studyLoad") && 
                                                        studyLoadResponse.getAsJsonObject("data").get("studyLoad").isJsonArray()) {
                                                    processEnrolledSubjects(studyLoadResponse.getAsJsonObject("data").getAsJsonArray("studyLoad"));
                                                }
                                                else if (studyLoadResponse.has("message") && studyLoadResponse.has("studyLoad") && 
                                                        studyLoadResponse.get("studyLoad").isJsonArray()) {
                                                    processEnrolledSubjects(studyLoadResponse.getAsJsonArray("studyLoad"));
                                                }
                                                else {
                                                    // If no study load data found, show a message
                                                    showAlert(Alert.AlertType.INFORMATION, "No Data", 
                                                        "No enrolled subjects found. Please check your enrollment status.");
                                                }
                                            } else {
                                                showAlert(Alert.AlertType.WARNING, "No Data", 
                                                    "Could not retrieve study load information.");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            showAlert(Alert.AlertType.ERROR, "Error", 
                                                "Could not process study load data: " + e.getMessage());
                                        }
                                    });
                                })
                                .exceptionally(ex -> {
                                    Platform.runLater(() -> {
                                        showAlert(Alert.AlertType.ERROR, "Network Error", 
                                            "Failed to connect to server. Please check your internet connection and try again: " + ex.getMessage());
                                    });
                                    return null;
                                });
                        });
                    } else {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.WARNING, "Data Error", 
                                "Could not extract student data from response.");
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.WARNING, "No Data", 
                            "No profile data received from server.");
                    });
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Network Error", 
                        "Failed to connect to server. Please check your internet connection and try again: " + ex.getMessage());
                });
                return null;
            });
    }

    /**
     * Load grades when switching to the e-grade tab
     */
    private void loadGrades() {
        if (studentData == null) {
            System.out.println("Cannot load grades: student data is null");
            return;
        }
        
        // Show message if not enrolled
        if (!studentData.isEnrolled()) {
            showAlert(Alert.AlertType.INFORMATION, "Not Enrolled", 
                "You are not currently enrolled. No grades to display.");
            return;
        }
        
        // Set school year for grades view
        if (gradesSchoolYearLabel != null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            int currentYear = now.getYear();
            gradesSchoolYearLabel.setText("School Year " + currentYear + "-" + (currentYear + 1));
        } else {
            System.err.println("WARNING: gradesSchoolYearLabel is null");
        }
        
        // Initialize E-Grade table columns if needed
        initializeGradesTableColumns();
        
        // Clear existing grades data
        ObservableList<EnrolledSubjectModel> gradesData = FXCollections.observableArrayList();
        
        // Load student's current academic term
        StudentModel.AcademicTerm currentTerm = studentData.getCurrentAcademicTerm();
        
        if (currentTerm != null && currentTerm.getSubjects() != null && !currentTerm.getSubjects().isEmpty()) {
            System.out.println("Loading grades from academic history. Found " + 
                currentTerm.getSubjects().size() + " subjects.");
            
            // Convert academic history subjects to EnrolledSubjectModel
            for (StudentModel.SubjectGrade subjectGrade : currentTerm.getSubjects()) {
                gradesData.add(new EnrolledSubjectModel(
                    subjectGrade.getEdpCode(),
                    subjectGrade.getSubjectName(),
                    subjectGrade.getUnits(),
                    "TBA", // Schedule not needed for e-grade view
                    "TBA", // Instructor not needed for e-grade view
                    subjectGrade.getSubjectId(),
                    subjectGrade.getMidtermGradeFormatted(),
                    subjectGrade.getFinalGradeFormatted()
                ));
            }
            
            // Set the table data
            gradesTable.setItems(gradesData);
            
            // Calculate and update GPA
            calculateGPA(gradesData);
            
            // Ensure text visibility in the UI
            ensureGradesTextVisibility();
            
            return;
        }
        
        // If academic history is empty, try loading from API
        String studentId = studentData.getStudentId();
        System.out.println("Loading grades for student ID: " + studentId);
        
        // Use the student study load endpoint which contains grade information
        AuthService.getStudentStudyLoad(studentId)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response != null && response.has("studyLoad") && response.get("studyLoad").isJsonArray()) {
                        JsonArray subjectsArray = response.getAsJsonArray("studyLoad");
                        
                        for (JsonElement element : subjectsArray) {
                            if (element.isJsonObject()) {
                                JsonObject subjectObj = element.getAsJsonObject();
                                
                                SubjectModel subject = new SubjectModel(subjectObj);
                                
                                // Extract grades if available
                                String midtermGrade = "N/A";
                                String finalGrade = "N/A";
                                
                                if (subjectObj.has("midtermGrade") && !subjectObj.get("midtermGrade").isJsonNull()) {
                                    Double midterm = subjectObj.get("midtermGrade").getAsDouble();
                                    midtermGrade = String.format("%.2f", midterm);
                                }
                                
                                if (subjectObj.has("finalGrade") && !subjectObj.get("finalGrade").isJsonNull()) {
                                    Double finals = subjectObj.get("finalGrade").getAsDouble();
                                    finalGrade = String.format("%.2f", finals);
                                }
                                
                                // Create model with grades
                                gradesData.add(new EnrolledSubjectModel(
                                    subject.getEdpCode(),
                                    subject.getSubjectName(),
                                    subject.getUnits(),
                                    "TBA", // Schedule not needed for e-grade view
                                    "TBA", // Instructor not needed for e-grade view
                                    subject.getEdpCode(),
                                    midtermGrade,
                                    finalGrade
                                ));
                            }
                        }
                        
                        // Set the table data
                        gradesTable.setItems(gradesData);
                        
                        // Calculate and update GPA
                        calculateGPA(gradesData);
                        
                        // Ensure text visibility
                        ensureGradesTextVisibility();
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "No Grades Found", 
                            "No grade records found for the current term.");
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not load grades: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Initialize the grades table columns
     */
    private void initializeGradesTableColumns() {
        if (gradeSubjectCodeCol == null || gradeSubjectNameCol == null || 
            gradeSubjectUnitsCol == null || midtermGradeCol == null || finalGradeCol == null) {
            System.err.println("ERROR: E-Grade table columns are not properly initialized");
            return;
        }
        
        // Set up column cell value factories if not already set
        gradeSubjectCodeCol.setCellValueFactory(cellData -> cellData.getValue().subjectCodeProperty());
        gradeSubjectNameCol.setCellValueFactory(cellData -> cellData.getValue().subjectNameProperty());
        gradeSubjectUnitsCol.setCellValueFactory(cellData -> cellData.getValue().unitsProperty().asObject());
        midtermGradeCol.setCellValueFactory(cellData -> cellData.getValue().midtermGradeProperty());
        finalGradeCol.setCellValueFactory(cellData -> cellData.getValue().finalGradeProperty());
        
        // Apply column styling for visibility
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
        gradeSubjectCodeCol.setStyle(columnStyle);
        gradeSubjectNameCol.setStyle(columnStyle);
        gradeSubjectUnitsCol.setStyle(columnStyle);
        
        String gradeStyle = "-fx-alignment: CENTER; -fx-text-fill: #333333;";
        midtermGradeCol.setStyle(gradeStyle);
        finalGradeCol.setStyle(gradeStyle);
        
        // Apply styling to table rows
        if (gradesTable != null) {
            gradesTable.setRowFactory(tv -> {
                TableRow<EnrolledSubjectModel> row = new TableRow<>();
                row.setStyle("-fx-text-fill: #333333;");
                return row;
            });
            
            // Set additional styling to ensure text is visible
            gradesTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
        }
    }
    
    /**
     * Calculate GPA based on loaded grades
     */
    private void calculateGPA(ObservableList<EnrolledSubjectModel> gradesData) {
        if (gradesData.isEmpty() || gpaLabel == null) {
            if (gpaLabel != null) gpaLabel.setText("0.00");
            if (gpaValue != null) gpaValue.setText("0.00");
            return;
        }

        System.out.println("\n=== GPA Calculation Details ===");
        System.out.println("Student: " + (studentData != null ? studentData.getName() : "Unknown"));
        System.out.println("Total subjects: " + gradesData.size());

        double totalGrades = 0;
        int validGrades = 0;

        System.out.println("\nSubject-wise GPA Calculation:");
        System.out.println("----------------------------------------");
        System.out.printf("%-30s %-8s %-8s%n", 
            "Subject", "Grade", "Running Total");
        System.out.println("----------------------------------------");

        for (EnrolledSubjectModel subject : gradesData) {
            String finalGradeStr = subject.getFinalGrade();
            
            if (finalGradeStr != null && !finalGradeStr.equals("N/A")) {
                try {
                    String cleanGrade = finalGradeStr.trim().replaceAll("[^0-9.]", "");
                    
                    if (!cleanGrade.isEmpty()) {
                        double grade = Double.parseDouble(cleanGrade);
                        totalGrades += grade;
                        validGrades++;
                        
                        System.out.printf("%-30s %-8.2f %-8.2f%n",
                            subject.getSubjectName(),
                            grade,
                            totalGrades);
                    }
                } catch (NumberFormatException e) {
                    System.out.printf("%-30s %-8s %-8s%n",
                        subject.getSubjectName(),
                        "N/A",
                        "N/A");
                }
            } else {
                System.out.printf("%-30s %-8s %-8s%n",
                    subject.getSubjectName(),
                    "N/A",
                    "N/A");
            }
        }

        System.out.println("----------------------------------------");
        double gpa = (validGrades > 0) ? (totalGrades / validGrades) : 0.0;
        System.out.printf("Total Grades: %.2f%n", totalGrades);
        System.out.printf("Number of Subjects: %d%n", validGrades);
        System.out.printf("Final GPA: %.2f%n", gpa);
        System.out.println("========================================\n");
        
        // Format GPA to 2 decimal places
        String formattedGPA = String.format("%.2f", gpa);
        
        // Update both GPA labels
        if (gpaLabel != null) {
            gpaLabel.setText(formattedGPA);
        }
        
        if (gpaValue != null) {
            gpaValue.setText(formattedGPA);
            
            // Update color based on GPA value
            if (gpa >= 1.0 && gpa <= 1.5) {
                gpaValue.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #27ae60;"); // Green for excellent
            } else if (gpa > 1.5 && gpa <= 2.0) {
                gpaValue.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c5364;"); // Blue for good
            } else if (gpa > 2.0 && gpa <= 3.0) {
                gpaValue.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #f39c12;"); // Orange for fair
            } else {
                gpaValue.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;"); // Red for poor
            }
        }
        
        // Update student model with new GPA
        if (studentData != null) {
            studentData.setGPA(gpa);
        }
    }
    
    /**
     * Apply consistent styling to E-Grade UI elements to ensure text visibility
     */
    private void ensureGradesTextVisibility() {
        // Apply styling to labels
        if (gradesSchoolYearLabel != null) {
            gradesSchoolYearLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;");
        }
        
        if (gpaLabel != null) {
            gpaLabel.setStyle("-fx-text-fill: #333333;");
        }
        
        // Apply styling to table
        if (gradesTable != null) {
            // Table styling
            gradesTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
            
            // Column styling
            String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
            if (gradeSubjectCodeCol != null) gradeSubjectCodeCol.setStyle(columnStyle);
            if (gradeSubjectNameCol != null) gradeSubjectNameCol.setStyle(columnStyle);
            if (gradeSubjectUnitsCol != null) gradeSubjectUnitsCol.setStyle(columnStyle);
            
            String gradeStyle = "-fx-alignment: CENTER; -fx-text-fill: #333333;";
            if (midtermGradeCol != null) midtermGradeCol.setStyle(gradeStyle);
            if (finalGradeCol != null) finalGradeCol.setStyle(gradeStyle);
        }
    }
    
    /**
     * Handle print grade report button click
     */
    @FXML
    public void handlePrintGradeReport() {
        showAlert(Alert.AlertType.INFORMATION, "Print Function", 
            "Print functionality will be implemented in a future update.");
    }
    
    /**
     * Handle export grade report button click
     */
    @FXML
    public void handleExportGradeReport() {
        showAlert(Alert.AlertType.INFORMATION, "Export Function", 
            "Export to PDF functionality will be implemented in a future update.");
    }

    /**
     * Handle refresh grades button click
     */
    @FXML
    public void handleRefreshGrades() {
        if (studentData == null || studentData.getStudentId() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "No student data available to refresh grades.");
            return;
        }
        
        // Show loading indicator
        showAlert(Alert.AlertType.INFORMATION, "Refreshing Data", "Loading your latest grade information...");
        
        // Call the method to refresh the grades
        loadGrades();
    }
}