/*
 */

package oolite.starter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
public class ProcessDataTest {
    private static final Logger log = LogManager.getLogger();

     public ProcessDataTest() {
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
     * Test of getCwd method, of class ProcessData.
     */
    @Test
    public void testGetCwd() {
        log.info("testGetCwd");

        File f = new File("random");
        List<String> command = new ArrayList<>();
        
        ProcessData instance = new ProcessData(f, command, 0);
        
        assertEquals(f, instance.getCwd());
    }

    /**
     * Test of getPid method, of class ProcessData.
     */
    @Test
    public void testGetPid() {
        log.info("getPid");

        File f = new File("random");
        List<String> command = new ArrayList<>();
        long pid = new Random().nextLong();
        
        ProcessData instance = new ProcessData(f, command, pid);
        
        assertEquals(pid, instance.getPid());
    }

    /**
     * Test of getCommand method, of class ProcessData.
     */
    @Test
    public void testGetCommand() {
        log.info("getCommand");
        
        File f = new File("random");
        List<String> command = new ArrayList<>();
        long pid = new Random().nextLong();
        
        ProcessData instance = new ProcessData(f, command, pid);
        
        assertEquals(command, instance.getCommand());
    }

}