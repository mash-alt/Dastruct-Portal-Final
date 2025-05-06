package org.finalproject.loginregisterfx;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import org.finalproject.loginregisterfx.Service.AuthService;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.stream.Collectors;

public class AdminController {
    @FXML private TableView<Object> mainTableView;
    @FXML private TextField searchField;
    @FXML private Button viewSubjectsBtn;
    @FXML private Button viewTeachersBtn;
    @FXML private Button viewStudentsBtn;
    @FXML private Label adminNameLabel;
    @FXML private Button logoutBtn;
    @FXML private ComboBox<Integer> rowsPerPageComboBox;
    @FXML private Label viewTitleLabel;
    @FXML private Label listTitleLabel;
    @FXML private Button addButton;
    
    private enum ViewType { SUBJECTS, TEACHERS, STUDENTS }
    private ViewType currentView = ViewType.SUBJECTS;

    @FXML
    private void initialize() {
        // Set up initial view
        setupEventHandlers();
        showSubjectsView();
        
        // Initialize rows per page combo box
        rowsPerPageComboBox.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        rowsPerPageComboBox.setValue(10);
        rowsPerPageComboBox.setOnAction(e -> refreshCurrentView());

        // Add button event handler
        addButton.setOnAction(e -> showAddDialog());

        logoutBtn.setOnAction(e -> handleLogout());
    }

    private void setupEventHandlers() {
        viewSubjectsBtn.setOnAction(e -> showSubjectsView());
        viewTeachersBtn.setOnAction(e -> showTeachersView());
        viewStudentsBtn.setOnAction(e -> showStudentsView());
        searchField.setOnAction(e -> handleSearch());
    }

    private void showSubjectsView() {
        currentView = ViewType.SUBJECTS;
        setupSubjectColumns();
        fetchAndDisplaySubjects("");
        updateHeaderTitle("Subjects Management");
        listTitleLabel.setText("Subject List");
        searchField.setPromptText("Search subjects...");
        addButton.setText("Add Subject");
        addButton.setVisible(true); // Show the add button in Subjects view
        listTitleLabel.setStyle(""); // Remove highlight
    }

    private void showTeachersView() {
        currentView = ViewType.TEACHERS;
        setupTeacherColumns();
        fetchAndDisplayTeachers("");
        updateHeaderTitle("Teachers Management");
        listTitleLabel.setText("Teacher List");
        searchField.setPromptText("Search teachers...");
        addButton.setText("Assign Subject");
        addButton.setVisible(true); // Show the add button in Teachers view
        listTitleLabel.setStyle(""); // Remove highlight
    }

    private void showStudentsView() {
        currentView = ViewType.STUDENTS;
        setupStudentColumns();
        fetchAndDisplayStudents("");
        updateHeaderTitle("Students Management");
        listTitleLabel.setText("Student List");
        searchField.setPromptText("Search students...");
        addButton.setVisible(false); // Hide the add button in Students view
        listTitleLabel.setStyle(""); // Remove highlight
    }

    private void updateHeaderTitle(String title) {
        if (viewTitleLabel != null) {
            viewTitleLabel.setText(title);
        }
    }

    private void handleSearch() {
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
    }

    private void refreshCurrentView() {
        handleSearch(); // This will refresh with current search term
    }

    private void setupSubjectColumns() {
        mainTableView.getColumns().clear();
        TableColumn<Object, String> codeCol = new TableColumn<>("Subject Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("edpCode"));
        TableColumn<Object, String> nameCol = new TableColumn<>("Subject Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        TableColumn<Object, Integer> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        TableColumn<Object, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        TableColumn<Object, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        mainTableView.getColumns().addAll(codeCol, nameCol, unitsCol, deptCol, statusCol);
    }

    private void setupTeacherColumns() {
        mainTableView.getColumns().clear();
        TableColumn<Object, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Object, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Object, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Object, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        mainTableView.getColumns().addAll(nameCol, emailCol, phoneCol, deptCol);
    }

    private void setupStudentColumns() {
        mainTableView.getColumns().clear();
        TableColumn<Object, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Object, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Object, String> yearCol = new TableColumn<>("Year Level");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("yearLevel"));
        TableColumn<Object, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("course"));
        mainTableView.getColumns().addAll(nameCol, emailCol, yearCol, courseCol);
    }

