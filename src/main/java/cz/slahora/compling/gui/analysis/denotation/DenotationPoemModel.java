package cz.slahora.compling.gui.analysis.denotation;

import cz.compling.text.poem.Poem;
import cz.compling.text.poem.Verse;
import cz.slahora.compling.gui.model.WorkingText;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 
 * TODO
 * 
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd>6.4.14 13:39</dd>
 * </dl>
 */
public class DenotationPoemModel {

	private final Poem poem;

	private final TIntObjectMap<DenotationStrophe> strophes;
	private final TIntObjectMap<DenotationWord> allWords;

	public DenotationPoemModel(WorkingText text) {
		this.poem = text.getCompLing().poemAnalysis().poem;
		this.strophes = new TIntObjectHashMap<DenotationStrophe>();
		this.allWords = new TIntObjectHashMap<DenotationWord>();

		DenotationStrophe strophe;
		DenotationVerse denotationVerse;
		int numberOfWord = 1;
		for (int i = 1; i <= poem.getCountOfStrophes(); i++) {
			Collection<Verse> versesOfStrophe = poem.getVersesOfStrophe(i);
			strophe = new DenotationStrophe();
			for (Verse verse : versesOfStrophe) {
				denotationVerse = new DenotationVerse();
				for (String word : verse.getWords(false)) {
					DenotationWord denotationWord = new DenotationWord(word, numberOfWord);
					allWords.put(numberOfWord, denotationWord);
					numberOfWord++;

					denotationVerse.add(denotationWord);
				}
				strophe.add(denotationVerse);
			}

			strophes.put(i, strophe);
		}
	}

	public int getCountOfStrophes() {
		return poem.getCountOfStrophes();
	}

	public DenotationStrophe getStrophe(int strophe) {
		return strophes.get(strophe);
	}

	public class DenotationStrophe {
		final List<DenotationVerse> verses;

		public DenotationStrophe() {
			this.verses = new ArrayList<DenotationVerse>();
		}

		public void add(DenotationVerse denotationVerse) {
			verses.add(denotationVerse);
		}
	}

	public class DenotationVerse {
		final List<DenotationWord> words;

		public DenotationVerse() {
			this.words = new ArrayList<DenotationWord>();
		}

		public void add(DenotationWord denotationWord) {
			words.add(denotationWord);
		}
	}

	public class DenotationWord {

		private final List<Integer> numbers;
		private final List<String> words;
		private final int number;
		private boolean ignored;

		/** if true this word is joined with another and should be ignored */
		private boolean joined;

		public DenotationWord(String word, final int number) {
			this.words = new ArrayList<String>() {
				@Override
				public String toString() {
					if (isEmpty()) {
						return "";
					}
					StringBuilder sb = new StringBuilder();
					sb.append(get(0));
					for (int i = 1; i < size(); i++) {
						sb.append(' ').append(get(i));
					}
					return sb.toString();
				}
			};
			this.words.add(word);
			this.numbers = new ArrayList<Integer>() {
				@Override
				public String toString() {
					if (isEmpty()) {
						return "";
					}
					StringBuilder sb = new StringBuilder();
					sb.append(get(0));
					for (int i = 1; i < size(); i++) {
						sb.append('|').append(get(i));
					}
					return sb.toString();
				}
			};
			this.numbers.add(number);
			this.number = number;
		}

		public boolean isIgnored() {
			return ignored;
		}

		public void setIgnored(final boolean ignored) {
			if (this.ignored == ignored) {
				return;
			}
			this.ignored = ignored;
			ForEachRunner runner;
			if (ignored) {
				final int decrement = numbers.size();
				numbers.clear();
				runner = new ForEachRunner() {
					@Override
					public void run(DenotationWord word) {
						word.incrementNumbers(-decrement); //decrement
					}
				};

			} else {
				DenotationWord previousWord = getPreviousWord();
				int value;
				if (previousWord == null) {
					value = 1;
				} else {
					value = previousWord.getHighestNumber() + 1;
				}

				numbers.add(value);
				runner = new ForEachRunner() {
					@Override
					public void run(DenotationWord word) {
						word.incrementNumbers(1);
					}
				};
			}
			allWords.forEachValue(new ForEach(runner));
		}

