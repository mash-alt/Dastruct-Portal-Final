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
 * Controller class for managing the e-grade view and functionality
 */
public class EGradeController {
    
    @FXML
    private TableView<EnrolledSubjectModel> gradesTable;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> subjectCodeCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> subjectNameCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, Integer> subjectUnitsCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> midtermGradeCol;
    
    @FXML
    private TableColumn<EnrolledSubjectModel, String> finalGradeCol;
    
    @FXML
    private Label schoolYearLabel;
    
    @FXML
    private Label gpaLabel;
    
    @FXML
    private Button printGradeReportBtn;
    
    @FXML
    private Button exportGradeReportBtn;
    
    @FXML
    private Button refreshGradesBtn;
    
    // Observable list for enrolled subjects with grades
    private ObservableList<EnrolledSubjectModel> gradesData = FXCollections.observableArrayList();
    
    private StudentModel student;

    /**
     * Initialize the e-grade controller with student data
     */
    @FXML
    public void initialize() {
        // Initialize table columns
        initializeTableColumns();
        
        // Set current school year
        java.time.LocalDate now = java.time.LocalDate.now();
        int currentYear = now.getYear();
        schoolYearLabel.setText("School Year " + currentYear + "-" + (currentYear + 1));
        
        System.out.println("EGradeController initialized");
        
        // Try to get student data from session
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null) {
            System.out.println("Found student data in session, loading grades");
            this.student = sessionStudent;
            loadGrades();
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
        
        loadGrades();
    }
    
    /**
     * Initialize table columns
     */
    private void initializeTableColumns() {
        // Set up column cell value factories
        subjectCodeCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectUnitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        midtermGradeCol.setCellValueFactory(new PropertyValueFactory<>("midtermGrade"));
        finalGradeCol.setCellValueFactory(new PropertyValueFactory<>("finalGrade"));
        
        // Style the columns for better text visibility
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
        subjectCodeCol.setStyle(columnStyle);
        subjectNameCol.setStyle(columnStyle);
        subjectUnitsCol.setStyle(columnStyle);
        
        // Grade columns with center alignment
        String gradeColumnStyle = "-fx-alignment: CENTER; -fx-text-fill: #333333;";
        midtermGradeCol.setStyle(gradeColumnStyle);
        finalGradeCol.setStyle(gradeColumnStyle);
        
        // Apply custom styling for table cells
        gradesTable.setRowFactory(tv -> {
            TableRow<EnrolledSubjectModel> row = new TableRow<>();
            row.setStyle("-fx-text-fill: #333333;");
            return row;
        });
        
        // Set additional styling to ensure text is visible
        gradesTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
        
        // Set table data
        gradesTable.setItems(gradesData);
    }

    /**
     * Load grades data for the student
     */
    public void loadGrades() {
        if (student == null) {
            System.out.println("Cannot load grades: student data is null");
            return;
        }
        
        System.out.println("Loading grades for student ID: " + student.getStudentId());
        
        // Clear existing data
        gradesData.clear();
        
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
            System.out.println("Loading grades from academic history. Found " + 
                currentTerm.getSubjects().size() + " subjects.");
            
            // Convert academic history subjects to EnrolledSubjectModel
            for (StudentModel.SubjectGrade subjectGrade : currentTerm.getSubjects()) {
                // Log subject details before adding
                System.out.println("Processing subject: " + subjectGrade.getSubjectName() + 
                                   " (EDP: " + subjectGrade.getEdpCode() + ", ID: " + subjectGrade.getSubjectId() + ")");
                System.out.println("Grades - Midterm: " + subjectGrade.getMidtermGradeFormatted() + 
                                   ", Final: " + subjectGrade.getFinalGradeFormatted());
                
                // Create EnrolledSubjectModel with full grade information from academic history
                gradesData.add(new EnrolledSubjectModel(
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
            
            calculateGPA();
            // Ensure text visibility after loading data
            ensureTextVisibility();
            System.out.println("Successfully loaded " + gradesData.size() + " subjects with grades from academic history");
            return;
        }
        
        // If no subjects found in student data, attempt to load from API
        if (student != null && student.getStudentId() != null) {
            System.out.println("Attempting to load grades from API for student ID: " + student.getStudentId());
            loadGradesFromAPI(student.getStudentId());
            return; // Exit the method, since loadGradesFromAPI will handle the UI updates
        }
        
        // Only show this message if we have no way to load data
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Grades Found");
            alert.setHeaderText(null);
            alert.setContentText("No grade records found for the current term.");
            alert.showAndWait();
        });
    }
    
