/*
 */

package oolite.starter.util;

import java.time.LocalDateTime;
import java.util.Comparator;
import oolite.starter.generic.FilteredListModel;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Supports working with FilterAndSearch.
 * 
 * Filter criteria:
 * - all expansions (no filtering)
 * - updateable
 * - problematic
 * 
 * Search:
 * - try as regexp
 * - literal search
 * 
 * Sort:
 * - by title (default)
 * - by Publish Date
 * - by Category
 * - by Tags
 * - by Author
 */
public class FilterAndSearchUtil {
    private static final Logger log = LogManager.getLogger();

    public enum FilterMode {
        NONE,
        UPDATEABLE,
        PROBLEMATIC
    }
    
    public enum SortMode {
        BY_TITLE,
        BY_PUBLISH_DATE,
        BY_CATEGORY,
        BY_TAGS,
        BY_AUTHOR
    }
    
    /**
     * A filter to find a user-specified item inside an Expansion.
     * Performs a direct pattern match across a number of the expansion's
     * fields.
     */
    public static class SearchFilter implements FilteredListModel.Filter<Expansion> {
        private static final Logger log = LogManager.getLogger();
        
        private String searchString;
        
        /**
         * Creates a new SearchFilter.
         * 
         * @param searchString the string to search for
         */
        public SearchFilter(String searchString) {
            log.debug("SearchFilter({})", searchString);
            
            if (searchString != null) {
                this.searchString = searchString.toLowerCase();
            }
        }
        
        @Override
        public boolean willShow(Expansion t) {
            log.debug("willShow({})", t);
            if (searchString == null) {
                return true;
            } else {
                if (t.getTitle() != null && t.getTitle().toLowerCase().contains(searchString)) {
                    return true;
                }
                if (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchString)) {
                    return true;
                }
                if (t.getAuthor() != null && t.getAuthor().toLowerCase().contains(searchString)) {
                    return true;
                }
                return false;
            }
        }

        @Override
        public String toString() {
            return "SearchFilter(" + searchString + ")";
        }
        
    }
    
    /**
     * Returns a filter suitable for the given filter mode and search string.
     * 
     * @param fm the filter mode
     * @param searchString the search string
     * @return the filter
     */
    public static FilteredListModel.Filter<Expansion> getExpansionFilter(FilterMode fm, String searchString) {
        log.debug("getExpansionFilter({}, {})", fm, searchString);
        
        if (fm == null) {
            throw new IllegalArgumentException("fm must not be null");
        }
        
        FilteredListModel.Filter<Expansion> searchFilter = new SearchFilter(searchString);
        
        FilteredListModel.Filter<Expansion> chosenFilter = null;
        switch (fm) {
            case NONE:
                break;
            case PROBLEMATIC:
                chosenFilter =  new FilteredListModel.Filter<Expansion>() {
                    @Override
                    public boolean willShow(Expansion t) {
                        return t.isEnabled() && (t.getEMStatus().isConflicting() || t.getEMStatus().isMissingDeps());
                    }

                    @Override
                    public String toString() {
                        return "Filter<Expansion>(PROBLEMATIC)";
                    }
                    
                };
                break;
            case UPDATEABLE:
                chosenFilter =  new FilteredListModel.Filter<Expansion>() {
                    /**
                     * Return the previous versions if already installed,
                     * or the latest versions that are not installed.
                     */
                    @Override
                    public boolean willShow(Expansion t) {
                        return t.isEnabled() && !t.getEMStatus().isLatest()
                                || !t.isEnabled() && t.getEMStatus().isLatest();
                    }

                    @Override
                    public String toString() {
                        return "Filter<Expansion>(UPDATEABLE)";
                    }
                };
                break;
            default:
                throw new IllegalArgumentException();
        }
        
        if (chosenFilter == null) {
            return searchFilter;
        } else {
            return new FilteredListModel.AndFilter<>(chosenFilter, searchFilter);
        }
    }
    
    /**
     * Returns a comparator suitable for the given sort mode.
     * 
     * @param sm the sort mode
     * @return the comparator
     */
    public static Comparator<Expansion> getExpansionComparator(SortMode sm) {
        log.debug("getExpansionComparator({})", sm);
        
        if (sm == null) {
            throw new IllegalArgumentException("sm must not be null");
        }
        
        switch(sm) {
            case BY_TITLE:
                return new Comparator<Expansion>() {
                        @Override
                        public int compare(Expansion t1, Expansion t2) {
                            if (t1 == null) {
                                throw new IllegalArgumentException("t1 must not be null");
                            }
                            if (t2 == null) {
                                throw new IllegalArgumentException("t2 must not be null");
                            }
                            return t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase());
                        }

                        @Override
                        public String toString() {
                            return "Comparator<Expansion>(BY_TITLE)";
                        }
                    };
            case BY_AUTHOR:
                return new Comparator<Expansion>() {
                        @Override
                        public int compare(Expansion t1, Expansion t2) {
                            String a1 = t1.getAuthor();
                            if (a1 == null) {
                                a1 = "";
                            }
                            String a2 = t2.getAuthor();
                            if (a2 == null) {
                                a2 = "";
                            }
                            int i = a1.toLowerCase().compareTo(a2.toLowerCase());
                            if (i != 0) {
                                return i;
                            }
                            return t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase());
                        }

                        @Override
                        public String toString() {
                            return "Comparator<Expansion>(BY_AUTHOR)";
                        }
                    };
            case BY_CATEGORY:
                return new Comparator<Expansion>() {
                        @Override
                        public int compare(Expansion t1, Expansion t2) {
                            String c1 = t1.getCategory();
                            if (c1 == null) {
                                c1 = "";
                            }
                            String c2 = t2.getCategory();
                            if (c2 == null) {
                                c2 = "";
                            }

                            int i = c1.toLowerCase().compareTo(c2.toLowerCase());
                            if (i != 0) {
                                return i;
                            }
                            return t1.getTitle().toLowerCase().compareTo(t2.getTitle().toLowerCase());
                        }

                        @Override
                        public String toString() {
                            return "Comparator<Expansion>(BY_CATEGORY)";
                        }
                    };
            case BY_TAGS:
                return new Comparator<Expansion>() {
                        @Override
                        public int compare(Expansion e1, Expansion e2) {
                            String t1 = e1.getTags();
                            if (t1 == null) {
                                t1 = "";
                            }
                            String t2 = e2.getTags();
                            if (t2 == null) {
                                t2 = "";
                            }
                            int i = t1.compareTo(t2);
                            if (i != 0) {
                                return i;
                            }
                            return e1.getTitle().toLowerCase().compareTo(e2.getTitle().toLowerCase());
                        }

                        @Override
                        public String toString() {
                            return "Comparator<Expansion>(BY_TAGS)";
                        }
                    };
            case BY_PUBLISH_DATE:
                return new Comparator<Expansion>() {
                        @Override
                        public int compare(Expansion e1, Expansion e2) {
                            LocalDateTime d1 = e1.getUploadDate();
                            if (d1 == null) {
                                d1 = LocalDateTime.MIN;
                            }
                            LocalDateTime d2 = e2.getUploadDate();
                            if (d2 == null) {
                                d2 = LocalDateTime.MIN;
                            }
                            
                            int i = d1.compareTo(d2);
                            if (i != 0) {
                                return i;
                            }
                            return e1.getTitle().toLowerCase().compareTo(e2.getTitle().toLowerCase());
                        }

                        @Override
                        public String toString() {
                            return "Comparator<Expansion>(BY_PUBLISH_DATE)";
                        }
                    };
            default:
                throw new IllegalArgumentException("unknown sm " + sm);
        }
    }
}
