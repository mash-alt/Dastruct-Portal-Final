package org.finalproject.loginregisterfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.models.EnrolledStudentsBySubjectModel;
import org.finalproject.loginregisterfx.models.StudentModel;

public class TeacherController {
    
    @FXML private Button logoutBtn;
    @FXML private Button refreshButton;
    @FXML private Label teacherNameLabel;
    @FXML private Label listTitleLabel;
    @FXML private TableView<EnrolledStudentsBySubjectModel> mainTableView;
    @FXML private TableColumn<EnrolledStudentsBySubjectModel, String> subjectCodeColumn;
    @FXML private TableColumn<EnrolledStudentsBySubjectModel, String> subjectNameColumn;
    @FXML private TableColumn<EnrolledStudentsBySubjectModel, Integer> enrolledStudentsColumn;
    @FXML private TextField searchField;
    
    // Data storage
    private ObservableList<EnrolledStudentsBySubjectModel> subjectsWithStudents = FXCollections.observableArrayList();
    private ObservableList<EnrolledStudentsBySubjectModel> filteredSubjects = FXCollections.observableArrayList();
    
    @FXML
    private void initialize() {
        System.out.println("Initializing TeacherController...");
        
        // Set up logout button handler
        if (logoutBtn != null) {
            logoutBtn.setOnAction(e -> handleLogout());
        } else {
            System.err.println("Logout button is null in TeacherController");
        }
        
        // Set up table columns
        setupTableColumns();
        
        // Set up search functionality
        if (searchField != null) {
            searchField.setOnAction(e -> handleSearch());
        }
        
        // Set up refresh button if it exists
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> fetchEnrolledStudents());
        }
        
        // Set up double-click handler for the table
        setupTableRowDoubleClickHandler();
        
        // Fetch enrolled students data
        fetchEnrolledStudents();
        
        // Load teacher name from session if available
        AuthService.getUserProfile().thenAccept(user -> {
            if (user != null && user.has("name")) {
                String teacherName = user.get("name").getAsString();
                System.out.println("Got teacher name from profile: " + teacherName);
                Platform.runLater(() -> {
                    if (teacherNameLabel != null) {
                        teacherNameLabel.setText(teacherName);
                    }
                });
            } else {
                System.out.println("No teacher name available in user profile");
            }
        }).exceptionally(ex -> {
            System.err.println("Error loading teacher profile: " + ex.getMessage());
            return null;
        });
    }
    
    /**
     * Handle logout button click - shows custom logout dialog
     */
    private void handleLogout() {
        try {
            System.out.println("Opening logout confirmation dialog...");
            
            // Load the Logout.fxml dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Logout.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the owner stage
            LogoutDialogController controller = loader.getController();
            controller.setOwnerStage((Stage) logoutBtn.getScene().getWindow());
            
            // Create and configure dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Logout");
            dialogStage.initOwner(logoutBtn.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Failed to open logout dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to open logout dialog: " + e.getMessage());
        }
    }
    
    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Set up the table columns with appropriate cell factories
     */
    private void setupTableColumns() {
        System.out.println("Setting up table columns");
        if (mainTableView == null) {
            System.err.println("Error: mainTableView is null");
            return;
        }
        
        // Configure the columns
        if (subjectCodeColumn != null) {
            subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        } else {
            System.err.println("Error: subjectCodeColumn is null");
        }
        
        if (subjectNameColumn != null) {
            subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        } else {
            System.err.println("Error: subjectNameColumn is null");
        }
        
        if (enrolledStudentsColumn != null) {
            enrolledStudentsColumn.setCellValueFactory(new PropertyValueFactory<>("enrolledStudentCount"));
        } else {
            System.err.println("Error: enrolledStudentsColumn is null");
        }
    }
    
    /**
     * Fetch enrolled students from the API
     */
    private void fetchEnrolledStudents() {
        System.out.println("Fetching enrolled students from API...");
        
        // Show loading indicator
        if (mainTableView != null) {
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(50, 50);
            mainTableView.setPlaceholder(progressIndicator);
        }
        
        // Clear previous data
        subjectsWithStudents.clear();
        
        // Make API request
        AuthService.makeGetRequest("/teacher/enrolled-students").thenAccept(response -> {
            System.out.println("Received enrolled students response: " + response);
            
            Platform.runLater(() -> {
                try {
                    if (response.has("subjects") && response.get("subjects").isJsonArray()) {
                        JsonArray subjectsArray = response.getAsJsonArray("subjects");
                        System.out.println("Found " + subjectsArray.size() + " subjects with students");
                        
                        for (int i = 0; i < subjectsArray.size(); i++) {
                            try {
                                JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                                EnrolledStudentsBySubjectModel model = new EnrolledStudentsBySubjectModel(subjectObj);
                                subjectsWithStudents.add(model);
                                
                                System.out.println("Added subject: " + model.getSubjectCode() + 
                                                   " with " + model.getEnrolledStudentCount() + " students");
                            } catch (Exception e) {
                                System.err.println("Error processing subject: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        // Update the filtered list and display
                        updateFilteredList("");
                        
                    } else {
                        // No subjects or empty response
                        mainTableView.setPlaceholder(new Label("No enrolled students found"));
                        System.out.println("No subjects with students found in response");
                    }
                } catch (Exception e) {
                    System.err.println("Error processing enrolled students response: " + e.getMessage());
                    e.printStackTrace();
                    showError("Failed to load enrolled students: " + e.getMessage());
                    mainTableView.setPlaceholder(new Label("Error loading data"));
                }
            });
        }).exceptionally(ex -> {
            System.err.println("API error fetching enrolled students: " + ex.getMessage());
            ex.printStackTrace();
            
            Platform.runLater(() -> {
                showError("Failed to fetch enrolled students: " + ex.getMessage());
                mainTableView.setPlaceholder(new Label("Error loading data"));
            });
            
            return null;
        });
    }
    
    /**
     * Handle search functionality
     */
    private void handleSearch() {
        String searchText = searchField.getText();
        System.out.println("Searching for: " + searchText);
        updateFilteredList(searchText);
    }
    
    /**
     * Update the filtered list based on search text
     */
    private void updateFilteredList(String searchText) {
        filteredSubjects.clear();
        
        if (searchText == null || searchText.isEmpty()) {
            filteredSubjects.addAll(subjectsWithStudents);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            
            for (EnrolledStudentsBySubjectModel subject : subjectsWithStudents) {
                if (subject.getSubjectCode().toLowerCase().contains(lowerCaseSearch) ||
                    subject.getSubjectName().toLowerCase().contains(lowerCaseSearch)) {
                    filteredSubjects.add(subject);
                }
            }
        }
        
        // Update table with filtered results
        mainTableView.setItems(filteredSubjects);
        
        // Update list title with count
        if (listTitleLabel != null) {
            listTitleLabel.setText("Subject List (" + filteredSubjects.size() + ")");
        }
        
        System.out.println("Filtered to " + filteredSubjects.size() + " subjects");
    }
    
    /**
     * Set up double-click handler for viewing student details
     */
    private void setupTableRowDoubleClickHandler() {
        if (mainTableView == null) return;
        
        mainTableView.setRowFactory(tv -> {
            TableRow<EnrolledStudentsBySubjectModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    EnrolledStudentsBySubjectModel subject = row.getItem();
                    showEnrolledStudentsDialog(subject);
                }
            });
            return row;
        });
    }
    
    /**
     * Show dialog with enrolled students for a subject
     */
    private void showEnrolledStudentsDialog(EnrolledStudentsBySubjectModel subject) {
        try {
            System.out.println("Showing enrolled students for subject: " + subject.getSubjectCode());
            
            // Create dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Students Enrolled in " + subject.getSubjectCode());
            dialog.setHeaderText("Students enrolled in " + subject.getSubjectName());
            
            // Set the button types
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            
            // Create a TableView for the students
            TableView<StudentModel> studentsTable = new TableView<>();
            studentsTable.setPrefWidth(500);
            studentsTable.setPrefHeight(400);
            
            // Set up columns
            TableColumn<StudentModel, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameColumn.setPrefWidth(150);
            
            TableColumn<StudentModel, String> idColumn = new TableColumn<>("ID Number");
            idColumn.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
            idColumn.setPrefWidth(100);
            
            TableColumn<StudentModel, String> courseColumn = new TableColumn<>("Course");
            courseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
            courseColumn.setPrefWidth(100);
            
            TableColumn<StudentModel, String> yearSectionColumn = new TableColumn<>("Year & Section");
            yearSectionColumn.setCellValueFactory(cellData -> {
                StudentModel student = cellData.getValue();
                String yearSection = student.getYearLevelString() + "-" + student.getSection();
                return javafx.beans.binding.Bindings.createStringBinding(() -> yearSection);
            });
            yearSectionColumn.setPrefWidth(120);
            
            // Add columns to table
            studentsTable.getColumns().addAll(nameColumn, idColumn, courseColumn, yearSectionColumn);
            
            // Add students to the table
            ObservableList<StudentModel> students = FXCollections.observableArrayList(subject.getEnrolledStudents());
            studentsTable.setItems(students);
            
            // Set the content
            dialog.getDialogPane().setContent(studentsTable);
            dialog.getDialogPane().setPrefWidth(520);
            dialog.getDialogPane().setPrefHeight(500);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error showing enrolled students dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Error displaying enrolled students: " + e.getMessage());
        }
    }
}
