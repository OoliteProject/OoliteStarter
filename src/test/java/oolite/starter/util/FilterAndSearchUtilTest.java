/*
 */

package oolite.starter.util;

import java.util.Comparator;
import oolite.starter.generic.FilteredListModel.Filter;
import oolite.starter.model.Expansion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
 
/**
 *
 * @author hiran
 */
public class FilterAndSearchUtilTest {
    private static final Logger log = LogManager.getLogger();
    
    private static Expansion e1;
    private static Expansion e2;
    private static Expansion e3;

    public FilterAndSearchUtilTest() {
        log.debug("FilterAndSearchUtilTest()");
        
        e1 = new Expansion();
        e1.setTitle("Title e1");
        e1.setDescription("This is a really nice expansion.");
        e2 = new Expansion();
        e2.setTitle("Title e2");
        e2.setDescription("This is compatible to e1.");
        e3 = new Expansion();
        e3.setTitle("Title e3");
        e3.setDescription("A wonderfully independent example.");
    }

    @BeforeAll
    public static void setUpClass() {
        log.debug("setUpClass()");
    }

    @AfterAll
    public static void tearDownClass() {
        log.debug("tearDownClass()");
    }

    @BeforeEach
    public void setUp() {
        log.debug("setUp()");
    }

    @AfterEach
    public void tearDown() {
        log.debug("tearDown()");
    }

//    /**
//     * Test of getExpansionFilter method, of class FilterAndSearchUtil.
//     */
//    @Test
//    public void testGetExpansionFilter0() {
//        log.info("getExpansionFilter");
//        FilterAndSearchUtil.FilterMode fm = null;
//        String searchString = "";
//        FilteredListModel.Filter<Expansion> expResult = null;
//        FilteredListModel.Filter<Expansion> result = FilterAndSearchUtil.getExpansionFilter(fm, searchString);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getExpansionFilter method, of class FilterAndSearchUtil.
     */
    @Test
    public void testGetExpansionFilter() {
        log.info("getExpansionFilter");

        try {
            FilterAndSearchUtil.getExpansionFilter(null, null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("fm must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    /**
     * Test of getExpansionFilter method, of class FilterAndSearchUtil.
     */
    @Test
    public void testGetExpansionFilter2() {
        log.info("getExpansionFilter");

        FilterAndSearchUtil.FilterMode fm = FilterAndSearchUtil.FilterMode.NONE;

        Filter<Expansion> fe = FilterAndSearchUtil.getExpansionFilter(fm, null);
        assertTrue(fe.willShow(e1));
        assertTrue(fe.willShow(e2));
        assertTrue(fe.willShow(e3));
    }

    /**
     * Test of getExpansionFilter method, of class FilterAndSearchUtil.
     */
    @Test
    public void testGetExpansionFilter3() {
        log.info("getExpansionFilter");

        FilterAndSearchUtil.FilterMode fm = FilterAndSearchUtil.FilterMode.NONE;

        Filter<Expansion> fe = FilterAndSearchUtil.getExpansionFilter(fm, "e1");
        assertTrue(fe.willShow(e1));
        assertTrue(fe.willShow(e2));
        assertFalse(fe.willShow(e3));
    }

    /**
     * Test of getExpansionComparator method, of class FilterAndSearchUtil.
     */
    @Test
    public void testGetExpansionComparator() {
        log.info("getExpansionComparator");

        try {
            FilterAndSearchUtil.getExpansionComparator(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("sm must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    /**
     * Test of getExpansionComparator method, of class FilterAndSearchUtil.
     */
    @Test
    public void testGetExpansionComparator2() {
        log.info("getExpansionComparator");

        FilterAndSearchUtil.SortMode sm = FilterAndSearchUtil.SortMode.BY_TITLE;
        
        Comparator<Expansion> comparator = FilterAndSearchUtil.getExpansionComparator(sm);
        assertEquals(0, comparator.compare(e1, e1));
        assertEquals(-1, comparator.compare(e1, e2));
        assertEquals(1, comparator.compare(e2, e1));
        assertEquals(-2, comparator.compare(e1, e3));
        assertEquals(2, comparator.compare(e3, e1));
    }

}