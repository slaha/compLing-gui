
/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 0-2008, by Object Refinery Limited and Contributors.
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
 10-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ---------------
 * AboutFrame.java
 * ---------------
 1-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 8/12/18 09:57:32 mungady Exp $
 *
 1)
 * --------------------------
 1 : Version 1, based on code from JFreeChart demo application (DG);
 1 : Added getPreferredSize() method (DG);
 2 : List of developers is now optional (DG);
 2 : Modified to use a ResourceBundle for elements that require
 *               localisation (DG);
 2 : Added new constructor (DG);
 2 : Removed redundant code (DG);
 2 : Fixed errors reported by Checkstyle (DG);
 8 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 * @author Jan Šlahora
 *  using customized AboutPanel
 */

package cz.slahora.compling.gui.about;

import org.jfree.ui.about.ContributorsPanel;
import org.jfree.ui.about.LibraryPanel;
import org.jfree.ui.about.ProjectInfo;
import org.jfree.ui.about.SystemPropertiesPanel;
import org.jfree.util.ResourceBundleWrapper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A frame that displays information about the demonstration application.
 * 
 * @author David Gilbert
 */
public class AboutFrame extends JFrame {

	/** The preferred size for the frame. */
	public static final Dimension PREFERRED_SIZE = new Dimension(700, 550);

	/** The default border for the panels in the tabbed pane. */
	public static final Border STANDARD_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

	/** Localised resources. */
	private ResourceBundle resources;

	/** The application name. */
	private String application;

	/** The application version. */
	private String version;

	/** The copyright string. */
	private String copyright;

	/** Other info about the application. */
	private String info;

	/** The project logo. */
	private Image logo;

	/** A list of contributors. */
	private List contributors;

	/** The licence. */
	private String licence;

	/** Application website */
	private String website;

	/**
	 * Constructs an about frame.
	 * 
	 * @param title
	 *            the frame title.
	 * @param project
	 *            information about the project.
	 */
	public AboutFrame(final String title, final String website, final ProjectInfo project) {

		this(title, project.getName(), "Version " + project.getVersion(), website, project.getInfo(), project.getLogo(), project
				.getCopyright(), project.getLicenceText(), project.getContributors(), project);

	}

	/**
	 * Constructs an 'About' frame.
	 * 
	 * @param title
	 *            the frame title.
	 * @param application
	 *            the application name.
	 * @param version
	 *            the version.
	 * @param info
	 *            other info.
	 * @param logo
	 *            an optional logo.
	 * @param copyright
	 *            the copyright notice.
	 * @param licence
	 *            the licence.
	 * @param contributors
	 *            a list of developers/contributors.
	 * @param project
	 *            info about the project.
	 */
	public AboutFrame(final String title, final String application, final String version, final String website, final String info,
			final Image logo, final String copyright, final String licence, final List contributors,
			final ProjectInfo project) {

		super(title);

		this.application = application;
		this.version = version;
		this.copyright = copyright;
		this.info = info;
		this.logo = logo;
		this.contributors = contributors;
		this.licence = licence;
		this.website = website;

		final String baseName = "org.jfree.ui.about.resources.AboutResources";
		this.resources = ResourceBundleWrapper.getBundle(baseName);

		final JPanel content = new JPanel(new BorderLayout());
		content.setBorder(STANDARD_BORDER);

		final JTabbedPane tabs = createTabs(project);
		content.add(tabs);
		setContentPane(content);

		pack();

	}

	/**
	 * Returns the preferred size for the about frame.
	 * 
	 * @return the preferred size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	/**
	 * Creates a tabbed pane containing an about panel and a system properties
	 * panel.
	 * 
	 * @return a tabbed pane.
	 * @param project
	 */
	private JTabbedPane createTabs(final ProjectInfo project) {

		final JTabbedPane tabs = new JTabbedPane();

		final JPanel aboutPanel = createAboutPanel(project);
		aboutPanel.setBorder(AboutFrame.STANDARD_BORDER);
		final String aboutTab = this.resources.getString("about-frame.tab.about");
		tabs.add(aboutTab, aboutPanel);

		final JPanel systemPanel = new SystemPropertiesPanel();
		systemPanel.setBorder(AboutFrame.STANDARD_BORDER);
		final String systemTab = this.resources.getString("about-frame.tab.system");
		tabs.add(systemTab, systemPanel);

		return tabs;

	}

	/**
	 * Creates a panel showing information about the application, including the
	 * name, version, copyright notice, URL for further information, and a list
	 * of contributors.
	 * 
	 * @param project
	 * 
	 * @return a panel.
	 */
	private JPanel createAboutPanel(final ProjectInfo project) {

		final JPanel about = new JPanel(new BorderLayout());

		final JPanel details = new cz.slahora.compling.gui.about.AboutPanel(this.application, this.version, this.copyright, this.website, this.info, this.logo);

		boolean includetabs = false;
		final JTabbedPane tabs = new JTabbedPane();

		if (this.contributors != null) {
			final JPanel contributorsPanel = new ContributorsPanel(this.contributors);
			contributorsPanel.setBorder(AboutFrame.STANDARD_BORDER);
			final String contributorsTab = this.resources.getString("about-frame.tab.contributors");
			tabs.add(contributorsTab, contributorsPanel);
			includetabs = true;
		}

		if (this.licence != null) {
			final JPanel licencePanel = createLicencePanel();
			licencePanel.setBorder(STANDARD_BORDER);
			final String licenceTab = this.resources.getString("about-frame.tab.licence");
			tabs.add(licenceTab, licencePanel);
			includetabs = true;
		}

		if (project != null) {
			final JPanel librariesPanel = new LibraryPanel(project);
			librariesPanel.setBorder(AboutFrame.STANDARD_BORDER);
			final String librariesTab = this.resources.getString("about-frame.tab.libraries");
			tabs.add(librariesTab, librariesPanel);
			includetabs = true;
		}

		about.add(details, BorderLayout.NORTH);
		if (includetabs) {
			about.add(tabs);
		}

		return about;

	}

	/**
	 * Creates a panel showing the licence.
	 * 
	 * @return a panel.
	 */
	private JPanel createLicencePanel() {

		final JPanel licencePanel = new JPanel(new BorderLayout());
		final JTextArea area = new JTextArea(this.licence);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setCaretPosition(0);
		area.setEditable(false);
		licencePanel.add(new JScrollPane(area));
		return licencePanel;

	}

}
