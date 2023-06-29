/*
 */

package oolite.starter.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import oolite.starter.Oolite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This FileChooserAccessory shall support users to select the
 * right filesystem location.
 * 
 * @author hiran
 */
public class OoliteFileChooserAccessory extends JPanel implements PropertyChangeListener {
    private static final Logger log = LogManager.getLogger();
    
    private JTextArea jta;
    
    /**
     * The file we are showing right now.
     */
    private File file;

    /**
     * Creates a new OoliteFileChooserAccessory.
     */
    public OoliteFileChooserAccessory(JFileChooser jfc) {
        setLayout(new BorderLayout());
        jta = new JTextArea();
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        add(new JScrollPane(jta), BorderLayout.CENTER);
        //setBorder(new LineBorder(Color.red));
        
        jfc.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        log.debug("propertyChange({})", pce);

        switch (pce.getPropertyName()) {
            case JFileChooser.DIRECTORY_CHANGED_PROPERTY:
            case JFileChooser.SELECTED_FILES_CHANGED_PROPERTY:
            case JFileChooser.SELECTED_FILE_CHANGED_PROPERTY:
                File f = (File)pce.getNewValue();
                jta.setText(Oolite.getDescription(Oolite.isOoliteRelevant(f)));
        }
        
    }
    
}
