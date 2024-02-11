/*
 */

package oolite.starter.ui2;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import oolite.starter.model.SaveGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to display a SaveGame in a JList.
 *
 * @author hiran
 */
public class SaveGameCellRenderer extends JLabel implements ListCellRenderer<SaveGame> {
    private static final Logger log = LogManager.getLogger();

    private ImageIcon resumeIcon = new ImageIcon(getClass().getResource("/icons/resume_FILL0_wght400_GRAD0_opsz24.png"));
    private ImageIcon playIcon = new ImageIcon(getClass().getResource("/icons/play_arrow_FILL0_wght400_GRAD0_opsz24.png"));

    /**
     * Creates a new SaveGameCellRenderer.
     */
    public SaveGameCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SaveGame> list, SaveGame saveGame, int i, boolean isSelected, boolean isFocused) {
        log.debug("getListCellRendererComponent(..., {}, {})", isSelected, isFocused);

        if (saveGame.getFile() == null) {
            setIcon(playIcon);
        } else {
            setIcon(resumeIcon);
        }
        
        setText(saveGame.getName());

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
