package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.DenotationElement;
import cz.compling.model.denotation.DenotationWord;
import cz.compling.model.denotation.Hreb;
import cz.slahora.compling.gui.model.Csv;
import cz.slahora.compling.gui.model.CsvData;
import cz.slahora.compling.gui.model.PipeArrayList;
import cz.slahora.compling.gui.utils.CsvParserUtils;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 12.4.14 10:38</dd>
 * </dl>
 */
public class GuiDenotationHrebsModel implements Csv<GuiDenotationHrebsModel> {

	private final IDenotation denotation;

	public GuiDenotationHrebsModel(IDenotation denotation) {
		this.denotation = denotation;
	}

	public int getHrebsCount() {
		return denotation.getHrebs().size();
	}

	/**
	 * Returns hreb for {@code row}<sup>th</sup> row of table
	 *
	 * @return Hreb which should be displayed on {@code row}<sup>th</sup> row of table
	 */
	public Hreb getHrebOnRow(int row) {
		return getHrebs().get(row);
	}

	/**
	 * Remove hreb with number {@code hrebNumber}.
	 *
	 * @param hrebNumber number of hreb to remove.
	 *
	 * @return the lowest number of {@code DenotationPoemModel.DenotationWord} which was in removed hreb
	 */
	public int removeHreb(int hrebNumber) {
		return denotation.removeHreb(hrebNumber);
	}

	/**
	 * @return all Hrebs as sorted array (by Hreb's number)
	 */
	public List<Hreb> getHrebs() {
		final List<Hreb> hrebs = (List<Hreb>) denotation.getHrebs();
		Collections.sort(hrebs, new Comparator<Hreb>() {
			@Override
			public int compare(Hreb o1, Hreb o2) {
				return o1.getNumber() - o2.getNumber();
			}
		});
		return hrebs;
	}

	@Override
	public CsvSaver<GuiDenotationHrebsModel> getCsvSaver() {
		return new DenotationHrebsModelSaver();
	}

	@Override
	public boolean supportsCsvImport() {
		return true;
	}

	@Override
	public CsvLoader<GuiDenotationHrebsModel> getCsvLoader() {
		return new DenotationHrebsModelLoader();
	}

	public boolean isAnyHrebInTheTable() {
		return getHrebsCount() > 0;
	}

	public void createNewHreb() {
		denotation.createNewHreb();
	}

	private void addHreb(Hreb hreb) {
		denotation.addHreb(hreb);

	}

	public String toStringForHreb(Hreb hreb) {
		if (hreb.getWords().isEmpty()) {
			return "-";
		}
		final TIntObjectMap<String> map = getElementsInHreb(hreb);
		if (map.isEmpty()) {
			return "-";
		}
		final int[] keys = map.keys();
		Arrays.sort(keys);
		StringBuilder b = new StringBuilder();
		b.append(map.get(keys[0]));
		for (int i = 1; i < keys.length; i++) {
			b
				.append(", ")
				.append(map.get(keys[i]));
		}

		return b.toString();
	}

	private TIntObjectMap<String> getElementsInHreb(Hreb hreb) {
		TIntObjectMap<String> map = new TIntObjectHashMap<String>();
		for (DenotationWord dw : hreb.getWords()) {
			for (DenotationElement element : dw.getDenotationElements()) {
				if (element.getHreb() == null) {
					continue;
				}
				if (element.getHreb().getNumber() == hreb.getNumber()) {
					String s;
					if (StringUtils.isEmpty(element.getText())) {
						s = dw.getWords().toString();
					} else {
						s = element.getText();
					}
					map.put(dw.getNumber(), (s + " " + element.getNumber()));
				}
			}
		}
		return map;
	}

	private static class DenotationHrebsModelSaver extends CsvSaver<GuiDenotationHrebsModel> {

		@Override
		public CsvData saveToCsv(GuiDenotationHrebsModel object, Object... params) {
			CsvData data = new CsvData();
			data.addSection();
			final CsvData.CsvDataSection section = data.getCurrentSection();
			section.addHeader("Hreb number");
			section.addHeader("Word number(s) [word number\\denotation element\\value]");
			for (Hreb hreb : object.getHrebs()) {
				section.startNewLine();
				section.addData(hreb.getNumber());
				PipeArrayList<GuiHrebWordsBundle> hrebWordsBundles = new PipeArrayList<GuiHrebWordsBundle>(hreb.size());
				for (DenotationWord w : hreb.getWords()) {
					if (w.isInHreb(hreb)) {

						final DenotationElement elementInHreb = w.getElementInHreb(hreb);
						String text = elementInHreb.getText() == null ? w.getWords().toString() : elementInHreb.getText();
						hrebWordsBundles.add(new GuiHrebWordsBundle(w.getNumber(), elementInHreb.getNumber(), text));
					}
				}
				section.addData(hrebWordsBundles);


			}
			return data;
		}

	}

	private static class DenotationHrebsModelLoader extends CsvLoader<GuiDenotationHrebsModel> {

		/**
		 * @param params [0]..DenotationHrebsModel; [1]..DenotationPoemModel
		 */
		@Override
		public void loadFromCsv(CsvData csv, GuiDenotationHrebsModel objectToLoad, Object... params) throws CsvParserException {
			GuiDenotationHrebsModel hrebsModel = (GuiDenotationHrebsModel) params[0];
			GuiDenotationPoemModel poemModel = (GuiDenotationPoemModel) params[1];
			final CsvParserUtils.CollectionSplitter splitter = new CsvParserUtils.CollectionSplitter() {
				@Override
				public String getSplitter() {
					return PipeArrayList.SPLITTER;
				}
			};
			final CsvParserUtils.CollectionParser<GuiHrebWordsBundle> parser = new CsvParserUtils.CollectionParser<GuiHrebWordsBundle>() {
				@Override
				public void parse(String toParse, Collection<GuiHrebWordsBundle> toAdd) throws CsvParserException {
					try {
						toParse = toParse.substring(1, toParse.length() - 1); //..remove [ and ]
						String[] split = toParse.split(GuiHrebWordsBundle.SPLITTER);
						int wordNumber = Integer.parseInt(split[0]);
						int elementNumber = Integer.parseInt(split[1]);
						String word = split[2];
						toAdd.add(new GuiHrebWordsBundle(wordNumber, elementNumber, word));
					} catch (NumberFormatException nfe) {
						throw new CsvParserException(nfe.getMessage());
					} catch (IndexOutOfBoundsException ioobe) {
						throw new CsvParserException(ioobe.getMessage());
					}
				}
			};
			int maxNumber = 0;
			for (List<Object> objects : csv.getCurrentSection().getDataLines()) {
				int number = CsvParserUtils.getAsInt(objects.get(0));
				Hreb hreb = new Hreb(number);
				hrebsModel.addHreb(hreb);

				Collection<GuiHrebWordsBundle> wordsNumbers = CsvParserUtils.getAsList(objects.get(1), splitter, parser);
				for (GuiHrebWordsBundle bundle : wordsNumbers) {

					DenotationWord word = poemModel.getWord(bundle.wordNumber);
					hreb.addWord(word, bundle.wordAsString, bundle.elementNumber);
				}
				if (maxNumber < number) {
					maxNumber = number;
				}

			}
		}
	}
}
