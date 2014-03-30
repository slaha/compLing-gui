package cz.slahora.compling.gui.analysis;

import cz.compling.model.CharacterFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import cz.slahora.compling.gui.panels.ResultsPanel;
import cz.slahora.compling.gui.utils.MapUtils;
import cz.slahora.compling.gui.panels.characters.CharacterFrequencyPanel;
import java.util.Map;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.3.14 21:55</dd>
 * </dl>
 */
public class ReportFactory {

	public static ResultsPanel createReport(Map<WorkingText, ?> results) {
		Object value = MapUtils.getFirstValue(results);
		if (value instanceof CharacterFrequency) {
			return new CharacterFrequencyPanel((Map<WorkingText, CharacterFrequency>) results);
		}
		throw new AnalysisResultReceiver.TypeNotSupportedException(value.getClass());
	}
}