    // Caches for client-side search
    private ObservableList<SubjectModel> cachedSubjects = FXCollections.observableArrayList();
    private ObservableList<TeacherModel> cachedTeachers = FXCollections.observableArrayList();
    private ObservableList<StudentModel> cachedStudents = FXCollections.observableArrayList();

    private void fetchAndDisplaySubjects(String search) {
        if (cachedSubjects.isEmpty() || (search == null && mainTableView.getItems().isEmpty())) {
            // Fetch from backend and cache
            AuthService.makeGetRequest("/admin/subjects").thenAccept(response -> {
                Platform.runLater(() -> {
                    JsonArray subjects = response.getAsJsonArray("subjects");
                    cachedSubjects.clear();
                    for (var subj : subjects) {
                        cachedSubjects.add(new SubjectModel(subj.getAsJsonObject()));
                    }
                    filterAndDisplaySubjects(search);
                });
            }).exceptionally(ex -> {
                showError("Failed to fetch subjects: " + ex.getMessage());
                return null;
            });
        } else {
            filterAndDisplaySubjects(search);
        }
    }

    private void filterAndDisplaySubjects(String search) {
        ObservableList<Object> filtered = FXCollections.observableArrayList();
        if (search == null || search.isEmpty()) {
            filtered.addAll(cachedSubjects);
        } else {
            String lower = search.toLowerCase();
            for (SubjectModel s : cachedSubjects) {
                if (s.getEdpCode().toLowerCase().contains(lower) ||
                    s.getSubjectName().toLowerCase().contains(lower) ||
                    s.getDepartment().toLowerCase().contains(lower)) {
                    filtered.add(s);
                }
            }
        }
        mainTableView.setItems(filtered);
    }

    private void fetchAndDisplayTeachers(String search) {
        if (cachedTeachers.isEmpty() || (search == null && mainTableView.getItems().isEmpty())) {
            AuthService.makeGetRequest("/admin/teachers").thenAccept(response -> {
                Platform.runLater(() -> {
                    JsonArray teachers = response.getAsJsonArray("teachers");
                    cachedTeachers.clear();
                    for (var t : teachers) {
                        cachedTeachers.add(new TeacherModel(t.getAsJsonObject()));
                    }
                    filterAndDisplayTeachers(search);
                });
            }).exceptionally(ex -> {
                showError("Failed to fetch teachers: " + ex.getMessage());
                return null;
            });
        } else {
            filterAndDisplayTeachers(search);
        }
    }

    private void filterAndDisplayTeachers(String search) {
        ObservableList<Object> filtered = FXCollections.observableArrayList();
        if (search == null || search.isEmpty()) {
            filtered.addAll(cachedTeachers);
        } else {
            String lower = search.toLowerCase();
            for (TeacherModel t : cachedTeachers) {
                if (t.getName().toLowerCase().contains(lower) ||
                    t.getEmail().toLowerCase().contains(lower) ||
                    t.getDepartment().toLowerCase().contains(lower)) {
                    filtered.add(t);
                }
            }
        }
        mainTableView.setItems(filtered);
    }

    private void fetchAndDisplayStudents(String search) {
        if (cachedStudents.isEmpty() || (search == null && mainTableView.getItems().isEmpty())) {
            AuthService.makeGetRequest("/admin/students").thenAccept(response -> {
                Platform.runLater(() -> {
                    JsonArray students = response.getAsJsonArray("students");
                    cachedStudents.clear();
                    for (var s : students) {
                        cachedStudents.add(new StudentModel(s.getAsJsonObject()));
                    }
                    filterAndDisplayStudents(search);
                });
            }).exceptionally(ex -> {
                showError("Failed to fetch students: " + ex.getMessage());
                return null;
            });
        } else {
            filterAndDisplayStudents(search);
        }
    }

    private void filterAndDisplayStudents(String search) {
        ObservableList<Object> filtered = FXCollections.observableArrayList();
        if (search == null || search.isEmpty()) {
            filtered.addAll(cachedStudents);
        } else {
            String lower = search.toLowerCase();
            for (StudentModel s : cachedStudents) {
                if (s.getName().toLowerCase().contains(lower) ||
                    s.getEmail().toLowerCase().contains(lower) ||
                    s.getYearLevel().toLowerCase().contains(lower) ||
                    s.getCourse().toLowerCase().contains(lower)) {
                    filtered.add(s);
                }
            }
        }
        mainTableView.setItems(filtered);
    }

