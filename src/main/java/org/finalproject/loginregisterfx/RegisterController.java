package org.finalproject.loginregisterfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField idNumberField;

    @FXML
    private TextField courseField;

    @FXML
    private TextField addressField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private DatePicker birthdayPicker;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize role combo box with options
        roleComboBox.getItems().addAll("Student", "Teacher", "Admin");

        // Add listener for role selection
        roleComboBox.setOnAction(event -> {
            String selectedRole = roleComboBox.getValue();
            if (selectedRole != null && (selectedRole.equals("Teacher") || selectedRole.equals("Admin"))) {
                showTeacherAdminForm();
            }
        });
    }

    @FXML
    protected void onRegisterButtonClick() {
        // Validate form
        if (validateForm()) {
            registerButton.setDisable(true);
            System.out.println("Registering student: " + fullNameField.getText());

            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", fullNameField.getText());
            userData.put("idNumber", idNumberField.getText());
            userData.put("course", courseField.getText());
            userData.put("address", addressField.getText());
            userData.put("birthday", birthdayPicker.getValue() != null ? birthdayPicker.getValue().toString() : null);
            userData.put("phone", phoneField.getText());
            userData.put("email", emailField.getText());
            userData.put("password", passwordField.getText());

            // Get selected role (default to "student" if nothing selected)
            String selectedRole = roleComboBox.getValue();
            if (selectedRole == null) selectedRole = "Student";

            final String role = selectedRole;

            // Call the API
            AuthService.register(userData, role.toLowerCase())
                    .thenAccept(response -> {
                        javafx.application.Platform.runLater(() -> {
                            registerButton.setDisable(false);
                            System.out.println("Registration successful! Response: " + response);
                            showAlert("Success", "Registration successful! You can now login.");
                            onLoginLinkClick(new ActionEvent());
                        });
                    })
                    .exceptionally(ex -> {
                        javafx.application.Platform.runLater(() -> {
                            registerButton.setDisable(false);
                            System.out.println("Registration failed: " + ex.getMessage());
                            showAlert("Registration Error", "Failed to register: " + ex.getMessage());
                        });
                        return null;
                    });
        }
    }

    @FXML
    protected void onLoginLinkClick(ActionEvent event) {
        try {
            // Navigate to login screen
            Parent loginView = FXMLLoader.load(getClass().getResource("LoginForm.fxml"));
            Scene loginScene = new Scene(loginView);

            Stage currentStage = (Stage) loginLink.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.show();
            currentStage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Navigation Error", "Could not navigate to login page.");
            e.printStackTrace();
        }
    }

    private void showTeacherAdminForm() {
        try {
            // Load the simplified form for teachers and admins
            FXMLLoader loader = new FXMLLoader(getClass().getResource("teacher-admin-form.fxml"));
            Parent teacherAdminView = loader.load();

            // Get controller and pass selected role
            TeacherAdminController controller = loader.getController();
            controller.setRole(roleComboBox.getValue());

            // Show in new stage or replace current scene
            Scene teacherAdminScene = new Scene(teacherAdminView);
            Stage currentStage = (Stage) roleComboBox.getScene().getWindow();
            currentStage.setScene(teacherAdminScene);
            currentStage.show();
            currentStage.centerOnScreen();
        } catch (IOException e) {
            showAlert("Form Error", "Could not load the special registration form.");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Basic validation
        if (fullNameField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your full name.");
            return false;
        }

        if (emailField.getText().isEmpty() || !emailField.getText().contains("@")) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter a password.");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Validation Error", "Passwords do not match.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}