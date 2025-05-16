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
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Hyperlink loginLink;    
    @Override
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
        
        // Add real-time validation on fields
        setupFieldValidation();
        
        // Set today as max date for birthdayPicker
        birthdayPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                // Disable dates in the future
                setDisable(empty || date.isAfter(today));
            }
        });
    }
    
    /**
     * Set up real-time validation for input fields
     */
    private void setupFieldValidation() {
        // Email field validation indicator
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                boolean isValid = Pattern.compile(emailRegex).matcher(newValue).matches();
                
                if (isValid) {
                    emailField.setStyle("-fx-border-color: green;");
                } else {
                    emailField.setStyle("-fx-border-color: red;");
                }
            } else {
                emailField.setStyle("");
            }
        });
          // Phone field validation indicator for Philippine phone numbers
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                // Check for valid characters (digits, +, spaces, and hyphens)
                boolean isValid = newValue.matches("^[0-9+\\- ]+$");
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                
                // Philippine mobile number validation: should be 11-13 digits
                // Format: +639XXXXXXXXX or 09XXXXXXXXX
                boolean isPhMobile = digitsOnly.matches("^(\\+?63|0)9\\d{9}$");
                
                // Philippine landline validation: Area code + number (typically 10-12 digits total)
                boolean isPhLandline = digitsOnly.matches("^(\\+?63|0)\\d{8,10}$");
                
                if ((isPhMobile || isPhLandline) && isValid) {
                    phoneField.setStyle("-fx-border-color: green;");
                } else {
                    phoneField.setStyle("-fx-border-color: red;");
                    // Tooltip can be added here for immediate feedback
                }
            } else {
                phoneField.setStyle("");
            }
        });
        
        // Password strength indicator
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                boolean hasUppercase = !newValue.equals(newValue.toLowerCase());
                boolean hasLowercase = !newValue.equals(newValue.toUpperCase());
                boolean hasDigit = newValue.matches(".*\\d.*");
                boolean longEnough = newValue.length() >= 8;
                
                if (longEnough && hasUppercase && hasLowercase && hasDigit) {
                    passwordField.setStyle("-fx-border-color: green;");
                } else if (newValue.length() > 3) {
                    passwordField.setStyle("-fx-border-color: orange;");
                } else {
                    passwordField.setStyle("-fx-border-color: red;");
                }
            } else {
                passwordField.setStyle("");
            }
        });
        
        // Password confirmation matching indicator
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !passwordField.getText().isEmpty()) {
                if (newValue.equals(passwordField.getText())) {
                    confirmPasswordField.setStyle("-fx-border-color: green;");
                } else {
                    confirmPasswordField.setStyle("-fx-border-color: red;");
                }
            } else {
                confirmPasswordField.setStyle("");
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
        userData.put("email", emailField.getText());
        userData.put("password", passwordField.getText());
        userData.put("phoneNumber", phoneField.getText());
        userData.put("course", programComboBox.getValue());
        userData.put("bday", birthdayPicker.getValue() != null ? birthdayPicker.getValue().toString() : null);
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
                        registerButton.setDisable(false);
                        if (role.equalsIgnoreCase("Student")) {
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
        // Full name validation
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showAlert("Validation Error", "Please enter your full name.");
            return false;
        }
        
        if (fullName.length() < 3) {
            showAlert("Validation Error", "Full name must be at least 3 characters long.");
            return false;
        }
        
        // Only allow letters, spaces, dots, and hyphens in names
        if (!fullName.matches("^[a-zA-Z .-]+$")) {
            showAlert("Validation Error", "Full name should only contain letters, spaces, hyphens, or dots.");
            return false;
        }
        
        // Email validation with regex
        String email = emailField.getText().trim();
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        
        if (email.isEmpty() || !pattern.matcher(email).matches()) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return false;
        }
        
        // Program validation
        if (programComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select a program.");
            return false;
        }
          // Phone number validation - specifically for Philippine format
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty()) {
            // Allow digits, plus sign, hyphens and spaces
            if (!phone.matches("^[0-9+\\- ]+$")) {
                showAlert("Validation Error", "Phone number can only contain digits, +, -, and spaces.");
                return false;
            }
            
            // Remove non-digits to check length
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            
            // Philippine mobile number validation: Format: +639XXXXXXXXX or 09XXXXXXXXX
            boolean isPhMobile = digitsOnly.matches("^(\\+?63|0)9\\d{9}$");
            
            // Philippine landline validation
            boolean isPhLandline = digitsOnly.matches("^(\\+?63|0)\\d{8,10}$");
            
            if (!isPhMobile && !isPhLandline) {
                showAlert("Validation Error", "Please enter a valid Philippine phone number.\nMobile: 09XXXXXXXXX or +639XXXXXXXXX\nLandline: (XX) XXX-XXXX or similar format.");
                return false;
            }
        }
        
        // Address validation
        String address = addressField.getText().trim();
        if (address.isEmpty()) {
            showAlert("Validation Error", "Please enter your address.");
            return false;
        }
        
        if (address.length() < 5) {
            showAlert("Validation Error", "Address is too short. Please enter a complete address.");
            return false;
        }
        
        // Birthday validation
        if (birthdayPicker.getValue() == null) {
            showAlert("Validation Error", "Please select your birthday.");
            return false;
        }
        
        LocalDate birthday = birthdayPicker.getValue();
        LocalDate today = LocalDate.now();
        
        if (birthday.isAfter(today)) {
            showAlert("Validation Error", "Birthday cannot be in the future.");
            return false;
        }
        
        int age = Period.between(birthday, today).getYears();
        if (age < 16) {
            showAlert("Validation Error", "You must be at least 16 years old to register.");
            return false;
        }
        
        if (age > 100) {
            showAlert("Validation Error", "Please enter a valid birth date.");
            return false;
        }
        
        // Password validation
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showAlert("Validation Error", "Please enter a password.");
            return false;
        }
        
        // Check password strength
        if (password.length() < 8) {
            showAlert("Validation Error", "Password must be at least 8 characters long.");
            return false;
        }
        
        // Check for at least one uppercase letter, one lowercase letter, and one number
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasUppercase || !hasLowercase || !hasDigit) {
            showAlert("Validation Error", "Password must contain at least one uppercase letter, one lowercase letter, and one number.");
            return false;
        }
        
        // Confirm password match
        if (!password.equals(confirmPasswordField.getText())) {
            showAlert("Validation Error", "Passwords do not match.");
            return false;
        }

        return true;
    }    private void showAlert(String title, String message) {
        Alert.AlertType alertType = Alert.AlertType.INFORMATION;
        
        // Use ERROR type for validation errors
        if (title.contains("Error")) {
            alertType = Alert.AlertType.ERROR;
        }
        
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }private void showUserDetailsWindow(com.google.gson.JsonObject response) {
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