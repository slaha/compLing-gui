package cz.slahora.compling.gui.utils;

import javax.swing.JLabel;

/**
 * 
 * Builder for creating JLabel with html content
 * 
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd>29.3.14 12:12</dd>
 * </dl>
 */
public class HtmlLabelBuilder {

	public static final String HTML = "<html>";
	public static final String HTML_END = "</html>";

	private final StringBuilder builder;

	public HtmlLabelBuilder() {
		builder = new StringBuilder(HTML);
	}

	public JLabel build() {
		builder.append(HTML_END);
		return new JLabel(builder.toString());
	}

	public HtmlLabelBuilder hx(int level, String text, Object... params) {
		builder.append("<h").append(level).append(">").append(String.format(text, params)).append("</h").append(level).append(">");
		return this;
	}

	public HtmlLabelBuilder p(String text, Object... params) {
		builder.append("<p>").append(String.format(text, params)).append("</p>");
		return this;
	}



	public HtmlLabelBuilder startBulletList() {
		builder.append("<ul>");
		return this;
	}

	public HtmlLabelBuilder li(String text, Object... params) {
		builder.append("<li>").append(String.format(text, params)).append("</li>");
		return this;
	}

	public HtmlLabelBuilder stopBulletList() {
		builder.append("</ul>");
		return this;
	}

	public HtmlLabelBuilder margin(int pxMargin) {
		builder.append("<div style='height: ").append(pxMargin).append("'>&nbsp</div>");
		return this;

	}
}