    /**
     * Load student's grades from the API
     * 
     * @param studentId The student ID
     */
    private void loadGradesFromAPI(String studentId) {
        // Use the student study load endpoint which contains grade information
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
                                EnrolledSubjectModel subjectWithGrades = new EnrolledSubjectModel(
                                    subject.getEdpCode(),
                                    subject.getSubjectName(),
                                    subject.getUnits(),
                                    "TBA", // Schedule not needed for e-grade view
                                    subject.getTeacherAssigned() != null ? subject.getTeacherAssigned() : "TBA",
                                    subject.getEdpCode(),
                                    midtermGrade,
                                    finalGrade
                                );
                                
                                gradesData.add(subjectWithGrades);
                                student.enrollSubject(subject);
                            }
                        }
                        
                        // Calculate GPA
                        calculateGPA();
                        // Ensure text visibility after loading data
                        ensureTextVisibility();
                        System.out.println("Grades loaded successfully with " + 
                            gradesData.size() + " subjects.");
                        
                        // If we successfully loaded subjects but UI previously showed "no subjects" message
                        if (!gradesData.isEmpty()) {
                            // Update the UI to show the grades table
                            gradesTable.setVisible(true);
                            System.out.println("Updated UI to show grades table with " + gradesData.size() + " subjects");
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
                    System.err.println("Error loading grades: " + ex.getMessage() + 
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
                                gradesData.add(new EnrolledSubjectModel(subject));
                                student.enrollSubject(subject);
                            }
                        }
                        
                        // Calculate GPA
                        calculateGPA();
                        // Ensure text visibility
                        ensureTextVisibility();
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not load grades: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Calculate GPA based on loaded grades
     */
    private void calculateGPA() {
        if (gradesData.isEmpty()) {
            gpaLabel.setText("0.00");
            return;
        }
        
        double totalPoints = 0;
        int totalUnits = 0;
        
        for (EnrolledSubjectModel subject : gradesData) {
            int units = subject.getUnits();
            totalUnits += units;
            
            // Convert final grade string to double for calculation
            try {
                if (!subject.getFinalGrade().equals("N/A")) {
                    double grade = Double.parseDouble(subject.getFinalGrade());
                    // Convert to 4.0 scale
                    double points = convertToGPAScale(grade);
                    totalPoints += (points * units);
                }
            } catch (NumberFormatException e) {
                // Skip this subject if grade is not a valid number
                System.out.println("Could not parse grade for " + subject.getSubjectName() + ": " + subject.getFinalGrade());
            }
        }
        
        double gpa = (totalUnits > 0) ? (totalPoints / totalUnits) : 0.0;
        gpaLabel.setText(String.format("%.2f", gpa));
    }
    
    /**
     * Convert percentage grade to 4.0 scale
     */
    private double convertToGPAScale(double percentageGrade) {
        // Simple conversion - can be adjusted based on institution's grading scale
        if (percentageGrade >= 96) return 4.0;
        if (percentageGrade >= 90) return 3.5;
        if (percentageGrade >= 84) return 3.0;
        if (percentageGrade >= 78) return 2.5;
        if (percentageGrade >= 72) return 2.0;
        if (percentageGrade >= 66) return 1.5;
        if (percentageGrade >= 60) return 1.0;
        return 0.0;
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
        
        if (gpaLabel != null) {
            gpaLabel.setStyle("-fx-text-fill: #333333;");
        }
        
        // Apply styling to table
        if (gradesTable != null) {
            // Table styling
            gradesTable.setStyle("-fx-text-fill: #333333; -fx-control-inner-background: white;");
            
            // Column styling
            String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
            if (subjectCodeCol != null) subjectCodeCol.setStyle(columnStyle);
            if (subjectNameCol != null) subjectNameCol.setStyle(columnStyle);
            if (subjectUnitsCol != null) subjectUnitsCol.setStyle(columnStyle);
            
            String gradeStyle = "-fx-alignment: CENTER; -fx-text-fill: #333333;";
            if (midtermGradeCol != null) midtermGradeCol.setStyle(gradeStyle);
            if (finalGradeCol != null) finalGradeCol.setStyle(gradeStyle);
            
            // Row styling
            gradesTable.setRowFactory(tv -> {
                TableRow<EnrolledSubjectModel> row = new TableRow<>();
                row.setStyle("-fx-text-fill: #333333;");
                return row;
            });
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
        if (student == null || student.getStudentId() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                "No student data available to refresh grades.");
            return;
        }
        
        System.out.println("Refreshing grades for student: " + student.getName() + 
                          " (ID: " + student.getStudentId() + ")");
        
        // Show loading message
        gradesData.clear();
        gpaLabel.setText("Loading...");
        
        // Get the latest student data from session
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null && sessionStudent.getStudentId().equals(student.getStudentId())) {
            System.out.println("Using updated student data from session");
            this.student = sessionStudent;
        } else {
            System.out.println("No updated student data found in session, using existing data");
        }
        
        // Make an API call to get the latest grade data
        System.out.println("Making API call to get latest grade data");
        loadGradesFromAPI(student.getStudentId());
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
}
