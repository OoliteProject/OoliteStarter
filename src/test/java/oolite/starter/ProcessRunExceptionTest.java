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
 
/**
 *
 * @author hiran
 */
public class ProcessRunExceptionTest {
    private static final Logger log = LogManager.getLogger();

     public ProcessRunExceptionTest() {
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

    @Test
    public void testProcessRunException() {
        log.info("testProcessRunException");
        
        Exception pre = new ProcessRunException("this message");
        assertEquals("oolite.starter.ProcessRunException", pre.getClass().getName());
        assertEquals("this message", pre.getMessage());
        assertNull(pre.getCause());
    }

    @Test
    public void testProcessRunException2() {
        log.info("testProcessRunException2");
        
        Exception cause = new Exception("cause");
        Exception pre = new ProcessRunException("this message", cause);
        assertEquals("oolite.starter.ProcessRunException", pre.getClass().getName());
        assertEquals("this message", pre.getMessage());
        assertEquals(cause, pre.getCause());
    }


    @Test
    public void testProcessRunException3() {
        log.info("testProcessRunException3");
        
        Exception cause = new Exception("mycause");
        Exception pre = new ProcessRunException(cause);
        assertEquals("oolite.starter.ProcessRunException", pre.getClass().getName());
        assertEquals("java.lang.Exception: mycause", pre.getMessage());
        assertEquals(cause, pre.getCause());
    }
}