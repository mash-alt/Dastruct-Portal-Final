package org.finalproject.loginregisterfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;
import org.finalproject.loginregisterfx.Service.SessionManager;

import com.google.gson.JsonObject;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeBox;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    void initialize() {
        // Initialize role combo box
        roleComboBox.getItems().addAll("Student", "Teacher", "Admin");
        roleComboBox.setValue("Student"); // Default value

        loginButton.setCursor(Cursor.HAND);

        // Set styling for focus effect with updated styling
        usernameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #1976D2; -fx-border-width: 2px; -fx-padding: 0 15;");
            } else {
                usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dcdcdc; -fx-border-width: 1px; -fx-padding: 0 15;");
            }
        });

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #1976D2; -fx-border-width: 2px; -fx-padding: 0 15;");
            } else {
                passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dcdcdc; -fx-border-width: 1px; -fx-padding: 0 15;");
            }
        });

        // Button hover effect with updated styling
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #1565C0; -fx-background-radius: 8; -fx-text-fill: white;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #1976D2; -fx-background-radius: 8; -fx-text-fill: white;"));
    }

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Basic validation
        if (username == null || username.isEmpty() || password == null || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Username, password and role must not be empty");
            return;
        }

        System.out.println("Login attempt with username: " + username + " and role: " + role);
        loginButton.setDisable(true);        // Call API for login
        AuthService.login(username, password, role)
                .thenAccept(response -> {
                    javafx.application.Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        System.out.println("Login successful! Response: " + response);

                        String name = "User";
                        try {
                            // Navigate into the JSON structure to get the name
                            if (response.has("user") && response.getAsJsonObject("user").has("name")) {
                                name = response.getAsJsonObject("user").get("name").getAsString();
                            }
                            
                            // Store session data
                            String token = response.has("token") ? response.get("token").getAsString() : "";
                            JsonObject userData = response.has("user") ? response.getAsJsonObject("user") : new JsonObject();
                            
                            // Start session with authentication data
                            SessionManager.getInstance().startSession(token, userData, role.toLowerCase());
                            
                        } catch (Exception e) {
                            System.out.println("Error extracting data from response: " + e.getMessage());
                        }
                        
                        showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                                "Welcome back, " + name + "!");
                        
                        if ("Admin".equalsIgnoreCase(role)) {
                            try {
                                Parent dashboardView = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
                                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                                currentStage.setScene(new Scene(dashboardView, 1000, 700));
                                currentStage.setTitle("Admin Dashboard");
                                currentStage.setResizable(true);
                                currentStage.setMaximized(true);
                                currentStage.show();
                                currentStage.centerOnScreen();
                            } catch (Exception e) {
                                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open admin dashboard.");
                                e.printStackTrace();
                            }                        } else if ("Student".equalsIgnoreCase(role)) {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentForm.fxml"));
                                Parent studentView = loader.load();
                                
                                // Get the controller and pass the student data
                                StudentController studentController = loader.getController();
                                if (response.has("user")) {
                                    JsonObject userObject = response.getAsJsonObject("user");
                                    studentController.initializeStudentData(userObject);
                                }
                                
                                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                                currentStage.setScene(new Scene(studentView, 930, 700));
                                currentStage.setTitle("Student Portal");
                                currentStage.setResizable(false);
                                currentStage.show();
                                currentStage.centerOnScreen();
                            } catch (Exception e) {
                                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open student dashboard.");
                                e.printStackTrace();
                            }
                        }
                        // TODO: Implement Teacher role navigation
                    });
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        System.out.println("Login failed: " + ex.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Login Failed",
                                "Invalid credentials or server error: " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    protected void onRegisterLinkClick(ActionEvent event) {
        try {
            // Navigate to registration form (do NOT close the current window, just replace the scene)
            Parent registerView = FXMLLoader.load(getClass().getResource("RegisterForm.fxml"));
            Scene registerScene = new Scene(registerView);

            Stage currentStage = (Stage) registerLink.getScene().getWindow();
            currentStage.setScene(registerScene);
            currentStage.setTitle("Registration Form");
            currentStage.setResizable(false);
            currentStage.show();
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open registration form.");
        }
    }

    @FXML
    protected void onForgotPasswordClick(ActionEvent event) {
        // Implement forgot password functionality
        System.out.println("Forgot password clicked");
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", "Password recovery is not implemented yet.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}