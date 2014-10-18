package cz.slahora.compling.gui.analysis;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfTemplate;
//import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.JPanel;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * TODO 
 *
 * <dl>
 * <dt>Created by:</dt>
 * <dd>slaha</dd>
 * <dt>On:</dt>
 * <dd> 29.3.14 18:35</dd>
 * </dl>
 */
public class PdfExporter {
	private final JPanel panel;

	public PdfExporter(JPanel panel) {
		this.panel = panel;
	}

	public void export(File pdfFile) throws FileNotFoundException/*, DocumentException*/ {
//
//		Document document = new Document(PageSize.A4);
//		// step 2
//		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
//		// step 3
//		document.open();
//		// step 4
//		PdfContentByte contentByte = writer.getDirectContent();
//		System.out.println(panel.getHeight());
//		PdfTemplate template = contentByte.createTemplate(panel.getWidth(),1000);
//		Graphics2D g2 = template.createGraphics(panel.getWidth(),1000);
//		panel.print(g2);
//		g2.dispose();
//		contentByte.addTemplate(template, 0, 0);
//		// step 5
//		document.close();

	}
}
