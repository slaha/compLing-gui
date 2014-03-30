package cz.slahora.compling.gui.panels;

import cz.slahora.compling.gui.model.CsvData;

import javax.swing.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 10:58</dd>
 * </dl>
 */
public interface ResultsPanel {

	JPanel getPanel();

	CsvData getCsvData();
}
