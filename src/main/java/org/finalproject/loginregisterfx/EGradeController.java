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
    private static final String COLUMN_STYLE = "-fx-alignment: CENTER-LEFT; -fx-text-fill: #333333;";
    private static final String GRADE_COLUMN_STYLE = "-fx-alignment: CENTER; -fx-text-fill: #333333;";
    private static final String TABLE_STYLE = "-fx-text-fill: #333333; -fx-control-inner-background: white;";
    
    @FXML private TableView<EnrolledSubjectModel> gradesTable;
    @FXML private TableColumn<EnrolledSubjectModel, String> subjectCodeCol;
    @FXML private TableColumn<EnrolledSubjectModel, String> subjectNameCol;
    @FXML private TableColumn<EnrolledSubjectModel, Integer> subjectUnitsCol;
    @FXML private TableColumn<EnrolledSubjectModel, String> midtermGradeCol;
    @FXML private TableColumn<EnrolledSubjectModel, String> finalGradeCol;
    @FXML private Label schoolYearLabel;
    @FXML private Label gpaLabel;
    @FXML private Button printGradeReportBtn;
    @FXML private Button exportGradeReportBtn;
    @FXML private Button refreshGradesBtn;
    
    private ObservableList<EnrolledSubjectModel> gradesData = FXCollections.observableArrayList();
    private StudentModel student;

    @FXML
    public void initialize() {
        initializeTableColumns();
        setCurrentSchoolYear();
        initializeStudentData();
    }
    
    private void setCurrentSchoolYear() {
        int currentYear = java.time.LocalDate.now().getYear();
        schoolYearLabel.setText("School Year " + currentYear + "-" + (currentYear + 1));
    }
    
    private void initializeStudentData() {
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null) {
            this.student = sessionStudent;
            loadGrades();
        }
    }
    
    public void setStudentData(StudentModel student) {
        this.student = student;
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null && sessionStudent.getStudentId().equals(student.getStudentId())) {
            this.student = sessionStudent;
        }
        loadGrades();
    }
    
    private void initializeTableColumns() {
        subjectCodeCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectUnitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        midtermGradeCol.setCellValueFactory(new PropertyValueFactory<>("midtermGrade"));
        finalGradeCol.setCellValueFactory(new PropertyValueFactory<>("finalGrade"));
        
        applyColumnStyles();
        gradesTable.setItems(gradesData);
    }
    
    private void applyColumnStyles() {
        subjectCodeCol.setStyle(COLUMN_STYLE);
        subjectNameCol.setStyle(COLUMN_STYLE);
        subjectUnitsCol.setStyle(COLUMN_STYLE);
        midtermGradeCol.setStyle(GRADE_COLUMN_STYLE);
        finalGradeCol.setStyle(GRADE_COLUMN_STYLE);
        
        gradesTable.setRowFactory(tv -> {
            TableRow<EnrolledSubjectModel> row = new TableRow<>();
            row.setStyle("-fx-text-fill: #333333;");
            return row;
        });
        
        gradesTable.setStyle(TABLE_STYLE);
    }

    public void loadGrades() {
        if (student == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No student data available");
            return;
        }
        
        gradesData.clear();
        StudentModel currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent != null && currentStudent.getStudentId().equals(student.getStudentId())) {
            student = currentStudent;
        }
        
        StudentModel.AcademicTerm currentTerm = student.getCurrentAcademicTerm();
        if (currentTerm != null && currentTerm.getSubjects() != null && !currentTerm.getSubjects().isEmpty()) {
            loadGradesFromAcademicTerm(currentTerm);
        } else {
            loadGradesFromAPI(student.getStudentId());
        }
    }
    
    private void loadGradesFromAcademicTerm(StudentModel.AcademicTerm term) {
        for (StudentModel.SubjectGrade subjectGrade : term.getSubjects()) {
            gradesData.add(new EnrolledSubjectModel(
                subjectGrade.getEdpCode(),
                subjectGrade.getSubjectName(),
                subjectGrade.getUnits(),
                "TBA",
                "TBA",
                subjectGrade.getSubjectId(),
                subjectGrade.getMidtermGradeFormatted(),
                subjectGrade.getFinalGradeFormatted()
            ));
        }
        
        calculateGPA();
        ensureTextVisibility();
    }
    
    private void loadGradesFromAPI(String studentId) {
        AuthService.getStudentStudyLoad(studentId)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (isValidStudyLoadResponse(response)) {
                        processStudyLoadResponse(response);
                    } else {
                        fallbackToEnrolledSubjectsAPI(studentId);
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    fallbackToEnrolledSubjectsAPI(studentId);
                });
                return null;
            });
    }
    
    private boolean isValidStudyLoadResponse(JsonObject response) {
        return response != null && response.has("studyLoad") && response.get("studyLoad").isJsonArray();
    }
    
    private void processStudyLoadResponse(JsonObject response) {
        JsonArray subjectsArray = response.getAsJsonArray("studyLoad");
        for (JsonElement element : subjectsArray) {
            if (element.isJsonObject()) {
                processSubjectElement(element.getAsJsonObject());
            }
        }
        calculateGPA();
        ensureTextVisibility();
    }
    
    private void processSubjectElement(JsonObject subjectObj) {
        SubjectModel subject = new SubjectModel(subjectObj);
        String midtermGrade = extractGrade(subjectObj, "midtermGrade");
        String finalGrade = extractGrade(subjectObj, "finalGrade");
        
        if (student != null && student.getId() != null && !midtermGrade.equals("N/A")) {
            subject.setGrade(student.getId(), 
                Double.parseDouble(midtermGrade),
                !finalGrade.equals("N/A") ? Double.parseDouble(finalGrade) : 0.0);
        }
        
        gradesData.add(new EnrolledSubjectModel(
            subject.getEdpCode(),
            subject.getSubjectName(),
            subject.getUnits(),
            "TBA",
            subject.getTeacherAssigned() != null ? subject.getTeacherAssigned() : "TBA",
            subject.getEdpCode(),
            midtermGrade,
            finalGrade
        ));
        
        student.enrollSubject(subject);
    }
    
    private String extractGrade(JsonObject subjectObj, String gradeField) {
        if (subjectObj.has(gradeField) && !subjectObj.get(gradeField).isJsonNull()) {
            return String.format("%.2f", subjectObj.get(gradeField).getAsDouble());
        }
        return "N/A";
    }
    
    private void fallbackToEnrolledSubjectsAPI(String studentId) {
        EnrollmentService.getEnrolledSubjects(studentId)
            .thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    if (response != null && response.has("subjects") && response.get("subjects").isJsonArray()) {
                        processEnrolledSubjectsResponse(response);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Could not load grades");
                    }
                });
            })
            .exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not load grades: " + ex.getMessage());
                });
                return null;
            });
    }
    
    private void processEnrolledSubjectsResponse(JsonObject response) {
        JsonArray subjectsArray = response.getAsJsonArray("subjects");
        for (JsonElement element : subjectsArray) {
            if (element.isJsonObject()) {
                SubjectModel subject = new SubjectModel(element.getAsJsonObject());
                gradesData.add(new EnrolledSubjectModel(subject));
                student.enrollSubject(subject);
            }
        }
        calculateGPA();
        ensureTextVisibility();
    }
    
    public void calculateGPA() {
        // Get the current student from session
        StudentModel currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent == null) {
            System.out.println("No student data available for GPA calculation");
            gpaLabel.setText("0.00");
            return;
        }

        // Get the formatted GPA from the student model
        String formattedGPA = currentStudent.getFormattedGPA();
        System.out.println("\n=== GPA from Student Model ===");
        System.out.println("Student: " + currentStudent.getName());
        System.out.println("GPA: " + formattedGPA);
        System.out.println("=============================\n");
        
        // Update the GPA label
        gpaLabel.setText(formattedGPA);
    }
    
    public void ensureTextVisibility() {
        if (schoolYearLabel != null) {
            schoolYearLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #333333;");
        }
        if (gpaLabel != null) {
            gpaLabel.setStyle("-fx-text-fill: #333333;");
        }
        applyColumnStyles();
    }
    
    @FXML
    public void handlePrintGradeReport() {
        showAlert(Alert.AlertType.INFORMATION, "Print Function", 
            "Print functionality will be implemented in a future update.");
    }
    
    @FXML
    public void handleExportGradeReport() {
        showAlert(Alert.AlertType.INFORMATION, "Export Function", 
            "Export to PDF functionality will be implemented in a future update.");
    }
    
    @FXML
    public void handleRefreshGrades() {
        if (student == null || student.getStudentId() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No student data available to refresh grades.");
            return;
        }
        
        gradesData.clear();
        gpaLabel.setText("Loading...");
        
        StudentModel sessionStudent = SessionManager.getInstance().getCurrentStudent();
        if (sessionStudent != null && sessionStudent.getStudentId().equals(student.getStudentId())) {
            this.student = sessionStudent;
        }
        
        loadGradesFromAPI(student.getStudentId());
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
