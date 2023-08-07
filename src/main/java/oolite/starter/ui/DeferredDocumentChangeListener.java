/*
 */

package oolite.starter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Triggers an eventwhen the user stopped typing for long enough.
 * Inspired by https://stackoverflow.com/questions/31666428/action-listener-for-a-jtextfield-to-change-value-in-another-textfield/31666660#31666660
 * 
 * @author hiran
 */
public class DeferredDocumentChangeListener implements DocumentListener {
    private static final Logger log = LogManager.getLogger();

    private Timer timer;
    private List<ChangeListener> listeners;
    
    /**
     * Creates a new DeferredDocumentChangeListener with default 250 milliseconds
     * delay.
     */
    public DeferredDocumentChangeListener() {
        this(250);
    }
    
    /**
     * Creates a new DeferredDocumentChangeListener with given delay.
     * 
     * @param delay the delay in milliseconds
     */
    public DeferredDocumentChangeListener(int delay) {
        log.debug("DeferredDocumentChangeListener({})", delay);
        
        listeners = new ArrayList<>();
        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                fireStateChanged();
            }
        });
        timer.setRepeats(false);
    }
    
    /**
     * Adds the given change listener.
     * 
     * @param listener the listener
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes the given change listener.
     * 
     * @param listener the listener
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireStateChanged() {
        log.debug("fireStateChanged()");
        if (!listeners.isEmpty()) {
            ChangeEvent evt = new ChangeEvent(this);
            for (ChangeListener listener: listeners) {
                listener.stateChanged(evt);
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        timer.restart();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        timer.restart();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        timer.restart();
    }
    
    
}
