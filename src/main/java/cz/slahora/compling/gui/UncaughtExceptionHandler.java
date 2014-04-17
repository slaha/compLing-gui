package cz.slahora.compling.gui;

import cz.slahora.compling.gui.utils.GridBagConstraintBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * Handler for catching exceptions. Displays dialog and logs the stack trace to compLingGuiError.log file
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 14.4.14 8:57</dd>
 * </dl>
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static final File LOG_FILE = new File(System.getProperty("user.home") + File.separator + "compLingGuiError.log");
	private static final Charset UTF8 = Charset.forName(CharEncoding.UTF_8);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		JPanel panel = new JPanel(new GridBagLayout());

		JLabel headline = new JLabel("V programu došlo k neočekávané chybě.");

		String name = e.getClass().getSimpleName();
		JLabel text1 = new JLabel("Ve vlákně " + t.toString() + " došlo k výjimce " + name);
		JLabel text2 = new JLabel("Příčinou chyby je: " + e.getMessage());
		JLabel text3;

		StringBuilder stackTrace = new StringBuilder(ExceptionUtils.getStackTrace(e));
		try {
			logStackTrace(stackTrace.toString(), name);
			text3 = new JLabel("Zpráva o chybě byla uložena do souboru " + LOG_FILE.getAbsolutePath());
		} catch (IOException e1) {
			text3 = new JLabel("Zprávu o chybě se nepodařilo uložit do souboru!");
			stackTrace
				.append("\n\n")
				.append(ExceptionUtils.getStackTrace(e1));
		}
		JTextArea area = new JTextArea(stackTrace.toString());
		area.setEditable(false);

		GridBagConstraintBuilder gbc;
		int y = 0;
		gbc = new GridBagConstraintBuilder().gridxy(0, y++).fill(GridBagConstraints.HORIZONTAL).weightx(1);

		panel.add(headline, gbc.build());
		Insets insets = new Insets(20, 0, 0, 0);
		panel.add(text1, gbc.copy().gridy(y++).insets(insets).build());
		panel.add(text2, gbc.copy().gridy(y++).insets(insets).build());
		panel.add(text3, gbc.copy().gridy(y++).insets(insets).build());
		panel.add(area, gbc.copy().gridy(y).insets(insets).build());

		JOptionPane.showMessageDialog(null, panel, "Neočekávaná chyba", JOptionPane.ERROR_MESSAGE);


	}

	public boolean logStackTrace(Throwable t) {
		try {
			logStackTrace(ExceptionUtils.getStackTrace(t), t.getClass().getSimpleName());
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void logStackTrace(String stackTrace, String name) throws IOException {

		if (!LOG_FILE.exists()) {
			boolean newFile = LOG_FILE.createNewFile();
			if (!newFile) {
				throw new IOException("Cannot create log file " + LOG_FILE.getAbsolutePath());
			}
		}
		FileUtils.write(LOG_FILE, createText(name, stackTrace), UTF8, true);
	}

	private String createText(String name, String stackTrace) {
		String date = new SimpleDateFormat("yyyy-MM-dd HH.mm:ss").format(new Date());
		StringBuilder sb = new StringBuilder();
		sb
			.append("\n\n")
			.append(date)
			.append('\n')
			.append(name)
			.append('\n');

		for (int i = 0; i < Math.max(date.length(), name.length()); i++) {
			sb.append('=');
		}

		return sb.append('\n').append(stackTrace).append('\n').toString();
	}
}
