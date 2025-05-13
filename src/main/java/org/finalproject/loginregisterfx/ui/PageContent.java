package org.finalproject.loginregisterfx.ui;

import javafx.scene.layout.VBox;
import org.finalproject.loginregisterfx.models.StudentModel;

/**
 * Base class for all page content in the student portal
 */
public abstract class PageContent extends VBox {
    
    protected StudentModel student;
    
    public PageContent() {
        super(15); // Default spacing of 15
        this.setPrefHeight(590);
        this.setPrefWidth(620);
    }
    
    /**
     * Sets the student for this page
     * @param student The student whose data should be displayed
     */
    public void setStudent(StudentModel student) {
        this.student = student;
        updateContent();
    }
    
    /**
     * Update the content of the page with the latest data
     */
    public abstract void updateContent();
}
