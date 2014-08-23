package cz.slahora.compling.gui.analysis.words;

/**
*
* TODO
*
* <dl>
* <dt>Created by:</dt>
* <dd>slaha</dd>
* <dt>On:</dt>
* <dd> 10.8.14 9:44</dd>
* </dl>
*/
public enum WordTextAnalysisType {
	WORD("Dle obsahu slova"),
	WORD_LENGHT("Dle délky slova");
	private final String toString;


	WordTextAnalysisType(String s) {
		this.toString = s;
	}

	@Override
	public String toString() {
		return toString;
	}
}
