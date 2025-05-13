package org.finalproject.loginregisterfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserDetailsController {
    @FXML
    private Label successLabel;
    
    @FXML
    private VBox detailsBox;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label studentIdLabel;
      @FXML
    private Label courseLabel;
    
    @FXML
    private Label yearLevelLabel;
    
    @FXML
    private Label phoneNumberLabel;
    
    @FXML
    private Label birthdayLabel;
    
    @FXML
    private Label addressLabel;
    
    @FXML
    private Button continueButton;
    
    private RegisterController parentController;
    
    public void setParentController(RegisterController controller) {
        this.parentController = controller;
    }      public void setUserData(JsonObject userData) {
        try {
            // Debug what we received
            System.out.println("UserDetailsController received: " + userData);
            
            // Set the values from the JSON response
            nameLabel.setText(safeGetString(userData, "name"));
            emailLabel.setText(safeGetString(userData, "email"));
            studentIdLabel.setText(safeGetString(userData, "studentId"));
            courseLabel.setText(safeGetString(userData, "course"));
            // Set year level - default to "1" if not present
            yearLevelLabel.setText(userData.has("yearLevel") && !userData.get("yearLevel").isJsonNull() ? 
                    userData.get("yearLevel").getAsString() : "1");
            phoneNumberLabel.setText(safeGetString(userData, "phoneNumber"));
            addressLabel.setText(safeGetString(userData, "address"));
            
            // Format birthday if available
            String bdayString = safeGetString(userData, "bday");
            if (!bdayString.isEmpty()) {
                birthdayLabel.setText(formatDate(bdayString));
            } else {
                birthdayLabel.setText("");
            }
            
            // Check if the data is nested inside a data field
            if (userData.has("data") && userData.get("data").isJsonObject()) {
                JsonObject dataObj = userData.getAsJsonObject("data");
                System.out.println("Found nested data object: " + dataObj);
                
                // Try to get values from the nested data object if main ones were empty
                if (nameLabel.getText().isEmpty()) 
                    nameLabel.setText(safeGetString(dataObj, "name"));
                    
                if (emailLabel.getText().isEmpty()) 
                    emailLabel.setText(safeGetString(dataObj, "email"));
                    
                if (studentIdLabel.getText().isEmpty()) 
                    studentIdLabel.setText(safeGetString(dataObj, "studentId"));
                      if (courseLabel.getText().isEmpty()) 
                    courseLabel.setText(safeGetString(dataObj, "course"));
                
                // Check year level in nested data
                if (yearLevelLabel.getText().isEmpty() || yearLevelLabel.getText().equals("1")) {
                    if (dataObj.has("yearLevel") && !dataObj.get("yearLevel").isJsonNull()) {
                        yearLevelLabel.setText(dataObj.get("yearLevel").getAsString());
                    } else {
                        yearLevelLabel.setText("1");
                    }
                }
                    
                if (phoneNumberLabel.getText().isEmpty()) 
                    phoneNumberLabel.setText(safeGetString(dataObj, "phoneNumber"));
                    
                if (addressLabel.getText().isEmpty()) 
                    addressLabel.setText(safeGetString(dataObj, "address"));
                    
                // Handle birthday from nested object
                if (birthdayLabel.getText().isEmpty()) {
                    String nestedBday = safeGetString(dataObj, "bday");
                    if (!nestedBday.isEmpty()) {
                        birthdayLabel.setText(formatDate(nestedBday));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error setting user data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Safely get a string from the JSON object
    private String safeGetString(JsonObject obj, String key) {
        try {
            if (obj != null && obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        } catch (Exception e) {
            System.out.println("Error getting " + key + ": " + e.getMessage());
        }
        return "";
    }
    
    private String formatDate(String dateString) {
        try {
            // Parse ISO date format and convert to more readable format
            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            
            // Try to parse the date and format it
            LocalDate date = LocalDate.parse(dateString.substring(0, 10));
            return outputFormatter.format(date);
        } catch (Exception e) {
            return dateString; // Return original if parsing fails
        }
    }
    
    @FXML
    protected void onContinueButtonClick(ActionEvent event) {
        // Close this window
        Stage stage = (Stage) continueButton.getScene().getWindow();
        stage.close();
        
        // Navigate to login using the parent controller
        if (parentController != null) {
            parentController.onLoginLinkClick(new ActionEvent());
        }
    }
}
