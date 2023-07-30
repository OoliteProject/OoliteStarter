/*
 */
package oolite.starter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import oolite.starter.model.Expansion;

/**
 *
 * @author hiran
 */
public class ExpansionsTableModel extends AbstractTableModel implements PropertyChangeListener {
   
    private transient List<Expansion> data;
    private Map<String, Integer> siblingCount;
    
    /**
     * Creates a new instance of ExpansionsTableModel.
     * 
     * @param data the data to show
     */
    public ExpansionsTableModel(List<Expansion> data) {
        this.data = data;
        siblingCount = new TreeMap<>();

        for (Expansion e: data) {
            e.addPropertyChangeListener(this);
            
            Integer c = siblingCount.get(e.getIdentifier());
            if (c == null) {
                siblingCount.put(e.getIdentifier(), 1);
            } else {
                siblingCount.put(e.getIdentifier(), c+1);
            }
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 1) {
            return LocalDateTime.class;
        } else {
            return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Identifier";
            case 1: return "Date";
            case 2: return "Title";
            case 3: return "Category";
            case 4: return "Tags";
            case 5: return "Status";
            default:
                return super.getColumnName(columnIndex);
        }
    }
    
    String getStatusString(Expansion row) {
        String result = "";
        if (row.isOnline()) {
            result += "O";
        } else {
            result += "o";
        }
        if (row.isLocal()) {
            result += "L";
        } else {
            result += "l";
        }
        if (row.isEnabled()) {
            result += "E";
        } else {
            result += "e";
        }
        if (row.getRequiresOxps() !=null && !row.getRequiresOxps().isEmpty()) {
            result += "R";
        } else {
            result += "r";
        }
        if (row.getConflictOxps() !=null && !row.getConflictOxps().isEmpty()) {
            result += "C";
        } else {
            result += "c";
        }
        return result;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Expansion row = data.get(rowIndex);
        switch(columnIndex) {
            case 0: return row.getIdentifier() + ":" + row.getVersion();
            case 1: return row.getUploadDate();
            case 2: return row.getTitle();
            case 3: return row.getCategory();
            case 4: return row.getTags();
            case 5: return getStatusString(row);
            default: return "n/a";
        }
    }

    /**
     * Returns the Expansion at given position.
     * 
     * @param rowIndex the position
     * @return the Expansion
     */
    public Expansion getRow(int rowIndex) {
        return data.get(rowIndex);
    }
    
    /**
     * Returns the sibling count - meaning how many related expansions.
     * 
     * @param expansion one specimen of the family
     * @return the count
     */
    public Integer getSiblingCount(Expansion expansion) {
        return siblingCount.get(expansion.getIdentifier());
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getSource() instanceof Expansion e) {
            int index = data.indexOf(e);

            SwingUtilities.invokeLater(() -> fireTableRowsUpdated(index, index) );
        }
    }
    
    /**
     * Returns the number of expansions that are missing required expansions.
     * 
     * @return the number
     */
    public int getNumberOfExpansionsMissingDeps() {
        int result = 0;
        for (Expansion e: data) {
            if (e.isEnabled() && e.getEMStatus().isMissingDeps()) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Returns the number of expansions that are conflicting with something.
     * 
     * @return the number
     */
    public int getNumberOfExpansionsConflicting() {
        int result = 0;
        for (Expansion e: data) {
            if (e.isEnabled() && e.getEMStatus().isConflicting()) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Returns the number of expansions that are incompatible.
     * 
     * @return the number
     */
    public int getNumberOfExpansionsIncompatible() {
        int result = 0;
        for (Expansion e: data) {
            if (e.getEMStatus().isIncompatible()) {
                result++;
            }
        }
        return result;
    }
}
