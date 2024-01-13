/*
 */
package oolite.starter.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import oolite.starter.Oolite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author hiran
 */
public class ExpansionTest {
    private static final Logger log = LogManager.getLogger();
    
    public ExpansionTest() {
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
     * Test of testSetGetAuthor method, of class Expansion.
     */
    @Test
    public void testSetGetAuthor() {
        log.info("testSetGetAuthor");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getAuthor());
        
        expansion.setAuthor("auth");
        assertEquals("auth", expansion.getAuthor());
    }

    /**
     * Test of testSetGetCategory method, of class Expansion.
     */
    @Test
    public void testSetGetCategory() {
        log.info("testSetGetCategory");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getCategory());
        
        expansion.setCategory("cat");
        assertEquals("cat", expansion.getCategory());
    }

    /**
     * Test of testSetGetConflictOxps method, of class Expansion.
     */
    @Test
    public void testSetGetConflictOxps() {
        log.info("testSetGetConflictOxps");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getConflictOxps());
        
        List<Expansion.Dependency> oxpRefs = new ArrayList<>();
        expansion.setConflictOxps(oxpRefs);
        assertEquals(oxpRefs, expansion.getConflictOxps());
    }

    /**
     * Test of testSetGetDescription method, of class Expansion.
     */
    @Test
    public void testSetGetDescription() {
        log.info("testSetGetDescription");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getDescription());
        
        expansion.setDescription("conflict");
        assertEquals("conflict", expansion.getDescription());
    }

    /**
     * Test of testSetGetDownloadUrl method, of class Expansion.
     */
    @Test
    public void testSetGetDownloadUrl() {
        log.info("testSetGetDownloadUrl");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getDownloadUrl());
        
        expansion.setDownloadUrl("value");
        assertEquals("value", expansion.getDownloadUrl());
    }

    /**
     * Test of testSetGetIdentifier method, of class Expansion.
     */
    @Test
    public void testSetGetIdentifier() {
        log.info("testSetGetIdentifier");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getIdentifier());
        
        expansion.setIdentifier("value");
        assertEquals("value", expansion.getIdentifier());
    }

    /**
     * Test of testSetGetInformationUrl method, of class Expansion.
     */
    @Test
    public void testSetGetInformationUrl() {
        log.info("testSetGetInformationUrl");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getInformationUrl());
        
        expansion.setInformationUrl("value");
        assertEquals("value", expansion.getInformationUrl());
    }

    /**
     * Test of testSetGetLicense method, of class Expansion.
     */
    @Test
    public void testSetGetLicense() {
        log.info("testSetGetLicense");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getLicense());
        
        expansion.setLicense("value");
        assertEquals("value", expansion.getLicense());
    }

    /**
     * Test of testSetGetMaximumOoliteVersion method, of class Expansion.
     */
    @Test
    public void testSetGetMaximumOoliteVersion() {
        log.info("testSetGetMaximumOoliteVersion");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getMaximumOoliteVersion());
        
        expansion.setMaximumOoliteVersion("value");
        assertEquals("value", expansion.getMaximumOoliteVersion());
    }

    /**
     * Test of testSetGetOptionalOxps method, of class Expansion.
     */
    @Test
    public void testSetGetOptionalOxps() {
        log.info("testSetGetOptionalOxps");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getOptionalOxps());

        List<Expansion.Dependency> oxpRefs = new ArrayList<>();
        expansion.setOptionalOxps(oxpRefs);
        assertEquals(oxpRefs, expansion.getOptionalOxps());
    }

    /**
     * Test of testSetGetRequiredOoliteVersion method, of class Expansion.
     */
    @Test
    public void testSetGetRequiredOoliteVersion() {
        log.info("testSetGetRequiredOoliteVersion");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getRequiredOoliteVersion());
        
        expansion.setRequiredOoliteVersion("value");
        assertEquals("value", expansion.getRequiredOoliteVersion());
    }

    /**
     * Test of testSetGetRequiresOxps method, of class Expansion.
     */
    @Test
    public void testSetGetRequiresOxps() {
        log.info("testSetGetRequiresOxps");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getRequiresOxps());
        
        List<Expansion.Dependency> oxpRefs = new ArrayList<>();
        expansion.setRequiresOxps(oxpRefs);
        assertEquals(oxpRefs, expansion.getRequiresOxps());
    }

    /**
     * Test of testSetGetTags method, of class Expansion.
     */
    @Test
    public void testSetGetTags() {
        log.info("testSetGetTags");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getTags());
        
        expansion.setTags("value");
        assertEquals("value", expansion.getTags());
    }

    /**
     * Test of testSetGetTitle method, of class Expansion.
     */
    @Test
    public void testSetGetTitle() {
        log.info("testSetGetTitle");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getTitle());
        
        expansion.setTitle("value");
        assertEquals("value", expansion.getTitle());
    }

    /**
     * Test of testSetGetVersion method, of class Expansion.
     */
    @Test
    public void testSetGetVersion() {
        log.info("testSetGetVersion");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getVersion());
        
        expansion.setVersion("value");
        assertEquals("value", expansion.getVersion());
    }

    /**
     * Test of testSetGetFileSize method, of class Expansion.
     */
    @Test
    public void testSetGetFileSize() {
        log.info("testSetGetFileSize(");
        
        Expansion expansion = new Expansion();
        assertEquals(0, expansion.getFileSize());
        
        expansion.setFileSize(2356);
        assertEquals(2356, expansion.getFileSize());
    }

    /**
     * Test of testSetIsOnline method, of class Expansion.
     */
    @Test
    public void testSetIsOnline() {
        log.info("testSetIsOnline(");
        
        Expansion expansion = new Expansion();
        assertEquals(false, expansion.isOnline());
        
        expansion.setOnline(true);
        assertEquals(true, expansion.isOnline());
    }

    /**
     * Test of testSetIsEnabled method, of class Expansion.
     */
    @Test
    public void testSetIsEnabled() {
        log.info("testSetIsEnabled(");
        
        Expansion expansion = new Expansion();
        try {
            expansion.isEnabled();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("oolite must be set before", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    /**
     * Test of testSetIsEnabled method, of class Expansion.
     */
    @Test
    public void testSetIsEnabled2() {
        log.info("testSetIsEnabled2(");
        
        Expansion expansion = new Expansion();
        Oolite oolite = Mockito.mock(Oolite.class);
        expansion.setOolite(oolite);
        
        assertFalse(expansion.isEnabled());
    }

    /**
     * Test of testSetIsEnabled method, of class Expansion.
     */
    @Test
    public void testSetIsEnabled3() throws IOException {
        log.info("testSetIsEnabled3(");
        
        Expansion expansion = new Expansion();
        Oolite oolite = Mockito.mock(Oolite.class);
        Mockito.when(oolite.isDisabled(Mockito.any())).thenReturn(false);
        
        File exp = new File("/somewhere/expansion");
        expansion.setLocalFile(exp);
        
        expansion.setFileSize(0);
        expansion.setOolite(oolite);
        
        assertEquals(true, expansion.isEnabled());

        Mockito.when(oolite.isDisabled(Mockito.any())).thenReturn(true);
        assertEquals(false, expansion.isEnabled());
    }
    
    @Test
    public void testSetIsManaged() throws IOException {
        log.info("testSetIsManaged");
        
        Oolite oolite = Mockito.mock(Oolite.class);
        Mockito.when(oolite.isDisabled(Mockito.any())).thenReturn(false);

        Expansion expansion = new Expansion();
        expansion.setOolite(oolite);
        
        File exp = new File("/somewhere/expansion");
        expansion.setLocalFile(exp);
        
        assertEquals(false, expansion.isManaged());
    }

    /**
     * Test of testSetGetUploadDate method, of class Expansion.
     */
    @Test
    public void testSetGetUploadDate() {
        log.info("testSetGetUploadDate(");
        
        Expansion expansion = new Expansion();
        assertEquals(null, expansion.getUploadDate());
        
        LocalDateTime ldt = LocalDateTime.now();
        expansion.setUploadDate(ldt);
        assertEquals(ldt, expansion.getUploadDate());
    }

    /**
     * Test of testSetGetOolite method, of class Expansion.
     */
    @Test
    public void testSetGetOolite() {
        log.info("testSetGetOolite(");
        
        Expansion expansion = new Expansion();
        assertEquals(null, expansion.getOolite());
        
        Oolite ldt = new Oolite();
        expansion.setOolite(ldt);
        assertEquals(ldt, expansion.getOolite());
    }

    /**
     * Test of testSetGetLocalFile method, of class Expansion.
     */
    @Test
    public void testSetGetLocalFile() {
        log.info("testSetGetLocalFile(");
        
        Expansion expansion = new Expansion();
        assertEquals(null, expansion.getLocalFile());
        assertFalse(expansion.isLocal());
        
        File ldt = new File("dummy");
        expansion.setLocalFile(ldt);
        assertEquals(ldt, expansion.getLocalFile());
        assertTrue(expansion.isLocal());
    }
    
    @Test
    public void testInstall() throws IOException {
        log.info("testInstall");
        
        Expansion expansion = new Expansion();
        try {
            expansion.install();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("oolite must be set before", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testInstall2() throws IOException {
        log.info("testInstall2");
        
        Expansion expansion = new Expansion();
        assertNull(expansion.getOolite());
        
        Oolite oolite = Mockito.mock(Oolite.class);
        expansion.setOolite(oolite);
        
        expansion.install();
        
        assertTrue(true);
    }
    
    @Test
    public void testEnable() throws IOException {
        log.info("testEnable");
        
        Expansion expansion = new Expansion();
        try {
            expansion.enable();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("oolite must be set before", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testEnable2() throws IOException {
        log.info("testEnable2");
        
        Expansion expansion = new Expansion();
        Oolite oolite = Mockito.mock(Oolite.class);
        expansion.setOolite(oolite);
        
        expansion.enable();
        
        assertTrue(true);
    }
    
    @Test
    public void testDisable() throws IOException {
        log.info("testDisable");
        
        Expansion expansion = new Expansion();
        try {
            expansion.disable();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("oolite must be set before", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testDisable2() throws IOException {
        log.info("testDisable2");
        
        Expansion expansion = new Expansion();
        Oolite oolite = Mockito.mock(Oolite.class);
        expansion.setOolite(oolite);
        
        expansion.disable();
        
        assertTrue(true);
    }
    
    @Test
    public void testRemove() throws IOException {
        log.info("testRemove");
        
        Expansion expansion = new Expansion();
        try {
            expansion.remove();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("oolite must be set before", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testRemove2() throws IOException {
        log.info("testRemove2");
        
        Expansion expansion = new Expansion();
        Oolite oolite = Mockito.mock(Oolite.class);
        expansion.setOolite(oolite);
        
        expansion.remove();
        
        assertTrue(true);
    }
    
    @Test
    public void testToString() {
        log.info("testToString");
        
        Expansion expansion = new Expansion();
        assertEquals("oolite.starter.model.Expansion(null, null, null)", expansion.toString());
        
        expansion.setIdentifier("identifier");
        assertEquals("oolite.starter.model.Expansion(identifier, null, null)", expansion.toString());
        
        expansion.setVersion("version");
        assertEquals("oolite.starter.model.Expansion(identifier, version, null)", expansion.toString());
        
        expansion.setDownloadUrl("url");
        assertEquals("oolite.starter.model.Expansion(identifier, version, url)", expansion.toString());
    }
    
    @Test
    public void testCompareTo() {
        log.info("testCompareTo");
        
        Expansion e1 = new Expansion();
        assertEquals(0, e1.compareTo(e1));
    }
    
    @Test
    public void testCompareTo2() {
        log.info("testCompareTo2");
        
        Expansion e1 = new Expansion();
        Expansion e2 = new Expansion();
        assertEquals(0, e1.compareTo(e2));
    }

    @Test
    public void testCompareTo3() {
        log.info("testCompareTo3");
        
        Expansion e1 = new Expansion();
        e1.setIdentifier("blah");
        Expansion e2 = new Expansion();
        assertEquals(-12, e1.compareTo(e2));
        assertEquals(12, e2.compareTo(e1));
    }

    @Test
    public void testCompareTo4() {
        log.info("testCompareTo4");
        
        Expansion e1 = new Expansion();
        e1.setIdentifier("blah");
        Expansion e2 = new Expansion();
        e2.setIdentifier("blah");
        e2.setVersion("1.2.3");
        assertEquals(61, e1.compareTo(e2));
        assertEquals(-61, e2.compareTo(e1));
    }
    
    @Test
    public void testEquals() {
        log.info("testEquals");

        Expansion e1 = new Expansion();
        assertFalse(e1.equals("Hallo"));
        
        Expansion e2 = new Expansion();
        assertTrue(e1.equals(e2));

        assertTrue(e1.equals(e1));
    }
    
    private class TestPropertyChangeListener implements PropertyChangeListener {
        
        private int count;

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            count++;
        }

        public int getCount() {
            return count;
        }
        
    }
    
    @Test
    public void testPropertyChangeSupport() {
        log.info("testPropertyChangeSupport");
        
        TestPropertyChangeListener l = new TestPropertyChangeListener();
        
        Expansion expansion = new Expansion();
        expansion.addPropertyChangeListener(l);
        assertEquals(0, l.getCount());
        
        expansion.setLocalFile(new File("dummy"));
        assertEquals(1, l.getCount());
        
        expansion.removePropertyChangeListener(l);
        expansion.setLocalFile(new File("dummy2"));
        assertEquals(1, l.getCount());
    }
    
    @Test
    public void testHashCode() {
        log.info("testHashCode");
        
        Expansion expansion = new Expansion();
        assertEquals(-1905561714, expansion.hashCode());
        
        expansion.setIdentifier("id");
        assertEquals(-1193161438, expansion.hashCode());
        
        expansion.setVersion("v1");
        assertEquals(3227862, expansion.hashCode());
    }
    
    @Test
    public void testEMStatus() {
        log.info("testEMStatus");
        
        Expansion.EMStatus instance = new Expansion.EMStatus();
        assertNull(instance.getColor());
        assertEquals(false, instance.isConflicting());
        assertEquals(false, instance.isIncompatible());
        assertEquals(false, instance.isLatest());
        assertEquals(false, instance.isMissingDeps());
        
        instance.setColor(Color.yellow);
        assertEquals(Color.yellow, instance.getColor());
        
        instance.setConflicting(true);
        assertTrue(instance.isConflicting());
        
        instance.setIncompatible(true);
        assertTrue(instance.isIncompatible());
        
        instance.setLatest(true);
        assertTrue(instance.isLatest());
        
        instance.setMissingDeps(true);
        assertTrue(instance.isMissingDeps());
    }
    
    @Test
    public void testEMStatus2() {
        log.info("testEMStatus2");
        
        Color color = Color.BLUE;
        Expansion.EMStatus instance = new Expansion.EMStatus(color, false, false, false, false);
        assertEquals(color, instance.getColor());
        assertEquals(false, instance.isConflicting());
        assertEquals(false, instance.isIncompatible());
        assertEquals(false, instance.isLatest());
        assertEquals(false, instance.isMissingDeps());
    }
    
    @Test
    public void testEMStatus3() {
        log.info("testEMStatus3");
        
        Color color = Color.black;
        Expansion.EMStatus instance = new Expansion.EMStatus(color, true, false, false, false);
        assertEquals(color, instance.getColor());
        assertEquals(false, instance.isConflicting());
        assertEquals(false, instance.isIncompatible());
        assertEquals(true, instance.isLatest());
        assertEquals(false, instance.isMissingDeps());
    }
    
    @Test
    public void testEMStatus4() {
        log.info("testEMStatus4");
        
        Color color = Color.yellow;
        Expansion.EMStatus instance = new Expansion.EMStatus(color, true, true, false, false);
        assertEquals(color, instance.getColor());
        assertEquals(true, instance.isConflicting());
        assertEquals(false, instance.isIncompatible());
        assertEquals(true, instance.isLatest());
        assertEquals(false, instance.isMissingDeps());
    }
    
    @Test
    public void testEMStatus5() {
        log.info("testEMStatus5");
        
        Color color = Color.CYAN;
        Expansion.EMStatus instance = new Expansion.EMStatus(color, true, true, true, false);
        assertEquals(color, instance.getColor());
        assertEquals(true, instance.isConflicting());
        assertEquals(false, instance.isIncompatible());
        assertEquals(true, instance.isLatest());
        assertEquals(true, instance.isMissingDeps());
    }
    
    @Test
    public void testEMStatus6() {
        log.info("testEMStatus6");
        
        Color color = Color.PINK;
        Expansion.EMStatus instance = new Expansion.EMStatus(color, true, true, true, true);
        assertEquals(color, instance.getColor());
        assertEquals(true, instance.isConflicting());
        assertEquals(true, instance.isIncompatible());
        assertEquals(true, instance.isLatest());
        assertEquals(true, instance.isMissingDeps());
    }
    
    @Test
    public void testGetRequiredRefs() {
        log.info("testGetRequiredRefs");
        
        List<Expansion.Dependency> oxpRefs = new ArrayList<>();
        oxpRefs.add(new Expansion.Dependency("org.oolite.One", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.One", "1.1"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Two", "1.0"));

        Oolite oolite = Mockito.mock(Oolite.class);
        Mockito.when(oolite.getExpansionReference("org.oolite.One:1.0")).thenReturn(new ExpansionReference("org.oolite.One"));
        Mockito.when(oolite.getExpansionReference("org.oolite.One:1.1")).thenReturn(new ExpansionReference("org.oolite.One"));
        Mockito.when(oolite.getExpansionReference("org.oolite.Two:1.0")).thenReturn(new ExpansionReference("org.oolite.Two"));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(0))).thenReturn(new ExpansionReference("org.oolite.One:1.0"));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(1))).thenReturn(new ExpansionReference("org.oolite.One:1.1"));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(2))).thenReturn(new ExpansionReference("org.oolite.Two:1.0"));
        //Mockito.when(oolite.getExpansionReference(Mockito.any())).thenReturn(null);

        Expansion expansion = new Expansion();
        expansion.setOolite(oolite);
        expansion.setRequiresOxps(oxpRefs);

        List<ExpansionReference> result = expansion.getRequiredRefs();

        assertEquals(3, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
        assertNotNull(result.get(2));
        assertEquals("org.oolite.One:1.0", result.get(0).getName());
        assertEquals("org.oolite.One:1.1", result.get(1).getName());
        assertEquals("org.oolite.Two:1.0", result.get(2).getName());
    }
    
    @Test
    public void testGetConflictRefs() {
        log.info("testGetConflictRefs");
        
        List<Expansion.Dependency> oxpRefs = new ArrayList<>();
        oxpRefs.add(new Expansion.Dependency("org.oolite.One", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.One", "1.1"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Two", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Three", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Four", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Five", "1.0"));

        Oolite oolite = Mockito.mock(Oolite.class);
        Mockito.when(oolite.getExpansionReference("org.oolite.One:1.0")).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference("org.oolite.One:1.1")).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference("org.oolite.Two:1.0")).thenReturn(new ExpansionReference("org.oolite.Two", ExpansionReference.Status.OK));
        Mockito.when(oolite.getExpansionReference("org.oolite.Three:1.0")).thenReturn(new ExpansionReference("org.oolite.Three", ExpansionReference.Status.CONFLICT));
        Mockito.when(oolite.getExpansionReference("org.oolite.Four:1.0")).thenReturn(new ExpansionReference("org.oolite.Four", ExpansionReference.Status.REQUIRED_MISSING));
        Mockito.when(oolite.getExpansionReference("org.oolite.Five:1.0")).thenReturn(new ExpansionReference("org.oolite.Five", ExpansionReference.Status.SURPLUS));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(0))).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(1))).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(2))).thenReturn(new ExpansionReference("org.oolite.Two", ExpansionReference.Status.OK));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(3))).thenReturn(new ExpansionReference("org.oolite.Three", ExpansionReference.Status.CONFLICT));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(4))).thenReturn(new ExpansionReference("org.oolite.Four", ExpansionReference.Status.REQUIRED_MISSING));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(5))).thenReturn(new ExpansionReference("org.oolite.Five", ExpansionReference.Status.SURPLUS));
        //Mockito.when(oolite.getExpansionReference(Mockito.any())).thenReturn(null);

        Expansion expansion = new Expansion();
        expansion.setOolite(oolite);
        expansion.setConflictOxps(oxpRefs);

        List<ExpansionReference> result = expansion.getConflictRefs();

        assertEquals(6, result.size());
        assertEquals("org.oolite.One", result.get(0).getName());
        assertEquals("org.oolite.One", result.get(1).getName());
        assertEquals("org.oolite.Two", result.get(2).getName());
        assertEquals("org.oolite.Three", result.get(3).getName());
        assertEquals("org.oolite.Four", result.get(4).getName());
        assertEquals("org.oolite.Five", result.get(5).getName());
    }
    
    @Test
    public void testGetOptionalRefs() {
        log.info("testGetOptionalRefs");
        
        List<Expansion.Dependency> oxpRefs = new ArrayList<>();
        oxpRefs.add(new Expansion.Dependency("org.oolite.One", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.One", "1.1"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Two", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Three", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Four", "1.0"));
        oxpRefs.add(new Expansion.Dependency("org.oolite.Five", "1.0"));

        Oolite oolite = Mockito.mock(Oolite.class);
        Mockito.when(oolite.getExpansionReference("org.oolite.One:1.0")).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference("org.oolite.One:1.1")).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference("org.oolite.Two:1.0")).thenReturn(new ExpansionReference("org.oolite.Two", ExpansionReference.Status.OK));
        Mockito.when(oolite.getExpansionReference("org.oolite.Three:1.0")).thenReturn(new ExpansionReference("org.oolite.Three", ExpansionReference.Status.CONFLICT));
        Mockito.when(oolite.getExpansionReference("org.oolite.Four:1.0")).thenReturn(new ExpansionReference("org.oolite.Four", ExpansionReference.Status.REQUIRED_MISSING));
        Mockito.when(oolite.getExpansionReference("org.oolite.Five:1.0")).thenReturn(new ExpansionReference("org.oolite.Five", ExpansionReference.Status.SURPLUS));

        Mockito.when(oolite.getExpansionReference(oxpRefs.get(0))).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(1))).thenReturn(new ExpansionReference("org.oolite.One", ExpansionReference.Status.MISSING));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(2))).thenReturn(new ExpansionReference("org.oolite.Two", ExpansionReference.Status.OK));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(3))).thenReturn(new ExpansionReference("org.oolite.Three", ExpansionReference.Status.CONFLICT));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(4))).thenReturn(new ExpansionReference("org.oolite.Four", ExpansionReference.Status.REQUIRED_MISSING));
        Mockito.when(oolite.getExpansionReference(oxpRefs.get(5))).thenReturn(new ExpansionReference("org.oolite.Five", ExpansionReference.Status.SURPLUS));
        //Mockito.when(oolite.getExpansionReference(Mockito.any())).thenReturn(null);

        Expansion expansion = new Expansion();
        expansion.setOolite(oolite);
        expansion.setOptionalOxps(oxpRefs);

        List<ExpansionReference> result = expansion.getOptionalRefs();

        assertEquals(6, result.size());
        assertEquals("org.oolite.One", result.get(0).getName());
        assertEquals("org.oolite.One", result.get(1).getName());
        assertEquals("org.oolite.Two", result.get(2).getName());
        assertEquals("org.oolite.Three", result.get(3).getName());
        assertEquals("org.oolite.Four", result.get(4).getName());
        assertEquals("org.oolite.Five", result.get(5).getName());
    }
    
    @Test
    public void testIsNested() {
        log.info("testIsNested");
        
        Expansion instance = new Expansion();
        boolean result = instance.isNested();
        
        assertFalse(result);
    }
    
    @Test
    public void testIsNested2() {
        log.info("testIsNested2");
        
        Expansion instance = new Expansion();
        instance.setLocalFile(new File("manhattan.oxp"));
        boolean result = instance.isNested();
        
        assertFalse(result);
    }
    
    @Test
    public void testIsNested3() {
        log.info("testIsNested3");
        
        Expansion instance = new Expansion();
        instance.setLocalFile(new File("new york.oxp/manhattan.oxp"));
        boolean result = instance.isNested();
        
        assertTrue(result);
    }
}
