package org.finalproject.loginregisterfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import org.finalproject.loginregisterfx.models.StudentModel;
import org.finalproject.loginregisterfx.models.EnrolledStudentsBySubjectModel;

/**
 * Controller for the MyStudents.fxml view that displays students enrolled in a subject.
 */
public class StudentsViewController {

    @FXML private TextField searchStudentsField;
    @FXML private Label subjectInfoLabel;
    @FXML private TableView<StudentModel> studentsTableView;
    @FXML private TableColumn<StudentModel, String> idNumberColumn;
    @FXML private TableColumn<StudentModel, String> studentNameColumn;
    @FXML private TableColumn<StudentModel, String> courseColumn;
    @FXML private TableColumn<StudentModel, String> sectionColumn;

    private ObservableList<StudentModel> allStudents = FXCollections.observableArrayList();
    private ObservableList<StudentModel> filteredStudents = FXCollections.observableArrayList();
    
    private String subjectCode;
    private String subjectName;    @FXML
    private void initialize() {
        System.out.println("Initializing StudentsViewController...");
        
        // Set up table columns
        setupTableColumns();
        
        // Set up search functionality
        if (searchStudentsField != null) {
            searchStudentsField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterStudents(newValue);
            });
            
            // Add action handler for Enter key
            searchStudentsField.setOnAction(event -> {
                System.out.println("Search triggered with text: " + searchStudentsField.getText());
                filterStudents(searchStudentsField.getText());
            });
        }
        
        // Add a context menu for refresh functionality
        setupContextMenu();
    }
    
    /**
     * Set up context menu for the table
     */
    private void setupContextMenu() {
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        
        javafx.scene.control.MenuItem refreshItem = new javafx.scene.control.MenuItem("Refresh Student List");
        refreshItem.setOnAction(event -> {
            if (subjectCode != null && !subjectCode.isEmpty()) {
                System.out.println("Refreshing student list for subject: " + subjectCode);
                fetchStudentsBySubjectEdp(subjectCode);
            }
        });
        
        contextMenu.getItems().add(refreshItem);
        studentsTableView.setContextMenu(contextMenu);
    }
    
    /**
     * Set up the table columns with appropriate cell factories
     */
    private void setupTableColumns() {
        if (studentsTableView == null) {
            System.err.println("Error: studentsTableView is null");
            return;
        }
        
        if (idNumberColumn != null) {
            idNumberColumn.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        } else {
            System.err.println("Error: idNumberColumn is null");
        }
        
        if (studentNameColumn != null) {
            studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        } else {
            System.err.println("Error: studentNameColumn is null");
        }
        
        if (courseColumn != null) {
            courseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
        } else {
            System.err.println("Error: courseColumn is null");
        }
          if (sectionColumn != null) {
            sectionColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                // Use only the section directly from backend 
                // since it's now providing the complete section information
                String section = student.getSection();
                return javafx.beans.binding.Bindings.createStringBinding(() -> section);
            });
        } else {
            System.err.println("Error: sectionColumn is null");
        }
        
        // Set up double-click handler for showing Add Grade dialog
        studentsTableView.setRowFactory(tv -> {
            TableRow<StudentModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    StudentModel student = row.getItem();
                    showAddGradeDialog(student);
                }
            });
            return row;
        });
    }
      /**
     * Set the subject data and load the students
     * 
     * @param subject The subject model containing enrolled students
     */    public void setSubjectData(EnrolledStudentsBySubjectModel subject) {
        if (subject == null) {
            System.err.println("Error: subject data is null");
            return;
        }
        
        this.subjectCode = subject.getSubjectCode();
        this.subjectName = subject.getSubjectName();
        
        System.out.println("Loading students for subject: " + subjectCode + " - " + subjectName);
        
        // Update the subject info label
        Platform.runLater(() -> {
            if (subjectInfoLabel != null) {
                subjectInfoLabel.setText("Subject: " + subjectCode + " - " + subjectName);
            }
            
            // Show loading indicator
            studentsTableView.setPlaceholder(new javafx.scene.control.ProgressIndicator());
        });
        
        // Fetch students from the API using the specific endpoint for subject EDP code
        fetchStudentsBySubjectEdp(subjectCode);
    }
    
    /**
     * Fetch students enrolled in a specific subject by EDP code from the API
     * 
     * @param edpCode The EDP code of the subject
     */    private void fetchStudentsBySubjectEdp(String edpCode) {
        System.out.println("Fetching students for subject EDP: " + edpCode);
        
        // Use the enrolled-students endpoint with edpCode query parameter
        String endpoint = "/teacher/enrolled-students?edpCode=" + edpCode;
        
        org.finalproject.loginregisterfx.Service.AuthService.makeGetRequest(endpoint)
            .thenAccept(response -> {
                System.out.println("Received students response for subject " + edpCode + ": " + response);
                
                Platform.runLater(() -> {
                    try {
                        // Clear existing data
                        allStudents.clear();
                          if (response.has("enrolledStudents") && response.get("enrolledStudents").isJsonArray()) {
                            com.google.gson.JsonArray studentsArray = response.getAsJsonArray("enrolledStudents");
                            System.out.println("Found " + studentsArray.size() + " students enrolled in subject " + edpCode);
                            
                            for (int i = 0; i < studentsArray.size(); i++) {
                                try {
                                    com.google.gson.JsonObject studentObj = studentsArray.get(i).getAsJsonObject();
                                    // Create StudentModel from the response
                                    org.finalproject.loginregisterfx.models.StudentModel student = 
                                        createStudentFromResponse(studentObj, edpCode);
                                    allStudents.add(student);
                                    
                                    System.out.println("Added student: " + student.getName() + 
                                                      " (" + student.getIdNumber() + ")");
                                } catch (Exception e) {
                                    System.err.println("Error processing student data: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                              // Update the filtered list and display
                            filteredStudents.clear();
                            filteredStudents.addAll(allStudents);
                            studentsTableView.setItems(filteredStudents);
                            
                            int studentCount = allStudents.size();
                            System.out.println("Loaded " + studentCount + " students for subject " + edpCode);
                            
                            // Update subject info label with count
                            if (subjectInfoLabel != null) {
                                subjectInfoLabel.setText("Subject: " + subjectCode + " - " + subjectName + 
                                                       " (" + studentCount + " student" + (studentCount != 1 ? "s" : "") + ")");
                            }
                            
                            // Update placeholder if no students found
                            if (allStudents.isEmpty()) {
                                studentsTableView.setPlaceholder(new javafx.scene.control.Label("No students enrolled in this subject"));
                            }
                            
                        } else if (response.has("message")) {
                            // Response contains a message but no students
                            String message = response.get("message").getAsString();
                            System.out.println("API message: " + message);
                            studentsTableView.setPlaceholder(new javafx.scene.control.Label(message));
                        } else {
                            // No students or empty response
                            System.out.println("No students found for subject " + edpCode);
                            studentsTableView.setPlaceholder(new javafx.scene.control.Label("No students enrolled in this subject"));
                        }                    } catch (Exception e) {
                        System.err.println("Error processing students response: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Create a more detailed error label
                        Label errorLabel = new Label("Error processing student data: " + e.getMessage());
                        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                        studentsTableView.setPlaceholder(errorLabel);
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("API error fetching students for subject " + edpCode + ": " + ex.getMessage());
                ex.printStackTrace();
                
                Platform.runLater(() -> {
                    // Create a visually appealing error message
                    VBox errorBox = new VBox();
                    errorBox.setAlignment(javafx.geometry.Pos.CENTER);
                    errorBox.setSpacing(10);
                    
                    Label iconLabel = new Label("❌");
                    iconLabel.setStyle("-fx-font-size: 30px;");
                    
                    Label errorTitleLabel = new Label("Failed to load students");
                    errorTitleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                    
                    Label errorMsgLabel = new Label(ex.getMessage());
                    errorMsgLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
                    
                    errorBox.getChildren().addAll(iconLabel, errorTitleLabel, errorMsgLabel);
                    studentsTableView.setPlaceholder(errorBox);
                });
                
                return null;
            });
    }
    
    /**
     * Filter students based on search text
     */    private void filterStudents(String searchText) {
        filteredStudents.clear();
        
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredStudents.addAll(allStudents);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            
            for (StudentModel student : allStudents) {
                if (student.getName().toLowerCase().contains(lowerCaseSearch) ||
                    student.getIdNumber().toLowerCase().contains(lowerCaseSearch) ||
                    student.getCourse().toLowerCase().contains(lowerCaseSearch)) {
                    filteredStudents.add(student);
                }
            }
        }
        
        System.out.println("Filtered to " + filteredStudents.size() + " students");
    }
    
    /**
     * Helper method to create a StudentModel from the new API response format
     * 
     * @param studentObj JsonObject containing student data from the API
     * @param edpCode EDP code of the subject to filter subject-specific information
     * @return StudentModel with the data from the response
     */    private StudentModel createStudentFromResponse(com.google.gson.JsonObject studentObj, String edpCode) {
        // Create a new StudentModel
        StudentModel student = new StudentModel();
        
        // Set basic student information
        if (studentObj.has("_id")) {
            student.setId(studentObj.get("_id").getAsString());
        }
        
        if (studentObj.has("name")) {
            student.setName(studentObj.get("name").getAsString());
        }
        
        if (studentObj.has("email")) {
            student.setEmail(studentObj.get("email").getAsString());
        }
        
        if (studentObj.has("course")) {
            student.setCourse(studentObj.get("course").getAsString());
        }
        
        if (studentObj.has("department")) {
            student.setDepartment(studentObj.get("department").getAsString());
        }
          // Extract section information
        if (studentObj.has("section")) {
            student.setSection(studentObj.get("section").getAsString());
            System.out.println("Set section to: " + student.getSection());
        } else {
            // Default section or fallback to creating one from yearLevel if available
            student.setSection("N/A");
        }
        
        // Extract year level information
        if (studentObj.has("yearLevel")) {
            try {
                if (studentObj.get("yearLevel").isJsonPrimitive()) {
                    if (studentObj.get("yearLevel").getAsJsonPrimitive().isNumber()) {
                        student.setYearLevel(studentObj.get("yearLevel").getAsInt());
                    } else {
                        // Try to parse string to int
                        try {
                            student.setYearLevel(Integer.parseInt(studentObj.get("yearLevel").getAsString()));
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse yearLevel as integer: " + e.getMessage());
                            student.setYearLevel(1);
                        }
                    }
                } else {
                    student.setYearLevel(1);
                }
                System.out.println("Set yearLevel to: " + student.getYearLevel());
            } catch (Exception e) {
                System.err.println("Error handling yearLevel: " + e.getMessage());
                student.setYearLevel(1);
            }
        }
        
        // Extract student ID number from various sources
        if (studentObj.has("idNumber")) {
            student.setIdNumber(studentObj.get("idNumber").getAsString());
        } else {
            // Fallback to extracting from email if idNumber not available
            String email = studentObj.has("email") ? studentObj.get("email").getAsString() : "";
            if (email.contains("@")) {
                String[] parts = email.split("@");
                if (parts.length > 0) {
                    student.setIdNumber(parts[0]);
                }
            }
        }
        
        // Process subjects array to find grades for the current subject
        if (studentObj.has("subjects") && studentObj.get("subjects").isJsonArray()) {
            com.google.gson.JsonArray subjectsArray = studentObj.getAsJsonArray("subjects");
            
            // Find the subject that matches the edpCode
            for (int i = 0; i < subjectsArray.size(); i++) {
                com.google.gson.JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                
                if (subjectObj.has("edpCode") && subjectObj.get("edpCode").getAsString().equals(edpCode)) {
                    // We found the matching subject, extract grade info if available
                    if (subjectObj.has("grades") && !subjectObj.get("grades").isJsonNull()) {
                        com.google.gson.JsonObject gradesObj = subjectObj.getAsJsonObject("grades");
                        
                        // You can extract and set grade information here if needed
                        // This depends on how your StudentModel is set up to handle grades
                    }
                    break;
                }
            }
        }
        
        return student;
    }
      /**
     * Show the Add Grade dialog for the selected student
     * 
     * @param student The student for whom to add grades
     */
    private void showAddGradeDialog(StudentModel student) {
        try {
            // Load the AddGrade.fxml file
            System.out.println("Showing Add Grade dialog for student: " + student.getName());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddGrade.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add/Edit Grade");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            
            // Get the window of the table view to set as owner
            Window owner = studentsTableView.getScene().getWindow();
            dialogStage.initOwner(owner);

            // Create scene and set it to the stage
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Get the controller and pass data
            AddGradeController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(student, subjectCode, subjectName);

            // Show the dialog and wait until user closes it
            dialogStage.showAndWait();
            
            // After dialog is closed, refresh the student list
            if (subjectCode != null && !subjectCode.isEmpty()) {
                fetchStudentsBySubjectEdp(subjectCode);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Add Grade dialog: " + e.getMessage());
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open the grade dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
}