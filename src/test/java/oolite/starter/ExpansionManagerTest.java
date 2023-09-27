/*
 */

package oolite.starter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
 
/**
 *
 * @author hiran
 */
public class ExpansionManagerTest {
    private static final Logger log = LogManager.getLogger();

     public ExpansionManagerTest() {
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
     * Test of getInstance method, of class ExpansionManager.
     */
    @Test
    public void testGetInstance() {
        log.info("getInstance");

        ExpansionManager result = ExpansionManager.getInstance();
        assertNotNull(result);
    }

    /**
     * Test of addExpansionManagerListener method, of class ExpansionManager.
     */
    @Test
    public void testAddRemoveExpansionManagerListener() {
        System.out.println("addRemoveExpansionManagerListener");
        
        ExpansionManager.ExpansionManagerListener listener = Mockito.mock(ExpansionManager.ExpansionManagerListener.class);
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.addExpansionManagerListener(listener);
        
        // force change status
        
        // count listener invocations
        
        instance.removeExpansionManagerListener(listener);

        // force change status
        
        // count listener invocations
    }

    /**
     * Test of addCommand method, of class ExpansionManager.
     */
    @Test
    public void testAddCommand() {
        System.out.println("addCommand");
    }

    /**
     * Test of addCommands method, of class ExpansionManager.
     */
    @Test
    public void testAddCommands() {
        System.out.println("addCommands");
    }

    /**
     * Test of getStatus method, of class ExpansionManager.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
    }

    /**
     * Test of getCommands method, of class ExpansionManager.
     */
    @Test
    public void testGetCommands() {
        System.out.println("getCommands");
    }

    @Test
    public void testStatusRecord() {
        log.info("testStatusRecord()");
        
        ExpansionManager.Activity activity = ExpansionManager.Activity.IDLE;
        ExpansionManager.Status status = new ExpansionManager.Status(3, 5, 7, activity);
        assertEquals(activity, status.activity());
        assertEquals(3, status.queueSize());
        assertEquals(5, status.processing());
        assertEquals(7, status.failed());
    }
}