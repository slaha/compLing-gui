package cz.slahora.compling.gui.analysis;

import cz.compling.CompLing;
import cz.slahora.compling.gui.model.WorkingText;

import javax.swing.*;
import java.util.Map;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 8:40</dd>
 * </dl>
 */
public interface MultipleTextsAnalysis<T> {
	void analyse(JPanel mainPanel, Map<WorkingText, CompLing> texts);

	Map<WorkingText, T> getResults();
}
