package cz.slahora.compling.gui.panels;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.Rectangle;

/**
*
* TODO
*
* <dl>
* <dt>Created by:</dt>
* <dd>slaha</dd>
* <dt>On:</dt>
* <dd> 23.8.14 12:50</dd>
* </dl>
*/
public class ResultsScrollablePanel extends JPanel implements Scrollable {
	public Dimension getPreferredScrollableViewportSize() {
		return super.getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 16;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
