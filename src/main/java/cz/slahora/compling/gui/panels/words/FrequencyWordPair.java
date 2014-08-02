package cz.slahora.compling.gui.panels.words;


import cz.slahora.compling.gui.model.StringableArrayList;

import java.util.List;

class FrequencyWordPair {


	private final int frequency;
	private final List<String> words;

	FrequencyWordPair(int freq, List<String>words) {
		this.frequency = freq;
		this.words = new StringableArrayList<String>(words, ", ");
	}

	public int getFrequency() {
		return frequency;
	}

	public List<String> getWords() {
		return words;
	}
}
