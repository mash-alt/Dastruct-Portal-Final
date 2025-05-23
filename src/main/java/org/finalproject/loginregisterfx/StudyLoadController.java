package org.finalproject.loginregisterfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.models.EnrolledSubjectModel;
import org.finalproject.loginregisterfx.models.StudentModel;
import org.finalproject.loginregisterfx.models.SubjectModel;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.Service.EnrollmentService;
import org.finalproject.loginregisterfx.Service.SessionManager;

import java.util.List;

/**
 * Controller class for managing the study load view and functionality
 */
public class StudyLoadController {
    
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
      @FXML
    private Button printStudyLoadBtn;
    
    @FXML
    private Button exportStudyLoadBtn;
    
    @FXML
    private Button refreshStudyLoadBtn;
    
    // Observable list for enrolled subjects
    private ObservableList<EnrolledSubjectModel> enrolledSubjectsData = FXCollections.observableArrayList();
    
    private StudentModel student;    /**
     * Initialize the study load controller with student data
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        initializeTableColumns();
        
        // Set current school year
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        schoolYearLabel.setText("School Year " + currentYear + "-" + (currentYear + 1));
        
        System.out.println("StudyLoadController initialized");
        
        // Try to get student data from session
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null) {
            System.out.println("Found student data in session, loading study load");
            this.student = sessionStudent;
            loadEnrolledSubjects();
        } else {
            System.out.println("No student data found in session yet");
        }
    }
      /**
     * Set student data for this controller
     */
    public void setStudentData(StudentModel student) {
        this.student = student;
        
        // Try to get the most recent data from session if available
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null && sessionStudent.getStudentId().equals(student.getStudentId())) {
            System.out.println("Using most up-to-date student data from session");
            this.student = sessionStudent;
        }
        
