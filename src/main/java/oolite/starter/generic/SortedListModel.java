/*
 */

package oolite.starter.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.SortOrder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A sorted ListModel.
 * Inspired by https://www.oracle.com/technical-resources/articles/javase/sorted-jlist.html
 * @author hiran
 */
public class SortedListModel<T> extends AbstractListModel<T> {
    private static final Logger log = LogManager.getLogger();
    
    private static final String LISTENER_THREW_EXCEPTION = "listener threw exception";

    private class SortedListEntry implements Comparable<SortedListEntry> {
        
        private int index;
        
        public SortedListEntry(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }

        @Override
        public int compareTo(SortedListEntry thatEntry) {
            // Retrieve the element that this entry points to 
              // in the original model.
              T thisElement = unsortedModel.getElementAt(index);
              // Retrieve the element that thatEntry points to 
              // in the original model.
              T thatElement = 
                unsortedModel.getElementAt(thatEntry.getIndex());
//              if (comparator instanceof Collator) {
//                thisElement = thisElement.toString();
//                thatElement = thatElement.toString();
//              }
              // Compare the base model's elements using the provided comparator.
              int comparison = comparator.compare(thisElement, thatElement);
              // Convert to descending order as necessary.
              if (sortOrder == SortOrder.DESCENDING) {
                comparison = -comparison;
              }
              return comparison;
        }
        
    }
    
    private transient ListModel<T> unsortedModel;
    private transient ArrayList<SortedListEntry> sortedList;
    private SortOrder sortOrder;
    private transient Comparator<T> comparator;

    /**
     * Creates a new SortedListModel.
     * 
     * @param model the unsorted list model
     * @param sortOrder the direction for the sort result
     * @param comp the algorithm defining the sort order
     */
    public SortedListModel(ListModel<T> model, SortOrder sortOrder, Comparator<T> comp) {
        log.debug("SortedListModel({}, {}, {})", model, sortOrder, comp);
        
        this.unsortedModel = model;
        unsortedModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent lde) {
                log.debug("intervalAdded(...)");
                resort();
                try {
                    fireIntervalAdded(this, 0, sortedList.size()-1);
                } catch (Exception e) {
                    log.warn(LISTENER_THREW_EXCEPTION, e);
                }
            }

            @Override
            public void intervalRemoved(ListDataEvent lde) {
                log.debug("intervalRemoved(...)");
                resort();
                try {
                    fireIntervalRemoved(this, 0, sortedList.size()-1);
                } catch (Exception e) {
                    log.warn(LISTENER_THREW_EXCEPTION, e);
                }
            }

            @Override
            public void contentsChanged(ListDataEvent lde) {
                log.debug("contentsChanged(...)");
                resort();
                try {
                    fireContentsChanged(this, 0, sortedList.size()-1);
                } catch (Exception e) {
                    log.warn(LISTENER_THREW_EXCEPTION, e);
                }
            }
        });
        
        this.sortOrder = sortOrder;
        
        if (comp != null) {
            this.comparator = comp;
        } else {
            throw new IllegalArgumentException("Comparator must not be null - we have no default");
        }
        
        resort();
    }
    
    private void resort() {
        sortedList = new ArrayList<>(unsortedModel.getSize());
        for (int x = 0; x < unsortedModel.getSize(); x++) {
            SortedListEntry entry = new SortedListEntry(x);
            int insertionPoint = findInsertionPoint(entry);
            sortedList.add(insertionPoint, entry);
        }
    }
    
    /**
     * Internal helper method to find the insertion point for a new 
     * entry in the sorted model.
     */
    private int findInsertionPoint(SortedListEntry entry) {
        int insertionPoint = sortedList.size();
        if (sortOrder != SortOrder.UNSORTED)  {
            insertionPoint =  Collections.binarySearch((List)sortedList, entry);
            if (insertionPoint < 0) {
                insertionPoint = -(insertionPoint +1);
            }
        }
        return insertionPoint;
    }

    @Override
    public int getSize() {
        return sortedList.size();
    }

    @Override
    public T getElementAt(int i) {
        if (i < 0 || i >= sortedList.size()) {
            throw new IndexOutOfBoundsException("Cannot find index " + i + ": Must be in [0 and " + (sortedList.size()-1) + "]");
        }
        return unsortedModel.getElementAt(sortedList.get(i).getIndex());
    }
    
}
