package org.finalproject.loginregisterfx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.finalproject.loginregisterfx.Service.AuthService;

/**
 * Controller for the logout confirmation dialog
 */
public class LogoutDialogController {
    
    @FXML private Button cancelBtn;
    @FXML private Button logoutBtn;
    
    private Stage ownerStage;
    
    @FXML
    private void initialize() {
        System.out.println("Initializing LogoutDialogController...");
        
        // Set up button handlers
        cancelBtn.setOnAction(e -> handleCancel());
        logoutBtn.setOnAction(e -> handleLogout());
    }
    
    /**
     * Set the owner stage for navigation purposes
     */
    public void setOwnerStage(Stage owner) {
        this.ownerStage = owner;
    }
    
    /**
     * Handle the cancel button click - just close the dialog
     */
    private void handleCancel() {
        System.out.println("Cancel button clicked");
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Handle the logout button click - perform logout and navigation
     */
    private void handleLogout() {
        System.out.println("Logout button clicked, initiating logout process");
        
        // Show loading state
        logoutBtn.setDisable(true);
        logoutBtn.setText("Logging out...");
        
        // Call logout API
        AuthService.logout().thenAccept(success -> {
            Platform.runLater(() -> {
                try {
                    System.out.println("Logout successful, navigating to login screen");
                    
                    // Close the dialog
                    Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
                    currentStage.close();
                    
                    // Return to login screen
                    Parent loginView = FXMLLoader.load(getClass().getResource("LoginForm.fxml"));
                    Scene loginScene = new Scene(loginView, 450, 500);
                    
                    if (ownerStage != null) {
                        ownerStage.setScene(loginScene);
                        ownerStage.setTitle("Login Form");
                        ownerStage.setResizable(false);
                        ownerStage.show();
                        ownerStage.centerOnScreen();
                    }
                } catch (Exception ex) {
                    System.err.println("Error navigating to login screen: " + ex.getMessage());
                    ex.printStackTrace();
                    showError("Could not return to login screen: " + ex.getMessage());
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Logout API call failed: " + ex.getMessage());
            Platform.runLater(() -> {
                logoutBtn.setDisable(false);
                logoutBtn.setText("Logout");
                showError("Could not log out: " + ex.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Logout Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
