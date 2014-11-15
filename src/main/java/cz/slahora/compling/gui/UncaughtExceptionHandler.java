package cz.slahora.compling.gui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

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
	public static final EmptyBorder BORDER = new EmptyBorder(new Insets(10, 10, 10, 10));
	private AtomicBoolean displayingDialog = new AtomicBoolean(false);

	@Override
	public void uncaughtException(Thread t, Throwable e) {

		String name = e.getClass().getSimpleName();
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

		if (displayingDialog.getAndSet(true)) {
			return;
		}
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JLabel headline = new JLabel("V programu došlo k neočekávané chybě.");


		JLabel text1 = new JLabel("Ve vlákně " + t.toString() + " došlo k výjimce " + name);
		JLabel text2 = new JLabel("Příčinou chyby je: " + e.getMessage());



		JTextArea area = new JTextArea(stackTrace.toString());
		area.setBorder(BORDER);
		area.setEditable(false);


		headline.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(headline);
		panel.add(Box.createVerticalStrut(25));

		text1.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(text1);
		panel.add(Box.createVerticalStrut(10));

		text2.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(text2);
		panel.add(Box.createVerticalStrut(10));

		text3.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(text3);
		panel.add(Box.createVerticalStrut(10));

		area.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(area);
		panel.add(Box.createVerticalStrut(10));

		JScrollPane allScroll = new JScrollPane(panel) {
			@Override
			public Dimension getPreferredSize() {

				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				int width = gd.getDisplayMode().getWidth();
				int height = gd.getDisplayMode().getHeight();

				return new Dimension((int)(width * 0.75), (int)(height * 0.75));
			}
		};
		panel.setBorder(BORDER);

		JOptionPane pane = new JOptionPane(allScroll, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);

		JDialog dialog = pane.createDialog("Neočekávaná chyba");
		dialog.setResizable(true);

		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				displayingDialog.set(false);
			}
		});

		dialog.setVisible(true);

	}

	private void logStackTrace(String stackTrace, String name) throws IOException {

		if (!LOG_FILE.exists()) {
			boolean newFile = LOG_FILE.createNewFile();
			if (!newFile) {
				throw new IOException("Cannot create log file " + LOG_FILE.getAbsolutePath());
			}
		}
		FileUtils.write(LOG_FILE, createText(name, stackTrace), cz.slahora.compling.gui.utils.FileUtils.UTF8, true);
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
