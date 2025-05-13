package org.finalproject.loginregisterfx;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
// Removed unused import: import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.LogoutDialogController;

// Removed unused import: import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DashboardController {
    @FXML private Label adminNameLabel;    @FXML private Button logoutBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button viewSubjectsBtn;
    @FXML private Button viewTeachersBtn;
    @FXML private Button viewStudentsBtn;
    @FXML private Button refreshButton;
    
    // Statistics labels
    @FXML private Label subjectsCountLabel;
    @FXML private Label teachersCountLabel;
    @FXML private Label studentsCountLabel;
    
    // Pie Chart for department distribution
    @FXML private PieChart departmentPieChart;
      // Removed unused departmentCounts field
      // Color mapping for departments
    private static final Map<String, String> DEPARTMENT_COLORS = Map.of(
        "BSCS", "#4287f5",
        "BSIT", "#42c46f",
        "BSBA", "#f5a742",
        "BSN", "#f54242",
        "BSMT", "#9042f5",
        "BSTM", "#42d7f5",
        "OTHER", "#8a8a8a"  // Default color for any other department
    );
    
    @FXML
    private void initialize() {
        setupEventHandlers();
          // Apply initial styling to navigation buttons
        updateNavButtonStyles();
            // Load admin name from session if available
        AuthService.getUserProfile().thenAccept(user -> {
            if (user != null && user.has("name")) {
                String userName = user.get("name").getAsString();
                Platform.runLater(() -> {
                    adminNameLabel.setText(userName);
                });
            }
        });
        
        // Fetch statistics
        fetchStatistics();
    }
    
    /**
     * Updates the navigation button styles to highlight Dashboard as active
     */
    private void updateNavButtonStyles() {
        // Reset all buttons to default style
        viewSubjectsBtn.getStyleClass().remove("nav-button-active");
        viewSubjectsBtn.getStyleClass().add("nav-button");
        
        viewTeachersBtn.getStyleClass().remove("nav-button-active");
        viewTeachersBtn.getStyleClass().add("nav-button");
        
        viewStudentsBtn.getStyleClass().remove("nav-button-active");
        viewStudentsBtn.getStyleClass().add("nav-button");
        
        // Set Dashboard button as active
        dashboardBtn.getStyleClass().remove("nav-button");
        dashboardBtn.getStyleClass().add("nav-button-active");
    }
      private void setupEventHandlers() {
        try {
            System.out.println("Setting up event handlers for Dashboard...");
            
            // Navigation buttons
            dashboardBtn.setOnAction(e -> {
                System.out.println("Dashboard button clicked - already on dashboard");
                // Already on dashboard, no action needed
            });
            
            viewSubjectsBtn.setOnAction(e -> {
                System.out.println("View Subjects button clicked");
                navigateToView("Admin.fxml");
            });
            
            viewTeachersBtn.setOnAction(e -> {
                System.out.println("View Teachers button clicked");
                navigateToAdminAndShow(AdminController.ViewType.TEACHERS);
            });
            
            viewStudentsBtn.setOnAction(e -> {
                System.out.println("View Students button clicked");
                navigateToAdminAndShow(AdminController.ViewType.STUDENTS);
            });
            
            // Add refresh button handler
            refreshButton.setOnAction(e -> {
                System.out.println("Refresh button clicked");
                handleRefresh();
            });
            
            logoutBtn.setOnAction(e -> {
                System.out.println("Logout button clicked");
                handleLogout();
            });
            
            System.out.println("Event handlers setup completed successfully");
        } catch (Exception e) {
            System.err.println("ERROR in setupEventHandlers: " + e.getMessage());
            e.printStackTrace();
            showError("Initialization error", "Failed to set up event handlers: " + e.getMessage());
        }
    }
      private void fetchStatistics() {
        // Fetch dashboard statistics from new endpoint
        AuthService.makeGetRequest("/admin/dashboard-stat").thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    // Process counts
                    if (response.has("counts")) {
                        JsonObject counts = response.getAsJsonObject("counts");
                        
                        // Update statistics labels
                        if (counts.has("subjects")) {
                            subjectsCountLabel.setText(String.valueOf(counts.get("subjects").getAsInt()));
                        }
                        
                        if (counts.has("teachers")) {
                            teachersCountLabel.setText(String.valueOf(counts.get("teachers").getAsInt()));
                        }
                        
                        if (counts.has("students")) {
                            studentsCountLabel.setText(String.valueOf(counts.get("students").getAsInt()));
                        }
                    }
                    
                    // Process department distribution
                    if (response.has("subjectsByDepartment")) {
                        JsonObject deptDistribution = response.getAsJsonObject("subjectsByDepartment");
                        updateDepartmentPieChart(deptDistribution);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error processing dashboard data: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Error fetching dashboard statistics: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }
    
    /**
     * Updates the pie chart with department distribution data
     */
    private void updateDepartmentPieChart(JsonObject deptDistribution) {
        // Create data for pie chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        // Add each department to the chart
        for (Entry<String, JsonElement> entry : deptDistribution.entrySet()) {
            String dept = entry.getKey();
            int count = entry.getValue().getAsInt();
            
            // Only add departments with at least 1 subject
            if (count > 0) {
                PieChart.Data slice = new PieChart.Data(dept + " (" + count + ")", count);
                pieChartData.add(slice);
            }
        }
          // Set data to pie chart
        departmentPieChart.setData(pieChartData);
        
        // Set chart properties
        departmentPieChart.setTitle("Subjects by Department");
        departmentPieChart.setLabelsVisible(true);
        departmentPieChart.setLegendSide(Side.RIGHT);
        departmentPieChart.setLabelLineLength(20);      // Increase label line length
        departmentPieChart.setLabelsVisible(true);      // Ensure labels are visible
        departmentPieChart.setStartAngle(90);          // Start the first slice at the top
        
        // Add tooltips to pie chart slices
        for (PieChart.Data data : pieChartData) {
            Tooltip tooltip = new Tooltip(data.getName());
            Tooltip.install(data.getNode(), tooltip);
            
            // Add hover effect
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle("-fx-opacity: 0.8;");
            });
            
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle("-fx-opacity: 1;");
            });
        }        // Apply custom colors to match the design
        String[] defaultColors = {
            "#8a8a8a", "#a6a6a6", "#c2c2c2", "#e0e0e0", "#cccccc", 
            "#999999", "#666666", "#777777", "#888888", "#555555"
        };
        
        // Ensure the chart layout is updated so that nodes are created for all slices
        departmentPieChart.setAnimated(false);
        departmentPieChart.layout();
        
        int i = 0;
        for (PieChart.Data data : pieChartData) {
            String dept = data.getName().split(" \\(")[0];
            if (DEPARTMENT_COLORS.containsKey(dept)) {
                // Use predefined color for known departments
                data.getNode().setStyle("-fx-pie-color: " + DEPARTMENT_COLORS.get(dept) + ";");
                System.out.println("Applied color " + DEPARTMENT_COLORS.get(dept) + " to department " + dept);
            } else {
                // Use a default color from our array for unknown departments
                String defaultColor = defaultColors[i % defaultColors.length];
                data.getNode().setStyle("-fx-pie-color: " + defaultColor + ";");
                System.out.println("Applied default color " + defaultColor + " to department " + dept);
            }
            i++;
        }
        
        // Add event listener to ensure labels are properly displayed after chart is shown
        departmentPieChart.setOnMouseEntered(e -> {
            if (!departmentPieChart.getData().isEmpty() && departmentPieChart.getData().get(0).getNode() != null) {
                // Force layout update if needed
                for (PieChart.Data data : departmentPieChart.getData()) {
                    if (data.getNode().lookup(".chart-pie-label") == null) {
                        departmentPieChart.layout();
                        break;
                    }
                }
            }
        });
    }
      private void navigateToView(String fxmlFile) {
        try {
            System.out.println("Navigating to view: " + fxmlFile);
            
            System.out.println("Loading " + fxmlFile + " resource...");
            Parent view = FXMLLoader.load(getClass().getResource(fxmlFile));
            
            System.out.println("Creating new scene...");
            Scene scene = new Scene(view);
            
            System.out.println("Setting scene on stage...");
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
            
            System.out.println("Showing stage...");
            stage.show();
            stage.centerOnScreen();
            
            System.out.println("Successfully navigated to " + fxmlFile);
        } catch (Exception e) {
            System.err.println("ERROR in navigateToView: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation error", "Could not navigate to the requested view: " + e.getMessage());
        }
    }
      private void navigateToAdminAndShow(AdminController.ViewType viewType) {
        try {
            System.out.println("Navigating to Admin view with selected view type: " + viewType);
            
            System.out.println("Loading Admin.fxml resource...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Admin.fxml"));
            
            System.out.println("Loading FXML...");
            Parent adminView = loader.load();
            
            System.out.println("Getting controller instance...");
            AdminController controller = loader.getController();
            
            // Show the specific view based on type
            System.out.println("Setting up the requested view: " + viewType);
            switch (viewType) {
                case SUBJECTS:
                    System.out.println("Calling showSubjectsView()...");
                    controller.showSubjectsView();
                    break;
                case TEACHERS:
                    System.out.println("Calling showTeachersView()...");
                    controller.showTeachersView();
                    break;
                case STUDENTS:
                    System.out.println("Calling showStudentsView()...");
                    controller.showStudentsView();
                    break;
            }
            
            System.out.println("Creating new scene...");
            Scene scene = new Scene(adminView);
            
            System.out.println("Setting scene on stage...");
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
            
            System.out.println("Successfully navigated to Admin view with " + viewType + " view");
        } catch (Exception e) {
            System.err.println("ERROR in navigateToAdminAndShow: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation error", "Could not navigate to the requested view: " + e.getMessage());
        }
    }    private void handleLogout() {
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
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Failed to open logout dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Logout Error", "Failed to open logout dialog: " + e.getMessage());
        }
    }
      private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
      /**
     * Handles the refresh button click event.
     * This method resets data and fetches fresh statistics.
     */
    private void handleRefresh() {
        try {
            System.out.println("Refreshing dashboard data...");
            
            // Create progress indicators for loading state
            ProgressIndicator subjectsProgress = new ProgressIndicator();
            subjectsProgress.setMaxSize(30, 30);
            ProgressIndicator teachersProgress = new ProgressIndicator();
            teachersProgress.setMaxSize(30, 30);
            ProgressIndicator studentsProgress = new ProgressIndicator();
            studentsProgress.setMaxSize(30, 30);
            
            // Reset statistics labels to show loading state
            Platform.runLater(() -> {
                System.out.println("Setting loading indicators...");
                
                // Replace labels with progress indicators
                VBox subjectsBox = (VBox) subjectsCountLabel.getParent();
                int subjectsIndex = subjectsBox.getChildren().indexOf(subjectsCountLabel);
                if (subjectsIndex >= 0) {
                    subjectsBox.getChildren().add(subjectsIndex, subjectsProgress);
                    subjectsCountLabel.setVisible(false);
                }
                
                VBox teachersBox = (VBox) teachersCountLabel.getParent();
                int teachersIndex = teachersBox.getChildren().indexOf(teachersCountLabel);
                if (teachersIndex >= 0) {
                    teachersBox.getChildren().add(teachersIndex, teachersProgress);
                    teachersCountLabel.setVisible(false);
                }
                
                VBox studentsBox = (VBox) studentsCountLabel.getParent();
                int studentsIndex = studentsBox.getChildren().indexOf(studentsCountLabel);
                if (studentsIndex >= 0) {
                    studentsBox.getChildren().add(studentsIndex, studentsProgress);
                    studentsCountLabel.setVisible(false);
                }
                
                // Clear the pie chart and add a loading indicator
                System.out.println("Clearing pie chart...");
                departmentPieChart.setData(FXCollections.observableArrayList());
                departmentPieChart.setTitle("Loading data...");
            });
            
            // Fetch fresh statistics with a small delay to show loading indicators
            System.out.println("Fetching fresh statistics...");
            new Thread(() -> {
                try {
                    // Small delay to show loading indicators
                    Thread.sleep(500);
                    
                    // Fetch statistics
                    fetchStatistics();
                    
                    // Restore visibility of labels and remove progress indicators
                    Platform.runLater(() -> {
                        // Remove progress indicators and restore labels
                        VBox subjectsBox = (VBox) subjectsCountLabel.getParent();
                        if (subjectsBox.getChildren().contains(subjectsProgress)) {
                            subjectsBox.getChildren().remove(subjectsProgress);
                        }
                        subjectsCountLabel.setVisible(true);
                        
                        VBox teachersBox = (VBox) teachersCountLabel.getParent();
                        if (teachersBox.getChildren().contains(teachersProgress)) {
                            teachersBox.getChildren().remove(teachersProgress);
                        }
                        teachersCountLabel.setVisible(true);
                        
                        VBox studentsBox = (VBox) studentsCountLabel.getParent();
                        if (studentsBox.getChildren().contains(studentsProgress)) {
                            studentsBox.getChildren().remove(studentsProgress);
                        }
                        studentsCountLabel.setVisible(true);
                        
                        departmentPieChart.setTitle("Subjects by Department");
                    });
                    
                    System.out.println("Dashboard refresh completed successfully");
                } catch (Exception e) {
                    System.err.println("ERROR in refresh thread: " + e.getMessage());
                    e.printStackTrace();
                    showError("Refresh error", "Could not refresh dashboard data: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("ERROR in handleRefresh: " + e.getMessage());
            e.printStackTrace();
            showError("Refresh error", "Could not refresh dashboard data: " + e.getMessage());
        }
    }
}
