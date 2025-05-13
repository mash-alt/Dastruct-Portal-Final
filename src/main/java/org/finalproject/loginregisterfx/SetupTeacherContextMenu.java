package org.finalproject.loginregisterfx;

import javafx.scene.control.*;
import org.finalproject.loginregisterfx.models.TeacherModel;

public class SetupTeacherContextMenu {    public static void apply(AdminController controller, TableView<Object> tableView) {
        System.out.println("Setting up teacher context menu");
        
        // Create context menu
        ContextMenu contextMenu = new ContextMenu();
        
        // Create menu items
        MenuItem assignSubjectsItem = new MenuItem("Assign Subjects");
        MenuItem viewSubjectsItem = new MenuItem("View Assigned Subjects");
        
        // Add menu items to context menu
        contextMenu.getItems().addAll(assignSubjectsItem, viewSubjectsItem);
        
        // Set action for assign subjects menu item
        assignSubjectsItem.setOnAction(event -> {
            Object selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem instanceof TeacherModel) {
                TeacherModel teacher = (TeacherModel) selectedItem;
                controller.showAssignSubjectsDialog(teacher);
            } else {
                controller.showError("No teacher selected. Please select a teacher to assign subjects to.");
            }
        });
        
        // Set action for view assigned subjects menu item
        viewSubjectsItem.setOnAction(event -> {
            Object selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem instanceof TeacherModel) {
                TeacherModel teacher = (TeacherModel) selectedItem;
                controller.showTeacherAssignedSubjects(teacher);
            } else {
                controller.showError("No teacher selected. Please select a teacher to view assigned subjects.");
            }
        });        // Set the row factory to display context menu on right-click        
        tableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            
            // Add context menu conditionally based on the row data type
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem instanceof TeacherModel) {
                    row.setContextMenu(contextMenu);
                } else {
                    row.setContextMenu(null);
                }
            });
            
            // Also handle mouse clicks for better compatibility            
            row.setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY && !row.isEmpty()) {
                    Object item = row.getItem();
                    System.out.println("Right-click detected on row with item: " + item.getClass().getName());
                    if (item instanceof TeacherModel) {
                        System.out.println("Showing teacher context menu for: " + ((TeacherModel)item).getName());
                        tableView.getSelectionModel().select(row.getIndex());
                        contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                }
            });
            
            return row;
        });
    }
}
