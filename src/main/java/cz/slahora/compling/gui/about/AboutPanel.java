

/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * ---------------
 * AboutPanel.java
 * ---------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: AboutPanel.java,v 1.6 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Nov-2001 : Version 1 (DG);
 * 27-Jun-2002 : Added logo (DG);
 * 08-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */

package cz.slahora.compling.gui.about;

import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import cz.slahora.compling.gui.utils.HyperlinkUtils;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URL;

/**
 * A standard panel for displaying information about an application.
 *
 * @author David Gilbert
 * @author Jan Šlahora
 */
public class AboutPanel extends JPanel implements HyperlinkListener {

    /**
     * Constructs a panel.
     *
     * @param application  the application name.
     * @param version  the version.
     * @param copyright  the copyright statement.
     * @param info  other info.
     * @param logo  an optional logo.
     */
    public AboutPanel(final String application,
                      final String version,
                      final String copyright,
                      final String website,
                      final String info,
                      final Image logo) {

        setLayout(new GridBagLayout());

        final JPanel textPanel = new JPanel(new GridBagLayout());

        final JPanel appPanel = new JPanel();
        final Font f1 = new Font("Dialog", Font.BOLD, 14);
        final JLabel appLabel = RefineryUtilities.createJLabel(application, f1, Color.black);
        appLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        appPanel.add(appLabel);

        final JPanel verPanel = new JPanel();
        final Font f2 = new Font("Dialog", Font.PLAIN, 12);
        final JLabel verLabel = RefineryUtilities.createJLabel(version, f2, Color.black);
        verLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        verPanel.add(verLabel);

        final JPanel copyrightPanel = new JPanel();
        final JLabel copyrightLabel = RefineryUtilities.createJLabel(copyright, f2, Color.black);
        copyrightLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        copyrightPanel.add(copyrightLabel);

	    final JPanel websitePanel = new JPanel();
        final JEditorPane websiteEditorPane = createEditorPanel("<html><center><a href='" + website + "'>Web aplikace</a></center></html>", f2, Color.black);
        websitePanel.add(websiteEditorPane);

        final JPanel infoPanel = new JPanel();
	    final JEditorPane infoEditorPane = createEditorPanel(info, f2, Color.black);
	    infoPanel.add(infoEditorPane);

	    int y = 0;
	    GridBagConstraintBuilder gbc = new GridBagConstraintBuilder().gridXY(0, y++).fill(GridBagConstraints.HORIZONTAL).weightX(1);
        textPanel.add(appPanel, gbc.build());
        textPanel.add(verPanel, gbc.copy().gridY(y++).build());
        textPanel.add(copyrightPanel, gbc.copy().gridY(y++).build());
        textPanel.add(websitePanel, gbc.copy().gridY(y++).build());
        textPanel.add(infoPanel, gbc.copy().gridY(y).build());

        add(textPanel, gbc.copy().gridXY(0, 0).build());

        if (logo != null) {
            final JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(logo)));
            imagePanel.setBorder(BorderFactory.createLineBorder(Color.black));
            final JPanel imageContainer = new JPanel(new BorderLayout());
            imageContainer.add(imagePanel, BorderLayout.NORTH);
            add(imageContainer, BorderLayout.WEST);
        }

    }

	private JEditorPane createEditorPanel(String text, Font font, Color clr) {
		final JEditorPane jep = new JEditorPane("text/html", text);
		jep.setEditable(false);
		jep.setOpaque(false);
		jep.setForeground(clr);
		jep.addHyperlinkListener(this);
		if (font != null) {
			jep.setFont(font);
		}
		return jep;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		HyperlinkEvent.EventType type;
		if ((type = e.getEventType()) == null) {
			return;
		}

		if (HyperlinkEvent.EventType.ENTERED.equals(type)) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		else if (HyperlinkEvent.EventType.EXITED.equals(type)) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		else if (HyperlinkEvent.EventType.ACTIVATED.equals(type)) {
			final URL url = e.getURL();
			if (!HyperlinkUtils.openUrl(url)) {
				String msg = "Není možné otevřít stránku %s<p>Adresu označte, zkopírujte do schránky a otevřete v prohlížeči";
				msg = String.format(msg, url);
				JEditorPane lbl = createEditorPanel(msg, null, Color.black);
				JOptionPane.showMessageDialog(this, lbl);
			}
		}


	}
}




























































