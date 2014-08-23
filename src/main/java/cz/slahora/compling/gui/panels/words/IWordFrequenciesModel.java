package cz.slahora.compling.gui.panels.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.table.TableModel;
import java.util.Map;
import java.util.Set;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 10.8.14 9:46</dd>
 * </dl>
 */
public interface IWordFrequenciesModel {
	String getMainParagraphText();

	FrequencyWordPair getMostFrequentWord();

	int getTotalWordsCount();

	TableModel getTableModel();

	PieDataset getPieDataSet(int lowerBound);

	CategoryDataset getAbsoluteBarDataSet(int lowerBound);

	CategoryDataset getRelativeBarDataSet(int lowerBound);

	int getFilterMaxValue();

	Set<String> getAllDomainElements();

	void addCompareChartCategory(String item);

	void removeComparePlotCategory(String item);

	boolean isInCompareChartCategories(String word);

	Set<String> getAllCompareChartCategories();

	CategoryDataset getBarDataSetFor(String... words);

	Map<WorkingText, IWordFrequency> getAllFrequencies();

	Set<WorkingText> getAllTexts();
}
