/*
 */

package oolite.starter.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import net.java.balloontip.BalloonTip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilty functions that can be shared across UI components.
 * 
 * @author hiran
 */
public class Util {
    private static final Logger log = LogManager.getLogger();

    /**
     * Prevent instances from being created.
     */
    private Util() {
    }
    
    /**
     * Configures columnWidth so columns can render data nicely.
     * Preferred width is the average width, maximum width is also set to limit
     * excess.
     * 
     * @param jTable1 the table to set the columns on
     */
    public static void setColumnWidths(JTable jTable1) {
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            DefaultTableColumnModel colModel = (DefaultTableColumnModel) jTable1.getColumnModel();
            TableColumn col = colModel.getColumn(i);

            int maxWidth = 0;
            int avgWidth = 0;
            int rows = jTable1.getRowCount();
            
            TableCellRenderer renderer = null;
            {
                // try to get the right header renderer
                Object value = col.getHeaderValue();
                renderer = col.getHeaderRenderer();
                if (renderer == null) {
                    jTable1.getTableHeader().getDefaultRenderer();
                }
                if (renderer == null) {
                    renderer = jTable1.getDefaultRenderer(value.getClass());
                }
                
                if (renderer != null) {
                    rows ++;
                    Component comp = renderer.getTableCellRendererComponent(jTable1, value, false, false, 0, i);
                    maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
                    avgWidth += comp.getPreferredSize().width;
                }
            }
            
            for (int r = 0; r < jTable1.getRowCount(); r++) {
                renderer = jTable1.getCellRenderer(r, i);
                Component comp = renderer.getTableCellRendererComponent(
                        jTable1, jTable1.getValueAt(r, i), false, false, r, i);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
                avgWidth += comp.getPreferredSize().width;
            }

            avgWidth = avgWidth / rows;
            col.setPreferredWidth(Math.min(avgWidth, maxWidth) + 2);
            col.setMaxWidth(maxWidth);
        }
    }
    
    private static class BalloonHandler extends MouseAdapter implements ActionListener {

        private BalloonTip balloon;
        private int refreshRate = 10;
        private int fadeDuration = 3000;
        private ActionListener onStop;

        private int timeDelta;
        private int curTime= 0;
        private Timer timer;
        private boolean mouseContained = false;
        
        public BalloonHandler(BalloonTip balloon) {
            this.balloon = balloon;
        }

        public BalloonHandler(BalloonTip balloon, ActionListener onStop, int fadeDuration, int refreshrate) {
            this.balloon = balloon;
            this.onStop = onStop;
            this.fadeDuration = fadeDuration;
            this.refreshRate = refreshrate;
        }

        public BalloonHandler(BalloonTip balloon, ActionListener onStop) {
            this.balloon = balloon;
            this.onStop = onStop;
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            log.debug("mouseEntered({})", me);
            curTime = 0;
            mouseContained = true;
        }

        @Override
        public void mouseExited(MouseEvent me) {
            log.debug("mouseExited({})", me);

            if (balloon.contains(me.getPoint())) {
                log.trace("still inside");
            } else {
                mouseContained = false;
            }
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            log.trace("actionPerformed({})", ae);
            
            if (!mouseContained) {
                curTime += timeDelta;
            }

            float newOpacity = (-1.0f/fadeDuration)*curTime+1.0f;
            
            if (newOpacity <= 0.0f || Float.isNaN(newOpacity)) {

                ((Timer)ae.getSource()).stop();

                balloon.setOpacity(0.0f);
                if (onStop != null) {
                    onStop.actionPerformed(ae);
                }

                balloon.removeMouseListener(this);
            } else {
                balloon.setOpacity(newOpacity);
            }
            
            if (!balloon.isVisible()) {
                timer.stop();
            }
        }

        public void start() {
            balloon.addMouseListener(this);

            timeDelta = 1000/refreshRate;

            timer = new Timer(timeDelta, this);
            timer.setRepeats(true);
            timer.start();
        }
    }

    /**
     * Execute a fade-in effect on a balloon tip.
     * @param balloon the balloon tip
     * @param onStop this action listener is triggered once the effect has stopped (may be null)
     * @param time the duration of the fade-out effect (in ms)
     * @param refreshRate at how many frames-per-second should the effect run
     */
    public static void fadeOutBalloon(final BalloonTip balloon, final ActionListener onStop, final int time, final int refreshRate) {
        balloon.setOpacity(0.9999999f);
        balloon.setVisible(true);
        
        new BalloonHandler(balloon, onStop, time, refreshRate).start();
    }
    
}
