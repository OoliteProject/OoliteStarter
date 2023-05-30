/*
 */

package oolite.starter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JTable;
import javax.swing.plaf.LayerUI;
import javax.swing.table.TableCellRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A TableCellRenderer that can decorate cells with annotations.
 * 
 * @author hiran
 */
public class AnnotationRenderer implements TableCellRenderer {
    private static final Logger log = LogManager.getLogger();
    
    private class MyLayerUI extends LayerUI {
        
        private String indicator = "b";
        private FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        
        public void setIndicator(String indicator) {
            this.indicator = String.valueOf(indicator);
        }
        
        @Override
        public void paint(Graphics g, JComponent c) {
            log.debug("layerUI.paint");
            super.paint(g, c);

            g.setColor(Color.red);
            g.drawLine(0, 0, c.getWidth(), c.getHeight());
            Rectangle2D r = g.getFont().getStringBounds(indicator, frc);
            g.drawString(indicator, (int)(c.getWidth()-r.getWidth()), (int)(c.getHeight() - 1));
        }
    };
    
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

    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        JComponent c = (JComponent)parent.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, rowIndex, columnIndex);
        if (columnIndex == 1 && jtable.getModel() instanceof ExpansionsTableModel etm) {
            int modelIndex = jtable.convertRowIndexToModel(rowIndex);
            //Expansion row = etm.getRow(modelIndex);
            layerUI.setIndicator(
                    String.valueOf(etm.getValueAt(modelIndex, 0))
            );
            
            c = new JLayer<JComponent>(c, layerUI);
        }
        return c;

//        return new JLabel("b");
    }

}
