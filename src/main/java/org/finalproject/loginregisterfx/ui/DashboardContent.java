package org.finalproject.loginregisterfx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Content for the dashboard page
 */
public class DashboardContent extends PageContent {
    
    private Label welcomeLabel;
    private Label statusLabel;
    private Label semesterLabel;
    private Label unitsLabel;
    private PieChart gradeDistributionChart;
    
    public DashboardContent() {
        super();
        this.setPadding(new Insets(25));
        this.getStyleClass().add("content-box");
        
        setupUI();
    }
    
    private void setupUI() {
        // Welcome Section
        welcomeLabel = new Label("Welcome!");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        welcomeLabel.setStyle("-fx-text-fill: #2c5364;");
        
        // Status Section
        HBox statusBox = new HBox(20);
        statusBox.setPadding(new Insets(15));
        statusBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        statusBox.setPrefHeight(100);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox enrollmentStatus = createInfoBox("Enrollment Status", "✓ Enrolled");
        VBox semesterInfo = createInfoBox("Current Semester", "First Semester 2023-2024");
        VBox unitsInfo = createInfoBox("Units Enrolled", "21 units");
        
        statusLabel = (Label) enrollmentStatus.getChildren().get(1);
        semesterLabel = (Label) semesterInfo.getChildren().get(1);
        unitsLabel = (Label) unitsInfo.getChildren().get(1);
        
        statusBox.getChildren().addAll(enrollmentStatus, createSeparator(), semesterInfo, createSeparator(), unitsInfo);
        
        // Grade Distribution Section (placeholder)
        Label gradesHeader = new Label("Grade Distribution");
        gradesHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        gradesHeader.setPadding(new Insets(15, 0, 10, 0));
        
        gradeDistributionChart = new PieChart();
        gradeDistributionChart.setPrefHeight(300);
        gradeDistributionChart.setLabelsVisible(true);
        gradeDistributionChart.setLegendVisible(true);
        
        // Add all elements to the main container
        this.getChildren().addAll(welcomeLabel, 
                                 createSpacer(20), 
                                 statusBox, 
                                 createSpacer(20), 
                                 gradesHeader, 
                                 gradeDistributionChart);
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
        separator.setPrefHeight(80);
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
            // Update welcome message with student name
            String firstName = student.getName().split(" ")[0];
            welcomeLabel.setText("Welcome, " + firstName + "!");
            
            // In a real app, you would update all these values from student data
            // For now, we'll use placeholder data
            statusLabel.setText("✓ Enrolled");
            semesterLabel.setText("First Semester 2023-2024");
            unitsLabel.setText("21 units");
            
            // Update chart with dummy data for now
            gradeDistributionChart.getData().clear();
            gradeDistributionChart.getData().addAll(
                new PieChart.Data("A", 30),
                new PieChart.Data("B", 45),
                new PieChart.Data("C", 15),
                new PieChart.Data("D", 10)
            );
        }
    }
}
