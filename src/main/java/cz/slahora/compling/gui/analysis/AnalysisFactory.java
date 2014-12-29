package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.analysis.aggregation.AggregationMultipleTexts;
import cz.slahora.compling.gui.analysis.aggregation.AggregationSingleText;
import cz.slahora.compling.gui.analysis.alliteration.AlliterationAnalysis;
import cz.slahora.compling.gui.analysis.assonance.AssonanceMultipleAnalysis;
import cz.slahora.compling.gui.analysis.character.CharacterMultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.character.CharacterSingleTextAnalysis;
import cz.slahora.compling.gui.analysis.denotation.DenotationSingleTextAnalysis;
import cz.slahora.compling.gui.analysis.words.WordMultipleTextsAnalysis;
import cz.slahora.compling.gui.analysis.words.WordSingleTextAnalysis;

public class AnalysisFactory {
	public static final int CHARACTER_COUNTS_ONE = 10;
	public static final int CHARACTER_COUNTS_ALL = 11;
	public static final int WORD_COUNTS_ONE = 15;
	public static final int WORD_COUNTS_ALL = 16;
	public static final int ALLITERATION = 30;
	public static final int AGGREGATION_ONE = 35;
	public static final int AGGREGATION_ALL = 36;
	public static final int ASSONANCE_ALL = 41;
	public static final int DENOTATION = 99;

	public static Analysis create(int id) {

		switch (id) {
			case AnalysisFactory.CHARACTER_COUNTS_ONE:
				return new CharacterSingleTextAnalysis();

			case AnalysisFactory.CHARACTER_COUNTS_ALL:
				return new CharacterMultipleTextsAnalysis();

			case AnalysisFactory.WORD_COUNTS_ONE:
				return new WordSingleTextAnalysis();

			case AnalysisFactory.WORD_COUNTS_ALL:
				return new WordMultipleTextsAnalysis();

			case AnalysisFactory.ALLITERATION:
				return new AlliterationAnalysis();

			case AnalysisFactory.ASSONANCE_ALL:
				return new AssonanceMultipleAnalysis();

			case AnalysisFactory.AGGREGATION_ONE:
				return new AggregationSingleText();

			case AnalysisFactory.AGGREGATION_ALL:
				return new AggregationMultipleTexts();


			case AnalysisFactory.DENOTATION:
				return new DenotationSingleTextAnalysis();
		}

		throw new IllegalArgumentException("Unknown id " + id);
	}
}
