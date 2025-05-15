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
    @FXML private TableView<StudentModel> studentsTableView;    @FXML private TableColumn<StudentModel, String> idNumberColumn;
    @FXML private TableColumn<StudentModel, String> studentNameColumn;
    @FXML private TableColumn<StudentModel, String> courseColumn;
    @FXML private TableColumn<StudentModel, String> sectionColumn;
    @FXML private TableColumn<StudentModel, String> midtermGradeColumn;
    @FXML private TableColumn<StudentModel, String> finalGradeColumn;private ObservableList<StudentModel> allStudents = FXCollections.observableArrayList();
    private ObservableList<StudentModel> filteredStudents = FXCollections.observableArrayList();
    
    private String subjectCode;
    private String subjectName;
    private String currentSubjectId; // To track the subject ID for grade lookup@FXML
    public void initialize() {
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
        }          if (sectionColumn != null) {
            sectionColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                // Use only the section directly from backend 
                // since it's now providing the complete section information
                String section = student.getSection();
                return javafx.beans.binding.Bindings.createStringBinding(() -> section);
            });        } else {
            System.err.println("Error: sectionColumn is null");
        }
        
        // Set up midterm grade column with a more direct property binding
        if (midtermGradeColumn != null) {
            System.out.println("Setting up midtermGradeColumn with ID: " + midtermGradeColumn.getId());
            
            // Use a simpler but more direct approach for cell value factory
            midtermGradeColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                if (student == null) {
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                }
                
                // Get the midterm grade directly from the student model
                String grade = student.getMidtermGrade();
                
                // Debug output to help trace grade display issues
                System.out.println("Cell value factory for " + student.getName() + " midterm grade: " + grade);
                
                // Create a new property that will trigger UI updates when changed
                javafx.beans.property.SimpleStringProperty prop = new javafx.beans.property.SimpleStringProperty();
                prop.set(grade != null && !grade.isEmpty() ? grade : "N/A");
                return prop;
            });
              // Add custom cell factory to style the grade cells            
              midtermGradeColumn.setCellFactory(column -> {
                return new javafx.scene.control.TableCell<StudentModel, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        // Get the actual student if possible
                        StudentModel student = null;
                        if (getTableRow() != null) {
                            student = (StudentModel) getTableRow().getItem();
                        }
                        
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            // Try to get grade directly from the student for more accurate display
                            String displayGrade = item;
                            if (student != null && student.getMidtermGrade() != null) {
                                displayGrade = student.getMidtermGrade();
                                System.out.println("Cell displaying midterm grade for " + student.getName() + ": " + displayGrade);
                            }
                            
                            // Handle null or "N/A" display case
                            if (displayGrade == null || displayGrade.equals("N/A") || displayGrade.isEmpty()) {
                                setText("N/A");
                                setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;"); // Gray and italic for N/A
                                return;
                            }
                            
                            // Format numeric grades to show with one decimal place if needed
                            try {
                                double grade = Double.parseDouble(displayGrade);
                                // Format to 1 decimal place for display
                                if (grade == (int) grade) {
                                    // For whole numbers like 1.0, show as "1"
                                    setText(String.valueOf((int) grade));
                                } else {
                                    // For decimal numbers, format with 1 decimal place
                                    setText(String.format("%.1f", grade));
                                }
                                
                                // Style passed grades (below 3.0) green and failed grades (3.0 or above) red
                                if (grade >= 3.0) {
                                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red for failing grades
                                } else {
                                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green for passing grades
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Could not parse grade as number: " + displayGrade);
                                setText(displayGrade); // Use original text
                                setStyle("-fx-text-fill: #333333;"); // Default style for non-numeric grades
                            }
                        }
                    }
                };
            });
            
            System.out.println("Midterm grade column setup complete");
        }
          // Set up final grade column with a more direct property binding
        if (finalGradeColumn != null) {
            System.out.println("Setting up finalGradeColumn with ID: " + finalGradeColumn.getId());              // Use a simpler but more direct approach for cell value factory
            finalGradeColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                if (student == null) return new javafx.beans.property.SimpleStringProperty("N/A");
                
                // Get the final grade directly from the student model
                String grade = student.getFinalGrade();
                System.out.println("Cell value factory for " + student.getName() + " final grade: " + grade);
                
                // Create a new property that will trigger UI updates when changed
                javafx.beans.property.SimpleStringProperty prop = new javafx.beans.property.SimpleStringProperty();
                prop.set(grade != null && !grade.isEmpty() ? grade : "N/A");
                return prop;
            });
            
            // Add custom cell factory to style the grade cells            
            finalGradeColumn.setCellFactory(column -> {
                return new javafx.scene.control.TableCell<StudentModel, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        // Get the actual student if possible
                        StudentModel student = null;
                        if (getTableRow() != null) {
                            student = (StudentModel) getTableRow().getItem();
                        }
                        
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            // Try to get grade directly from the student for more accurate display
                            String displayGrade = item;
                            if (student != null && student.getFinalGrade() != null) {
                                displayGrade = student.getFinalGrade();
                                System.out.println("Cell displaying final grade for " + student.getName() + ": " + displayGrade);
                            }
                            
                            // Handle null or "N/A" display case
                            if (displayGrade == null || displayGrade.equals("N/A") || displayGrade.isEmpty()) {
                                setText("N/A");
                                setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;"); // Gray and italic for N/A
                                return;
                            }
                            
                            // Format numeric grades to show with one decimal place if needed
                            try {
                                double grade = Double.parseDouble(displayGrade);
                                // Format to 1 decimal place for display
                                if (grade == (int) grade) {
                                    // For whole numbers like 1.0, show as "1"
                                    setText(String.valueOf((int) grade));
                                } else {
                                    // For decimal numbers, format with 1 decimal place
                                    setText(String.format("%.1f", grade));
                                }
                                
                                // Style passed grades (below 3.0) green and failed grades (3.0 or above) red
                                if (grade >= 3.0) {
                                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Red for failing grades
                                } else {
                                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Green for passing grades
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Could not parse grade as number: " + displayGrade);
                                setText(displayGrade); // Use original text
                                setStyle("-fx-text-fill: #333333;"); // Default style for non-numeric grades
                            }
                        }
                    }
                };
            });
            
            System.out.println("Final grade column setup complete");
        }
        
        // Set up double-click handler for showing Add Grade dialog
        studentsTableView.setRowFactory(tv -> {
            TableRow<StudentModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    StudentModel student = row.getItem();
                    showAddGradeDialog(student);
                }
            });            return row;
        });
    }
    
    /**
     * Set the subject data and load the students
     * 
     * @param subject The subject model containing enrolled students
     */
    public void setSubjectData(EnrolledStudentsBySubjectModel subject) {
        if (subject == null) {
            System.err.println("Error: subject data is null");
            return;
        }
          this.subjectCode = subject.getSubjectCode();
        this.subjectName = subject.getSubjectName();
        this.currentSubjectId = subject.getSubjectId(); // Store subject ID for grade matching
        
        System.out.println("Loading students for subject: " + subjectCode);
        
        // Update the subject info label
        Platform.runLater(() -> {
            if (subjectInfoLabel != null) {
                subjectInfoLabel.setText("Subject: " + subjectCode);
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
                        allStudents.clear();                          if (response.has("enrolledStudents") && response.get("enrolledStudents").isJsonArray()) {
                            com.google.gson.JsonArray studentsArray = response.getAsJsonArray("enrolledStudents");
                            System.out.println("Found " + studentsArray.size() + " students enrolled in subject " + edpCode);
                            
                            // Store the subject ID for looking up grades later
                            String targetSubjectId = null;
                            if (response.has("subjects") && response.get("subjects").isJsonArray()) {
                                com.google.gson.JsonArray subjectsArray = response.getAsJsonArray("subjects");
                                for (int i = 0; i < subjectsArray.size(); i++) {
                                    com.google.gson.JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                                    if (subjectObj.has("edpCode") && subjectObj.get("edpCode").getAsString().equals(edpCode)) {
                                        if (subjectObj.has("_id")) {
                                            targetSubjectId = subjectObj.get("_id").getAsString();
                                            this.currentSubjectId = targetSubjectId;
                                            System.out.println("Found subject ID for " + edpCode + ": " + targetSubjectId);
                                        }
                                        break;
                                    }
                                }
                            }
                            
                            for (int i = 0; i < studentsArray.size(); i++) {
                                try {
                                    com.google.gson.JsonObject studentObj = studentsArray.get(i).getAsJsonObject();
                                    // Create StudentModel from the response
                                    org.finalproject.loginregisterfx.models.StudentModel student = 
                                        createStudentFromResponse(studentObj, edpCode);
                                    allStudents.add(student);
                                      System.out.println("Added student: " + student.getName() + 
                                                      " (" + student.getIdNumber() + ")" + 
                                                      " | Midterm: " + student.getMidtermGrade() +
                                                      " | Final: " + student.getFinalGrade());
                                } catch (Exception e) {
                                    System.err.println("Error processing student data: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }// Update the filtered list and display
                            filteredStudents.clear();
                            filteredStudents.addAll(allStudents);
                            
                            // Debug output of all students with their grades
                            System.out.println("===== STUDENT LIST WITH GRADES =====");
                            for(StudentModel s : allStudents) {
                                System.out.println("Student: " + s.getName() + 
                                                " | ID: " + s.getId() + 
                                                " | Midterm: " + (s.getMidtermGrade() != null ? s.getMidtermGrade() : "N/A") + 
                                                " | Final: " + (s.getFinalGrade() != null ? s.getFinalGrade() : "N/A"));
                            }
                            System.out.println("==================================");
                              // Force complete UI refresh with the new method
                            // Ensure we have our most current data in the table view
                            studentsTableView.getItems().clear();
                            studentsTableView.setItems(filteredStudents);
                            forceTableViewRefresh();
                            
                            // Print each student's data after refresh for verification
                            System.out.println("Students in table after refresh:");
                            for (StudentModel s : filteredStudents) {
                                System.out.println("Student: " + s.getName() + 
                                                " | ID: " + s.getId() + 
                                                " | Midterm: " + (s.getMidtermGrade() != null ? s.getMidtermGrade() : "N/A") + 
                                                " | Final: " + (s.getFinalGrade() != null ? s.getFinalGrade() : "N/A"));
                            }
                            
                            // Verify column visibility - ensure grade columns are showing
                            checkColumnVisibility();
                            
                            int studentCount = allStudents.size();
                            
                            System.out.println("Loaded " + studentCount + " students for subject " + edpCode);
                            
                            // Update subject info label with count
                            if (subjectInfoLabel != null) {
                                subjectInfoLabel.setText("Subject: " + subjectCode);
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
     * Helper method to create a StudentModel from the API response format specifically for the format:
     * {"message":"Students enrolled in subjects assigned to the teacher retrieved successfully",
     *  "teacherName":"teacher","totalSubjects":1,"totalStudents":1,
     *  "subjects":[{"_id":"682596683f312c017e300e42","subjectName":"English Communication Skills 1","edpCode":"eng100","studentCount":1}],
     *  "enrolledStudents":[{"_id":"6825c141ca79e23402a6cde1","name":"Cindy A. Casquejo","email":"cindycasq@tcshs.edu.ph","course":"BSIT",
     *                       "subjects":[{"_id":"682596683f312c017e300e42","subjectName":"English Communication Skills 1","edpCode":"eng100",
     *                                   "grades":{"midtermGrade":1,"_id":"6825e408f402489a33e7438c"}}]}]}
     * 
     * @param studentObj JsonObject containing student data from the API
     * @param edpCode EDP code of the subject to filter subject-specific information
     * @return StudentModel with the data from the response
     */
    private StudentModel createStudentFromResponse(com.google.gson.JsonObject studentObj, String edpCode) {
        // Create a new StudentModel
        StudentModel student = new StudentModel();
        
        System.out.println("Creating student from JSON: " + studentObj.toString());
        
        // Set basic student information
        if (studentObj.has("_id")) {
            student.setId(studentObj.get("_id").getAsString());
            System.out.println("Set student ID: " + student.getId());
        }
        
        if (studentObj.has("name")) {
            student.setName(studentObj.get("name").getAsString());
            System.out.println("Set student name: " + student.getName());
        }
        
        if (studentObj.has("email")) {
            student.setEmail(studentObj.get("email").getAsString());
            System.out.println("Set student email: " + student.getEmail());
                  // Extract idNumber from email if needed
            if (!studentObj.has("idNumber")) {
                // Check if studentId field exists (preferred)
                if (studentObj.has("studentId")) {
                    student.setIdNumber(studentObj.get("studentId").getAsString());
                    System.out.println("Extracted idNumber from studentId field: " + student.getIdNumber());
                } else {
                    // Fallback to email extraction
                    String email = studentObj.get("email").getAsString();
                    if (email.contains("@")) {
                        String[] parts = email.split("@");
                        if (parts.length > 0) {
                            student.setIdNumber(parts[0]);
                            System.out.println("Extracted idNumber from email: " + student.getIdNumber());
                        }
                    }
                }
            }
        }
        
        if (studentObj.has("course")) {
            student.setCourse(studentObj.get("course").getAsString());
            System.out.println("Set student course: " + student.getCourse());
        }
        
        if (studentObj.has("department")) {
            student.setDepartment(studentObj.get("department").getAsString());
            System.out.println("Set student department: " + student.getDepartment());
        }
        
        // Extract section information
        if (studentObj.has("section")) {
            student.setSection(studentObj.get("section").getAsString());
        } else {
            // Default section to N/A
            student.setSection("N/A");
        }
        
        // Extract year level information (if available)
        if (studentObj.has("yearLevel")) {
            try {
                if (studentObj.get("yearLevel").isJsonPrimitive()) {
                    if (studentObj.get("yearLevel").getAsJsonPrimitive().isNumber()) {
                        student.setYearLevel(studentObj.get("yearLevel").getAsInt());
                    } else {
                        try {
                            student.setYearLevel(Integer.parseInt(studentObj.get("yearLevel").getAsString()));
                        } catch (NumberFormatException e) {
                            student.setYearLevel(1);
                        }
                    }
                } else {
                    student.setYearLevel(1);
                }
            } catch (Exception e) {
                student.setYearLevel(1);
            }
        }
          // Extract student ID number
        if (studentObj.has("idNumber")) {
            student.setIdNumber(studentObj.get("idNumber").getAsString());
        } else if (studentObj.has("studentId")) {
            student.setIdNumber(studentObj.get("studentId").getAsString());
            System.out.println("Set idNumber from studentId: " + student.getIdNumber());
        }
        
        // Process subjects array to find grades for the current subject
        if (studentObj.has("subjects") && studentObj.get("subjects").isJsonArray()) {
            com.google.gson.JsonArray subjectsArray = studentObj.getAsJsonArray("subjects");
            System.out.println("Student has " + subjectsArray.size() + " subjects");
            
            // Find the subject that matches the edpCode
            for (int i = 0; i < subjectsArray.size(); i++) {
                com.google.gson.JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                
                boolean isMatchingSubject = false;
                
                // Check if subject matches by edpCode
                if (subjectObj.has("edpCode") && edpCode != null && !edpCode.isEmpty()) {
                    String subjEdpCode = subjectObj.get("edpCode").getAsString();
                    if (edpCode.equals(subjEdpCode)) {
                        System.out.println("Found matching subject by edpCode: " + subjEdpCode);
                        isMatchingSubject = true;
                    }
                }
                
                // Check if subject matches by ID
                if (subjectObj.has("_id") && currentSubjectId != null && !currentSubjectId.isEmpty()) {
                    String subjId = subjectObj.get("_id").getAsString();
                    if (currentSubjectId.equals(subjId)) {
                        System.out.println("Found matching subject by ID: " + subjId);
                        isMatchingSubject = true;
                    }
                }
                
                if (isMatchingSubject) {
                    // Extract grades directly from the response format
                    if (subjectObj.has("grades") && !subjectObj.get("grades").isJsonNull()) {
                        com.google.gson.JsonObject gradesObj = subjectObj.getAsJsonObject("grades");
                        System.out.println("Found grades object: " + gradesObj);
                          // Process midterm grade using the utility method
                        if (gradesObj.has("midtermGrade")) {
                            String midtermGrade = extractGradeValue(gradesObj.get("midtermGrade"));
                            student.setMidtermGrade(midtermGrade);
                            System.out.println("Set midterm grade for student " + student.getName() + ": " + midtermGrade);
                        } else {
                            student.setMidtermGrade(null);
                            System.out.println("No midterm grade found for student " + student.getName());
                        }
                        
                        // Process final grade using the utility method
                        if (gradesObj.has("finalGrade")) {
                            String finalGrade = extractGradeValue(gradesObj.get("finalGrade"));
                            student.setFinalGrade(finalGrade);
                            System.out.println("Set final grade for student " + student.getName() + ": " + finalGrade);
                        } else {
                            student.setFinalGrade(null);
                            System.out.println("No final grade found for student " + student.getName());
                        }
                    } else {
                        System.out.println("No grades object found for the subject");
                    }
                    break; // Stop after finding the matching subject
                }
            }
        } else {
            System.out.println("No subjects array found for student: " + student.getName());
        }
        
        return student;
    }
    
    /**
     * Utility method to extract a grade value from a JSON element
     * Handles different formats of grades (numeric, string, etc.)
     * 
     * @param gradeElement The JSON element containing the grade
     * @return String representation of the grade, or null if not available
     */
    private String extractGradeValue(com.google.gson.JsonElement gradeElement) {
        if (gradeElement == null || gradeElement.isJsonNull()) {
            return null;
        }
        
        try {
            if (gradeElement.isJsonPrimitive()) {
                com.google.gson.JsonPrimitive primitive = gradeElement.getAsJsonPrimitive();
                
                if (primitive.isNumber()) {
                    // Handle numeric grade (e.g., 1.0, 2.5)
                    double gradeValue = primitive.getAsDouble();
                    return String.valueOf(gradeValue);
                } else if (primitive.isString()) {
                    // Handle string grade (e.g., "1.0", "A", "Pass")
                    return primitive.getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting grade value: " + e.getMessage());
        }
        
        // Fallback: return the string representation with quotes removed
        return gradeElement.toString().replace("\"", "");
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

            // Pass this controller to the dialog for callbacks
            dialogStage.setUserData(this);
            System.out.println("Setting StudentsViewController as userData for grade dialog");

            // Get the controller and pass data
            AddGradeController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(student, subjectCode, subjectName);
            
            // Set up a handler for when the dialog closes to refresh the UI
            dialogStage.setOnHidden(event -> {
                System.out.println("Grade dialog closed, checking for updates to student: " + student.getName());
                // Force a refresh of the UI to show updated grades
                Platform.runLater(() -> {
                    // Just refresh the UI without reloading from the API
                    forceTableViewRefresh();
                    
                    // Debug output - check if student grades were updated
                    System.out.println("After dialog closed - Student: " + student.getName() + 
                                     " | Midterm: " + (student.getMidtermGrade() != null ? student.getMidtermGrade() : "N/A") + 
                                     " | Final: " + (student.getFinalGrade() != null ? student.getFinalGrade() : "N/A"));
                });
            });

            // Show the dialog and wait until user closes it
            dialogStage.showAndWait();
            
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
      /**
     * Refresh the displayed data for a specific student after grade update
     * 
     * @param updatedStudent The student with updated grade information
     */
    public void refreshStudentData(StudentModel updatedStudent) {
        if (updatedStudent == null) {
            System.out.println("Cannot refresh null student");
            return;
        }
        
        System.out.println("========== REFRESHING STUDENT DATA ==========");
        System.out.println("Student: " + updatedStudent.getName() + 
                          " (ID: " + updatedStudent.getId() + ")");
        System.out.println("Updated grades - Midterm: " + updatedStudent.getMidtermGrade() + 
                          ", Final: " + updatedStudent.getFinalGrade());
        
        boolean found = false;
        
        // Find the student in the list and update grades
        for (int i = 0; i < allStudents.size(); i++) {
            StudentModel student = allStudents.get(i);
            if (student.getId().equals(updatedStudent.getId())) {
                System.out.println("Found student in position " + i + " in all students list");
                System.out.println("Before update - Midterm: " + student.getMidtermGrade() + ", Final: " + student.getFinalGrade());
                
                // Update the grades
                student.setMidtermGrade(updatedStudent.getMidtermGrade());
                student.setFinalGrade(updatedStudent.getFinalGrade());
                
                System.out.println("After update - Midterm: " + student.getMidtermGrade() + ", Final: " + student.getFinalGrade());
                found = true;
                
                // Also update in filtered students list if present
                for (int j = 0; j < filteredStudents.size(); j++) {
                    if (filteredStudents.get(j).getId().equals(updatedStudent.getId())) {
                        System.out.println("Also updating student in filtered list at position " + j);
                        filteredStudents.get(j).setMidtermGrade(updatedStudent.getMidtermGrade());
                        filteredStudents.get(j).setFinalGrade(updatedStudent.getFinalGrade());
                        break;
                    }
                }
                break;
            }
        }
        
        if (!found) {
            System.out.println("Could not find student with ID: " + updatedStudent.getId() + " in the all students list");
        }
        
        // Refresh the TableView to display the updated grades
        Platform.runLater(() -> {
            System.out.println("Forcing UI refresh from Platform.runLater...");
            
            // Check if this student is in the current table items
            boolean studentInTable = false;
            for (StudentModel s : studentsTableView.getItems()) {
                if (s.getId().equals(updatedStudent.getId())) {
                    studentInTable = true;
                    System.out.println("Student is in current TableView items");
                    break;
                }
            }
            
            if (studentInTable) {
                System.out.println("Student is in table, doing full refresh");
                forceTableViewRefresh();
            } else {
                System.out.println("Student not found in current table items, unable to refresh directly");
                // If student isn't in the current view, we may need to reload or add them
                fetchStudentsBySubjectEdp(subjectCode);
            }
            
            // Debug output to verify the update
            System.out.println("===== REFRESHED STUDENT LIST WITH GRADES =====");
            for (StudentModel s : allStudents) {
                if (s.getId().equals(updatedStudent.getId())) {
                    System.out.println("UPDATED => Student: " + s.getName() + 
                                     " | ID: " + s.getId() + 
                                     " | Midterm: " + (s.getMidtermGrade() != null ? s.getMidtermGrade() : "N/A") + 
                                     " | Final: " + (s.getFinalGrade() != null ? s.getFinalGrade() : "N/A"));
                }
            }
            System.out.println("===========================================");
        });
    }
    
    /**
     * Forces a refresh of the TableView UI without reloading data from the API
     * This ensures that updated properties like grades are refreshed in the UI
     */
    private void forceTableViewRefresh() {
        if (studentsTableView == null) {
            System.out.println("Cannot refresh null TableView");
            return;
        }
        
        System.out.println("Forcing TableView refresh...");
        
        // Display all columns to verify they're properly set up
        System.out.println("TableView columns configuration:");
        for (TableColumn<?,?> column : studentsTableView.getColumns()) {
            System.out.println(" - Column: " + column.getId() + ", Visible: " + column.isVisible() + 
                              ", Width: " + column.getWidth());
        }
        
        // Store current items and selection
        ObservableList<StudentModel> currentItems = studentsTableView.getItems();
        int selectedIndex = studentsTableView.getSelectionModel().getSelectedIndex();
        System.out.println("Current items count: " + (currentItems != null ? currentItems.size() : 0));

        try {
            // Make a copy of the items to force property change notifications
            ObservableList<StudentModel> refreshedItems = FXCollections.observableArrayList();
            if (currentItems != null) {
                for (StudentModel student : currentItems) {
                    // Debug each student's grades
                    System.out.println("Refreshing: " + student.getName() + 
                                      " | Midterm: " + (student.getMidtermGrade() != null ? student.getMidtermGrade() : "N/A") +
                                      " | Final: " + (student.getFinalGrade() != null ? student.getFinalGrade() : "N/A"));
                    refreshedItems.add(student);
                }
            }
            
            // Apply the refreshed items
            Platform.runLater(() -> {
                try {
                    // Force cell update by emptying and resetting the items
                    studentsTableView.getItems().clear();
                    studentsTableView.setItems(null);
                    studentsTableView.layout();
                    studentsTableView.setItems(refreshedItems);
                    
                    // Restore selection if needed
                    if (selectedIndex >= 0 && selectedIndex < refreshedItems.size()) {
                        studentsTableView.getSelectionModel().select(selectedIndex);
                    }
                      // Extra refresh for good measure
                    studentsTableView.refresh();
                    
                    // Refresh the grade columns specifically
                    refreshGradeColumns();
                    
                    // Ensure columns are visible
                    checkColumnVisibility();
                    
                    System.out.println("TableView refresh completed successfully");
                } catch (Exception e) {
                    System.err.println("Error during TableView refresh: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error preparing TableView refresh: " + e.getMessage());
            e.printStackTrace();
            
            // Final fallback - just refresh without recreating items
            studentsTableView.refresh();
        }
    }
    
    /**
     * Check and ensure the visibility of important columns in the TableView
     */
    private void checkColumnVisibility() {
        if (midtermGradeColumn != null && !midtermGradeColumn.isVisible()) {
            System.out.println("Midterm grade column is not visible, making it visible");
            midtermGradeColumn.setVisible(true);
        }
        
        if (finalGradeColumn != null && !finalGradeColumn.isVisible()) {
            System.out.println("Final grade column is not visible, making it visible");
            finalGradeColumn.setVisible(true);
        }
    }
    
    /**
     * Explicitly refresh the grade columns to ensure they display current data
     * Call this after updating student grades
     */
    private void refreshGradeColumns() {
        System.out.println("Explicitly refreshing grade columns...");
        
        if (midtermGradeColumn != null) {
            // Use a direct approach to refresh the column values
            midtermGradeColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                if (student == null) return new javafx.beans.property.SimpleStringProperty("N/A");
                
                String grade = student.getMidtermGrade();
                System.out.println("Refreshed midterm grade for " + student.getName() + ": " + grade);
                return new javafx.beans.property.SimpleStringProperty(grade != null && !grade.isEmpty() ? grade : "N/A");
            });
            
            System.out.println("Refreshed midterm grade column");
        }
        
        if (finalGradeColumn != null) {
            // Use a direct approach to refresh the column values
            finalGradeColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                if (student == null) return new javafx.beans.property.SimpleStringProperty("N/A");
                
                String grade = student.getFinalGrade();
                System.out.println("Refreshed final grade for " + student.getName() + ": " + grade);
                return new javafx.beans.property.SimpleStringProperty(grade != null && !grade.isEmpty() ? grade : "N/A");
            });
            
            System.out.println("Refreshed final grade column");
        }
        
        // Also ensure columns are visible
        checkColumnVisibility();
        
        // Refresh the table view itself
        studentsTableView.refresh();
        
        // Debug output - check current data in the table
        System.out.println("Current table data after column refresh:");
        for (StudentModel student : studentsTableView.getItems()) {
            System.out.println("Student: " + student.getName() + 
                             " | Midterm: " + student.getMidtermGrade() +
                             " | Final: " + student.getFinalGrade());
        }
    }
}