/*
 */

package oolite.starter.ui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Renders a tag.
 *
 * @author hiran
 */
public class Tag extends JPanel {
    private static final Logger log = LogManager.getLogger();
    
    private int offset = 10;

    /**
     * Creates a new tag.
     * 
     * @param name the tag label
     */
    public Tag(String name) {
        super();
        log.debug("Tag({})", name);
        setOpaque(false);
        add(new JLabel(name));
    }

    /**
     * Creates a new tag.
     * Foreground color will be calculated based on background color.
     * 
     * @param name the tag label
     * @param color background color
     */
    public Tag(String name, Color color) {
        super();
        log.debug("Tag({}, {})", name, color);
        setOpaque(false);
        JLabel l = new JLabel(name);
        add(l);
        setBackground(color);
        
        l.setForeground(new Color((color.getRed()+128)%256, (color.getGreen()+128)%256, (color.getBlue()+256)%256));
                
        setBorder(new EmptyBorder(2, 2*offset, 2, offset));
    }

    /**
     * Creates a new tag.
     * Foreground color will be calculated based on background color.
     * 
     * @param name the tag label
     * @param bgcolor background color
     */
    public Tag(String name, Color bgcolor, Color fgcolor) {
        super();
        log.debug("Tag({}, {}, {})", name, bgcolor, fgcolor);
        setOpaque(false);
        JLabel l = new JLabel(name);
        add(l);
        setBackground(bgcolor);
        l.setForeground(fgcolor);
                
        setBorder(new EmptyBorder(2, 2*offset, 2, offset));
    }

    /**
     * Returns the offset. 
     * This determines the triangle size on the left.
     * 
     * @return 
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the offset. 
     * This determines the triangle size on the left.
     * 
     * @param offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        
        int[] x = {0,             offset, getWidth(),  getWidth(), offset     };
        int[] y = {getHeight()/2,      0,          0, getHeight(), getHeight()};
        int count = x.length;
        
        g.setColor(getBackground());
        g.fillPolygon(x, y, count);
        
        g.setColor(new Color(0f,0f,0f,1f));
        g.fillArc(offset/2-2, getHeight()/2-2, 4, 4, 0, 360);
    }
}
