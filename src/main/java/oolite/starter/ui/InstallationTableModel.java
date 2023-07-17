/*
 */

package oolite.starter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.table.AbstractTableModel;
import oolite.starter.Configuration;
import oolite.starter.model.Installation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class InstallationTableModel extends AbstractTableModel implements PropertyChangeListener {
    private static final Logger log = LogManager.getLogger();
    
    private transient Configuration data;

    /**
     * Creates a new instance.
     * 
     * @param data the data to display
     */
    public InstallationTableModel(Configuration data) {
        log.debug("InstallationTableModel({})", data);
        this.data = data;
    }
    
    /**
     * Returns the row object with specified index.
     * 
     * @param rowIndex the index of the row
     * @return the row
     */
    public Installation getRow(int rowIndex) {
        return data.getInstallations().get(rowIndex);
    }

    /**
     * Adds a row to this tablemodel.
     * 
     * @param row the row to add 
     * @return the index of the new row
     */
    public int addRow(Installation row) {
        data.getInstallations().add(row);
        int rowIndex = data.getInstallations().indexOf(row);
        
        fireTableRowsInserted(rowIndex, rowIndex);
        return rowIndex;
    }

    /**
     * Update a table row.
     * 
     * @param row the row that was updated 
     */
    public void updateRow(Installation row) {
        if (data.getInstallations().contains(row)) {
            int rowIndex = data.getInstallations().indexOf(row);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    /**
     * remove a table row.
     * 
     * @param rowIndex the row that needs to be removed
     */
    public void removeRow(int rowIndex) {
        data.getInstallations().remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Home Directory";
            case 1: return "Version";
            case 2: return "Selected";
            default:
                return super.getColumnName(columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) {
            return Boolean.class;
        } else {
            return super.getColumnClass(columnIndex);
        }
    }
    
    @Override
    public int getRowCount() {
        return data.getInstallations().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
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
            case 2: 
                return row == data.getActiveInstallation();
            default:
                return "n/a";
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
