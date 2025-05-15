package org.finalproject.loginregisterfx.util;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Helper class for testing and debugging purposes
 */
public class TestHelper {

    private static final boolean ENABLE_DEBUG_TOOLS = true;

    /**
     * Shows a hidden debug element if debugging is enabled
     * 
     * @param element The UI element to show
     * @param id The ID of the element to check
     * @return true if the element was shown
     */
    public static boolean showDebugElementIfEnabled(Parent root, String id) {
        if (!ENABLE_DEBUG_TOOLS) {
            return false;
        }
        
        try {
            Node element = root.lookup("#" + id);
            if (element != null) {
                element.setVisible(true);
                System.out.println("Debug element '" + id + "' is now visible");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error showing debug element: " + e.getMessage());
        }
        
        return false;
    }
}