        loadEnrolledSubjects();
    }
      /**
     * Initialize table columns
     */    private void initializeTableColumns() {
        // Set up column cell value factories
        subjectCodeCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectUnitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        scheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        
        // Style the columns for better text visibility
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
        subjectCodeCol.setStyle(columnStyle);
        subjectNameCol.setStyle(columnStyle);
        subjectUnitsCol.setStyle(columnStyle);
        scheduleCol.setStyle(columnStyle);
        instructorCol.setStyle(columnStyle);
        
        // Add midterm and final grade columns
        TableColumn<EnrolledSubjectModel, String> midtermGradeCol = new TableColumn<>("Midterm");
        midtermGradeCol.setCellValueFactory(new PropertyValueFactory<>("midtermGrade"));
        midtermGradeCol.setPrefWidth(80);
        midtermGradeCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #333333;");
        
        TableColumn<EnrolledSubjectModel, String> finalGradeCol = new TableColumn<>("Final");
        finalGradeCol.setCellValueFactory(new PropertyValueFactory<>("finalGrade"));
        finalGradeCol.setPrefWidth(80);
        finalGradeCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #333333;");
        
        // Add the new columns to the table
        subjectsTable.getColumns().addAll(midtermGradeCol, finalGradeCol);
          // Apply custom styling for table cells
        subjectsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<EnrolledSubjectModel> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-text-fill: #333333;");
            return row;
        });
        
        // Set additional styling to ensure text is visible
        subjectsTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
        
        // Set table data
        subjectsTable.setItems(enrolledSubjectsData);
    }    /**
     * Load enrolled subjects from student data
     * Prioritizes academic history information which comes from login response
     */
    public void loadEnrolledSubjects() {
        if (student == null) {
            System.out.println("Cannot load study load: student data is null");
            return;
        }
        
        System.out.println("Loading enrolled subjects for student ID: " + student.getStudentId());
        
        // Clear existing data
        enrolledSubjectsData.clear();
        
        // First try to get the most recent data from SessionManager
        StudentModel currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent != null && currentStudent.getStudentId().equals(student.getStudentId())) {
            System.out.println("Using latest student data from session manager");
            student = currentStudent;
        }
          // Check for academic history first (this will be populated from the login response)
        StudentModel.AcademicTerm currentTerm = student.getCurrentAcademicTerm();
        
        // Add detailed debug info about the academic term
        if (currentTerm == null) {
            System.out.println("WARNING: No current academic term found for student");
        } else {
            System.out.println("Found academic term: " + currentTerm.getAcademicYear() + " " + currentTerm.getSemester());
            if (currentTerm.getSubjects() == null) {
                System.out.println("WARNING: Academic term has null subjects list");
            } else if (currentTerm.getSubjects().isEmpty()) {
                System.out.println("WARNING: Academic term has empty subjects list");
            }
        }
        
        if (currentTerm != null && currentTerm.getSubjects() != null && !currentTerm.getSubjects().isEmpty()) {
            System.out.println("Loading study load from academic history. Found " + 
                currentTerm.getSubjects().size() + " subjects.");
            
            // Convert academic history subjects to EnrolledSubjectModel
            for (StudentModel.SubjectGrade subjectGrade : currentTerm.getSubjects()) {
                // Log subject details before adding
                System.out.println("Processing subject: " + subjectGrade.getSubjectName() + 
                                   " (EDP: " + subjectGrade.getEdpCode() + ", ID: " + subjectGrade.getSubjectId() + ")");
                System.out.println("Grades - Midterm: " + subjectGrade.getMidtermGradeFormatted() + 
                                   ", Final: " + subjectGrade.getFinalGradeFormatted());
                
                // Create EnrolledSubjectModel with full grade information from academic history
                enrolledSubjectsData.add(new EnrolledSubjectModel(
                    subjectGrade.getEdpCode(),
                    subjectGrade.getSubjectName(),
                    subjectGrade.getUnits(),
                    "TBA", // Schedule not available in academic history
                    "TBA", // Instructor not available in academic history
                    subjectGrade.getSubjectId(),
                    subjectGrade.getMidtermGradeFormatted(),
                    subjectGrade.getFinalGradeFormatted()
                ));
            }
              updateTotalUnits();
            // Ensure text visibility after loading data
            ensureTextVisibility();
            System.out.println("Successfully loaded " + enrolledSubjectsData.size() + " subjects from academic history");
            return;
        }
        
        // If academic history is empty or not available, check enrolledSubjects list
        // This should never happen if login response includes academic history, but provides a fallback
        System.out.println("No academic history found, checking enrolled subjects list...");
        List<SubjectModel> enrolledSubjects = student.getEnrolledSubjects();
        
        if (enrolledSubjects != null && !enrolledSubjects.isEmpty()) {
            System.out.println("Found " + enrolledSubjects.size() + " enrolled subjects in student model");
            // Convert to EnrolledSubjectModel and add to observable list
            for (SubjectModel subject : enrolledSubjects) {
                enrolledSubjectsData.add(new EnrolledSubjectModel(subject));
            }
            updateTotalUnits();        } else {
            // If no subjects found in student model either, show a message
            System.out.println("No enrolled subjects found in student data");                // Attempt to load from API before showing the message
            if (student != null && student.getStudentId() != null) {
                System.out.println("Attempting to load study load from API for student ID: " + student.getStudentId());
                loadStudyLoadFromAPI(student.getStudentId());
                return; // Exit the method, since loadStudyLoadFromAPI will handle the UI updates
            }
            
            // Only show this message if we have no way to load data
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Subjects Found");
                alert.setHeaderText(null);
                alert.setContentText("No enrolled subjects found for the current term.");
                alert.showAndWait();
            });
        }
    }
    
    /**
     * Load student's study load from the API
     * 
     * @param studentId The student ID
     */    private void loadStudyLoadFromAPI(String studentId) {
        // Use the new dedicated study load endpoint
        AuthService.getStudentStudyLoad(studentId)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (response != null && response.has("studyLoad") && response.get("studyLoad").isJsonArray()) {
                        JsonArray subjectsArray = response.getAsJsonArray("studyLoad");
                        
                        // Convert JSON subjects to models
                        for (JsonElement element : subjectsArray) {
                            if (element.isJsonObject()) {
                                JsonObject subjectObj = element.getAsJsonObject();
                                
                                // Create basic subject model
                                SubjectModel subject = new SubjectModel(subjectObj);
                                
                                // Extract grades if available in the API response
                                String midtermGrade = "N/A";
                                String finalGrade = "N/A";
                                  // Extract grades more reliably from the API response
                                if (subjectObj.has("midtermGrade") && !subjectObj.get("midtermGrade").isJsonNull()) {
                                    Double midterm = subjectObj.get("midtermGrade").getAsDouble();
                                    midtermGrade = String.format("%.2f", midterm);
                                    
                                    // Set grade in the subject model too
                                    if (student != null && student.getId() != null) {
                                        subject.setGrade(student.getId(), midterm, 
                                            subjectObj.has("finalGrade") && !subjectObj.get("finalGrade").isJsonNull() ? 
                                            subjectObj.get("finalGrade").getAsDouble() : 0.0);
                                    }
                                }
                                
                                if (subjectObj.has("finalGrade") && !subjectObj.get("finalGrade").isJsonNull()) {
                                    Double finals = subjectObj.get("finalGrade").getAsDouble();
                                    finalGrade = String.format("%.2f", finals);
                                }
                                  // Create model with grades if available
                                EnrolledSubjectModel enrolledSubject = new EnrolledSubjectModel(
                                    subject.getEdpCode(),
                                    subject.getSubjectName(),
                                    subject.getUnits(),
                                    "TBA", // Schedule often not provided
                                    subject.getTeacherAssigned() != null ? subject.getTeacherAssigned() : "TBA",
                                    subject.getEdpCode(),
                                    midtermGrade,
                                    finalGrade
                                );
                                
                                enrolledSubjectsData.add(enrolledSubject);
                                student.enrollSubject(subject);
                            }
                        }                        // Update total units
                        updateTotalUnits();
                        // Ensure text visibility after loading data
                        ensureTextVisibility();
                        System.out.println("Study load loaded successfully with " + 
                            enrolledSubjectsData.size() + " subjects.");
                        
                        // If we successfully loaded subjects but UI previously showed "no subjects" message
                        if (!enrolledSubjectsData.isEmpty()) {
                            // Update the UI to show the subjects table
                            subjectsTable.setVisible(true);
                            System.out.println("Updated UI to show subjects table with " + enrolledSubjectsData.size() + " subjects");
                        }
                    } else {
                        // If the new endpoint fails, fall back to the old one
                        System.out.println("New study load endpoint response not in expected format, falling back to old endpoint");
                        fallbackToEnrolledSubjectsAPI(studentId);
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("Error loading study load: " + ex.getMessage() + 
                        ". Falling back to old endpoint.");
                    fallbackToEnrolledSubjectsAPI(studentId);
                });
                return null;
            });
    }
    
    /**
     * Fallback to using the old enrolled subjects API
     * 
     * @param studentId The student ID
     */
    private void fallbackToEnrolledSubjectsAPI(String studentId) {
        EnrollmentService.getEnrolledSubjects(studentId)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (response != null && response.has("subjects") && response.get("subjects").isJsonArray()) {
                        JsonArray subjectsArray = response.getAsJsonArray("subjects");
                        
                        // Convert JSON subjects to models
                        for (JsonElement element : subjectsArray) {
                            if (element.isJsonObject()) {
                                JsonObject subjectObj = element.getAsJsonObject();
                                SubjectModel subject = new SubjectModel(subjectObj);
                                enrolledSubjectsData.add(new EnrolledSubjectModel(subject));
                                student.enrollSubject(subject);
                            }
                        }
                        
                        // Update total units
                        updateTotalUnits();
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
     * Update the total units label
     */
    private void updateTotalUnits() {
        int total = 0;
        for (EnrolledSubjectModel subject : enrolledSubjectsData) {
            total += subject.getUnits();
        }
        totalUnitsLabel.setText(String.valueOf(total));
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
     */    @FXML
    public void handleExportStudyLoad() {
        showAlert(Alert.AlertType.INFORMATION, "Export Function", 
            "Export to PDF functionality will be implemented in a future update.");
    }    /**
     * Handle refresh study load button click
     */    @FXML
    public void handleRefreshStudyLoad() {
        if (student == null || student.getStudentId() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "No student data available to refresh study load.");
            return;
        }
        
        System.out.println("Refreshing study load for student: " + student.getName() + 
                          " (ID: " + student.getStudentId() + ")");
        
        // Show loading message
        enrolledSubjectsData.clear();
        totalUnitsLabel.setText("Loading...");
        
        // Get the latest student data from session
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null && sessionStudent.getStudentId().equals(student.getStudentId())) {
            System.out.println("Using updated student data from session");
            this.student = sessionStudent;
        } else {
            System.out.println("No updated student data found in session, using existing data");
        }
        
        // Make an API call to get the latest study load data
        System.out.println("Making API call to get latest study load data");
        loadStudyLoadFromAPI(student.getStudentId());
        
        // Note that the rest of the processing will happen in the API callback
        // We don't need to show alerts here as they will be handled in the callback
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Apply consistent styling to all UI elements to ensure text visibility
     * Called after loading data to make sure text is visible against white background
     */
    private void ensureTextVisibility() {
        // Apply styling to labels
        if (schoolYearLabel != null) {
            schoolYearLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;");
        }
        
        if (totalUnitsLabel != null) {
            totalUnitsLabel.setStyle("-fx-text-fill: #333333;");
        }
        
        // Apply styling to table
        if (subjectsTable != null) {
            // Table styling
            subjectsTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
            
            // Column styling
            String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
            if (subjectCodeCol != null) subjectCodeCol.setStyle(columnStyle);
            if (subjectNameCol != null) subjectNameCol.setStyle(columnStyle);
            if (subjectUnitsCol != null) subjectUnitsCol.setStyle(columnStyle);
            if (scheduleCol != null) scheduleCol.setStyle(columnStyle);
            if (instructorCol != null) instructorCol.setStyle(columnStyle);
            
            // Row styling
            subjectsTable.setRowFactory(tv -> {
                javafx.scene.control.TableRow<EnrolledSubjectModel> row = new javafx.scene.control.TableRow<>();
                row.setStyle("-fx-text-fill: #333333;");
                return row;
            });
        }
    }
}
