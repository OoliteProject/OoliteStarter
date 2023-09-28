/*
 */

package oolite.starter;

import java.util.ArrayList;
import java.util.List;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
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
    public void testAddRemoveExpansionManagerListener() throws InterruptedException {
        log.info("addRemoveExpansionManagerListener");
        
        ExpansionManager.ExpansionManagerListener listener = Mockito.mock(ExpansionManager.ExpansionManagerListener.class);
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        instance.addExpansionManagerListener(listener);

        Expansion expansion = new Expansion();

        Mockito.verify(listener, Mockito.times(0)).updateStatus(Mockito.any(), Mockito.any());
        
        // force change status
        instance.addCommand(new Command(Command.Action.UNKNOWN, expansion));
        Thread.sleep(3000);
        // count listener invocations
        Mockito.verify(listener, Mockito.times(3)).updateStatus(Mockito.any(), Mockito.any());
        
        instance.removeExpansionManagerListener(listener);

        // force change status
        instance.addCommand(new Command(Command.Action.UNKNOWN, expansion));
        Thread.sleep(3000);
        
        // count listener invocations
        Mockito.verify(listener, Mockito.times(3)).updateStatus(Mockito.any(), Mockito.any());
    }

    /**
     * Test of addCommands method, of class ExpansionManager.
     */
    @Test
    public void testAddCommands() throws InterruptedException {
        System.out.println("addCommands");

        ExpansionManager.ExpansionManagerListener listener = Mockito.mock(ExpansionManager.ExpansionManagerListener.class);
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        instance.addExpansionManagerListener(listener);

        Expansion expansion = new Expansion();
        List<Command> list = new ArrayList<>();
        // the list is empty on purpose

        Mockito.verify(listener, Mockito.times(0)).updateStatus(Mockito.any(), Mockito.any());

        // force change status
        instance.addCommands(list);
        Thread.sleep(2000);
        // count listener invocations
        Mockito.verify(listener, Mockito.times(1)).updateStatus(Mockito.any(), Mockito.any());
    }

    /**
     * Test of addCommands method, of class ExpansionManager.
     */
    @Test
    public void testAddCommands2() throws InterruptedException {
        System.out.println("addCommands2");

        ExpansionManager.ExpansionManagerListener listener = Mockito.mock(ExpansionManager.ExpansionManagerListener.class);
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        instance.addExpansionManagerListener(listener);

        Expansion expansion = new Expansion();
        List<Command> list = new ArrayList<>();
        list.add(new Command(Command.Action.DISABLE, expansion));
        list.add(new Command(Command.Action.KEEP, expansion));
        list.add(new Command(Command.Action.INSTALL, expansion));

        Mockito.verify(listener, Mockito.times(0)).updateStatus(Mockito.any(), Mockito.any());
        
        // force change status
        instance.addCommands(list);
        Thread.sleep(3000);
        // count listener invocations
        Mockito.verify(listener, Mockito.times(3)).updateStatus(Mockito.any(), Mockito.any());
    }

    /**
     * Test of getStatus method, of class ExpansionManager.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");

        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        ExpansionManager.Status s = instance.getStatus();
        assertEquals(ExpansionManager.Activity.IDLE, s.activity());
        assertEquals(0, s.failed());
        assertEquals(0, s.processing());
        assertEquals(0, s.queueSize());
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