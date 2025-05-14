package org.finalproject.loginregisterfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
                String section = student.getYearLevelString() + "-" + student.getSection();
                return javafx.beans.binding.Bindings.createStringBinding(() -> section);
            });
        } else {
            System.err.println("Error: sectionColumn is null");
        }
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
     */
    private void fetchStudentsBySubjectEdp(String edpCode) {
        System.out.println("Fetching students for subject EDP: " + edpCode);
        
        // Use the specific endpoint to get students by subject EDP code
        String endpoint = "/teacher/subject/edp/" + edpCode + "/students";
        
        org.finalproject.loginregisterfx.Service.AuthService.makeGetRequest(endpoint)
            .thenAccept(response -> {
                System.out.println("Received students response for subject " + edpCode + ": " + response);
                
                Platform.runLater(() -> {
                    try {
                        // Clear existing data
                        allStudents.clear();
                        
                        if (response.has("students") && response.get("students").isJsonArray()) {
                            com.google.gson.JsonArray studentsArray = response.getAsJsonArray("students");
                            System.out.println("Found " + studentsArray.size() + " students enrolled in subject " + edpCode);
                            
                            for (int i = 0; i < studentsArray.size(); i++) {
                                try {
                                    com.google.gson.JsonObject studentObj = studentsArray.get(i).getAsJsonObject();
                                    org.finalproject.loginregisterfx.models.StudentModel student = 
                                        new org.finalproject.loginregisterfx.models.StudentModel(studentObj);
                                    allStudents.add(student);
                                    
                                    System.out.println("Added student: " + student.getName() + 
                                                      " (" + student.getIdNumber() + ")");
                                } catch (Exception e) {
                                    System.err.println("Error processing student data: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                            
                            // Update the filtered list and display                            filteredStudents.clear();
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
                            
                        } else {
                            // No students or empty response
                            System.out.println("No students found for subject " + edpCode);
                            studentsTableView.setPlaceholder(new javafx.scene.control.Label("No students enrolled in this subject"));
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing students response: " + e.getMessage());
                        e.printStackTrace();
                        studentsTableView.setPlaceholder(new javafx.scene.control.Label("Error loading student data"));
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("API error fetching students for subject " + edpCode + ": " + ex.getMessage());
                ex.printStackTrace();
                
                Platform.runLater(() -> {
                    studentsTableView.setPlaceholder(new javafx.scene.control.Label("Failed to load student data"));
                });
                
                return null;
            });
    }
    
    /**
     * Filter students based on search text
     */
    private void filterStudents(String searchText) {
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
}