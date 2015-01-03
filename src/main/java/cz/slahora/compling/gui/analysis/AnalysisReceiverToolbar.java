package cz.slahora.compling.gui.analysis;

import cz.slahora.compling.gui.model.LastDirectory;
import cz.slahora.compling.gui.ui.ResultsPanel;
import cz.slahora.compling.gui.utils.FileChooserUtils;
import cz.slahora.compling.gui.utils.FileIOUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import static cz.slahora.compling.gui.utils.IconUtils.Icon;
import static cz.slahora.compling.gui.utils.IconUtils.getIcon;

/**
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 13:26</dd>
 * </dl>
 */
public class AnalysisReceiverToolbar extends JToolBar implements ActionListener {
	private static final int EXPORT = 1;
	private static final int PRINT = 2;
	private static final int SAVE = 3;

	private final ResultsPanel panel;

	public AnalysisReceiverToolbar(ResultsPanel panel) {
		this.panel = panel;

		JButton export = new JButton("Export vstupních dat do CSV", getIcon(Icon.DOCUMENT_SAVE));
		export.addActionListener(this);
		export.putClientProperty("id", EXPORT);
		add(export);

		JButton print = new JButton("Tisk");
		print.addActionListener(this);
		print.putClientProperty("id", PRINT);
		//add(print);

		JButton saveAs = new JButton("Uložit");
		saveAs.addActionListener(this);
		saveAs.putClientProperty("id", SAVE);
		//add(saveAs);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object id = ((JComponent) e.getSource()).getClientProperty("id");
		if (id == null) {
			throw new IllegalStateException(e.getSource() + " does not have any id");
		}
		switch ((Integer)id) {

			case EXPORT:
				LastDirectory lastDirectory = LastDirectory.getInstance();
				File csvFile = FileChooserUtils.getFileToSave(lastDirectory.getLastDirectory(), getParent(), "csv");
				if (FileIOUtils.checkFile(csvFile, getParent(), false)) {
					FileIOUtils.perform(new FileIOUtils.IoOperation(csvFile) {
						@Override
						public void perform() throws IOException {
							CsvExporter csvExporter = new CsvExporter(panel.getCsvData());
							csvExporter.export(file);

						}
					}, getParent());
				}
				break;
			case PRINT:
				PrinterJob pj = PrinterJob.getPrinterJob();
				Printable printable = new Printable() {

					public int print(Graphics pg, PageFormat pf, int pageNum){
						if (pageNum > 0){
							return Printable.NO_SUCH_PAGE;
						}

						Graphics2D g2 = (Graphics2D) pg;
						g2.translate(pf.getImageableX(), pf.getImageableY());
						panel.getPanel().paint(g2);
						return Printable.PAGE_EXISTS;
					}
				};
				pj.setPrintable(printable);
				if (pj.printDialog()) {
					try {pj.print();}
					catch (PrinterException exc) {
						System.out.println(exc);
					}
				}
				break;
			case SAVE:
				PdfExporter exporter = new PdfExporter(panel.getPanel());
				lastDirectory = LastDirectory.getInstance();
				File pdfFile = null;
				try {
					pdfFile = FileChooserUtils.getFileToSave(lastDirectory.getLastDirectory(), getParent(), "pdf");
					if (pdfFile != null) {
						exporter.export(pdfFile);
					}
				} catch (Exception ex) {

				} finally {
					if (pdfFile != null) {
						lastDirectory.setLastDirectory(pdfFile.getParentFile());
					}
				}

				break;
		}

	}
}
