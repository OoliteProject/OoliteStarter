/*
 */

package oolite.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingWorker;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.awaitility.Awaitility;
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
        instance.start();
        instance.addExpansionManagerListener(listener);

        Expansion expansion = new Expansion();

        Mockito.verify(listener, Mockito.times(0)).updateStatus(Mockito.any(), Mockito.any());
        assertEquals(ExpansionManager.Activity.STOPPED, instance.getStatus().activity());
        
        // force change status
        instance.addCommand(new Command(Command.Action.UNKNOWN, expansion));
        
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> instance.getStatus().activity() == ExpansionManager.Activity.ERRORS );
        
        // count listener invocations
        Mockito.verify(listener, Mockito.times(2)).updateStatus(Mockito.any(), Mockito.any());
        assertEquals(ExpansionManager.Activity.ERRORS, instance.getStatus().activity());
        
        instance.removeExpansionManagerListener(listener);

        // force change status
        Command cmd = new Command(Command.Action.UNKNOWN, expansion);
        assertEquals(SwingWorker.StateValue.PENDING, cmd.getState());
        instance.addCommand(cmd);

        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> cmd.getState() == SwingWorker.StateValue.DONE );
        
        // count listener invocations
        Mockito.verify(listener, Mockito.times(2)).updateStatus(Mockito.any(), Mockito.any());
    }

    /**
     * Test of addCommands method, of class ExpansionManager.
     */
    @Test
    public void testAddCommands() throws InterruptedException {
        log.info("addCommands");

        ExpansionManager.ExpansionManagerListener listener = Mockito.mock(ExpansionManager.ExpansionManagerListener.class);
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        instance.addExpansionManagerListener(listener);

        Expansion expansion = new Expansion();
        List<Command> list = new ArrayList<>();
        // the list is empty on purpose

        assertEquals(ExpansionManager.Activity.STOPPED, instance.getStatus().activity());
        Mockito.verify(listener, Mockito.times(0)).updateStatus(Mockito.any(), Mockito.any());

        // force change status
        instance.start();
        instance.addCommands(list);
        
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> ExpansionManager.Activity.IDLE == instance.getStatus().activity());        assertEquals(ExpansionManager.Activity.IDLE, instance.getStatus().activity());
        
        
        // count listener invocations
        Mockito.verify(listener, Mockito.times(2)).updateStatus(Mockito.any(), Mockito.any());
    }

    /**
     * Test of addCommands method, of class ExpansionManager.
     */
    @Test
    public void testAddCommands2() throws InterruptedException {
        log.info("addCommands2");

        ExpansionManager.ExpansionManagerListener listener = Mockito.mock(ExpansionManager.ExpansionManagerListener.class);
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        instance.start();
        instance.addExpansionManagerListener(listener);

        Expansion expansion = new Expansion();
        List<Command> list = new ArrayList<>();
        list.add(new Command(Command.Action.DISABLE, expansion));
        list.add(new Command(Command.Action.KEEP, expansion));
        list.add(new Command(Command.Action.INSTALL, expansion));

        Mockito.verify(listener, Mockito.times(0)).updateStatus(Mockito.any(), Mockito.any());
        
        // force change status
        instance.addCommands(list);
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> instance.getStatus().queueSize() == 2);
        
        // count listener invocations
        Mockito.verify(listener, Mockito.times(1)).updateStatus(Mockito.any(), Mockito.any());
    }

    /**
     * Test of getStatus method, of class ExpansionManager.
     */
    @Test
    public void testGetStatus() throws InterruptedException {
        log.info("getStatus");

        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();
        assertEquals(ExpansionManager.Activity.STOPPED, instance.getStatus().activity());

        instance.start();
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> instance.getStatus().activity() == ExpansionManager.Activity.IDLE);
        
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
        log.info("getCommands");
        
        ExpansionManager instance = ExpansionManager.getInstance();
        instance.reset();

        assertEquals(0, instance.getCommands().size());
        
        instance.addCommand(new Command(Command.Action.UNKNOWN, new Expansion()));

        assertEquals(1, instance.getCommands().size());
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