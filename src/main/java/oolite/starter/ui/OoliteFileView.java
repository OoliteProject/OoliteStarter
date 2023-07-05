/*
 */

package oolite.starter.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;
import oolite.starter.Oolite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class OoliteFileView extends FileView {
    private static final Logger log = LogManager.getLogger();
    
    private static ImageIcon ii;
    
    static {
        try {
            BufferedImage bi = ImageIO.read(OoliteFileView.class.getResource("/oolite_logo.png"));
            ii = new ImageIcon(bi.getScaledInstance(24, 24, Image.SCALE_DEFAULT));
        } catch (Exception e) {
            log.error("Could not load icon", e);
        }
    }
        
    /**
     * Returns an icon representing the file or its type.
     * Huge images are accepted but will increase the space needed for each
     * file entry.
     * 
     * @param f
     * @return 
     */
    @Override
    public Icon getIcon(File f) {
        if (Oolite.isOoliteRelevant(f) != null) {
            return ii;
        } else {
            return super.getIcon(f);
        }
    }
    
}
