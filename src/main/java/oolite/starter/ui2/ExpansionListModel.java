/*
 */

package oolite.starter.ui2;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 * @deprecated Make use of the Oolite2-integrated list
 */
@Deprecated
public class ExpansionListModel extends AbstractListModel<Expansion> {
    private static final Logger log = LogManager.getLogger();
    
    /**
     * Interface class to define custom filters.
     */
    public interface Filter {
        
        /**
         * This method shall decide whether an expansion will show up or not.
         * @param e the expansion to decide on
         * @return true if the expansion shall be shown, false otherwise
         */
        public boolean willShow(Expansion e);
    }
    
    private List<Expansion> expansions;

    /**
     * Creates a new ExpansionListModel.
     * All expansions will be shown.
     * 
     * @param expansions the list of expansions
     */
    public ExpansionListModel(List<Expansion> expansions) {
        this.expansions = new ArrayList<>(expansions);
    }

    /**
     * Creates a new ExpansionListModel.
     * Expansions will be filtered.
     * 
     * @param expansions the list of expansions
     * @param f the filter function to apply
     */
    public ExpansionListModel(List<Expansion> expansions, Filter f) {
        this.expansions = new ArrayList<>();
        for (Expansion e: expansions) {
            if (f.willShow(e)) {
                this.expansions.add(e);
            }
        }
    }
    
    /**
     * Adds an expansion to the model.
     * 
     * @param e the expansion to add
     */
    public void add(Expansion e) {
        expansions.add(e);
        int rowIndex = expansions.indexOf(e);
        
        fireIntervalAdded(this, rowIndex, rowIndex);
    }
    
    /**
     * Removes an expansion from the model.
     * 
     * @param e the expansion to remove
     */
    public void remove(Expansion e) {
        int rowIndex = expansions.indexOf(e);
        remove(rowIndex);
    }
    
    /**
     * Removes an expansion from the model.
     * 
     * @param e the index of the expansion
     */
    public void remove(int rowIndex) {
        expansions.remove(rowIndex);
        fireIntervalRemoved(this, rowIndex, rowIndex);
    }

    @Override
    public int getSize() {
        return expansions.size();
    }

    @Override
    public Expansion getElementAt(int i) {
        return expansions.get(i);
    }
}
