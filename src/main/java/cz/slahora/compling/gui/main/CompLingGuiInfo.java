package cz.slahora.compling.gui.main;

import cz.slahora.compling.gui.about.Licence;
import org.jfree.base.Library;
import org.jfree.ui.about.ProjectInfo;

public class CompLingGuiInfo extends ProjectInfo {

	public CompLingGuiInfo() {
		super(
			"CompLing Gui", //..name of app
			MainWindowMenu.class.getPackage().getImplementationVersion(), //..version
			"<html><p style='text-align:center;'>This application provides graphical user interface for using <a href='https://github.com/slaha/compLing'>compLing - the computional linguistic library</a>."
				+ "<p style='text-align:center;'>The application is developed as part of my diploma thesis on Univerzita Pardubice</html>",
			null, "Jan Šlahora", "Unlicense - Public Domain", Licence.LICENCE);
		addLibraries();
	}

	private void addLibraries() {

		addLibrary(new Library("compLing", "0.99", "Public domain", "https://github.com/slaha/compLing"));

		addLibrary(new Library("ginger", "0.2.1", "Apache License, Version 2.0", "https://github.com/avityuk/ginger"));

		addLibrary(new Library("commons-lang", "2.5", "Apache License, Version 2.0", "http://commons.apache.org"));

		addLibrary(new Library("commons-io", "2.4", "Apache License, Version 2.0", "http://commons.apache.org/proper/commons-io"));

		addLibrary(new Library("JFreeChart", "1.0.13", "GNU Lesser General Public Licence (LGPL)", "http://www.jfree.org"));

		addLibrary(new Library("JGoodies", "1.0.5", "BSD Open Source License", "http://www.jgoodies.com"));

		addLibrary(new Library("Apache ActiveMQ ™", "5.3.0", "Apache License, Version 2.0", "http://activemq.apache.org"));

		addLibrary(new Library("SwingX", "1.6.1", "http://openjdk.java.net/legal/gplv2+ce.html", "https://java.net/projects/swingx"));

		addLibrary(new Library("JIDE Common Layer", "3.6.0", "LGPL", "http://www.jidesoft.com/products/oss.htm"));

		addLibrary(new Library("GraphStream ", "1.2", "LGPL", "http://graphstream-project.org"));
	}
}
