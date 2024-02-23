/*
 */

package oolite.starter.generic;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class shall support Cut, Copy and Paste operations for non-text components.
 * Taken from https://docs.oracle.com/javase/tutorial/uiswing/dnd/listpaste.html
 * but we are still unsure how to apply it.
 * 
 * @author hiran
 */
public class TransferActionListener implements ActionListener, PropertyChangeListener {
    private static final Logger log = LogManager.getLogger();

    private JComponent focusOwner = null;

    /**
     * Creates a new TransferActionListener.
     */
    public TransferActionListener() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addPropertyChangeListener("permanentFocusOwner", this);
    }

    /**
     * Called whenever a property changes. For some reason this
     * action listener at the same time is a property change listener.
     * 
     * @param e 
     */
    public void propertyChange(PropertyChangeEvent e) {
        Object o = e.getNewValue();
        if (o instanceof JComponent) {
            focusOwner = (JComponent)o;
        } else {
            focusOwner = null;
        }
    }

    /**
     * Called when the action is performed (the cut/copy/paste operation
     * is triggered by the user).
     * 
     * @param e 
     */
    public void actionPerformed(ActionEvent e) {
        if (focusOwner == null)
            return;
        String action = (String)e.getActionCommand();
        Action a = focusOwner.getActionMap().get(action);
        if (a != null) {
            a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
        }
    }
}
