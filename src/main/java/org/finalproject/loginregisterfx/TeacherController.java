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
import javafx.scene.layout.VBox;
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
          // Set up refresh button handler
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> fetchEnrolledStudents());
            System.out.println("Refresh button handler configured");
        } else {
            System.err.println("Refresh button is null in TeacherController");
        }
        
        // Set up search functionality
        if (searchField != null) {
            searchField.setOnAction(e -> handleSearch());
            System.out.println("Search field handler configured");
        } else {
            System.err.println("Search field is null in TeacherController");
        }
        
        // Set up double-click handler for the table
        setupTableRowDoubleClickHandler();
        
        // Fetch enrolled students data
        fetchEnrolledStudents();
          // Load teacher name from session if available
        AuthService.getUserProfile().thenAccept(response -> {
            System.out.println("Teacher profile response: " + response);
            
            // Extract the user data - API now returns nested user object
            JsonObject userData = null;
            if (response != null) {
                if (response.has("user") && response.get("user").isJsonObject()) {
                    // Extract from nested user object format: { user: { name: "..." } }
                    userData = response.getAsJsonObject("user");
                } else {
                    // Direct format without nesting: { name: "..." }
                    userData = response;
                }
            }
            
            // Process the user data
            if (userData != null && userData.has("name")) {
                String teacherName = userData.get("name").getAsString();
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
        subjectsWithStudents.clear();        // Make API request
        AuthService.makeGetRequest("/teacher/enrolled-students").thenAccept(response -> {
            System.out.println("Received enrolled students response: " + response);
            
            Platform.runLater(() -> {
                try {
                    // Make sure we clear any loading indicators
                    if (mainTableView == null) {
                        System.err.println("mainTableView is null, cannot update UI");
                        return;
                    }
                    
                    // Extract message from the response if available
                    String message = "No enrolled students found";
                    if (response.has("message")) {
                        message = response.get("message").getAsString();
                    }
                    
                    if (response.has("subjects") && response.get("subjects").isJsonArray()) {
                        JsonArray subjectsArray = response.getAsJsonArray("subjects");
                        System.out.println("Found " + subjectsArray.size() + " subjects with students");
                        
                        if (subjectsArray.size() > 0) {
                            // Process the subjects array
                            for (int i = 0; i < subjectsArray.size(); i++) {
                                try {
                                    JsonObject subjectObj = subjectsArray.get(i).getAsJsonObject();
                                    
                                    // Create a new model from the subject object
                                    EnrolledStudentsBySubjectModel model = new EnrolledStudentsBySubjectModel(subjectObj);
                                    
                                    // Check if we have enrolledStudents array in the main response
                                    if (response.has("enrolledStudents") && response.get("enrolledStudents").isJsonArray()) {
                                        JsonArray enrolledStudentsArray = response.getAsJsonArray("enrolledStudents");
                                        int studentCount = 0;
                                        
                                        // Count students enrolled in this specific subject
                                        for (int j = 0; j < enrolledStudentsArray.size(); j++) {
                                            JsonObject studentObj = enrolledStudentsArray.get(j).getAsJsonObject();
                                            
                                            // Check if student is enrolled in this subject
                                            if (studentObj.has("subjects") && studentObj.get("subjects").isJsonArray()) {
                                                JsonArray studentSubjects = studentObj.getAsJsonArray("subjects");
                                                
                                                for (int k = 0; k < studentSubjects.size(); k++) {
                                                    JsonObject studentSubject = studentSubjects.get(k).getAsJsonObject();
                                                    
                                                    if (studentSubject.has("_id") && 
                                                        studentSubject.get("_id").getAsString().equals(model.getSubjectId())) {
                                                        studentCount++;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // If we have a student count from the API response, use it
                                        if (subjectObj.has("studentCount")) {
                                            model.setEnrolledStudentCount(subjectObj.get("studentCount").getAsInt());
                                        } else {
                                            // Otherwise set the counted value
                                            model.setEnrolledStudentCount(studentCount);
                                        }
                                        
                                        System.out.println("Counted " + studentCount + " students for subject " + model.getSubjectCode());
                                    }
                                    
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
                            // Empty subjects array - display the message to the user
                            handleEmptyResponse(message);
                        }
                    } else {
                        // No subjects array in response
                        handleEmptyResponse(message);
                    }} catch (Exception e) {
                    System.err.println("Error processing enrolled students response: " + e.getMessage());
                    e.printStackTrace();
                    showError("Failed to load enrolled students: " + e.getMessage());
                    
                    // Ensure we have a placeholder even if there's an error
                    Label errorLabel = new Label("Error loading data: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                    mainTableView.setPlaceholder(errorLabel);
                    
                    // Update list title with 0 count
                    if (listTitleLabel != null) {
                        listTitleLabel.setText("Subject List (0)");
                    }
                }
            });
        }).exceptionally(ex -> {
            System.err.println("API error fetching enrolled students: " + ex.getMessage());
            ex.printStackTrace();
            
            Platform.runLater(() -> {
                // Don't show an error dialog for "no subjects" responses
                if (ex.getMessage() != null && !ex.getMessage().contains("No subjects")) {
                    showError("Failed to fetch enrolled students: " + ex.getMessage());
                }
                
                // Set up a nice looking error message
                Label errorLabel = new Label("Connection error: " + ex.getMessage());
                errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                mainTableView.setPlaceholder(errorLabel);
                
                // Update list title with 0 count
                if (listTitleLabel != null) {
                    listTitleLabel.setText("Subject List (0)");
                }
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
    }    /**
     * Show enrolled students for a subject using MyStudents.fxml
     */
    private void showEnrolledStudentsDialog(EnrolledStudentsBySubjectModel subject) {
        try {
            System.out.println("Opening MyStudents.fxml for subject: " + subject.getSubjectCode());
            
            // Load the MyStudents.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyStudents.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            StudentsViewController controller = loader.getController();
            
            // Add a listener to the stage that will be triggered when the window closes
            Stage stage = new Stage();
            stage.setOnHidden(event -> {
                System.out.println("MyStudents window closed, refreshing data...");
                // Refresh the subject list when returning from the student view
                // This ensures any grade changes are reflected in the UI
                fetchEnrolledStudents();
            });
            
            // Pass the subject data to the controller
            controller.setSubjectData(subject);
            
            // Configure the stage
            stage.setTitle("Students Enrolled in " + subject.getSubjectCode() + " - " + subject.getSubjectName());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainTableView.getScene().getWindow());
            
            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Apply CSS if needed
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            
            // Set stage properties
            stage.setResizable(true);
            stage.setMinWidth(600);
            stage.setMinHeight(450);
            
            // Show the stage
            stage.show();
            
            // Debug info - note that the actual student count will be determined by the API call
            System.out.println("MyStudents view opened for subject: " + subject.getSubjectCode() + 
                               " with ID: " + subject.getSubjectId());
            
        } catch (Exception e) {
            System.err.println("Error opening MyStudents view: " + e.getMessage());
            e.printStackTrace();
            showError("Error displaying enrolled students: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to handle empty response from the API
     * @param message The message to display to the user
     */
    private void handleEmptyResponse(String message) {
        // Create a visually appealing "no data" message
        VBox noDataBox = new VBox();
        noDataBox.setAlignment(javafx.geometry.Pos.CENTER);
        noDataBox.setSpacing(10);
        
        // Add an icon or image if you like
        Label iconLabel = new Label("📚");
        iconLabel.setStyle("-fx-font-size: 40px;");
        
        // Add the message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px;");
        
        // Add a smaller hint message
        Label hintLabel = new Label("You will see subject data once students are enrolled");
        hintLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        
        // Add components to the box
        noDataBox.getChildren().addAll(iconLabel, messageLabel, hintLabel);
        
        // Set as table placeholder
        mainTableView.setPlaceholder(noDataBox);
        
        // Update list title with 0 count
        if (listTitleLabel != null) {
            listTitleLabel.setText("Subject List (0)");
        }
        
        // Clear any filtered results
        filteredSubjects.clear();
        
        System.out.println("Displayed empty state with message: " + message);
    }
    
    /**
     * Public method to refresh the teacher's subject and student data
     * Can be called from other controllers when data needs updating
     */
    public void refreshData() {
        System.out.println("Explicitly refreshing teacher data...");
        fetchEnrolledStudents();
    }
}
