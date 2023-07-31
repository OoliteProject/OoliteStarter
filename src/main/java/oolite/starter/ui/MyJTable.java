/*
 */

package oolite.starter.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class MyJTable extends JTable {
    private static final Logger log = LogManager.getLogger();
    
    private static Color coInstallableNoDeps = Color.YELLOW;
    private static Color coCurrent = Color.WHITE;
    private static Color coUpdatable = Color.cyan;
    private static Color coInstallableWithDeps = Color.ORANGE;
    private static Color coConflict = new Color(150, 70, 50);
    private static Color coIncompatible = Color.GRAY;
    private static Color coManuallyInstalled = Color.RED;
    private static Color coNotInstallable = Color.BLUE;

    /**
     * Creates a new MyJTable.
     */
    public MyJTable() {
    }

    /**
     * Creates a new MyJTable.
     */
    public MyJTable(TableModel dm) {
        super(dm);
    }

    /**
     * Creates a new MyJTable.
     */
    public MyJTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        initColors();
    }

    /**
     * Creates a new MyJTable.
     */
    public MyJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        initColors();
    }

    /**
     * Creates a new MyJTable.
     */
    public MyJTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        initColors();
    }

    /**
     * Creates a new MyJTable.
     */
    public MyJTable(Vector<? extends Vector> rowData, Vector<?> columnNames) {
        super(rowData, columnNames);
        initColors();
    }

    /**
     * Creates a new MyJTable.
     */
    public MyJTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
        initColors();
    }
    
    private void initColors() {
        UIDefaults uidefaults = UIManager.getLookAndFeelDefaults();
        //Color defaultBackground = uidefaults.getColor("Button.background");
        
        Enumeration<Object> keys = uidefaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = uidefaults.get(key);
            log.warn("uidefault {}->{}", key, value);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int column) {
        log.trace("prepareRenderer({}, {}, {})", renderer, rowIndex, column);
        Component c = super.prepareRenderer(renderer, rowIndex, column);

//        if (getModel() instanceof ExpansionsTableModel etm) {
//            rowIndex = convertRowIndexToModel(rowIndex);
//            Expansion row = etm.getRow(rowIndex);
//            
//            /*
//    private static Color coCurrent = Color.WHITE;
//    private static Color coManuallyInstalled = Color.RED;
//    private static Color coNotInstallable = Color.BLUE;
//            */
//            JComponent jc = (JComponent)c;
//            
//            if (row.getEMStatus().isIncompatible()) {
//                //jc.setBorder(new LineBorder(coIncompatible));
//                jc.setForeground(coIncompatible);
//            } else if (row.getEMStatus().isConflicting()) {
//                //jc.setBorder(new LineBorder(coConflict));
//                jc.setForeground(coConflict);
//            } else if (!row.getEMStatus().isLatest()) {
//                //jc.setBorder(new LineBorder(coUpdatable));
//                jc.setForeground(coUpdatable);
//            } else if (row.isOnline() && (row.getRequiresOxps() == null || row.getRequiresOxps().isEmpty())) {
//                //jc.setBorder(new LineBorder(coInstallableNoDeps));
//                jc.setForeground(coInstallableNoDeps);
//            } else if (row.isOnline() && row.getRequiresOxps()!= null &&!row.getRequiresOxps().isEmpty()) {
//                //jc.setBorder(new LineBorder(coInstallableWithDeps));
//                jc.setForeground(coInstallableWithDeps);
//            } else if (!row.isOnline() && row.isLocal()) {
//                //jc.setBorder(new LineBorder(coManuallyInstalled));
//                jc.setForeground(coManuallyInstalled);
//            } else {
//                //c.setBackground(null);
//            }
//        }

        return c;
    }
}
