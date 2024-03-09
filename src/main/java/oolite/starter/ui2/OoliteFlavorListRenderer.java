/*
 */
package oolite.starter.ui2;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import oolite.starter.model.OoliteFlavor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class OoliteFlavorListRenderer extends javax.swing.JPanel implements ListCellRenderer<OoliteFlavor> {
    private static final Logger log = LogManager.getLogger();
    
    JEditorPane editor;

    /**
     * Creates new form OoliteFlavorListRenderer.
     */
    public OoliteFlavorListRenderer() {
        initComponents();
        editor = new JEditorPane();
        editor.setEditable(false);
        add(editor, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public Component getListCellRendererComponent(JList<? extends OoliteFlavor> list, OoliteFlavor data, int i, boolean isSelected, boolean isFocused) {
        StringBuilder sb = new StringBuilder("<html><table width=\"100%\" border=\"1\">");
        sb.append("<tr><td>");
        sb.append("<img src=\"").append(data.getImageUrl()).append("\"></img>");
        sb.append("</td><td width=\"*\"><b>");
        sb.append(data.getName());
        sb.append("</b><p>");
        sb.append(data.getDescription());
        sb.append("</td></tr>");
        sb.append("</table></html>");
        
        log.trace("text={}", sb);
        editor.setContentType("text/html");
        editor.setText(sb.toString());
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());

        return this;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
