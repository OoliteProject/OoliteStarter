/*
 */

package oolite.starter.model;

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
public class ExpansionReferenceTest {
    private static final Logger log = LogManager.getLogger();

     public ExpansionReferenceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of compareTo method, of class ExpansionReference.
     */
    @Test
    public void testCompareTo() {
        log.info("testCompareTo");

        ExpansionReference one = new ExpansionReference();
        assertEquals(1, one.compareTo(null));
    }

    /**
     * Test of compareTo method, of class ExpansionReference.
     */
    @Test
    public void testCompareTo2() {
        log.info("testCompareTo2");

        ExpansionReference one = new ExpansionReference();
        assertEquals(0, one.compareTo(one));
    }

    /**
     * Test of compareTo method, of class ExpansionReference.
     */
    @Test
    public void testCompareTo3() {
        log.info("testCompareTo3");

        ExpansionReference one = new ExpansionReference();
        ExpansionReference other = new ExpansionReference();
        
        // we have null names
        assertEquals(1, one.compareTo(other));
    }

    /**
     * Test of compareTo method, of class ExpansionReference.
     */
    @Test
    public void testCompareTo4() {
        log.info("testCompareTo4");

        ExpansionReference one = new ExpansionReference();
        one.setName("foo");
        ExpansionReference other = new ExpansionReference();
        other.setName("bar");
        
        assertEquals(4, one.compareTo(other));
        assertEquals(-4, other.compareTo(one));
    }

    /**
     * Test of compareTo method, of class ExpansionReference.
     */
    @Test
    public void testCompareTo5() {
        log.info("testCompareTo5");

        ExpansionReference one = new ExpansionReference();
        one.setName("foo");
        ExpansionReference other = new ExpansionReference();
        other.setName("foo");
        
        assertEquals(0, one.compareTo(other));
        assertEquals(0, other.compareTo(one));
    }

    /**
     * Test of toString method, of class ExpansionReference.
     */
    @Test
    public void testToString() {
        log.info("toString");

        ExpansionReference instance = new ExpansionReference();
        assertEquals("ExpansionReference{name=null, status=null}", instance.toString());
    }

}