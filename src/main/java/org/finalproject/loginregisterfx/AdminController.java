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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox; // Added import for HBox
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.models.StudentModel;
import org.finalproject.loginregisterfx.models.SubjectModel;
import org.finalproject.loginregisterfx.models.TeacherModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.stream.Collectors;

public class AdminController {    @FXML private TableView<Object> mainTableView;
    @FXML private TextField searchField;    @FXML private Button viewSubjectsBtn;
    @FXML private Button viewTeachersBtn;
    @FXML private Button viewStudentsBtn;
    @FXML private Button dashboardBtn;
    @FXML private Label adminNameLabel;
    @FXML private Button logoutBtn;
    @FXML private Label viewTitleLabel;
    @FXML private Label listTitleLabel;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    
    // Pagination controls
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label paginationInfoLabel;      // Pagination values
    private int currentPage = 0;
    private final int ROWS_PER_PAGE = 10; // Display 10 rows per page in the UI
    private ObservableList<Object> pagedItems = FXCollections.observableArrayList();
    private ObservableList<Object> allFilteredItems = FXCollections.observableArrayList();
    public enum ViewType { SUBJECTS, TEACHERS, STUDENTS }
    private ViewType currentView = ViewType.SUBJECTS;    @FXML
    private void initialize() {
        try {
            System.out.println("Initializing AdminController...");
            
            // Set up initial view
            System.out.println("Setting up event handlers...");
            setupEventHandlers();
            
            System.out.println("Setting up initial view (subjects)...");
            showSubjectsView();
            
            // Set up pagination
            System.out.println("Setting up pagination...");
            setupPagination();
    
            // Add button event handler
            System.out.println("Setting up add button event handler...");
            addButton.setOnAction(e -> showAddDialog());
            
            // Refresh button event handler
            System.out.println("Setting up refresh button event handler...");
            refreshButton.setOnAction(e -> handleRefresh());
            
            // Load admin name from session if available
            System.out.println("Loading admin user profile...");
            AuthService.getUserProfile().thenAccept(user -> {
                if (user != null && user.has("name")) {
                    String userName = user.get("name").getAsString();
                    System.out.println("Got admin name from profile: " + userName);
                    Platform.runLater(() -> {
                        adminNameLabel.setText(userName);
                    });
                } else {
                    System.out.println("No admin name available in user profile");
                }
            }).exceptionally(ex -> {
                System.err.println("Error loading admin profile: " + ex.getMessage());
                return null;
            });
            
            System.out.println("Setting up logout button event handler...");
            logoutBtn.setOnAction(e -> handleLogout());
            
            System.out.println("AdminController initialization completed");
        } catch (Exception e) {
            System.err.println("ERROR during AdminController initialization: " + e.getMessage());
            e.printStackTrace();
            showError("Error initializing Admin view: " + e.getMessage());
        }
    }
      private void setupPagination() {
        // Set up pagination buttons
        prevPageButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePagedItems();
                System.out.println("Moving to previous page: " + currentPage);
            } else {
                System.out.println("Already on first page, can't go back");
            }
        });
          nextPageButton.setOnAction(e -> {
            int maxPage = (int) Math.ceil(allFilteredItems.size() / (double) ROWS_PER_PAGE) - 1;
            if (currentPage < maxPage) {
                currentPage++;
                updatePagedItems();
                System.out.println("Moving to next page: " + currentPage + " of " + maxPage);
                System.out.println("Items showing: " + (currentPage * ROWS_PER_PAGE + 1) + "-" + 
                    Math.min((currentPage + 1) * ROWS_PER_PAGE, allFilteredItems.size()) + 
                    " of " + allFilteredItems.size());
            } else {
                System.out.println("Already on last page, can't go forward");
                System.out.println("Current page: " + currentPage + ", Max page: " + maxPage);
                System.out.println("Items in list: " + allFilteredItems.size());
            }
        });
        
        // Initially disable prev button
        prevPageButton.setDisable(true);
        nextPageButton.setDisable(true);
        
        // Initialize pagination info label
        updatePaginationInfo();
    }    private void setupEventHandlers() {
        try {
            System.out.println("Setting up view subjects button click handler...");
            viewSubjectsBtn.setOnAction(e -> {
                System.out.println("View subjects button clicked");
                showSubjectsView();
            });
            
            System.out.println("Setting up view teachers button click handler...");
            viewTeachersBtn.setOnAction(e -> {
                System.out.println("View teachers button clicked");
                showTeachersView();
            });
            
            System.out.println("Setting up view students button click handler...");
            viewStudentsBtn.setOnAction(e -> {
                System.out.println("View students button clicked");
                showStudentsView();
            });
            
            System.out.println("Setting up search field action handler...");
            searchField.setOnAction(e -> {
                System.out.println("Search action triggered with query: " + searchField.getText());
                handleSearch();
            });
            
            // Navigate to dashboard when dashboard button is clicked
            if (dashboardBtn != null) {
                System.out.println("Setting up dashboard button click handler...");
                dashboardBtn.setOnAction(e -> {
                    System.out.println("Dashboard button clicked");
                    navigateToDashboard();
                });
            } else {
                System.err.println("WARNING: Dashboard button is null, cannot set up click handler");
            }
            
            System.out.println("All event handlers set up successfully");
        } catch (Exception e) {
            System.err.println("ERROR setting up event handlers: " + e.getMessage());
            e.printStackTrace();
            showError("Error setting up event handlers: " + e.getMessage());
        }
    }private void navigateToDashboard() {
        try {
            System.out.println("Navigating to dashboard...");
            
            // Update button styling before navigation
            dashboardBtn.getStyleClass().remove("nav-button");
            dashboardBtn.getStyleClass().add("nav-button-active");
            
            // Load dashboard view
            System.out.println("Loading Dashboard.fxml resource...");
            Parent dashboardView = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
            
            System.out.println("Creating new scene...");
            Scene scene = new Scene(dashboardView);
            
            System.out.println("Setting scene on stage...");
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
            
            System.out.println("Successfully navigated to dashboard");
        } catch (Exception e) {
            System.err.println("Error navigating to dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation error: " + e.getMessage());
        }
    }    public void showSubjectsView() {
        try {
            System.out.println("Switching to SUBJECTS view");
            currentView = ViewType.SUBJECTS;
            
            // Add debug output for columns setup
            System.out.println("Setting up subject columns...");
            setupSubjectColumns();
            
            System.out.println("Fetching and displaying subjects...");
            fetchAndDisplaySubjects("");
            
            System.out.println("Updating UI elements for subjects view...");
            updateHeaderTitle("Subjects Management");
            listTitleLabel.setText("Subject List");
            searchField.setPromptText("Search subjects...");
            addButton.setText("Add Subject");
            addButton.setVisible(true); // Show the add button in Subjects view
            listTitleLabel.setStyle(""); // Remove highlight
            
            // Apply the subject context menu
            System.out.println("Applying context menu for subjects view...");
            applyContextMenu(ViewType.SUBJECTS);
            
            // Update navigation button styles
            System.out.println("Updating navigation button styles...");
            updateNavButtonStyles(ViewType.SUBJECTS);
            
            System.out.println("Successfully switched to SUBJECTS view");
        } catch (Exception e) {
            System.err.println("ERROR in showSubjectsView: " + e.getMessage());
            e.printStackTrace();
            showError("Error switching to Subjects view: " + e.getMessage());
        }
    }    public void showTeachersView() {
        try {
            System.out.println("Switching to TEACHERS view");
            currentView = ViewType.TEACHERS;
            
            System.out.println("Setting up teacher columns...");
            setupTeacherColumns();
            
            System.out.println("Fetching and displaying teachers...");
            fetchAndDisplayTeachers("");
            
            System.out.println("Updating UI elements for teachers view...");
            updateHeaderTitle("Teachers Management");
            listTitleLabel.setText("Teacher List");
            searchField.setPromptText("Search teachers...");
            addButton.setVisible(false); // Hide the add button in Teachers view since we're using context menu
            listTitleLabel.setStyle(""); // Remove highlight
            
            // Apply the teacher context menu
            System.out.println("Applying context menu for teachers view...");
            applyContextMenu(ViewType.TEACHERS);
            
            // Update navigation button styles
            System.out.println("Updating navigation button styles...");
            updateNavButtonStyles(ViewType.TEACHERS);
            
            System.out.println("Successfully switched to TEACHERS view");
        } catch (Exception e) {
            System.err.println("ERROR in showTeachersView: " + e.getMessage());
            e.printStackTrace();
            showError("Error switching to Teachers view: " + e.getMessage());
        }
    }
      public void showStudentsView() {
        try {
            System.out.println("Switching to STUDENTS view");
            currentView = ViewType.STUDENTS;
            
            System.out.println("Setting up student columns...");
            setupStudentColumns();
            
            System.out.println("Fetching and displaying students...");
            fetchAndDisplayStudents("");
            
            System.out.println("Updating UI elements for students view...");        updateHeaderTitle("Students Management");
        listTitleLabel.setText("Student List");
        searchField.setPromptText("Search students...");
        addButton.setVisible(false); // Hide the add button in Students view
        listTitleLabel.setStyle(""); // Remove highlight
        
        // Apply the appropriate context menu for students
        System.out.println("Applying context menu for students view...");
        applyContextMenu(ViewType.STUDENTS);
        
        // Update navigation button styles
        System.out.println("Updating navigation button styles...");
        updateNavButtonStyles(ViewType.STUDENTS);
        
        System.out.println("Successfully switched to STUDENTS view");
    } catch (Exception e) {
        System.err.println("ERROR in showStudentsView: " + e.getMessage());
        e.printStackTrace();
        showError("Error switching to Students view: " + e.getMessage());
    }
    }
    
    /**
     * Updates the navigation button styles to highlight the active view
     */
    private void updateNavButtonStyles(ViewType activeView) {
        // Reset all buttons to default style
        dashboardBtn.getStyleClass().remove("nav-button-active");
        dashboardBtn.getStyleClass().add("nav-button");
        
        viewSubjectsBtn.getStyleClass().remove("nav-button-active");
        viewSubjectsBtn.getStyleClass().add("nav-button");
        
        viewTeachersBtn.getStyleClass().remove("nav-button-active");
        viewTeachersBtn.getStyleClass().add("nav-button");
        
        viewStudentsBtn.getStyleClass().remove("nav-button-active");
        viewStudentsBtn.getStyleClass().add("nav-button");
        
        // Set active button style
        switch (activeView) {
            case SUBJECTS:
                viewSubjectsBtn.getStyleClass().remove("nav-button");
                viewSubjectsBtn.getStyleClass().add("nav-button-active");
                break;
            case TEACHERS:
                viewTeachersBtn.getStyleClass().remove("nav-button");
                viewTeachersBtn.getStyleClass().add("nav-button-active");
                break;
            case STUDENTS:
                viewStudentsBtn.getStyleClass().remove("nav-button");
                viewStudentsBtn.getStyleClass().add("nav-button-active");
                break;
        }
    }

    private void updateHeaderTitle(String title) {
        if (viewTitleLabel != null) {
            viewTitleLabel.setText(title);
        }
    }    private void handleSearch() {
        String query = searchField.getText();
        switch (currentView) {
            case SUBJECTS:
                fetchAndDisplaySubjects(query);
                break;
            case TEACHERS:
                fetchAndDisplayTeachers(query);
                break;
            case STUDENTS:
                fetchAndDisplayStudents(query);
                break;
        }
    }    private void setupSubjectColumns() {
        mainTableView.getColumns().clear();
        TableColumn<Object, String> codeCol = new TableColumn<>("Subject Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("edpCode"));
        TableColumn<Object, String> nameCol = new TableColumn<>("Subject Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        TableColumn<Object, Integer> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        TableColumn<Object, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        // Add cell factory to handle null/empty departments
        deptCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("N/A");
                } else {
                    setText(item);
                }
            }        });        // Add columns to the TableView, suppressing type safety warning
        @SuppressWarnings("unchecked")
        TableColumn<Object, ?>[] columns = new TableColumn[] {codeCol, nameCol, unitsCol, deptCol};
        mainTableView.getColumns().addAll(columns);
        
        // Apply context menu for right-click actions
        SetupSubjectContextMenu.apply(this, mainTableView);
    }    private void setupTeacherColumns() {
        mainTableView.getColumns().clear();
        TableColumn<Object, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Object, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Fixed the phone column
        TableColumn<Object, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<Object, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        
        // Add columns to the TableView, suppressing type safety warning
        @SuppressWarnings("unchecked")
        TableColumn<Object, ?>[] columns = new TableColumn[] {nameCol, emailCol, phoneCol, deptCol};
        mainTableView.getColumns().addAll(columns);
        
        // Apply context menu for right-click actions
        SetupTeacherContextMenu.apply(this, mainTableView);
    }

    private void setupStudentColumns() {
        mainTableView.getColumns().clear();
        TableColumn<Object, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Object, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));        TableColumn<Object, String> yearCol = new TableColumn<>("Year Level");        yearCol.setCellValueFactory(new PropertyValueFactory<>("yearLevelString"));
        TableColumn<Object, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        // Add columns to the TableView, suppressing type safety warning
        @SuppressWarnings("unchecked")
        TableColumn<Object, ?>[] columns = new TableColumn[] {nameCol, emailCol, yearCol, courseCol};
        mainTableView.getColumns().addAll(columns);
    }

    // Caches for client-side search
    private ObservableList<SubjectModel> cachedSubjects = FXCollections.observableArrayList();
    private ObservableList<TeacherModel> cachedTeachers = FXCollections.observableArrayList();
    private ObservableList<StudentModel> cachedStudents = FXCollections.observableArrayList();    private void fetchAndDisplaySubjects(String search) {
        if (cachedSubjects.isEmpty() || (search == null && mainTableView.getItems().isEmpty())) {
            // Fetch all pages from backend and cache
            cachedSubjects.clear();
            fetchAllSubjectsPages(search);
        } else {
            filterAndDisplaySubjects(search);
        }
    }
    
    private void fetchAllSubjectsPages(String search) {
        fetchSubjectsPage(1, search);
    }
    
    private void fetchSubjectsPage(int page, String search) {
        System.out.println("Fetching subjects page " + page);
        
        AuthService.makeGetRequest("/admin/subjects?page=" + page + "&limit=20").thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    JsonArray subjects = response.getAsJsonArray("subjects");
                    int totalSubjects = response.has("totalSubjects") ? response.get("totalSubjects").getAsInt() : 0;
                    int totalPages = response.has("totalPages") ? response.get("totalPages").getAsInt() : 1;
                    
                    System.out.println("Received page " + page + " of " + totalPages + 
                                       " (items: " + subjects.size() + ", total: " + totalSubjects + ")");
                    
                    for (var subj : subjects) {
                        try {
                            JsonObject subjObj = subj.getAsJsonObject();
                            SubjectModel subject = new SubjectModel(subjObj);
                            cachedSubjects.add(subject);
                            System.out.println("Added subject: " + subject.getSubjectName() + 
                                              " (" + subject.getEdpCode() + "), Department: '" + 
                                              subject.getDepartment() + "'");
                        } catch (Exception e) {
                            System.err.println("Error parsing subject data: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    // If there are more pages, fetch the next page
                    if (page < totalPages) {
                        fetchSubjectsPage(page + 1, search);
                    } else {
                        // All pages have been fetched, sort and display
                        System.out.println("All " + cachedSubjects.size() + " subjects fetched and cached");
                        
                        // Sort the cached subjects by EDP code
                        FXCollections.sort(cachedSubjects, (s1, s2) -> 
                            s1.getEdpCode().compareTo(s2.getEdpCode()));
                            
                        filterAndDisplaySubjects(search);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing subjects response: " + e.getMessage());
                    e.printStackTrace();
                    showError("Error loading subjects: " + e.getMessage());
                }
            });
        }).exceptionally(ex -> {
            System.err.println("API error fetching subjects: " + ex.getMessage());
            ex.printStackTrace();
            showError("Failed to fetch subjects: " + ex.getMessage());
            return null;
        });
    }private void filterAndDisplaySubjects(String search) {
        allFilteredItems.clear();
        
        if (search == null || search.isEmpty()) {
            allFilteredItems.addAll(cachedSubjects);
        } else {
            String lower = search.toLowerCase();
            for (SubjectModel s : cachedSubjects) {
                if (s.getEdpCode().toLowerCase().contains(lower) ||
                    s.getSubjectName().toLowerCase().contains(lower) ||
                    (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(lower))) {
                    allFilteredItems.add(s);
                }
            }
        }
        
        // Note: We don't need to sort here anymore because we're sorting the cachedSubjects directly
        // when loading them from the API. This improves performance.
        
        // Reset to first page when search changes
        currentPage = 0;
        updatePagedItems();
          // Debug information
        int maxPage = (int) Math.ceil(allFilteredItems.size() / (double) ROWS_PER_PAGE) - 1;
        System.out.println("Filtered items: " + allFilteredItems.size());
        System.out.println("Current page: " + currentPage);
        System.out.println("Max page: " + maxPage);
        System.out.println("Items per page: " + ROWS_PER_PAGE);
        System.out.println("Prev button disabled: " + prevPageButton.isDisabled());
        System.out.println("Next button disabled: " + nextPageButton.isDisabled());
    }private void fetchAndDisplayTeachers(String search) {
        if (cachedTeachers.isEmpty() || (search == null && mainTableView.getItems().isEmpty())) {
            // Fetch all pages from backend and cache
            cachedTeachers.clear();
            fetchAllTeachersPages(search);
        } else {
            filterAndDisplayTeachers(search);
        }
    }
    
    private void fetchAllTeachersPages(String search) {
        fetchTeachersPage(1, search);
    }
    
    private void fetchTeachersPage(int page, String search) {
        System.out.println("Fetching teachers page " + page);
        
        AuthService.makeGetRequest("/admin/teachers?page=" + page + "&limit=20").thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    JsonArray teachers = response.getAsJsonArray("teachers");
                    int totalTeachers = response.has("totalSubjects") ? response.get("totalSubjects").getAsInt() : 0;
                    int totalPages = response.has("totalPages") ? response.get("totalPages").getAsInt() : 1;
                    
                    System.out.println("Received page " + page + " of " + totalPages + 
                                      " (items: " + teachers.size() + ", total: " + totalTeachers + ")");
                    
                    for (var t : teachers) {
                        try {
                            TeacherModel teacher = new TeacherModel(t.getAsJsonObject());
                            cachedTeachers.add(teacher);
                            System.out.println("Added teacher: " + teacher.getName());
                        } catch (Exception e) {
                            System.err.println("Error parsing teacher data: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    // If there are more pages, fetch the next page
                    if (page < totalPages) {
                        fetchTeachersPage(page + 1, search);
                    } else {
                        // All pages have been fetched, display
                        System.out.println("All " + cachedTeachers.size() + " teachers fetched and cached");
                        filterAndDisplayTeachers(search);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing teachers response: " + e.getMessage());
                    e.printStackTrace();
                    showError("Error loading teachers: " + e.getMessage());
                }
            });
        }).exceptionally(ex -> {
            System.err.println("API error fetching teachers: " + ex.getMessage());
            ex.printStackTrace();
            showError("Failed to fetch teachers: " + ex.getMessage());
            return null;
        });
    }private void filterAndDisplayTeachers(String search) {
        allFilteredItems.clear();
        
        if (search == null || search.isEmpty()) {
            allFilteredItems.addAll(cachedTeachers);
        } else {
            String lower = search.toLowerCase();
            for (TeacherModel t : cachedTeachers) {
                if (t.getName().toLowerCase().contains(lower) ||
                    t.getEmail().toLowerCase().contains(lower) ||
                    t.getDepartment().toLowerCase().contains(lower)) {
                    allFilteredItems.add(t);
                }
            }
        }
        
        // Reset to first page when search changes
        currentPage = 0;
        updatePagedItems();
    }private void fetchAndDisplayStudents(String search) {
        if (cachedStudents.isEmpty() || (search == null && mainTableView.getItems().isEmpty())) {
            // Fetch all pages from backend and cache
            cachedStudents.clear();
            fetchAllStudentsPages(search);
        } else {
            filterAndDisplayStudents(search);
        }
    }
    
    private void fetchAllStudentsPages(String search) {
        fetchStudentsPage(1, search);
    }
    
    private void fetchStudentsPage(int page, String search) {
        System.out.println("Fetching students page " + page);
        
        AuthService.makeGetRequest("/admin/students?page=" + page + "&limit=20").thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    JsonArray students = response.getAsJsonArray("students");
                    int totalStudents = response.has("totalSubjects") ? response.get("totalSubjects").getAsInt() : 0;
                    int totalPages = response.has("totalPages") ? response.get("totalPages").getAsInt() : 1;
                    
                    System.out.println("Received page " + page + " of " + totalPages + 
                                      " (items: " + students.size() + ", total: " + totalStudents + ")");
                    
                    for (var s : students) {
                        try {
                            StudentModel student = new StudentModel(s.getAsJsonObject());
                            cachedStudents.add(student);
                            System.out.println("Added student: " + student.getName() + 
                                              ", Year: " + student.getYearLevelString());
                        } catch (Exception e) {
                            System.err.println("Error parsing student data: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    // If there are more pages, fetch the next page
                    if (page < totalPages) {
                        fetchStudentsPage(page + 1, search);
                    } else {
                        // All pages have been fetched, display
                        System.out.println("All " + cachedStudents.size() + " students fetched and cached");
                        filterAndDisplayStudents(search);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing students response: " + e.getMessage());
                    e.printStackTrace();
                    showError("Error loading students: " + e.getMessage());
                }
            });
        }).exceptionally(ex -> {
            System.err.println("API error fetching students: " + ex.getMessage());
            ex.printStackTrace();
            showError("Failed to fetch students: " + ex.getMessage());
            return null;
        });
    }private void filterAndDisplayStudents(String search) {
        allFilteredItems.clear();
        
        if (search == null || search.isEmpty()) {
            allFilteredItems.addAll(cachedStudents);
        } else {
            String lower = search.toLowerCase();
            for (StudentModel s : cachedStudents) {
                if (s.getName().toLowerCase().contains(lower) ||
                    s.getEmail().toLowerCase().contains(lower) ||
                    s.getYearLevelString().toLowerCase().contains(lower) ||
                    s.getCourse().toLowerCase().contains(lower)) {
                    allFilteredItems.add(s);
                }
            }
        }
        
        // Reset to first page when search changes
        currentPage = 0;
        updatePagedItems();
    }    private void showAddDialog() {
        switch (currentView) {
            case SUBJECTS:
                showAddSubjectDialog();
                break;
            case STUDENTS:
                // TODO: Implement add student dialog
                break;
            default:
                // No action for other views
                break;
        }
    }private void showAddSubjectDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Add New Subject");
        dialog.setHeaderText("Enter subject details");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Subject name field
        TextField subjectNameField = new TextField();
        subjectNameField.setPromptText("Subject name");
        
        // Units field
        TextField unitsField = new TextField();
        unitsField.setPromptText("Units");
        
        // Department dropdown
        ComboBox<String> departmentComboBox = new ComboBox<>();
        departmentComboBox.setPromptText("Select department");
        departmentComboBox.setEditable(true); // Allow custom entries
          // Set the specific departments as requested
        departmentComboBox.getItems().addAll(
            "BSCS", // Bachelor of Science in Computer Science
            "BSIT", // Bachelor of Science in Information Technology
            "BSBA", // Bachelor of Science in Business Administration
            "BSN",  // Bachelor of Science in Nursing
            "BSMT", // Bachelor of Science in Medical Technology
            "BSTM"  // Bachelor of Science in Tourism Management
        );
        
        // Prerequisites multi-select ListView
        ListView<CheckBox> prerequisitesListView = new ListView<>();
        prerequisitesListView.setPrefHeight(150); // Set a reasonable height
        
        // Add all subjects to the prerequisites list with checkboxes
        ObservableList<CheckBox> checkBoxList = FXCollections.observableArrayList();
        for (SubjectModel subject : cachedSubjects) {
            CheckBox cb = new CheckBox(subject.getSubjectName() + " (" + subject.getEdpCode() + ")");
            cb.setUserData(subject.getEdpCode()); // Store EDP code as user data for later retrieval
            checkBoxList.add(cb);
        }
        // Sort the checkboxes by subject name
        FXCollections.sort(checkBoxList, (cb1, cb2) -> cb1.getText().compareTo(cb2.getText()));
        prerequisitesListView.setItems(checkBoxList);
        
        // Add components to grid
        grid.add(new Label("Subject Name:"), 0, 0);
        grid.add(subjectNameField, 1, 0);
        grid.add(new Label("Units:"), 0, 1);
        grid.add(unitsField, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(departmentComboBox, 1, 2);
        grid.add(new Label("Prerequisites:"), 0, 3);
        grid.add(prerequisitesListView, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500); // Make dialog wider
        Platform.runLater(subjectNameField::requestFocus);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int units = Integer.parseInt(unitsField.getText());
                    
                    // Collect all selected prerequisites
                    java.util.List<String> selectedPrerequisites = new java.util.ArrayList<>();
                    for (CheckBox cb : prerequisitesListView.getItems()) {
                        if (cb.isSelected()) {
                            selectedPrerequisites.add(cb.getUserData().toString());
                        }
                    }
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("subjectName", subjectNameField.getText());
                    result.put("units", units);
                    result.put("department", departmentComboBox.getValue());
                    result.put("prerequisites", selectedPrerequisites.toArray(new String[0]));
                    return result;
                } catch (NumberFormatException e) {
                    showError("Units must be a number");
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (result != null) {
                JsonArray prerequisitesArray = new JsonArray();
                for (String prereq : (String[]) result.get("prerequisites")) {
                    prerequisitesArray.add(prereq);
                }
                
                addSubject(
                    result.get("subjectName").toString(),
                    (int) result.get("units"),
                    prerequisitesArray,
                    result.get("department") != null ? result.get("department").toString() : null
                );
            }
        });
    }    private void addSubject(String subjectName, int units, JsonArray prerequisites, String department) {
        Map<String, Object> subjectData = new HashMap<>();
        subjectData.put("subjectName", subjectName);
        subjectData.put("units", units);
        subjectData.put("prerequisites", prerequisites);
          // Only add department if it's not null or empty
        if (department != null && !department.trim().isEmpty()) {
            subjectData.put("department", department);
            System.out.println("Including department in request: " + department);
        } else {
            System.out.println("Department is null or empty, not including in request");
        }
        
        System.out.println("Sending subject data to API: " + subjectData);
          // Make a direct POST request to the API endpoint
        AuthService.makePostRequest("/admin/subjects", subjectData)
            .thenAccept(response -> {
                System.out.println("Subject API response: " + response);
                
                // Extract subject details from response if available
                String responseMessage = "Subject added successfully!";
                if (response.has("subject") && response.get("subject").isJsonObject()) {
                    JsonObject subjectObj = response.getAsJsonObject("subject");
                    if (subjectObj.has("edpCode")) {
                        responseMessage += " EDP Code: " + subjectObj.get("edpCode").getAsString();
                    }
                    System.out.println("Added subject with details: " + subjectObj);
                    
                    // Verify if department was saved correctly
                    if (subjectObj.has("department")) {
                        String savedDept = subjectObj.get("department").getAsString();
                        System.out.println("Server saved department as: " + savedDept);
                        if (department != null && !department.equals(savedDept)) {
                            System.out.println("WARNING: Department mismatch - sent: " + department + ", received: " + savedDept);
                        }
                    } else if (department != null && !department.trim().isEmpty()) {
                        System.out.println("WARNING: Department was sent but not saved by server");
                    }
                }
                
                final String finalMessage = responseMessage;
                
                Platform.runLater(() -> {
                    showInfo(finalMessage);
                    // Clear the cache and reload subjects from the server to get the server-generated EDP code
                    cachedSubjects.clear();
                    fetchAndDisplaySubjects(""); // Refresh the table
                });
            })
            .exceptionally(ex -> {
                System.err.println("Error adding subject: " + ex.getMessage());
                ex.printStackTrace();
                Platform.runLater(() -> showError("Failed to add subject: " + ex.getMessage()));
                return null;
            });
    }    // This method was replaced by showAssignSubjectsDialog(TeacherModel teacher) to support context menu
    // Left as a stub for backward compatibility
    @Deprecated
    @SuppressWarnings("unused")
    private void showAssignSubjectDialog() {
        // Show a message that this functionality has been moved to right-click context menu
        showInfo("Please right-click on a teacher to assign subjects.");
    }
      /* Old implementation removed - now using context menu approach */
    
    /**
     * Show the assign subjects dialog for a specific teacher
     * @param teacher The teacher to assign subjects to
     */
    public void showAssignSubjectsDialog(TeacherModel teacher) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assign Subjects to " + teacher.getName());
        dialog.setHeaderText("Select subjects to assign to " + teacher.getName());
        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Subject list with search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search subjects...");
        
        ObservableList<SubjectModel> allSubjects = FXCollections.observableArrayList();
        ObservableList<SubjectModel> filteredSubjects = FXCollections.observableArrayList();
        ListView<SubjectModel> subjectListView = new ListView<>(filteredSubjects);
        subjectListView.setPrefHeight(250);

        // Checklist logic
        Map<SubjectModel, BooleanProperty> checkedMap = new HashMap<>();
        subjectListView.setCellFactory(lv -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(e -> {
                    SubjectModel item = getItem();
                    if (item != null) {
                        checkedMap.get(item).set(checkBox.isSelected());
                    }
                });
            }
            @Override
            protected void updateItem(SubjectModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(item.getSubjectName() + " (" + item.getEdpCode() + ")");
                    checkBox.setSelected(checkedMap.getOrDefault(item, new SimpleBooleanProperty(false)).get());
                    setGraphic(checkBox);
                }
            }
        });

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredSubjects.clear();
            if (newValue == null || newValue.isEmpty()) {
                filteredSubjects.addAll(allSubjects);
            } else {
                String lowerCaseSearch = newValue.toLowerCase();
                for (SubjectModel subject : allSubjects) {
                    if (subject.getSubjectName().toLowerCase().contains(lowerCaseSearch) ||
                        subject.getEdpCode().toLowerCase().contains(lowerCaseSearch)) {
                        filteredSubjects.add(subject);
                    }
                }
            }
        });

        Button selectAllBtn = new Button("Select All");
        selectAllBtn.setOnAction(e -> {
            for (SubjectModel subj : filteredSubjects) {
                checkedMap.get(subj).set(true);
            }
            subjectListView.refresh();
        });

        Button clearAllBtn = new Button("Clear All");
        clearAllBtn.setOnAction(e -> {
            for (SubjectModel subj : filteredSubjects) {
                checkedMap.get(subj).set(false);
            }
            subjectListView.refresh();
        });
        
        // Create a HBox for the buttons
        HBox buttonBox = new HBox(10, selectAllBtn, clearAllBtn);

        grid.add(new Label("Search:"), 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(new Label("Subjects:"), 0, 1);
        grid.add(subjectListView, 1, 1);
        grid.add(buttonBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500); // Make dialog wider

        // Fetch subjects for the dialog
        AuthService.makeGetRequest("/admin/subjects").thenAccept(subjectResp -> {
            Platform.runLater(() -> {
                JsonArray subjectsArray = subjectResp.getAsJsonArray("subjects"); // Corrected variable name
                allSubjects.clear();
                filteredSubjects.clear();
                checkedMap.clear();
                for (var s : subjectsArray) { // Corrected variable name
                    JsonObject subjectJson = s.getAsJsonObject();
                    SubjectModel subjectModel = new SubjectModel(
                        subjectJson.get("edpCode").getAsString(),
                        subjectJson.get("subjectName").getAsString(),
                        subjectJson.get("units").getAsInt(),
                        subjectJson.has("department") ? subjectJson.get("department").getAsString() : "",
                        // Assuming prerequisites are not directly needed for display in this dialog,
                        // or fetched/handled differently. If they are, this part needs adjustment.
                        new String[0] 
                    );
                    allSubjects.add(subjectModel);
                    filteredSubjects.add(subjectModel); // Initially, all subjects are shown
                    checkedMap.put(subjectModel, new SimpleBooleanProperty(false));
                }
                subjectListView.refresh();
                
                // Pre-select already assigned subjects
                if (teacher.getAssignedSubjectIds() != null) {
                    for (String assignedId : teacher.getAssignedSubjectIds()) {
                        for (SubjectModel subj : allSubjects) {
                            if (subj.getEdpCode().equals(assignedId)) {
                                checkedMap.get(subj).set(true);
                                break;
                            }
                        }
                    }
                    subjectListView.refresh(); // Refresh to show pre-selections
                }
            });
        }).exceptionally(ex -> { // Added exceptionally block for error handling
            Platform.runLater(() -> {
                showError("Failed to load subjects: " + ex.getMessage());
                ex.printStackTrace();
            });
            return null;
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                // Get checked subjects
                var selectedSubjects = checkedMap.entrySet().stream()
                    .filter(e -> e.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                
                if (selectedSubjects.isEmpty()) {
                    // Optionally, inform the user that no subjects were selected or handle as needed.
                    // For now, we'll proceed, and the backend might handle it as "unassign all" or no-op.
                    // showInfo("No subjects selected."); 
                }                // Prepare data for API call
                List<String> selectedSubjectIds = selectedSubjects.stream()
                                                                .map(SubjectModel::getEdpCode)
                                                                .collect(Collectors.toList());                
                Map<String, Object> payload = new HashMap<>();
                payload.put("edpCodes", selectedSubjectIds); // Backend requires 'edpCodes' as the key (plural form)
                payload.put("teacherName", teacher.getName()); // Using teacherName as required by the API
                
                // Log the payload to debug the API request                
                System.out.println("DEBUG: Assign subjects payload: " + payload);
                System.out.println("DEBUG: edpCodes value type: " + (selectedSubjectIds.isEmpty() ? "empty list" : 
                                   selectedSubjectIds.getClass().getName() + ", first item type: " + 
                                   (selectedSubjectIds.isEmpty() ? "N/A" : selectedSubjectIds.get(0).getClass().getName())));
                System.out.println("DEBUG: teacherName value: '" + teacher.getName() + "'");
                
                // Make API call to assign subjects
                // Based on the API error message, the correct endpoint is /admin/assign-subjects
                AuthService.makePostRequest("/admin/assign-subjects", payload)
                    .thenAccept(response -> {
                        System.out.println("Assign subjects response: " + response);
                        Platform.runLater(() -> {
                        // Update the teacher model with the newly assigned subjects
                        teacher.clearAssignedSubjects(); // Clear existing assignments
                        for (String subjectId : selectedSubjectIds) {
                            teacher.assignSubjectById(subjectId);
                        }
                        
                        showInfo("Subjects assigned successfully to " + teacher.getName());
                        // Log updated assignments for debugging
                        System.out.println("Updated teacher " + teacher.getName() + " with " + 
                                         teacher.getAssignedSubjectIds().size() + " subject assignments");
                          // Refresh teacher data 
                        cachedTeachers.clear(); // Invalidate cache
                        fetchAndDisplayTeachers(searchField.getText()); // Refresh view
                    }); // <-- FIX: close Platform.runLater
                }) // <-- FIX: close thenAccept
                .exceptionally(ex -> {                    System.err.println("Error assigning subjects: " + ex.getMessage());
                    System.err.println("Attempted URL: /admin/assign-subjects");
                    System.err.println("Payload: " + payload);
                    Platform.runLater(() -> showError("Failed to assign subjects: " + ex.getMessage()));
                    ex.printStackTrace();
                    return null;
                });
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    // Using model classes from dedicated model package
    // See models/SubjectModel.java, models/TeacherModel.java, and models/StudentModel.java    
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    // Inside AdminController
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Logout.fxml"));
            Parent root = loader.load();

            // Find buttons by fx:id
            Button cancelBtn = (Button) root.lookup("#cancelBtn");
            Button logoutBtn = (Button) root.lookup("#logoutBtn");

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Logout");
            dialogStage.initOwner(logoutBtn.getScene().getWindow());
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            // Cancel action: just close the dialog
            if (cancelBtn != null) {
                cancelBtn.setOnAction(e -> dialogStage.close());
            }

            // Logout action: perform logout logic, then close dialog and go to login
            if (logoutBtn != null) {
                logoutBtn.setOnAction(e -> {
                    // TODO: Add your logout logic here (e.g., clear session)
                    try {
                        Parent loginView = FXMLLoader.load(getClass().getResource("LoginForm.fxml"));
                        Scene loginScene = new Scene(loginView, 450, 500);
                        Stage stage = (Stage) this.logoutBtn.getScene().getWindow();
                        stage.setScene(loginScene);
                        stage.setTitle("Login Form");
                        stage.setResizable(false);
                        stage.show();
                        stage.centerOnScreen();
                    } catch (Exception ex) {
                        showError("Logout error: " + ex.getMessage());
                    }
                    dialogStage.close();
                });
            }

            dialogStage.showAndWait();
        } catch (Exception e) {
            showError("Failed to open logout dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Pagination methods
    private void updatePagedItems() {
        // Clear current items
        pagedItems.clear();
        
        // Calculate start and end indices for current page
        int startIdx = currentPage * ROWS_PER_PAGE;
        int endIdx = Math.min(startIdx + ROWS_PER_PAGE, allFilteredItems.size());
        
        // Add items for current page
        if (startIdx < allFilteredItems.size()) {
            for (int i = startIdx; i < endIdx; i++) {
                pagedItems.add(allFilteredItems.get(i));
            }
        }
        
        // Update TableView
        mainTableView.setItems(pagedItems);
        
        // Update pagination info
        updatePaginationInfo();
          // Update button states
        prevPageButton.setDisable(currentPage == 0);
        
        // Calculate max page number (0-based index)
        int maxPage = (int) Math.ceil(allFilteredItems.size() / (double) ROWS_PER_PAGE) - 1;
        nextPageButton.setDisable(currentPage >= maxPage);
    }
    
    private void updatePaginationInfo() {
        int totalItems = allFilteredItems.size();
        int startItem = totalItems == 0 ? 0 : currentPage * ROWS_PER_PAGE + 1;
        int endItem = Math.min((currentPage + 1) * ROWS_PER_PAGE, totalItems);

        if (paginationInfoLabel != null) { // Add null check
            paginationInfoLabel.setText(String.format("%d-%d of %d", startItem, endItem, totalItems));
        } else {
            System.err.println("paginationInfoLabel is null, cannot update text.");
        }
    }

    public void showUpdateSubjectDialog(SubjectModel subject) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Update Subject");
        dialog.setHeaderText("Edit subject details for: " + subject.getSubjectName() + " (" + subject.getEdpCode() + ")");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Subject name field
        TextField subjectNameField = new TextField(subject.getSubjectName());
        subjectNameField.setPromptText("Subject name");

        // Units field
        TextField unitsField = new TextField(String.valueOf(subject.getUnits()));
        unitsField.setPromptText("Units");

        // Department dropdown
        ComboBox<String> departmentComboBox = new ComboBox<>();
        departmentComboBox.setPromptText("Select department");
        departmentComboBox.setEditable(true);
        departmentComboBox.getItems().addAll(
            "BSCS", "BSIT", "BSBA", "BSN", "BSMT", "BSTM"
        );
        if (subject.getDepartment() != null && !subject.getDepartment().isEmpty()) {
            departmentComboBox.setValue(subject.getDepartment());
        }

        // Prerequisites multi-select ListView
        ListView<CheckBox> prerequisitesListView = new ListView<>();
        prerequisitesListView.setPrefHeight(150);
        ObservableList<CheckBox> checkBoxList = FXCollections.observableArrayList();
        List<String> currentPrerequisites = subject.getPrerequisites() != null ?
                                            subject.getPrerequisites() : // Corrected this line
                                            java.util.Collections.emptyList();

        for (SubjectModel s : cachedSubjects) {
            if (s.getEdpCode().equals(subject.getEdpCode())) continue; // Cannot be a prerequisite of itself
            CheckBox cb = new CheckBox(s.getSubjectName() + " (" + s.getEdpCode() + ")");
            cb.setUserData(s.getEdpCode());
            if (currentPrerequisites.contains(s.getEdpCode())) {
                cb.setSelected(true);
            }
            checkBoxList.add(cb);
        }
        FXCollections.sort(checkBoxList, (cb1, cb2) -> cb1.getText().compareTo(cb2.getText()));
        prerequisitesListView.setItems(checkBoxList);

        grid.add(new Label("Subject Name:"), 0, 0);
        grid.add(subjectNameField, 1, 0);
        grid.add(new Label("Units:"), 0, 1);
        grid.add(unitsField, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(departmentComboBox, 1, 2);
        grid.add(new Label("Prerequisites:"), 0, 3);
        grid.add(prerequisitesListView, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        Platform.runLater(subjectNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int units = Integer.parseInt(unitsField.getText());
                    List<String> selectedPrerequisites = new java.util.ArrayList<>();
                    for (CheckBox itemCb : prerequisitesListView.getItems()) { // Renamed 'cb' to 'itemCb'
                        if (itemCb.isSelected()) {
                            selectedPrerequisites.add(itemCb.getUserData().toString());
                        }
                    }
                    Map<String, Object> data = new HashMap<>(); // Renamed 'result' to 'data'
                    data.put("subjectName", subjectNameField.getText());
                    data.put("units", units);
                    data.put("department", departmentComboBox.getValue());
                    data.put("prerequisites", selectedPrerequisites.toArray(new String[0]));
                    return data;
                } catch (NumberFormatException e) {
                    showError("Units must be a number");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> { // Renamed 'result' to 'data'
            if (data != null) {
                updateSubject(subject.getEdpCode(), data);
            }
        });
    }

    private void updateSubject(String edpCode, Map<String, Object> subjectDataMap) {
        // Prepare payload for API
        JsonObject payload = new JsonObject();
        payload.addProperty("subjectName", (String) subjectDataMap.get("subjectName"));
        payload.addProperty("units", (Integer) subjectDataMap.get("units"));
        
        String department = (String) subjectDataMap.get("department");
        if (department != null && !department.trim().isEmpty()) {
            payload.addProperty("department", department);
        }

        JsonArray prerequisitesArray = new JsonArray();
        String[] prerequisites = (String[]) subjectDataMap.get("prerequisites");
        if (prerequisites != null) {
            for (String prereq : prerequisites) {
                prerequisitesArray.add(prereq);
            }
        }
        payload.add("prerequisites", prerequisitesArray);

        AuthService.makePutRequest("/admin/subjects/" + edpCode, payload)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    showInfo("Subject updated successfully!");
                    cachedSubjects.clear();
                    fetchAndDisplaySubjects(""); // Refresh list
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Failed to update subject: " + ex.getMessage()));
                ex.printStackTrace();
                return null;
            });
    }
    
    public void showDeleteSubjectConfirmation(SubjectModel subject) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Are you sure you want to delete this subject?");
        confirmDialog.setContentText("Subject: " + subject.getSubjectName() + " (" + subject.getEdpCode() + ")");

        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(deleteButton, cancelButton);

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                deleteSubject(subject.getEdpCode());
            }
        });
    }

    private void deleteSubject(String edpCode) {
        AuthService.makeDeleteRequest("/admin/subjects/" + edpCode)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    showInfo("Subject deleted successfully!");
                    cachedSubjects.clear();
                    fetchAndDisplaySubjects(""); // Refresh list
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Failed to delete subject: " + ex.getMessage()));
                ex.printStackTrace();
                return null;
            });
    }
    
    /**
     * Show a dialog with all subjects assigned to a specific teacher
     * @param teacher The teacher to view assigned subjects for
     */
    public void showTeacherAssignedSubjects(TeacherModel teacher) {
        // Create a dialog to display the subjects
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Subjects Assigned to " + teacher.getName());
        dialog.setHeaderText("Viewing subjects assigned to " + teacher.getName());
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        
        // Create layout grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create table view for subjects
        TableView<SubjectModel> subjectsTableView = new TableView<>();
        subjectsTableView.setPrefWidth(500);
        subjectsTableView.setPrefHeight(300);
        
        // Add columns to the table
        TableColumn<SubjectModel, String> codeCol = new TableColumn<>("Subject Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("edpCode"));
        codeCol.setPrefWidth(100);
        
        TableColumn<SubjectModel, String> nameCol = new TableColumn<>("Subject Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        nameCol.setPrefWidth(200);
        
        TableColumn<SubjectModel, Integer> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        unitsCol.setPrefWidth(60);
        
        TableColumn<SubjectModel, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        deptCol.setPrefWidth(100);
          
        // Add columns to the table view
        @SuppressWarnings("unchecked")
        TableColumn<SubjectModel, ?>[] columns = new TableColumn[] {codeCol, nameCol, unitsCol, deptCol};
        subjectsTableView.getColumns().addAll(columns);
        
        // Add a loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(true);
        
        // Add a status label
        Label statusLabel = new Label("Loading subjects...");
        
        // Create a container for the progress indicator
        HBox loadingBox = new HBox(10, progressIndicator, statusLabel);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Add components to the grid
        grid.add(loadingBox, 0, 0);
        grid.add(subjectsTableView, 0, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Make API call to get the teacher's subjects
        String teacherName = teacher.getName();
        
        // Log for debugging
        System.out.println("Getting subjects for teacher: " + teacherName);
        if (teacher.getAssignedSubjectIds() != null) {
            System.out.println("Teacher has " + teacher.getAssignedSubjectIds().size() + " subject IDs stored locally");
        }
        
        // Show the dialog before making the API call
        Platform.runLater(() -> {
            dialog.show();
            
            // Make API call to get subjects for this teacher using the updated endpoint format
            AuthService.makeGetRequest("/admin/teachers/" + teacherName + "/subjects")
                .thenAccept(response -> Platform.runLater(() -> {
                    // Hide loading indicator
                    loadingBox.setVisible(false);
                    
                    // Debug the response
                    System.out.println("Teacher subjects response: " + response);
                    
                    // Process the response - using the new API format
                    if (response != null && response.has("assignedSubjects") && response.get("assignedSubjects").isJsonArray()) {
                        JsonArray subjectsArray = response.getAsJsonArray("assignedSubjects");
                        
                        System.out.println("API returned " + subjectsArray.size() + " subjects for teacher " + teacherName);
                        
                        if (subjectsArray.size() == 0) {
                            statusLabel.setText("No subjects assigned to this teacher.");
                            statusLabel.setVisible(true);
                        } else {
                            // Create an observable list to store the subjects
                            ObservableList<SubjectModel> subjectsList = FXCollections.observableArrayList();
                            
                            // Process each subject in the array with the new format
                            for (var i = 0; i < subjectsArray.size(); i++) {
                                try {
                                    JsonObject subject = subjectsArray.get(i).getAsJsonObject();
                                    
                                    // Extract the fields according to the updated API response
                                    String edpCode = subject.has("edpCode") ? subject.get("edpCode").getAsString() : "";
                                    String name = subject.has("name") ? subject.get("name").getAsString() : "Unknown Subject";
                                    int units = subject.has("units") ? subject.get("units").getAsInt() : 0;
                                    String department = subject.has("department") ? subject.get("department").getAsString() : "";
                                    
                                    // Create a subject model and add to the list
                                    SubjectModel subjectModel = new SubjectModel(
                                        edpCode,
                                        name,
                                        units,
                                        department,
                                        new String[0] // No prerequisites information in this view
                                    );
                                    
                                    subjectsList.add(subjectModel);
                                    System.out.println("Added subject: " + name + " (" + edpCode + ")");
                                } catch (Exception e) {
                                    System.err.println("Error processing subject at index " + i + ": " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                            
                            // Update the table view
                            subjectsTableView.setItems(subjectsList);
                        }
                    } else {
                        // No data or error from API
                        statusLabel.setText("No subjects found or error in API response");
                        statusLabel.setVisible(true);
                        System.err.println("Invalid response format: missing 'assignedSubjects' array");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingBox.setVisible(false);
                        statusLabel.setText("Error: " + ex.getMessage());
                        statusLabel.setVisible(true);
                        
                        System.err.println("API error when fetching teacher subjects: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                    return null;
                });
        });
    }
      // Helper method to reset and set up the context menu
    private void applyContextMenu(ViewType viewType) {
        try {
            System.out.println("Applying context menu for view type: " + viewType);
            
            // First clear any existing row factory
            System.out.println("Clearing existing row factory...");
            mainTableView.setRowFactory(null);
            
            // Apply the appropriate context menu based on view type
            System.out.println("Setting up context menu based on view type: " + viewType);
            switch (viewType) {
                case SUBJECTS:
                    System.out.println("Applying SetupSubjectContextMenu...");
                    SetupSubjectContextMenu.apply(this, mainTableView);
                    break;
                case TEACHERS:
                    System.out.println("Applying SetupTeacherContextMenu...");
                    SetupTeacherContextMenu.apply(this, mainTableView);
                    break;
                case STUDENTS:
                    System.out.println("No context menu for students view yet");
                    // No context menu for students yet
                    break;
            }
            
            System.out.println("Context menu successfully applied for " + viewType);
        } catch (Exception e) {
            System.err.println("ERROR in applyContextMenu: " + e.getMessage());
            e.printStackTrace();
            showError("Error applying context menu: " + e.getMessage());
        }
    }

    // Fallback method to display teacher's subjects from locally stored IDs
    private void displayAssignedSubjectsFromLocalData(TeacherModel teacher, TableView<SubjectModel> tableView, Label statusLabel) {
        List<String> subjectIds = teacher.getAssignedSubjectIds();
        if (subjectIds == null || subjectIds.isEmpty()) {
            statusLabel.setText("No subjects assigned to this teacher.");
            statusLabel.setVisible(true);
            return;
        }

        System.out.println("Attempting to display " + subjectIds.size() + " locally stored subject IDs");

        // Create an observable list for subjects
        ObservableList<SubjectModel> subjectsList = FXCollections.observableArrayList();

        // Use a counter to track how many subjects we've processed
        final int[] processedCount = {0};

        // For each subject ID, fetch the subject details
        for (String subjectId : subjectIds) {
            // Create a temporary placeholder
            SubjectModel placeholder = new SubjectModel(
                    subjectId,
                    "Loading subject...",
                    0,
                    "",
                    new String[0]
            );
            subjectsList.add(placeholder);

            // Make API call to get subject details - use all-subjects instead
            final String finalSubjectId = subjectId; // Create final copy for lambda
            AuthService.makeGetRequest("/admin/all-subjects")
                    .thenAccept(subjectResponse -> Platform.runLater(() -> {
                        processedCount[0]++;
                        try {
                            System.out.println("Got all-subjects response: " + subjectResponse);
                            if (subjectResponse != null && !subjectResponse.isJsonNull() && subjectResponse.has("subjects")) {
                                // Find matching subject in the array by ID
                                JsonObject matchedSubject = null;
                                JsonArray allSubjects = subjectResponse.getAsJsonArray("subjects");

                                System.out.println("Looking for subject ID " + finalSubjectId + " in " + allSubjects.size() + " subjects");
                                for (var i = 0; i < allSubjects.size(); i++) {
                                    JsonObject subject = allSubjects.get(i).getAsJsonObject();
                                    // Log all the IDs for debugging
                                    String edpCode = subject.has("edpCode") ? subject.get("edpCode").getAsString() : "n/a";
                                    String subjId = subject.has("id") ? subject.get("id").getAsString() : "n/a";
                                    String _id = subject.has("_id") ? subject.get("_id").getAsString() : "n/a";
                                    System.out.println("Subject " + i + ": edpCode=" + edpCode + ", id=" + subjId + ", _id=" + _id);

                                    if ((subject.has("edpCode") && subject.get("edpCode").getAsString().equals(finalSubjectId)) ||
                                            (subject.has("id") && subject.get("id").getAsString().equals(finalSubjectId)) ||
                                            (subject.has("_id") && subject.get("_id").getAsString().equals(finalSubjectId))) {
                                        matchedSubject = subject;
                                        System.out.println("Found matching subject: " + subject);
                                        break;
                                    }
                                }

                                if (matchedSubject != null) {
                                    JsonObject subjectJson = matchedSubject;

                                    // Find the placeholder in the list
                                    int index = -1;
                                    for (int i = 0; i < subjectsList.size(); i++) {
                                        if (subjectsList.get(i).getEdpCode().equals(finalSubjectId)) {
                                            index = i;
                                            break;
                                        }
                                    }

                                    if (index >= 0) {
                                        // Replace the placeholder with real data - updated for new API format
                                        String edpCode = finalSubjectId;
                                        if (subjectJson.has("edpCode")) {
                                            edpCode = subjectJson.get("edpCode").getAsString();
                                        }

                                        String subjectName = "Unknown Subject";
                                        if (subjectJson.has("name")) {
                                            subjectName = subjectJson.get("name").getAsString();
                                        } else if (subjectJson.has("subjectName")) {
                                            subjectName = subjectJson.get("subjectName").getAsString();
                                        }

                                        SubjectModel updatedSubject = new SubjectModel(
                                                edpCode,
                                                subjectName,
                                                subjectJson.has("units") ? subjectJson.get("units").getAsInt() : 0,
                                                subjectJson.has("department") ? subjectJson.get("department").getAsString() : "",
                                                new String[0]
                                        );
                                        subjectsList.set(index, updatedSubject);
                                        System.out.println("Updated subject placeholder: " + updatedSubject.getSubjectName());
                                    }
                                } else {
                                    System.out.println("Subject ID " + finalSubjectId + " not found in all-subjects response");
                                    // Update the placeholder with a "Not Found" message
                                    for (int i = 0; i < subjectsList.size(); i++) {
                                        if (subjectsList.get(i).getEdpCode().equals(finalSubjectId)) {
                                            SubjectModel notFoundPlaceholder = new SubjectModel(
                                                    finalSubjectId,
                                                    "Subject Not Found",
                                                    0,
                                                    "",
                                                    new String[0]
                                            );
                                            subjectsList.set(i, notFoundPlaceholder);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                System.out.println("Failed to get details for subject ID: " + finalSubjectId);
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing subject response: " + e.getMessage());
                            e.printStackTrace();
                        }

                        // If all subjects have been processed, refresh the table
                        if (processedCount[0] >= subjectIds.size()) {
                            tableView.refresh();
                            if (subjectsList.isEmpty()) {
                                statusLabel.setText("No subjects could be loaded for this teacher.");
                                statusLabel.setVisible(true);
                            }
                        }
                    }))
                    .exceptionally(ex -> {
                        processedCount[0]++;
                        System.err.println("Failed to fetch subject " + finalSubjectId + ": " + ex.getMessage());
                        System.err.println("Attempting to use cached subject data if available");

                        // Try to find the subject in the cached subjects
                        for (SubjectModel cachedSubject : cachedSubjects) {
                            if (cachedSubject.getEdpCode().equals(finalSubjectId)) {
                                System.out.println("Found subject " + finalSubjectId + " in cached subjects: " + cachedSubject.getSubjectName());
                                // Find the placeholder in the list
                                for (int i = 0; i < subjectsList.size(); i++) {
                                    if (subjectsList.get(i).getEdpCode().equals(finalSubjectId)) {
                                        final int finalI = i;  // Create a final copy of i for the lambda
                                        Platform.runLater(() -> {
                                            subjectsList.set(finalI, cachedSubject);
                                            tableView.refresh();
                                        });
                                        break;
                                    }
                                }
                                break;
                            }
                        }

                        return null;
                    });
        }


        // Set the items in the table
        tableView.setItems(subjectsList);

        // Show a message if we're fetching data
        if (!subjectIds.isEmpty()) {
            statusLabel.setText("Loading subject details...");
            statusLabel.setVisible(true);
        }
    }    /**
     * Handles the refresh button click event.
     * This method resets the cache and fetches fresh data based on the current view.
     */    private void handleRefresh() {
        try {
            System.out.println("Refreshing current view: " + currentView);
            
            // Disable refresh button to avoid multiple clicks
            Platform.runLater(() -> {
                refreshButton.setDisable(true);
                refreshButton.setText("Refreshing...");
            });
            
            // Show loading indicator
            System.out.println("Setting loading indicator...");
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setMaxSize(50, 50);
            mainTableView.setPlaceholder(progressIndicator);
            
            // Clear cached data based on current view
            System.out.println("Clearing cache for view: " + currentView);
            switch (currentView) {
                case SUBJECTS:
                    System.out.println("Clearing subjects cache...");
                    cachedSubjects.clear();
                    System.out.println("Fetching subjects...");
                    fetchAndDisplaySubjects("");
                    break;
                case TEACHERS:
                    System.out.println("Clearing teachers cache...");
                    cachedTeachers.clear();
                    System.out.println("Fetching teachers...");
                    fetchAndDisplayTeachers("");
                    break;
                case STUDENTS:
                    System.out.println("Clearing students cache...");
                    cachedStudents.clear();
                    System.out.println("Fetching students...");
                    fetchAndDisplayStudents("");
                    break;
            }
            
            // Reset pagination
            System.out.println("Resetting pagination...");
            currentPage = 0;
            updatePaginationInfo();
            
            // Re-enable refresh button with slight delay for visual feedback
            new Thread(() -> {
                try {
                    // Small delay to make the refresh action visible to users
                    Thread.sleep(800);
                    Platform.runLater(() -> {
                        refreshButton.setDisable(false);
                        refreshButton.setText("Refresh");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            System.out.println("Refresh completed for view: " + currentView);
        } catch (Exception e) {
            System.err.println("ERROR in handleRefresh: " + e.getMessage());
            e.printStackTrace();
            // Make sure button is re-enabled in case of error
            Platform.runLater(() -> {
                refreshButton.setDisable(false);
                refreshButton.setText("Refresh");
            });
            showError("Error refreshing view: " + e.getMessage());
        }
    }
}

