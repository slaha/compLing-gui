package cz.slahora.compling.gui.analysis.denotation;

/**
 * This is helper class for saving
 * <ul>
 *     <li>Number of word</li>
 *     <li>Number of denotation element</li>
 *     <li>String which represents the DenotationWord</li>
 * </ul>
 * into csv file
 */
class GuiHrebWordsBundle {

	/** splitter for values - when saving */
	public static final char SPLITTER_CHAR = '\\';
	/** splitter for values - when loading */
	public static final String SPLITTER = "\\\\";


	public final int wordNumber;
	public final int elementNumber;
	public final String wordAsString;

	public GuiHrebWordsBundle(int wordNumber, int elementNumber, String elementInHreb) {
		this.wordNumber = wordNumber;
		this.elementNumber = elementNumber;
		this.wordAsString = elementInHreb;
	}


	@Override
	public String toString() {
		return "[" + wordNumber + SPLITTER_CHAR + elementNumber + SPLITTER_CHAR + wordAsString + "]";
	}
}
