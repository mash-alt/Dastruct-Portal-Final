package org.finalproject.loginregisterfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class StudentController implements Initializable {

    // Sidebar profile
    @FXML private Label studentNameLabel;
    @FXML private Label studentIDLabel;

    // Top header
    @FXML private Label academicYearLabel;
    @FXML private Label semesterLabel;

    // Student information section
    @FXML private Label nameLabel;
    @FXML private Label courseLabel;
    @FXML private Label yearLevelLabel;
    @FXML private Label collegeLabel;

    // Study load summary
    @FXML private Label semesterInfoLabel;
    @FXML private Label totalUnitsLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label statusLabel;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button enrollmentBtn;
    @FXML private Button assessmentBtn;
    @FXML private Button prospectusBtn;
    @FXML private Button studyLoadBtn;
    @FXML private Button eGradeBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}