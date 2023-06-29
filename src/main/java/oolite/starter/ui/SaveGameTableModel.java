/*
 */
package oolite.starter.ui;

import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import oolite.starter.model.SaveGame;

/**
 * Renders SaveGames in a JTable.
 * @author hiran
 */
public class SaveGameTableModel extends AbstractTableModel {
    
    private transient List<SaveGame> data;
    
    /**
     * Creates a new SaveGameTableModel.
     * 
     * @param data the data to render
     */
    public SaveGameTableModel(List<SaveGame> data) {
        this.data = data;  
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Save Game";
            case 1:
                return "Ship Kills";
            case 2:
                return "Date";
            default:
                return super.getColumnName(column);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 1:
                return Long.class;
            case 2:
                return String.class;
            default:
                return String.class;
        }
    }
    
    

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SaveGame row = getRow(rowIndex);
        switch(columnIndex) {
            case 0:
                return row.getName();
            case 1:
                return row.getShipKills();
            case 2:
                return new Date(row.getFile().lastModified());
            default:
                return "n/a";
        }
    }
    
    /**
     * Returns the SaveGame at given position.
     * 
     * @param rowIndex the position
     * @return the SaveGame
     */
    public SaveGame getRow(int rowIndex) {
        return data.get(rowIndex);
    }
    
}
