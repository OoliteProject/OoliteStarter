/*
 */
package oolite.starter.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Shows one centered component, and all excess space around will be 
 * semitransparent (dimmed down).
 * 
 * @author hiran
 */
public class DimAroundCenteredPanel extends JPanel {
    private static Logger log = LogManager.getLogger();

    /**
     * Creates a new DimAroundCenteredPanel.
     * 
     * @param component the component to show in the center
     */
    public DimAroundCenteredPanel(JComponent component) {
        log.debug("DimAroundCenteredPanel({})", component);
        
        setLayout(new GridBagLayout());
        add(component, new GridBagConstraints(1, 1, 1, 1, 1.0d, 1.0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(50, 50, 50, 50), 0, 0));
        addMouseListener(new MouseAdapter() {
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    
}
