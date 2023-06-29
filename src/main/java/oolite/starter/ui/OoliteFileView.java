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
     * Returns a description of the file. The intent is to describe individual 
     * files more specifically. A common implementation of this method returns 
     * null to indicate that the look and feel should figure it out.
     * 
     * @param f
     * @return 
     */
    @Override
    public String getDescription(File f) {
        //return super.getDescription(f);
        return "Description";
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

    /**
     * Returns the name of the file. Most implementations of this method should 
     * return null to indicate that the look and feel should figure it out. 
     * Another common implementation returns f.getName().
     * 
     * @param f
     * @return 
     */
    @Override
    public String getName(File f) {
        return null;
    }

    @Override
    public String getTypeDescription(File f) {
        return super.getTypeDescription(f);
    }

    @Override
    public Boolean isTraversable(File f) {
        return super.isTraversable(f);
    }
    
}
