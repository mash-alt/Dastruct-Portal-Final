package org.finalproject.loginregisterfx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Content for the E-Grade page
 */
public class EGradeContent extends PageContent {
    
    private ComboBox<String> semesterSelector;
    private Label gpaLabel;
    private Label statusLabel;
    private TableView<GradeEntry> gradesTable;
    
    public EGradeContent() {
        super();
        this.setPadding(new Insets(25));
        this.getStyleClass().add("content-box");
        
        setupUI();
    }
    
    private void setupUI() {
        // Header with semester selector
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label headerLabel = new Label("Grade Report");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label semesterLabel = new Label("Semester:");
        semesterSelector = new ComboBox<>();
        semesterSelector.getItems().addAll(
            "1st Semester, 2023-2024",
            "2nd Semester, 2022-2023",
            "1st Semester, 2022-2023"
        );
        semesterSelector.setValue("1st Semester, 2023-2024");
        semesterSelector.setOnAction(e -> updateGrades());
        
        Button exportButton = new Button("Export PDF");
        exportButton.getStyleClass().add("export-button");
        
        headerBox.getChildren().addAll(headerLabel, spacer, semesterLabel, semesterSelector, exportButton);
        
        // Summary Box
        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(15));
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        summaryBox.setPrefHeight(80);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox gpaInfo = createInfoBox("GPA", "3.75");
        VBox statusInfo = createInfoBox("Status", "Dean's List");
        
        gpaLabel = (Label) gpaInfo.getChildren().get(1);
        statusLabel = (Label) statusInfo.getChildren().get(1);
        
        summaryBox.getChildren().addAll(gpaInfo, createSeparator(), statusInfo);
        
        // Grades Table
        gradesTable = new TableView<>();
        gradesTable.getStyleClass().add("modern-table-view");
        gradesTable.setPrefHeight(350);
        
        TableColumn<GradeEntry, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);
        
        TableColumn<GradeEntry, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));
        subjectCol.setPrefWidth(200);
        
        TableColumn<GradeEntry, Double> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(new PropertyValueFactory<>("units"));
        unitsCol.setPrefWidth(60);
        
        TableColumn<GradeEntry, String> midtermCol = new TableColumn<>("Midterm");
        midtermCol.setCellValueFactory(new PropertyValueFactory<>("midterm"));
        midtermCol.setPrefWidth(80);
        
        TableColumn<GradeEntry, String> finalCol = new TableColumn<>("Final");
        finalCol.setCellValueFactory(new PropertyValueFactory<>("finalGrade"));
        finalCol.setPrefWidth(80);
        
        TableColumn<GradeEntry, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        remarksCol.setPrefWidth(120);
        
        gradesTable.getColumns().addAll(
            codeCol, subjectCol, unitsCol, midtermCol, finalCol, remarksCol
        );
        
        // Add all elements to the main container
        this.getChildren().addAll(headerBox, 
                                 createSpacer(15), 
                                 summaryBox,
                                 createSpacer(20),
                                 gradesTable);
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
    
    private void updateGrades() {
        // In a real app, this would fetch the grades for the selected semester
        // For now, we'll use mock data
        gradesTable.getItems().clear();
        
        // Different mock data depending on which semester is selected
        String semester = semesterSelector.getValue();
        
        if (semester.equals("1st Semester, 2023-2024")) {
            gradesTable.getItems().addAll(
                new GradeEntry("CS101", "Introduction to Computing", 3.0, "A", "A", "Passed"),
                new GradeEntry("CS102", "Programming Fundamentals", 3.0, "A", "A-", "Passed"),
                new GradeEntry("MATH101", "Calculus I", 4.0, "B+", "B", "Passed"),
                new GradeEntry("ENG101", "English Composition", 3.0, "A-", "A", "Passed"),
                new GradeEntry("HUM101", "Introduction to Philosophy", 3.0, "B", "B+", "Passed")
            );
            gpaLabel.setText("3.75");
            statusLabel.setText("Dean's List");
        } else if (semester.equals("2nd Semester, 2022-2023")) {
            gradesTable.getItems().addAll(
                new GradeEntry("CS201", "Data Structures", 3.0, "B+", "A-", "Passed"),
                new GradeEntry("CS202", "Object-Oriented Programming", 3.0, "A", "A", "Passed"),
                new GradeEntry("MATH201", "Discrete Mathematics", 3.0, "B", "B", "Passed"),
                new GradeEntry("SCI101", "General Physics", 4.0, "C+", "B-", "Passed")
            );
            gpaLabel.setText("3.45");
            statusLabel.setText("Good Standing");
        } else {
            gradesTable.getItems().addAll(
                new GradeEntry("CS001", "Computer Basics", 3.0, "A", "A", "Passed"),
                new GradeEntry("MATH001", "Pre-Calculus", 3.0, "A-", "B+", "Passed"),
                new GradeEntry("ENG001", "Basic English", 3.0, "B+", "A-", "Passed")
            );
            gpaLabel.setText("3.80");
            statusLabel.setText("Dean's List");
        }
    }
    
    @Override
    public void updateContent() {
        if (student != null) {
            // Initialize with default semester
            updateGrades();
        }
    }
    
    /**
     * Inner class for grade entries in the table
     */
    public static class GradeEntry {
        private String code;
        private String subject;
        private double units;
        private String midterm;
        private String finalGrade;
        private String remarks;
        
        public GradeEntry(String code, String subject, double units, String midterm, String finalGrade, String remarks) {
            this.code = code;
            this.subject = subject;
            this.units = units;
            this.midterm = midterm;
            this.finalGrade = finalGrade;
            this.remarks = remarks;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getSubject() { return subject; }
        public double getUnits() { return units; }
        public String getMidterm() { return midterm; }
        public String getFinalGrade() { return finalGrade; }
        public String getRemarks() { return remarks; }
    }
}
