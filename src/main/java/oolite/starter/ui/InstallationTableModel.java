/*
 */

package oolite.starter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import oolite.starter.model.Installation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class InstallationTableModel extends AbstractTableModel implements PropertyChangeListener {
    private static final Logger log = LogManager.getLogger();
    
    private List<Installation> data;

    /**
     * Creates a new instance.
     * 
     * @param data the data to display
     */
    public InstallationTableModel(List<Installation> data) {
        this.data = new ArrayList<>(data);
    }
    
    /**
     * Returns the row object with specified index.
     * 
     * @param rowIndex the index of the row
     * @return the row
     */
    public Installation getRow(int rowIndex) {
        return data.get(rowIndex);
    }

    /**
     * Adds a row to this tablemodel.
     * 
     * @param row the row to add 
     */
    public void addRow(Installation row) {
        data.add(row);
        int rowIndex = data.indexOf(row);
        
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    /**
     * Update a table row.
     * 
     * @param row the row that was updated 
     */
    public void updateRow(Installation row) {
        if (data.contains(row)) {
            int rowIndex = data.indexOf(row);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    /**
     * remove a table row.
     * 
     * @param rowIndex the row that needs to be removed
     */
    public void removeRow(int rowIndex) {
        data.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Home Directory";
            case 1: return "Version";
            default:
                return super.getColumnName(columnIndex);
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Installation row = getRow(rowIndex);
        
        if (row == null) {
            return "n/a";
        }
        
        switch (columnIndex) {
            case 0: return row.getHomeDir();
            case 1: return row.getVersion();
            default:
                return "n/a";
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
