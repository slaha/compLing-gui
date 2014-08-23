package cz.slahora.compling.gui.panels.words;


import cz.slahora.compling.gui.model.StringableArrayList;

import java.util.List;

class LengthFrequencyWordPair extends FrequencyWordPair {


	private final List<Integer> lengths;
	private final int frequency;
	private final List<String> words;

	LengthFrequencyWordPair(List<Integer> lengths, int freq, List<String> words) {
		super(freq, words);
		this.lengths = new StringableArrayList<Integer>(lengths, ", ");
		this.frequency = freq;
		this.words = new StringableArrayList<String>(words, ", ");
	}

	public int getFrequency() {
		return frequency;
	}

	public List<String> getWords() {
		return words;
	}

	public List<Integer> getLengths() {
		return lengths;
	}
}
