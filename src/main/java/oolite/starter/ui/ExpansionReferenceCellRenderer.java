/*
 */

package oolite.starter.ui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import oolite.starter.model.SaveGame;
import static oolite.starter.model.SaveGame.ExpansionReference.Status.missing;
import static oolite.starter.model.SaveGame.ExpansionReference.Status.ok;
import static oolite.starter.model.SaveGame.ExpansionReference.Status.surplus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionReferenceCellRenderer  extends JLabel implements ListCellRenderer<SaveGame.ExpansionReference> {
    private static final Logger log = LogManager.getLogger();

        private static final ImageIcon iiOk = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/check_circle_FILL0_wght400_GRAD0_opsz24.png"));
        private static final ImageIcon iiMissing = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/report_FILL0_wght400_GRAD0_opsz24_red.png"));
        private static final ImageIcon iiSurplus = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/warning_FILL0_wght400_GRAD0_opsz24_orange.png"));

        @Override
        public Component getListCellRendererComponent(
                JList<? extends SaveGame.ExpansionReference> list, 
                SaveGame.ExpansionReference value, 
                int index, 
                boolean isSelected, 
                boolean isFocused) {
            setText(value.name);
            switch(value.status) {
                case ok:
                    setIcon(iiOk);
                    break;
                case missing:
                    setIcon(iiMissing);
                    break;
                case surplus:
                    setIcon(iiSurplus);
                    break;
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
}
