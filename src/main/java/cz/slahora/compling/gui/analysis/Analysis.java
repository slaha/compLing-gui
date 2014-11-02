package cz.slahora.compling.gui.analysis;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JPanel;
import java.util.Map;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 20.4.14 9:26</dd>
 * </dl>
 */
public interface Analysis {
	void analyse(JPanel mainPanel, ResultsHandler handler, Map<WorkingText, CompLing> texts);
	Results getResults();
}
