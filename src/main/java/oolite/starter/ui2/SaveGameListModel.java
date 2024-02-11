/*
 */

package oolite.starter.ui2;

import java.util.List;
import javax.swing.AbstractListModel;
import oolite.starter.model.SaveGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class SaveGameListModel extends AbstractListModel<SaveGame> {
    private static final Logger log = LogManager.getLogger();
    
    private List<SaveGame> saveGames;

    /**
     * Creates a new SaveGameListModel.
     * 
     * @param saveGames the saveGames
     */
    public SaveGameListModel(List<SaveGame> saveGames) {
        this.saveGames = saveGames;
    }

    @Override
    public int getSize() {
        return saveGames.size();
    }

    @Override
    public SaveGame getElementAt(int i) {
        return saveGames.get(i);
    }
}
