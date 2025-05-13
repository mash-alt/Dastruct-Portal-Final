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
    private TextField fullNameField;    @FXML
    private ComboBox<String> programComboBox;

    @FXML
    private TextField addressField;    @FXML
    private DatePicker birthdayPicker;

    @FXML
    private TextField phoneField;    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the program ComboBox with the program codes
        programComboBox.getItems().addAll(
            "BSCS", // Bachelor of Science in Computer Science
            "BSIT", // Bachelor of Science in Information Technology
            "BSBA", // Bachelor of Science in Business Administration
            "BSN",  // Bachelor of Science in Nursing
            "BSMT", // Bachelor of Science in Medical Technology
            "BSTM"  // Bachelor of Science in Tourism Management
        );
    }

    @FXML
    protected void onRegisterButtonClick() {
        // Validate form
        if (validateForm()) {
            registerButton.setDisable(true);
            System.out.println("Registering student: " + fullNameField.getText());            // Prepare user data
            Map<String, Object> userData = new HashMap<>();            userData.put("name", fullNameField.getText());
            userData.put("email", emailField.getText());            userData.put("password", passwordField.getText());            userData.put("phoneNumber", phoneField.getText());
            userData.put("course", programComboBox.getValue());            userData.put("bday", birthdayPicker.getValue() != null ? birthdayPicker.getValue().toString() : null);
            userData.put("address", addressField.getText());
            // All new students start at year level 1
            userData.put("yearLevel", 1);
            // Always register as student
            userData.put("role", "student");
            final String role = "Student";

            // Call the API
            AuthService.register(userData, role.toLowerCase())
                    .thenAccept(response -> {
                        javafx.application.Platform.runLater(() -> {
                            registerButton.setDisable(false);                            if (role.equalsIgnoreCase("Student")) {
                                String studentName = response.has("name") ? response.get("name").getAsString() : fullNameField.getText();
                                System.out.println("A new student has registered (from API): " + studentName);
                            }                            
                            System.out.println("Registration successful! Response: " + response);
                            
                            // Debug: print all keys in the response
                            System.out.println("DEBUG - Response keys:");
                            for (String key : response.keySet()) {
                                System.out.println("Key: " + key + ", Value type: " + 
                                    (response.get(key) != null ? response.get(key).getClass().getName() : "null"));
                            }
                            
                            // Show user details in a new window instead of an alert
                            showUserDetailsWindow(response);
                            // Navigate to login after they close the details window
                            // onLoginLinkClick(new ActionEvent());
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
    }    // Method removed as it's no longer needed with Student-only registration

    private boolean validateForm() {
        // Basic validation
        if (fullNameField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter your full name.");
            return false;
        }        if (emailField.getText().isEmpty() || !emailField.getText().contains("@")) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return false;
        }
        
        if (programComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select a program.");
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
    }    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }      private void showUserDetailsWindow(com.google.gson.JsonObject response) {
        try {
            // Extract the correct user data from the response
            com.google.gson.JsonObject userData = response;
            
            // Check if data is inside a "data" or "user" field (common API response patterns)
            if (response.has("data") && response.get("data").isJsonObject()) {
                userData = response.getAsJsonObject("data");
                System.out.println("Using data object: " + userData);
            } else if (response.has("user") && response.get("user").isJsonObject()) {
                userData = response.getAsJsonObject("user");
                System.out.println("Using user object: " + userData);
            } else {
                // If no nested structure, just create a new object with form values as backup
                if (!response.has("name") && !response.has("email")) {                    userData = new com.google.gson.JsonObject();
                    userData.addProperty("name", fullNameField.getText());
                    userData.addProperty("email", emailField.getText());
                    userData.addProperty("course", programComboBox.getValue());
                    userData.addProperty("phoneNumber", phoneField.getText());
                    userData.addProperty("address", addressField.getText());
                    userData.addProperty("yearLevel", 1);
                    
                    if (birthdayPicker.getValue() != null) {
                        userData.addProperty("bday", birthdayPicker.getValue().toString());
                    }
                    
                    System.out.println("Created backup user data from form fields: " + userData);
                }
            }
            
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserDetailsWindow.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set data
            UserDetailsController controller = loader.getController();
            controller.setParentController(this);
            controller.setUserData(userData);
            
            // Create a new stage
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Registration Successful");
            detailsStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Block input to other windows
            detailsStage.setScene(new Scene(root));
            
            // Show the window and wait for it to close
            detailsStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not display registration details: " + e.getMessage());
        }
    }
}