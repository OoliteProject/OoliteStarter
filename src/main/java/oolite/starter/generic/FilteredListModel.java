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
    
    public static class AndFilter<T> implements Filter<T> {
        private static final Logger log = LogManager.getLogger();        
        
        private Filter<T> f1;
        private Filter<T> f2;
        
        /**
         * Creates a new AndFilter. It shows a value
         * if both underlying filters would show it.
         * 
         * @param f1 the one filter
         * @param f2 the other filter
         */
        public AndFilter(Filter<T> f1, Filter<T> f2) {
            log.debug("AndFilter({}, {})", f1, f2);
            
            this.f1 = f1;
            this.f2 = f2;
        }

        @Override
        public boolean willShow(T t) {
            log.debug("willShow({})", t);

            return f1.willShow(t) && f2.willShow(t);
        }
        
        /**
         * Returns a string representation of this filter. 
         * 
         * @return the string
         */
        public String toString() {
            return "AndFilter(" + f1 + ", " + f2 + ")";
        }
    }
    
    public static class OrFilter<T> implements Filter<T> {
        private static final Logger log = LogManager.getLogger();        
        
        private Filter<T> f1;
        private Filter<T> f2;
        
        /**
         * Creates a new OrFilter. It shows a value
         * if either one of the underlying filters would show it.
         * 
         * @param f1 the one filter
         * @param f2 the other filter
         */
        public OrFilter(Filter<T> f1, Filter<T> f2) {
            log.debug("OrFilter({}, {})", f1, f2);
            
            this.f1 = f1;
            this.f2 = f2;
        }

        @Override
        public boolean willShow(T t) {
            log.debug("willShow({})", t);

            return f1.willShow(t) || f2.willShow(t);
        }
        
        /**
         * Returns a string representation of this filter. 
         * 
         * @return the string
         */
        public String toString() {
            return "OrFilter(" + f1 + ", " + f2 + ")";
        }
    }
    
    public static class NotFilter<T> implements Filter<T> {
        private static final Logger log = LogManager.getLogger();        
        
        private Filter<T> f1;
        
        /**
         * Creates a new NotFilter.
         * 
         * @param f1 the filter to negate
         */
        public NotFilter(Filter<T> f1) {
            log.debug("NotFilter({})", f1);
            
            this.f1 = f1;
        }

        @Override
        public boolean willShow(T t) {
            log.debug("willShow({})", t);

            return !f1.willShow(t);
        }
        
        /**
         * Returns a string representation of this filter. 
         * 
         * @return the string
         */
        public String toString() {
            return "NotFilter(" + f1 + ")";
        }
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
