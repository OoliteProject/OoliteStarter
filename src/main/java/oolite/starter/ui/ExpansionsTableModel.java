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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionsTableModel extends AbstractTableModel implements PropertyChangeListener {
    private static final Logger log = LogManager.getLogger();
   
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
        return 8;
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
            case 0: return "Title";
            case 1: return "Date";
            case 2: return "Identifier";
            case 3: return "Version";
            case 4: return "Category";
            case 5: return "Tags";
            case 6: return "Status";
            case 7: return "Author";
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
        try {
            Expansion row = getRow(rowIndex);
            switch(columnIndex) {
                case 0: return row.getTitle();
                case 1: return row.getUploadDate();
                case 2: return row.getIdentifier() + ":" + row.getVersion();
                case 3: return row.getVersion();
                case 4: return row.getCategory();
                case 5: return row.getTags();
                case 6: return getStatusString(row);
                case 7: return row.getAuthor();
                default: return "n/a";
            }
        } catch (IndexOutOfBoundsException e) {
            log.warn("Access error", e);
            return null;
        }
    }

    /**
     * Returns the Expansion at given position.
     * 
     * @param rowIndex the position
     * @return the Expansion
     */
    public Expansion getRow(int rowIndex) {
        if (rowIndex > data.size()) {
            rowIndex = data.size() - 1;
        }
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
