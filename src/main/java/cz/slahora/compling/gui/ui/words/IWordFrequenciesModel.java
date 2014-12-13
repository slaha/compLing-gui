package cz.slahora.compling.gui.ui.words;

import cz.compling.analysis.analysator.frequency.words.IWordFrequency;
import cz.slahora.compling.gui.model.WorkingText;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 10.8.14 9:46</dd>
 * </dl>
 */
public interface IWordFrequenciesModel<T> {
	String getMainParagraphText();

	FrequencyWordPair getMostFrequentWord();

	int getTotalWordsCount();

	TableModel getTableModel();

	PieDataset getPieDataSet(int lowerBound);

	CategoryDataset getAbsoluteBarDataSet(int lowerBound);

	CategoryDataset getRelativeBarDataSet(int lowerBound);

	int getFilterMaxValue();

	T[] getAllDomainElements();
	Comparator<T> getDomainElementsComparator();

	void addCompareChartCategory(T item);

	void removeComparePlotCategory(T item);

	boolean isInCompareChartCategories(T word);

	Set<T> getAllCompareChartCategories();

	CategoryDataset getBarDataSetFor(Collection<T> words);

	Map<WorkingText, IWordFrequency> getAllFrequencies();

	Set<WorkingText> getAllTexts();

	WordLengthFrequenciesModel.ChiSquare getChiSquareFor(WorkingText workingText);

	WordLengthFrequenciesModel.ChiSquare getChiSquareFor(WorkingText workingText, WordLengthFrequenciesModel.ChiSquare alpha);
}
