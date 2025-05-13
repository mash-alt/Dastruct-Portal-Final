package org.finalproject.loginregisterfx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.finalproject.loginregisterfx.models.SubjectModel;

/**
 * Content for the enrollment page
 */
public class EnrollmentContent extends PageContent {
    
    private Label enrollmentStatusLabel;
    private Label enrollmentPeriodLabel;
    private TableView<SubjectModel> availableSubjectsTable;
    
    public EnrollmentContent() {
        super();
        this.setPadding(new Insets(25));
        this.getStyleClass().add("content-box");
        
        setupUI();
    }
    
    private void setupUI() {
        // Enrollment Status Section
        Label headerLabel = new Label("Enrollment");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // Status Box
        HBox statusBox = new HBox(20);
        statusBox.setPadding(new Insets(15));
        statusBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        statusBox.setPrefHeight(80);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox statusInfo = createInfoBox("Status", "Open");
        VBox periodInfo = createInfoBox("Enrollment Period", "May 1 - May 30, 2023");
        
        enrollmentStatusLabel = (Label) statusInfo.getChildren().get(1);
        enrollmentPeriodLabel = (Label) periodInfo.getChildren().get(1);
        
        statusBox.getChildren().addAll(statusInfo, createSeparator(), periodInfo);
        
        // Action Buttons
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button printButton = new Button("Print Form");
        printButton.getStyleClass().add("action-button");
        
        Button applyButton = new Button("Apply for Enrollment");
        applyButton.getStyleClass().add("primary-button");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        actionBox.getChildren().addAll(spacer, printButton, applyButton);
        
        // Available Subjects Section
        Label availableSubjectsLabel = new Label("Available Subjects");
        availableSubjectsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        availableSubjectsLabel.setPadding(new Insets(15, 0, 10, 0));
        
        // Create subjects table
        availableSubjectsTable = new TableView<>();
        availableSubjectsTable.getStyleClass().add("modern-table-view");
        availableSubjectsTable.setPrefHeight(350);
        
        TableColumn<SubjectModel, String> edpCodeCol = new TableColumn<>("EDP Code");
        edpCodeCol.setCellValueFactory(new PropertyValueFactory<>("edpCode"));
        edpCodeCol.setPrefWidth(80);
        
        TableColumn<SubjectModel, String> subjectNameCol = new TableColumn<>("Subject");
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectNameCol.setPrefWidth(150);
        
        TableColumn<SubjectModel, Integer> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        unitsCol.setPrefWidth(60);
        
        TableColumn<SubjectModel, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setPrefWidth(120);
        
        TableColumn<SubjectModel, String> roomCol = new TableColumn<>("Room");
        roomCol.setPrefWidth(80);
        
        TableColumn<SubjectModel, String> teacherCol = new TableColumn<>("Teacher");
        teacherCol.setCellValueFactory(new PropertyValueFactory<>("teacherAssigned"));
        teacherCol.setPrefWidth(120);
        
        availableSubjectsTable.getColumns().addAll(
            edpCodeCol, subjectNameCol, unitsCol, scheduleCol, roomCol, teacherCol
        );
        
        // Add all elements to the main container
        this.getChildren().addAll(headerLabel, 
                                 createSpacer(10), 
                                 statusBox,
                                 createSpacer(15),
                                 actionBox,
                                 createSpacer(10),
                                 availableSubjectsLabel, 
                                 availableSubjectsTable);
    }
    
    private VBox createInfoBox(String title, String value) {
        VBox box = new VBox(5);
        box.setMinWidth(150);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #555;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        valueLabel.setStyle("-fx-text-fill: #2c5364;");
        
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }
    
    private Region createSeparator() {
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #dee2e6; -fx-max-width: 1px; -fx-pref-width: 1px;");
        separator.setPrefHeight(60);
        return separator;
    }
    
    private Region createSpacer(double height) {
        Region spacer = new Region();
        spacer.setPrefHeight(height);
        return spacer;
    }
    
    @Override
    public void updateContent() {
        if (student != null) {
            // In a real app, you would update with actual enrollment data
            // For now, we'll use placeholder data
            enrollmentStatusLabel.setText("Open");
            enrollmentPeriodLabel.setText("May 1 - May 30, 2023");
            
            // Populate available subjects - in a real app, this would come from an API call
            // For now, we'll leave it empty or could add mock data here
        }
    }
}
