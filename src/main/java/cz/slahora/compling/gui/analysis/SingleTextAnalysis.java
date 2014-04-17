package cz.slahora.compling.gui.analysis;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.JPanel;

/**
 *
 * Interface for analysis of just one text
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 8:40</dd>
 * </dl>
 */
public interface SingleTextAnalysis<T> {
	void analyse(JPanel mainPanel, CompLing compLing, WorkingText text);

	Results getResults();
}
