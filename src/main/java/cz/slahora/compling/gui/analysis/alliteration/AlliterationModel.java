package cz.slahora.compling.gui.analysis.alliteration;

import cz.compling.analysis.analysator.frequency.character.ICharacterFrequency;
import cz.compling.analysis.analysator.poems.alliteration.IAlliteration;
import cz.compling.model.Alliteration;
import cz.compling.model.AlliterationMath;
import cz.compling.model.CharacterFrequency;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * TODO
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 25.10.14 14:30</dd>
 * </dl>
 */
class AlliterationModel {

	private final TIntObjectMap<Alliteration.LineAlliteration> cache = new TIntObjectHashMap<Alliteration.LineAlliteration>();
	private final TIntDoubleMap probabilities = new TIntDoubleHashMap();

	private final Alliteration alliteration;
	private final CharacterFrequency characterFrequency;
	private AlliterationMath alliterationMath;
	private String textName;

	public AlliterationModel(String textName, IAlliteration alliteration, ICharacterFrequency characterFrequency) {
		this.textName = textName;
		this.alliteration = alliteration.getAlliteration();
		this.characterFrequency = characterFrequency.getCharacterFrequency();
	}

	public int getVersesCount() {
		return alliteration.getVerseCount();
	}

	private Alliteration.LineAlliteration getLine(int verseNumber) {
		Alliteration.LineAlliteration lineAlliteration = cache.get(verseNumber);
		if (lineAlliteration == null) {
			lineAlliteration = alliteration.getAlliterationFor(verseNumber);
			cache.put(verseNumber, lineAlliteration);
		}
		return lineAlliteration;
	}

	private AlliterationMath getAlliterationMath() {
		if (alliterationMath == null) {
			alliterationMath = new AlliterationMath(characterFrequency);
		}
		return alliterationMath;
	}

	public Collection<String> getPhonemes(int verseNumber) {
		final Alliteration.LineAlliteration lineAlliteration = getLine(verseNumber);

		if (!lineAlliteration.hasAnyAlliteration()) {
			return Collections.emptyList();
		}
		return lineAlliteration.getFirstCharactersWithAlliteration();
	}

	public int getWordsCountInVerse(int verseNumber) {
		final Alliteration.LineAlliteration lineAlliteration = getLine(verseNumber);

		return lineAlliteration.getCountOfWordsInVerse();
	}

	public int[] getKsFor(int verseNumber, Collection<String> phonemes) {

		final Alliteration.LineAlliteration lineAlliteration = getLine(verseNumber);

		int[] Ks = new int[phonemes.size()];
		int i = 0;
		for (String phoneme : phonemes) {

			Ks[i++] = lineAlliteration.getAlliterationFor(phoneme);
		}

		return Ks;
	}

	public double getProbability(int verseNumber) {
		if (probabilities.containsKey(verseNumber)) {
			return probabilities.get(verseNumber);
		}
		final Alliteration.LineAlliteration lineAlliteration = getLine(verseNumber);
		double probability;
		if (lineAlliteration.hasAnyAlliteration()) {
			AlliterationMath math = getAlliterationMath();
			probability = math.computeProbability(lineAlliteration);
		} else {
			probability = -1;
		}
		probabilities.put(verseNumber, probability);
		return probability;
	}

	public double getKA(double alpha, double probability) {
		AlliterationMath math = getAlliterationMath();

		return math.computeKA(alpha, probability);
	}

	public String getTextName() {
		return textName;
	}

	public double getKaSum(double alpha) {
		final AlliterationMath math = getAlliterationMath();
		double ka = 0;
		for (int verseNumber = 1; verseNumber <= getVersesCount(); verseNumber++) {
			double probability = getProbability(verseNumber);
			ka += math.computeKA(alpha, probability);
		}
		return ka;
	}

	public double getTotalKa(double alpha) {
		return getKaSum(alpha) / getVersesCount();
	}
}
