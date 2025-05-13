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
    
    // Observable list for enrolled subjects
    private ObservableList<EnrolledSubjectModel> enrolledSubjectsData = FXCollections.observableArrayList();
    
    private StudentModel student;
    
    /**
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
    }
    
    /**
     * Set student data for this controller
     */
    public void setStudentData(StudentModel student) {
        this.student = student;
        loadEnrolledSubjects();
    }
    
    /**
     * Initialize table columns
     */
    private void initializeTableColumns() {
        subjectCodeCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectUnitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        scheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        instructorCol.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        
        // Set table data
        subjectsTable.setItems(enrolledSubjectsData);
    }
    
    /**
     * Load enrolled subjects from API
     */
    public void loadEnrolledSubjects() {
        if (student == null) {
            return;
        }
        
        // Clear existing data
        enrolledSubjectsData.clear();
        
        // Get enrolled subjects from student model
        List<SubjectModel> enrolledSubjects = student.getEnrolledSubjects();
        
        // If we already have subjects, use them
        if (enrolledSubjects != null && !enrolledSubjects.isEmpty()) {
            // Convert to EnrolledSubjectModel and add to observable list
            for (SubjectModel subject : enrolledSubjects) {
                enrolledSubjectsData.add(new EnrolledSubjectModel(subject));
            }
            updateTotalUnits();
        } else {
            // Otherwise fetch from API
            String studentId = student.getId();
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
     */
    @FXML
    public void handleExportStudyLoad() {
        showAlert(Alert.AlertType.INFORMATION, "Export Function", 
            "Export to PDF functionality will be implemented in a future update.");
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
