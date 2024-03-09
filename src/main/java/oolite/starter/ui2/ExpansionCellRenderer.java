/*
 */

package oolite.starter.ui2;

import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
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
    private final Border emtpyBorder = new EmptyBorder(4, 2, 4, 4);
    private final Border normalBorder = new CompoundBorder(new MatteBorder(0, 4, 0, 0, getBackground()), emtpyBorder);
    private final Border warningBorder = new CompoundBorder(new MatteBorder(0, 4, 0, 0, Color.ORANGE), emtpyBorder);
    private final Border problemBorder = new CompoundBorder(new MatteBorder(0, 4, 0, 0, Color.RED), emtpyBorder);
    
    /**
     * Creates a new ExpansionCellRenderer.
     */
    public ExpansionCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Expansion> list, Expansion expansion, int i, boolean isSelected, boolean isFocused) {
        log.debug("getListCellRendererComponent(..., {}, {})", isSelected, isFocused);

        setIcon(expansionIcon);
        
        setText("<html>%s<br>%s %s</html>".formatted(expansion.getTitle(), expansion.getVersion(), expansion.getCategory()));
        setToolTipText("%s (%s)".formatted(expansion.getTitle(), expansion.getVersion()));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        
        if (expansion.getEMStatus().isConflicting() || expansion.getEMStatus().isMissingDeps()) {
            setBorder(problemBorder);
        } else if (expansion.getEMStatus().isRequired()) {
            setBorder(warningBorder);
        } else  {
            setBorder(normalBorder);
        }
            
        return this;
    }

}
