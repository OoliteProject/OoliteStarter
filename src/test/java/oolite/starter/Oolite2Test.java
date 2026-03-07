package oolite.starter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author hiran
 */
public class Oolite2Test {
    private static final Logger log = LogManager.getLogger();

    @Test
    public void testGetVersionFromHelpMenu() {
        log.info("testGetVersionFromHelpMenu");
        
        try {
            Oolite2.getVersionFromHelpMenu(null);
            fail("exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("helpMenu must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testGetVersionFromHelpMenu1() {
        log.info("testGetVersionFromHelpMenu1");
        
        String result = Oolite2.getVersionFromHelpMenu("");
        assertNull(result);
    }

    @Test
    public void testGetVersionFromHelpMenu2() {
        log.info("testGetVersionFromHelpMenu2");
        
        String h = """
2026-03-07 22:52:05.568 oolite[37922:37923] Truncating thread name 'OOLogOutputHandler logging thread' to 15 characters due to platform limitations
Usage: oolite [options]

Options can be any of the following: 

--compile-sysdesc			Compile system descriptions *
--export-sysdesc			Export system descriptions *
-load [filepath]			Load commander from [filepath]
                                     ("-load" is optional)
-message [messageString]		Display [messageString] at startup
-nodust    				Do not draw space dust
-noshaders				Start up with shaders disabled
-nosplash    				Force disable splash screen on startup
-nosound    				Start up with sound disabled
-novsync				Force disable V-Sync
--openstep				When compiling or exporting
                                     system descriptions, use openstep
                                     format *
-showversion				Display version at startup screen
-splash					Force splash screen on startup
-verify-oxp [filepath]    		Verify OXP at [filepath] *
--xml					When compiling or exporting
                                     system descriptions, use xml
                                     format *

Options marked with "*" are available only in Test Release configuration.
Version 1.92.1.7797-260228-9ca54c9
Built with Clang version 14.0.0
                                      """;
        
        String result = Oolite2.getVersionFromHelpMenu(h);
        assertEquals("1.92.1.7797-260228-9ca54c9", result);
    }
}
