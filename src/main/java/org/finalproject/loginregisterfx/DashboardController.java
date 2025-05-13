package org.finalproject.loginregisterfx;

import com.google.gson.JsonArray;
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
import javafx.scene.control.Tooltip;
// Removed unused import: import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;

// Removed unused import: import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DashboardController {
    @FXML private Label adminNameLabel;
    @FXML private Button logoutBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button viewSubjectsBtn;    @FXML private Button viewTeachersBtn;
    @FXML private Button viewStudentsBtn;
    
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
        // Navigation buttons
        dashboardBtn.setOnAction(e -> {
            // Already on dashboard, no action needed
        });
        
        viewSubjectsBtn.setOnAction(e -> {
            navigateToView("Admin.fxml");
        });
        
        viewTeachersBtn.setOnAction(e -> {
            navigateToAdminAndShow(AdminController.ViewType.TEACHERS);
        });
        
        viewStudentsBtn.setOnAction(e -> {
            navigateToAdminAndShow(AdminController.ViewType.STUDENTS);
        });
        
        logoutBtn.setOnAction(e -> handleLogout());
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
            Parent view = FXMLLoader.load(getClass().getResource(fxmlFile));
            Scene scene = new Scene(view);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Navigation error", "Could not navigate to the requested view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToAdminAndShow(AdminController.ViewType viewType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Admin.fxml"));
            Parent adminView = loader.load();
            AdminController controller = loader.getController();
            
            // Show the specific view based on type
            switch (viewType) {
                case SUBJECTS:
                    controller.showSubjectsView();
                    break;
                case TEACHERS:
                    controller.showTeachersView();
                    break;
                case STUDENTS:
                    controller.showStudentsView();
                    break;
            }
            
            Scene scene = new Scene(adminView);
            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Navigation error", "Could not navigate to the requested view: " + e.getMessage());
            e.printStackTrace();
        }
    }
      private void handleLogout() {
        // Create a confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Logout");
        confirmDialog.setHeaderText("Are you sure you want to log out?");
        confirmDialog.setContentText("Any unsaved changes will be lost.");
        
        // Customize button text
        ButtonType logoutButton = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(logoutButton, cancelButton);
        
        // Show dialog and wait for response
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == logoutButton) {
                // User confirmed logout, proceed with logout process
                AuthService.logout().thenAccept(success -> {
                    Platform.runLater(() -> {
                        try {
                            // Return to login screen
                            Parent loginView = FXMLLoader.load(getClass().getResource("LoginForm.fxml"));
                            Scene loginScene = new Scene(loginView, 450, 500);
                            Stage stage = (Stage) logoutBtn.getScene().getWindow();
                            stage.setScene(loginScene);
                            stage.setTitle("Login Form");
                            stage.setResizable(false);
                            stage.show();
                            stage.centerOnScreen();
                        } catch (Exception e) {
                    showError("Logout error", "Could not return to login screen: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }).exceptionally(ex -> {            Platform.runLater(() -> showError("Logout failed", "Could not log out: " + ex.getMessage()));
            return null;
                });
            }
            // If user clicked Cancel, do nothing and return to the application
        });
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
}