    private void showAddDialog() {
        switch (currentView) {
            case SUBJECTS:
                showAddSubjectDialog();
                break;
            case TEACHERS:
                showAssignSubjectDialog();
                break;
            case STUDENTS:
                // TODO: Implement add student dialog
                break;
        }
    }

    private void showAddSubjectDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Add New Subject");
        dialog.setHeaderText("Enter subject details");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField subjectNameField = new TextField();
        subjectNameField.setPromptText("Subject name");
        TextField unitsField = new TextField();
        unitsField.setPromptText("Units");
        TextField prerequisitesField = new TextField();
        prerequisitesField.setPromptText("Prerequisites (comma-separated)");
        grid.add(new Label("Subject Name:"), 0, 0);
        grid.add(subjectNameField, 1, 0);
        grid.add(new Label("Units:"), 0, 1);
        grid.add(unitsField, 1, 1);
        grid.add(new Label("Prerequisites:"), 0, 2);
        grid.add(prerequisitesField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(subjectNameField::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int units = Integer.parseInt(unitsField.getText());
                    String[] prerequisites = prerequisitesField.getText().isEmpty() ? new String[0] : prerequisitesField.getText().split(",");
                    Map<String, Object> result = new HashMap<>();
                    result.put("subjectName", subjectNameField.getText());
                    result.put("units", units);
                    result.put("prerequisites", prerequisites);
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
                    prerequisitesArray.add(prereq.trim());
                }
                addSubject(
                    result.get("subjectName").toString(),
                    (int) result.get("units"),
                    prerequisitesArray
                );
            }
        });
    }

    private void addSubject(String subjectName, int units, JsonArray prerequisites) {
        Map<String, Object> subjectData = new HashMap<>();
        subjectData.put("subjectName", subjectName);
        subjectData.put("units", units);
        subjectData.put("prerequisites", prerequisites);

        AuthService.makePostRequest("/admin/subjects", subjectData)
            .thenAccept(response -> {
                Platform.runLater(() -> {
                    showInfo("Subject added successfully!");
                    cachedSubjects.clear();
                    fetchAndDisplaySubjects(""); // Refresh the table
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Failed to add subject: " + ex.getMessage()));
                return null;
            });
    }

    private void showAssignSubjectDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assign Subject to Teacher");
        dialog.setHeaderText("Select a teacher and assign subjects");
        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<TeacherModel> teacherComboBox = new ComboBox<>();
        teacherComboBox.setPromptText("Select Teacher");
        ObservableList<SubjectModel> subjectList = FXCollections.observableArrayList();
        ListView<SubjectModel> subjectListView = new ListView<>(subjectList);
        subjectListView.setPrefHeight(150);

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
                    setText(item.getSubjectName());
                    checkBox.setSelected(checkedMap.getOrDefault(item, new SimpleBooleanProperty(false)).get());
                    setGraphic(checkBox);
                }
            }
        });

        Button selectAllBtn = new Button("Select All");
        selectAllBtn.setOnAction(e -> {
            for (SubjectModel subj : subjectList) {
                checkedMap.get(subj).set(true);
            }
            subjectListView.refresh();
        });

        grid.add(new Label("Teacher:"), 0, 0);
        grid.add(teacherComboBox, 1, 0);
        grid.add(new Label("Subjects:"), 0, 1);
        grid.add(subjectListView, 1, 1);
        grid.add(selectAllBtn, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Fetch teachers and subjects for the dialog
        AuthService.makeGetRequest("/admin/teachers").thenAccept(teacherResp -> {
            Platform.runLater(() -> {
                JsonArray teachers = teacherResp.getAsJsonArray("teachers");
                ObservableList<TeacherModel> teacherList = FXCollections.observableArrayList();
                teachers.forEach(t -> teacherList.add(new TeacherModel(t.getAsJsonObject())));
                teacherComboBox.setItems(teacherList);
            });
        });
        AuthService.makeGetRequest("/admin/subjects").thenAccept(subjectResp -> {
            Platform.runLater(() -> {
                JsonArray subjects = subjectResp.getAsJsonArray("subjects");
                subjectList.clear();
                checkedMap.clear();
                for (var s : subjects) {
                    SubjectModel model = new SubjectModel(s.getAsJsonObject());
                    subjectList.add(model);
                    checkedMap.put(model, new SimpleBooleanProperty(false));
                }
                subjectListView.refresh();
            });
        });

        teacherComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(TeacherModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        teacherComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TeacherModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                TeacherModel selectedTeacher = teacherComboBox.getValue();
                // Get checked subjects
                var selectedSubjects = checkedMap.entrySet().stream()
                    .filter(e -> e.getValue().get())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                if (selectedTeacher == null || selectedSubjects.isEmpty()) {
                    showError("Please select a teacher and at least one subject.");
                    return null;
                }
                // Prepare JSON for backend (edpCodes and teacherName)
                JsonArray edpCodes = new JsonArray();
                for (SubjectModel subj : selectedSubjects) {
                    edpCodes.add(subj.getEdpCode());
                }
                JsonObject payload = new JsonObject();
                payload.add("edpCodes", edpCodes);
                payload.addProperty("teacherName", selectedTeacher.getName());
                AuthService.makePostRequest("/admin/assign-subjects", payload).thenAccept(resp -> {
                    Platform.runLater(() -> {
                        showInfo("Subjects assigned successfully!");
                        // Clear all checkboxes after successful assignment
                        checkedMap.values().forEach(prop -> prop.set(false));
                        subjectListView.refresh();
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Failed to assign subjects: " + ex.getMessage());
                        showError("Failed to assign subjects: " + ex.getMessage());
                    });
                    return null;
                });
            }
            return null;
        });
        dialog.showAndWait();
    }

    // Model classes for table data
    public static class SubjectModel {
        private String edpCode;
        private String subjectName;
        private int units;
        private String department;
        private String status;

        public SubjectModel(JsonObject json) {
            this.edpCode = json.get("edpCode").getAsString();
            this.subjectName = json.get("subjectName").getAsString();
            this.units = json.get("units").getAsInt();
            this.department = json.has("department") ? json.get("department").getAsString() : "";
            this.status = json.has("status") ? json.get("status").getAsString() : "Active";
        }

        // Getters
        public String getEdpCode() { return edpCode; }
        public String getSubjectName() { return subjectName; }
        public int getUnits() { return units; }
        public String getDepartment() { return department; }
        public String getStatus() { return status; }
    }

    public static class TeacherModel {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String department;

        public TeacherModel(JsonObject json) {
            this.id = json.has("_id") ? json.get("_id").getAsString() : "";
            this.name = json.get("name").getAsString();
            this.email = json.get("email").getAsString();
            this.phone = json.has("phone") ? json.get("phone").getAsString() : "";
            this.department = json.has("department") ? json.get("department").getAsString() : "";
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getDepartment() { return department; }
        @Override public String toString() { return name; }
    }

    public static class StudentModel {
        private String name;
        private String email;
        private String yearLevel;
        private String course;

        public StudentModel(JsonObject json) {
            this.name = json.get("name").getAsString();
            this.email = json.get("email").getAsString();
            this.yearLevel = json.has("yearLevel") ? json.get("yearLevel").getAsString() : "";
            this.course = json.has("course") ? json.get("course").getAsString() : "";
        }

        // Getters
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getYearLevel() { return yearLevel; }
        public String getCourse() { return course; }
    }

    private void showError(String message) {
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

    private void handleLogout() {
        AuthService.logout().thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    // Return to login screen
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("LoginForm.fxml"));
                    javafx.scene.Parent loginView = loader.load();
                    javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
                    stage.setScene(new javafx.scene.Scene(loginView, 450, 500));
                    stage.setTitle("Login Form");
                    stage.setResizable(false);
                    stage.show();
                    stage.centerOnScreen();
                } catch (Exception ex) {
                    showError("Failed to return to login screen: " + ex.getMessage());
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showError("Logout failed: " + ex.getMessage()));
            return null;
        });
    }
}