		private void incrementNumbers(int increment) {
			for (int i = 0; i < numbers.size(); i++) {
				numbers.set(i, numbers.get(i) + increment);
			}
		}

		public void addElement() {
			if (ignored) {
				return;
			}
			numbers.add(getHighestNumber() + 1);
			allWords.forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					word.incrementNumbers(1);
				}
			}));
		}

		public void removeElement() {
			if (ignored) {
				return;
			}
			numbers.remove((Object) getHighestNumber());
			allWords.forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					word.incrementNumbers(-1);
				}
			}));
		}

		/**
		 * @return true if join another word; false if not
		 */
		public boolean joinNext() {
			if (ignored) {
				return false;
			}
			DenotationWord next = getNextWord();
			if (next == null) {
				//..end of text
				return false;
			}
			words.addAll(next.words);
			next.words.clear();
			next.numbers.clear();
			next.joined = true;

			allWords.forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					word.incrementNumbers(-1);
				}
			}));

			return true;
		}

		public void splitLast() {
			if (words.size() == 1) {
				return;
			}
			String remove = words.remove(words.size() - 1);

			final DenotationWord nextWord = getNextWord(false);
			if (nextWord != null) {
				nextWord.words.add(remove);
				nextWord.numbers.add(getHighestNumber() + 1);
				nextWord.joined = false;

				allWords.forEachValue(new ForEach(new ForEachRunner() {
				@Override
				public void run(DenotationWord word) {
					if (word.number > nextWord.number) {
						word.incrementNumbers(1);
					}
				}
			}));
			}
		}

		public DenotationWord getNextWord() {
			DenotationWord w;
			int nmbr = number;
			do {
				w = allWords.get(++nmbr);
			} while (w != null && w.joined);
			return w;
		}

		private DenotationWord getNextWord(boolean ignoreJoined) {
			DenotationWord w;
			int nmbr = number + words.size() - 1;
			do {
				w = allWords.get(++nmbr);
			} while (w == null);
			return w;
		}

		private DenotationWord getPreviousWord() {
			DenotationWord w;
			int nmbr = number;
			do {
				w = allWords.get(--nmbr);
			} while (w != null && w.joined);
			return w;
		}

		@Override
		public String toString() {
			return words.toString();
		}

		public int getLowestNumber() {
			if (numbers.isEmpty()) {
				throw new IllegalStateException("Numbers were empty when calling getLowestNumber");
			}
			int min = numbers.get(0);
			for (int i = 1; i < numbers.size(); i++) {
				if (numbers.get(i) < min) {
					min = numbers.get(i);
				}
			}
			return min;
		}

		public int getHighestNumber() {
			if (numbers.isEmpty()) {
				throw new IllegalStateException("Numbers were empty when calling getHighestNumber");
			}
			int max = numbers.get(0);
			for (int i = 1; i < numbers.size(); i++) {
				if (numbers.get(i) > max) {
					max = numbers.get(i);
				}
			}
			return max;
		}

		public int getNumber() {
			return number;
		}

		public boolean canRemoveElement() {
			return numbers.size() > 1;
		}

		public List<String> getWords() {
			return Collections.unmodifiableList(words);
		}

		public List<Integer> getElements() {
			return Collections.unmodifiableList(numbers);
		}

		public boolean isJoined() {
			return joined;
		}

		public boolean hasJoined() {
			return words.size() > 1;

		}

		public String getLastJoined() {
			return words.get(words.size() - 1);
		}

		private class ForEach implements TObjectProcedure<DenotationWord> {

			final ForEachRunner runnable;

			private ForEach(ForEachRunner runnable) {
				this.runnable = runnable;
			}

			@Override
			public boolean execute(DenotationWord denotationWord) {
				if (denotationWord.number > number && !denotationWord.joined && !denotationWord.ignored) {
					runnable.run(denotationWord);
				}
				return true;
			}
		}
	}

	private interface ForEachRunner {
		void run(DenotationWord word);
	}
}
