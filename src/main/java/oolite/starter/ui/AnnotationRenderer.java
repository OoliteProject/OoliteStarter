/*
 */

package oolite.starter.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JTable;
import javax.swing.plaf.LayerUI;
import javax.swing.table.TableCellRenderer;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A TableCellRenderer that can decorate cells with annotations.
 * 
 * @author hiran
 */
public class AnnotationRenderer implements TableCellRenderer {
    private static final Logger log = LogManager.getLogger();
    
    private class MyLayerUI extends LayerUI<JComponent> {
        
        private String indicator = " ";
        private Color color = Color.RED;
        
        private transient FontRenderContext frc;
        
        public MyLayerUI() {
            frc = new FontRenderContext(new AffineTransform(), false, false);
        }
        
        public void setIndicator(String indicator) {
            this.indicator = String.valueOf(indicator);
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        @Override
        public void paint(Graphics g, JComponent c) {
            log.trace("layerUI.paint(...)");
            super.paint(g, c);

            Graphics2D g2d = (Graphics2D)g;
            
            int baseLine = c.getBaseline(c.getWidth(), c.getHeight());
            if (baseLine >=0) {
                baseLine += 2;
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawLine(0, baseLine, c.getWidth(), baseLine);
                Rectangle2D r = g2d.getFont().getStringBounds(indicator, frc);
                g2d.drawString(indicator, (int)(c.getWidth()-r.getWidth()), (int)(c.getHeight() - 1));
            }
        }
    }
    
    private TableCellRenderer parent;
    private MyLayerUI layerUI;
    
    /**
     * Creates a new AnnotationRenderer.
     * 
     * @param parent the renderer that was used before
     */
    public AnnotationRenderer(TableCellRenderer parent) {
        log.debug("AnnotationRenderer({})", parent);
        
        this.parent = parent;
        this.layerUI = new MyLayerUI();
    }

    /**
     * Creates a new AnnotationRenderer.
     * 
     * @param parent the renderer that was used before
     */
    public AnnotationRenderer(TableCellRenderer parent, Color color) {
        log.debug("AnnotationRenderer({})", parent);
        
        this.parent = parent;
        this.layerUI = new MyLayerUI();
        layerUI.setColor(color);
    }

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        JComponent c = (JComponent)parent.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, rowIndex, columnIndex);
        
        if (jtable.getModel() instanceof ExpansionsTableModel etm) {
            int modelIndex = jtable.convertRowIndexToModel(rowIndex);
            Expansion row = etm.getRow(modelIndex);
            
            if (row.isEnabled()) {
                String indicator = "";
                
                if (row.getEMStatus().isConflicting()) {
                    indicator += "C";
                }
                if (row.getEMStatus().isMissingDeps()) {
                    indicator += "D";
                }
                
                if (!indicator.isEmpty()) {
                    //layerUI.setIndicator(indicator);
                    c = new JLayer<JComponent>(c, layerUI);
                }
            }
            
        }
        return c;
    }

}
