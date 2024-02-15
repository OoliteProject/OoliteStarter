/*
 */

package oolite.starter.generic;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class FilteredListModel<T> extends AbstractListModel<T> {
    private static final Logger log = LogManager.getLogger();
    
    /**
     * Interface class to define custom filters.
     */
    public interface Filter<T> {
        
        /**
         * This method shall decide whether an expansion will show up or not.
         * @param t the entity to decide on
         * @return true if the expansion shall be shown, false otherwise
         */
        public boolean willShow(T t);
    }
    
    private ListModel<T> model;
    private Filter filter;
    private ArrayList<Integer> entries;

    /**
     * Creates a new FilteredListModel.
     * 
     * @param model the model to filter on
     * @param filter the filter criteria
     */
    public FilteredListModel(ListModel<T> model, Filter filter) {
        log.debug("FilteredListModel({}, {})", model, filter);
        
        this.model = model;
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent lde) {
                updateFilter();
                fireIntervalAdded(this, 0, entries.size()-1);
            }

            @Override
            public void intervalRemoved(ListDataEvent lde) {
                updateFilter();
                fireIntervalRemoved(this, 0, entries.size()-1);
            }

            @Override
            public void contentsChanged(ListDataEvent lde) {
                updateFilter();
                fireContentsChanged(this, 0, entries.size()-1);
            }
        });
        
        this.filter = filter;
        
        updateFilter();
    }
    
    protected void updateFilter() {
        log.debug("updateFilter()");

        entries = new ArrayList<>();
        
        for (int i=0; i<model.getSize(); i++) {
            T t = model.getElementAt(i);
            if (filter.willShow(t)) {
                entries.add(i);
            }
        }
    }

    @Override
    public int getSize() {
        log.debug("getSize()");
        
        return entries.size();
    }

    @Override
    public T getElementAt(int i) {
        log.debug("getElementAt({})", i);

        return model.getElementAt(entries.get(i));
    }

}
