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
   
    private List<Expansion> data;
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
        switch (columnIndex) {
            case 1: return LocalDateTime.class;
            default:
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
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Expansion row = data.get(rowIndex);
        switch(columnIndex) {
            case 0: return row.getIdentifier() + ":" + row.getVersion();
            case 1: return row.getUploadDate();
            case 2: return row.getTitle();
            case 3: return row.getCategory();
            case 4: return row.getTags();
            case 5: {
                String s = "";
                if (row.isOnline()) {
                    s += "O";
                } else {
                    s += "o";
                }
                if (row.isLocal()) {
                    s += "L";
                } else {
                    s += "l";
                }
                if (row.isEnabled()) {
                    s += "E";
                } else {
                    s += "e";
                }
                if (row.getRequiresOxps() !=null && row.getRequiresOxps().size()>0) {
                    s += "R";
                } else {
                    s += "r";
                }
                if (row.getConflictOxps() !=null && row.getConflictOxps().size()>0) {
                    s += "C";
                } else {
                    s += "c";
                }
                return s;
            }
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

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableRowsUpdated(index, index);
                }
            });
        }
    }
}
