package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.panels.ResultsPanel;

/**
 *
 * Interface for getting results of analysis
 * @see SingleTextAnalysis
 * @see MultipleTextsAnalysis
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 17.4.14 9:47</dd>
 * </dl>
 * @author slaha
 */
public interface Results {

	ResultsPanel getResultPanel();
}
