package org.finalproject.loginregisterfx;

import javafx.scene.control.*;
import org.finalproject.loginregisterfx.models.SubjectModel;

public class SetupSubjectContextMenu {    public static void apply(AdminController controller, TableView<Object> tableView) {
        System.out.println("Setting up subject context menu");
        
        // Create context menu
        ContextMenu contextMenu = new ContextMenu();
        
        // Create menu items
        MenuItem updateItem = new MenuItem("Update Subject");
        MenuItem deleteItem = new MenuItem("Delete Subject");
        
        // Add menu items to context menu
        contextMenu.getItems().addAll(updateItem, deleteItem);
        
        // Set action for update menu item
        updateItem.setOnAction(event -> {
            Object selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem instanceof SubjectModel) {
                SubjectModel subject = (SubjectModel) selectedItem;
                controller.showUpdateSubjectDialog(subject);
            } else {
                controller.showError("No subject selected. Please select a subject to update.");
            }
        });
        
        // Set action for delete menu item
        deleteItem.setOnAction(event -> {
            Object selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem instanceof SubjectModel) {
                SubjectModel subject = (SubjectModel) selectedItem;
                controller.showDeleteSubjectConfirmation(subject);
            } else {
                controller.showError("No subject selected. Please select a subject to delete.");
            }
        });        // Set the row factory to display context menu on right-click        
        tableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            
            // Add context menu conditionally based on the row data type
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem instanceof SubjectModel) {
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
                    if (item instanceof SubjectModel) {
                        System.out.println("Showing subject context menu for: " + ((SubjectModel)item).getSubjectName());
                        tableView.getSelectionModel().select(row.getIndex());
                        contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                }
            });
            
            return row;
        });
    }
}
