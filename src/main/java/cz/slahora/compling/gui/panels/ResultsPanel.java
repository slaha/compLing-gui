package cz.slahora.compling.gui.panels;

import cz.slahora.compling.gui.model.CsvData;

import javax.swing.JPanel;

/**
 *
 * Interface that must be implemented by all results of analysis
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 10:58</dd>
 * </dl>
 */
public interface ResultsPanel {


	/**
	 * Returns JPanel that hold results of analysis
	 *
	 * @return the panel
	 */
	JPanel getPanel();

	/**
	 * Returns data that can be exported as csv
	 *
	 * @return csv data
	 */
	CsvData getCsvData();
}
