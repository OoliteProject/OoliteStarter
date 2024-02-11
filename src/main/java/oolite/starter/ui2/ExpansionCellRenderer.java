/*
 */

package oolite.starter.ui2;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionCellRenderer extends JLabel implements ListCellRenderer<Expansion> {
    private static final Logger log = LogManager.getLogger();

    private ImageIcon expansionIcon = new ImageIcon(getClass().getResource("/icons/32px-Oolite-oxp-icon.png"));
    
    /**
     * Creates a new ExpansionCellRenderer.
     */
    public ExpansionCellRenderer() {
        setOpaque(true);
        setBorder(new EmptyBorder(4, 4, 4, 4));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Expansion> list, Expansion expansion, int i, boolean isSelected, boolean isFocused) {
        log.debug("getListCellRendererComponent(..., {}, {})", isSelected, isFocused);

        setIcon(expansionIcon);
        
        setText("<html>%s<br>%s %s %s</html>".formatted(expansion.getTitle(), expansion.getVersion(), expansion.getCategory(), expansion.isManaged()));
        setToolTipText("%s (%s)".formatted(expansion.getTitle(), expansion.getVersion()));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());

        return this;
    }

}
