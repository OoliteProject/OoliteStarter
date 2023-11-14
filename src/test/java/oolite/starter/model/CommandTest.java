/*
 */

package oolite.starter.model;

import oolite.starter.model.Command.Result;
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
public class CommandTest {
    private static final Logger log = LogManager.getLogger();

     public CommandTest() {
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
     * Test of getAction method, of class Command.
     */
    @Test
    public void testGetAction() {
        log.info("getAction");

        Expansion expansion = new Expansion();
        Command instance = new Command(Command.Action.UNKNOWN, expansion);
        
        Command.Action result = instance.getAction();
        
        assertNotNull(result);
        assertEquals(Command.Action.UNKNOWN, result);
    }

    /**
     * Test of getExpansion method, of class Command.
     */
    @Test
    public void testGetExpansion() {
        log.info("getExpansion");

        Expansion expansion = new Expansion();
        Command instance = new Command(Command.Action.UNKNOWN, expansion);

        assertEquals(expansion, instance.getExpansion());
    }

    /**
     * Test of getException method, of class Command.
     */
    @Test
    public void testGetException() {
        log.info("getException");

        Expansion expansion = new Expansion();
        Command instance = new Command(Command.Action.UNKNOWN, expansion);
        assertNull(instance.getException());
    }

    /**
     * Test of doInBackground method, of class Command.
     */
    @Test
    public void testDoInBackground() throws Exception {
        log.info("doInBackground");
        
        Expansion expansion = Mockito.mock(Expansion.class);
        Command instance = new Command(Command.Action.INSTALL, expansion);
        Result result = instance.doInBackground();
        assertNotNull(result);
    }

    /**
     * Test of doInBackground method, of class Command.
     */
    @Test
    public void testDoInBackground2() throws Exception {
        log.info("doInBackground2");
        
        Expansion expansion = Mockito.mock(Expansion.class);
        Command instance = new Command(Command.Action.DELETE, expansion);
        Result result = instance.doInBackground();
        assertNotNull(result);
    }

    /**
     * Test of doInBackground method, of class Command.
     */
    @Test
    public void testDoInBackground3() throws Exception {
        log.info("doInBackground3");
        
        Expansion expansion = Mockito.mock(Expansion.class);
        Command instance = new Command(Command.Action.DISABLE, expansion);
        Result result = instance.doInBackground();
        assertNotNull(result);
    }

    /**
     * Test of doInBackground method, of class Command.
     */
    @Test
    public void testDoInBackground4() throws Exception {
        log.info("doInBackground4");
        
        Expansion expansion = Mockito.mock(Expansion.class);
        Command instance = new Command(Command.Action.ENABLE, expansion);
        Result result = instance.doInBackground();
        assertNotNull(result);
    }

    /**
     * Test of toString method, of class Command.
     */
    @Test
    public void testToString() {
        log.info("toString");

        Expansion expansion = Mockito.mock(Expansion.class);
        Command instance = new Command(Command.Action.INSTALL, expansion);
        assertTrue(instance.toString().matches("Command\\{action=INSTALL, expansion=Mock for Expansion, hashCode: \\d+, status=PENDING\\}"));
    }

}