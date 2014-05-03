package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.analysis.analysator.poems.denotation.IDenotation;
import cz.compling.model.denotation.DenotationElement;
import cz.compling.model.denotation.DenotationWord;
import cz.compling.model.denotation.DenotationWordNotFoundException;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 3.5.14 15:46</dd>
 * </dl>
 */
public class GuiDenotationWord {

	private final DenotationWord denotationWord;
	private final IDenotation denotation;

	public GuiDenotationWord(DenotationWord denotationWord, IDenotation denotation) {
		this.denotationWord = denotationWord;
		this.denotation = denotation;
	}

	public DenotationWord getDenotationWord() {
		return denotationWord;
	}

	private DenotationWord getWordOrNull(int index) {
		try {
			return denotation.getWord(index);
		} catch (DenotationWordNotFoundException notFound) {
			return null;
		}
	}

	public DenotationWord getNextWord() {
		return getWordOrNull(denotationWord.getNumber() + 1);
	}

	public DenotationWord getNextWordToJoin() {
		DenotationWord word;
		int index = denotationWord.getNumber() + 1;
		do {
			word = getWordOrNull(index);
			index++;
		} while (word != null && (word.isIgnored() || word.isJoined()));

		return word;
	}

	public IDenotation getDenotation() {
		return denotation;
	}

	public DenotationWord getLastJoinedWord() {
		final List<DenotationWord> joinedWords = denotationWord.getJoinedWords();
		if (joinedWords.isEmpty()) {
			throw new IllegalStateException("getLastJoinedWord called when word is not joined!");
		}
		return joinedWords.get(joinedWords.size() - 1);
	}

	public boolean canRemoveElement() {
		final List<DenotationElement> elements = denotationWord.getDenotationElements();
		return elements.size() > 1;
	}

	public void joinNext() {
		final DenotationWord nextWord = getNextWordToJoin();
		denotation.joinWords(denotationWord.getNumber(), nextWord.getNumber());
	}

	public void splitLast() {
		final List<DenotationWord> joinedWords = denotationWord.getJoinedWords();
		final DenotationWord lastJoinedWord = joinedWords.get(joinedWords.size() - 1);
		denotation.split(denotationWord.getNumber(), lastJoinedWord.getNumber());
	}

	public DenotationElement getHighestDenotationElement() {
		final List<DenotationElement> numbers = denotationWord.getDenotationElements();
		if (numbers.isEmpty()) {
			throw new IllegalStateException("Numbers were empty when calling getHighestDenotationElement");
		}
		DenotationElement max = numbers.get(0);
		for (int i = 1; i < numbers.size(); i++) {
			if (numbers.get(i).getNumber() > max.getNumber()) {
				max = numbers.get(i);
			}
		}
		return max;
	}

	public String getDenotationElementsAsString() {
		final List<DenotationElement> denotationElements = denotationWord.getDenotationElements();
		if (denotationWord.isIgnored() || denotationElements.isEmpty()) {
			return StringUtils.EMPTY;
		}
		StrBuilder s = new StrBuilder();
		s.appendWithSeparators(denotationElements, " | ");
		return s.toString();
	}

	public String getDenotationWordsAsString() {
		final List<DenotationWord> joinedWords = denotationWord.getJoinedWords();
		if (joinedWords.isEmpty()) {
			return denotationWord.toString();
		}
		StrBuilder s = new StrBuilder();
		s
			.append(denotationWord)
			.append(" ")
			.appendWithSeparators(joinedWords, " ");
		return s.toString();
	}

	public void addElement() {
		denotation.addNewElementTo(denotationWord.getNumber());

	}

	public void duplicate(DenotationElement element) {
		denotation.duplicateElement(denotationWord.getNumber(), element);
	}

	public void removeElement(DenotationElement element) {
		denotation.removeElement(denotationWord.getNumber(), element);

	}

	public boolean hasJoinedWords() {
		return !denotationWord.getJoinedWords().isEmpty();
	}

	public void setIgnored(boolean ignored) {
		denotation.ignoreWord(denotationWord.getNumber(), ignored);
	}
}