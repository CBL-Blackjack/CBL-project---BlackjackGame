package bj.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A specialized JLayeredPane that automatically resizes all its children
 * to fill the entire pane area. Used for the game table and overlays.
 */
public class GameLayeredPane extends JLayeredPane {
    
    public GameLayeredPane() {
        setOpaque(false);
    }
    
    @Override
    public void doLayout() {
        Dimension size = getSize();
        
        // Resize all components to fill the entire pane
        for (Component c : getComponents()) {
            c.setBounds(0, 0, size.width, size.height);
        }
    }
}
