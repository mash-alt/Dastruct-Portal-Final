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
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TeacherAdminController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private ListView<String> subjectsListView;

    private String role;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (subjectsListView != null) {
            subjectsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            fetchSubjectsForListView();
        }
    }

    public void setRole(String role) {
        this.role = role;
        // Update the title according to the role
        if (role != null) {
            titleLabel.setText(role + " Registration");
        }
    }

    @FXML
    protected void onRegisterButtonClick() {
        // Validate form
        if (validateForm()) {
            registerButton.setDisable(true);
            System.out.println("Registering " + role + ": " + fullNameField.getText() + " with email: " + emailField.getText());

            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", fullNameField.getText());
            userData.put("email", emailField.getText());
            userData.put("phone", phoneField.getText());
            userData.put("password", passwordField.getText());

            ObservableList<String> selectedSubjects = subjectsListView.getSelectionModel().getSelectedItems();
            userData.put("subjects", new ArrayList<>(selectedSubjects));

            // Call the API
            AuthService.register(userData, role.toLowerCase())
                    .thenAccept(response -> {
                        javafx.application.Platform.runLater(() -> {
                            registerButton.setDisable(false);
                            System.out.println("Registration successful! Response: " + response);
                            showAlert(Alert.AlertType.INFORMATION, "Success",
                                    "Registration successful! You can now login.");
                            onLoginLinkClick(new ActionEvent());
                        });
                    })
                    .exceptionally(ex -> {
                        javafx.application.Platform.runLater(() -> {
                            registerButton.setDisable(false);
                            System.out.println("Registration failed: " + ex.getMessage());
                            showAlert(Alert.AlertType.ERROR, "Registration Error",
                                    "Failed to register: " + ex.getMessage());
                        });
                        return null;
                    });
        }
    }

    @FXML
    protected void onBackButtonClick() {
        try {
            // Navigate back to main registration form
            Parent registrationView = FXMLLoader.load(getClass().getResource("RegisterForm.fxml"));
            Scene registrationScene = new Scene(registrationView);

            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.setScene(registrationScene);
            currentStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate back to main registration form.");
            e.printStackTrace();
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
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate to login page.");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Basic validation
        if (fullNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your full name.");
            return false;
        }

        if (emailField.getText().isEmpty() || !emailField.getText().contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return false;
        }

        if (phoneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter your phone number.");
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a password.");
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Passwords do not match.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void fetchSubjectsForListView() {
        AuthService.makeGetRequest("/admin/subjects").thenAccept(response -> {
            ObservableList<String> subjectNames = FXCollections.observableArrayList();
            JsonArray subjects = response.getAsJsonArray("subjects");
            for (int i = 0; i < subjects.size(); i++) {
                JsonObject subj = subjects.get(i).getAsJsonObject();
                String name = subj.get("subjectName").getAsString();
                subjectNames.add(name);
            }
            javafx.application.Platform.runLater(() -> subjectsListView.setItems(subjectNames));
        });
    }
}