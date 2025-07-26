package oolite.starter.ui;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A TableCellRenderer to render Dates.
 * Inspired by https://stackoverflow.com/a/15993736/4222206
 * 
 * @author hiran
 */
public class DateCellRenderer extends JLabel implements TableCellRenderer {
    private static final Logger log = LogManager.getLogger();
    
    private DateFormat dateFormat;

    /**
     * Creates a new instance.
     * 
     * @param dateFormat the format to be applied to dates
     */
    public DateCellRenderer(DateFormat dateFormat) {
        super();
        this.setOpaque(true);
        this.dateFormat = dateFormat;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean isFocused, int row, int column) {
        if (value instanceof Date) {
            setText(dateFormat.format((Date)value));
        } else {
            setText("--");
        }
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }
    
    
}
