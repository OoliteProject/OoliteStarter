/*
 */
package oolite.starter.ui2;

import java.util.List;
import oolite.starter.Oolite;
import oolite.starter.model.Expansion;
import oolite.starter.ui.HyperLinkListener;
import oolite.starter.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Panel to show expansion details.
 * 
 * @author hiran
 */
public class ExpansionPanel extends javax.swing.JPanel implements ExpansionsPanel2.SelectionListener {
    private static final Logger log = LogManager.getLogger();
    
    /**
     * Creates new form ExpansionPanel.
     */
    public ExpansionPanel() {
        initComponents();
        jEditorPane1.addHyperlinkListener(new HyperLinkListener());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    void addCurrentlyRequired(Expansion expansion, StringBuilder sb) {
        sb.append("<h2><font color=\"ffc800\">■</font> Required by</h2>");
        sb.append("<p>Do not remove. Otherwise the following other expansions will not work correctly due to missing dependencies:</p>");
        sb.append("<table>");
        for (Expansion e: expansion.getEMStatus().getRequiredBy()) {
            if (e.isEnabled()) {
                sb.append("<tr><td>").append(e.getTitle()).append(" ").append(e.getVersion()).append("</td></tr>");
            }
        }
        sb.append("</table>");
    }
    
    @Override
    public void selectionChanged(Expansion expansion) {
        log.debug("selectionChanged({})", expansion);
        
        StringBuilder sb = new StringBuilder("<html>");
        if (expansion != null) {
            sb.append("<h2>").append(expansion.getTitle()).append(" ").append(expansion.getVersion()).append("</h2>");
            sb.append("<table border=\"0\"><tr>");
            sb.append("<td>Title</td><td colspan=\"4\">").append(expansion.getTitle()).append("</td>");
            sb.append("</tr><tr>");
            sb.append("<td valign=\"top\">Description</td><td colspan=\"3\" valign=\"top\">").append(expansion.getDescription()).append("</td>");
            sb.append("</tr><tr>");
            sb.append("<td>Version</td><td>").append(expansion.getVersion()).append("</td><td>Category</td><td>").append(expansion.getCategory()).append("</td><td></td>");
            sb.append("</tr><tr>");
            sb.append("<td>Size</td><td>").append(Util.humanreadableSize(expansion.getFileSize())).append("</td><td>Author</td><td>").append(expansion.getAuthor()).append("</td><td></td>");
            sb.append("</tr><tr>");
            sb.append("<td>Local File</td><td colspan=\"3\">").append(expansion.getLocalFile()).append("</td>");
            sb.append("</tr><tr>");
            sb.append("<td>Download&nbsp;URL</td><td colspan=\"3\">").append(expansion.getDownloadUrl()).append("</td>");
            sb.append("</tr><tr>");
            sb.append("<td></td><td>");

            String urlstr = Oolite.getOoliteWikiPageUrl(expansion.getTitle());
            sb.append(" <a href=\"" + urlstr + "\">Wiki Page</a> ");

            if (!urlstr.equals(expansion.getInformationUrl())) {
                sb.append(" <a href=\"" + expansion.getInformationUrl() + "\">Information</a> ");
            }
            sb.append("</td>");
            sb.append("</tr></table>");

            if (expansion.getEMStatus().isConflicting()) {
                sb.append("<h2><font color=\"ff0000\">■</font> Conflicting with</h2>");
                sb.append("<p>This expansion is known to not work together with the following ones. Decide which to keep, otherwise Oolite has to decide what to load on startup.</p>");
                sb.append("<table>");
                List<Expansion> cs = expansion.getEMStatus().getConflicting();
                if (cs == null || cs.isEmpty()) {
                    sb.append("<tr><td>Other expansions declare conflicts with this one</td></tr>");
                } else {
                    for (Expansion e: cs) {
                        sb.append("<tr><td>").append(e.getTitle()).append(" ").append(e.getVersion()).append("</td></tr>");
                    }
                }
                sb.append("</table>");
            }
            if (expansion.getEMStatus().isIncompatible()) {
                sb.append("<h2><font color=\"ff0000\">■</font> Incompatible!</h2>");
                sb.append("<p>This expansion is not compatible with the current version of Oolite.</p>");
            }
            if (expansion.getEMStatus().isMissingDeps()) {
                sb.append("<h2><font color=\"ff0000\">■</font> Missing Dependencies</h2>");
                sb.append("<p>This expansion will not work properly due to these missing dependencies:</p>");
                sb.append("<table>");
                for (Expansion e: expansion.getEMStatus().getMissing()) {
                    sb.append("<tr><td>").append(e.getTitle()).append(" ").append(e.getVersion()).append("</td></tr>");
                }
                sb.append("</table>");
            } else if (expansion.getRequiresOxps() != null && !expansion.getRequiresOxps().isEmpty()) {
                sb.append("<h2>Required Dependencies</h2>");
                sb.append("<p>This expansion will not work properly if one of these dependencies is missing:</p>");
                sb.append("<table>");
                for (Expansion.Dependency d: expansion.getRequiresOxps()) {
                    sb.append("<tr><td>").append(d.getIdentifier()).append(" ").append(d.getVersion()).append("</td></tr>");
                }
                sb.append("</table>");
            }
            if (expansion.getEMStatus().isCurrentlyRequired()) {
                addCurrentlyRequired(expansion, sb);
            }
            if (!expansion.getEMStatus().isLatest()) {
                Expansion exp = expansion.getEMStatus().getLatest();
                sb.append("<h2><font color=\"0000ff\">■</font> Update to</h2>");
                sb.append("<p>There is an updated version available that you likely want to install.</p>");
                sb.append("<h3>").append(exp.getTitle()).append(" ").append(exp.getVersion()).append("</h3>");
                sb.append("<table border=\"0\"><tr>");
                sb.append("<td>Title</td><td colspan=\"4\">").append(exp.getTitle()).append("</td>");
                sb.append("</tr><tr>");
                sb.append("<td valign=\"top\">Description</td><td colspan=\"3\" valign=\"top\">").append(exp.getDescription()).append("</td>");
                sb.append("</tr><tr>");
                sb.append("<td>Version</td><td>").append(exp.getVersion()).append("</td><td>Category</td><td>").append(exp.getCategory()).append("</td><td></td>");
                sb.append("</tr><tr>");
                sb.append("<td>Size</td><td>").append(Util.humanreadableSize(exp.getFileSize())).append("</td><td>Author</td><td>").append(exp.getAuthor()).append("</td><td></td>");
                sb.append("</tr><tr>");
                sb.append("<td>Local File</td><td colspan=\"3\">").append(exp.getLocalFile()).append("</td>");
                sb.append("</tr><tr>");
                sb.append("<td>Download&nbsp;URL</td><td colspan=\"3\">").append(exp.getDownloadUrl()).append("</td>");
                sb.append("</tr></table>");
            }
            if (expansion.getEMStatus().isUpdate()) {
                sb.append("<h2><font color=\"0000ff\">■</font> Update</h2>");
                sb.append("<p>Install this as update.</p>");
            }
        }
        sb.append("</html>");
        jEditorPane1.setText(sb.toString());
        jEditorPane1.setCaretPosition(0);
    }
}
