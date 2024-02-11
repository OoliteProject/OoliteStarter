/*
 */

package oolite.starter.generic;

import java.awt.event.*;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Add an Action to a JList that can be invoked either by using
 * the keyboard or a mouse.
 *
 * By default the Enter will will be used to invoke the Action
 * from the keyboard although you can specify and KeyStroke you wish.
 *
 * A double click with the mouse will invoke the same Action.
 *
 * The Action can be reset at any time.
 */
public class ListAction implements MouseListener {
    private static final Logger log = LogManager.getLogger();
    
    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

    private JList list;
    private KeyStroke keyStroke;

    /**
     * Add an Action to the JList bound by the default KeyStroke.
     */
    public ListAction(JList list, Action action) {
        this(list, action, ENTER);
    }

    /**
     * Add an Action to the JList bound by the specified KeyStroke.
     */
    public ListAction(JList list, Action action, KeyStroke keyStroke) {
        this.list = list;
        this.keyStroke = keyStroke;

        //  Add the KeyStroke to the InputMap

        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);

        //  Add the Action to the ActionMap

        setAction( action );

        //  Handle mouse double click

        list.addMouseListener( this );
    }

    /**
     * Add the Action to the ActionMap.
     */
    public void setAction(Action action)
    {
        list.getActionMap().put(keyStroke, action);
    }

    // Implement MouseListener interface

    /**
     * Called when the mouse is clicked.
     * 
     * @param e the mouse event
     */
    public void mouseClicked(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            Action action = list.getActionMap().get(keyStroke);

            if (action != null)
            {
                ActionEvent event = new ActionEvent(
                        list,
                        ActionEvent.ACTION_PERFORMED,
                        "");
                action.actionPerformed(event);
            }
        }
    }

    /**
     * Called when the mouse enters the component. Does nothing.
     * 
     * @param e the mouse event
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Called when the mouse exits the component. Does nothing.
     * 
     * @param e the mouse event
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Called when the mouse button was pressed.
     * 
     * @param e the mouse event
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Called when the mouse is released.
     * 
     * @param e the mouse event
     */
    public void mouseReleased(MouseEvent e) {}
}
