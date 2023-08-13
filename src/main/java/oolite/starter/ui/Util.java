/*
 */

package oolite.starter.ui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilty functions that can be shared across UI components.
 * 
 * @author hiran
 */
public class Util {
    private static final Logger log = LogManager.getLogger();

    /**
     * Prevent instances from being created.
     */
    private Util() {
    }
    
    /**
     * Configures columnWidth so columns can render data nicely.
     * Preferred width is the average width, maximum width is also set to limit
     * excess.
     * 
     * @param jTable1 the table to set the columns on
     */
    public static void setColumnWidths(JTable jTable1) {
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            DefaultTableColumnModel colModel = (DefaultTableColumnModel) jTable1.getColumnModel();
            TableColumn col = colModel.getColumn(i);

            int maxWidth = 0;
            int avgWidth = 0;
            int rows = jTable1.getRowCount();
            
            TableCellRenderer renderer = null;
            {
                // try to get the right header renderer
                renderer = col.getHeaderRenderer();
                if (renderer == null) {
                    jTable1.getTableHeader().getDefaultRenderer();
                }
                if (renderer == null) {
                    renderer = col.getCellRenderer();
                }
                if (renderer == null) {
                    renderer = jTable1.getCellRenderer(0, i);
                }
                
                if (renderer != null) {
                    rows ++;
                    Component comp = renderer.getTableCellRendererComponent(jTable1, col.getHeaderValue(), false, false, 0, i);
                    maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
                    avgWidth += comp.getPreferredSize().width;
                }
            }
            
            for (int r = 0; r < jTable1.getRowCount(); r++) {
                renderer = jTable1.getCellRenderer(r, i);
                Component comp = renderer.getTableCellRendererComponent(
                        jTable1, jTable1.getValueAt(r, i), false, false, r, i);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
                avgWidth += comp.getPreferredSize().width;
            }

            avgWidth = avgWidth / rows;
            col.setPreferredWidth(Math.min(avgWidth, maxWidth) + 2);
            col.setMaxWidth(maxWidth);
        }
    }
}
