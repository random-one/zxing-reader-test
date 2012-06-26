
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

import com.google.zxing.*

class ReportGenerator {
	private reportName
	private PdfPTable table
	private Document report

	ReportGenerator(reportName) {
		report = new Document(PageSize.A4)
		PdfWriter writer = PdfWriter.getInstance(report, new FileOutputStream(reportName))
		report.open()
	}
	def addHeader(tableColumns) {
		table = new PdfPTable(tableColumns)
		float[] widths = [ 30.0f, 70.0f ]
		table.setWidths(widths)
		table.setHorizontalAlignment(Element.ALIGN_CENTER)
	}
	def addHeaderColumn(title, hAlign = Element.ALIGN_CENTER, bgColor = new BaseColor(204, 255, 230)) {
		PdfPCell cell = new PdfPCell(new Paragraph(title))
		cell.setHorizontalAlignment(hAlign)
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE)
		cell.setBackgroundColor(bgColor)
		cell.setPadding(10.0f)
		table.addCell(cell)
	}
	def addRow(name, result, imagePath) {
		PdfPCell cell = new PdfPCell()
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE)
		Chunk file = new Chunk(name)
		file.setAction(new PdfAction("file:///" + imagePath))
		Paragraph p = new Paragraph(file)
		p.setAlignment(Element.ALIGN_CENTER)
		cell.addElement(p)
		table.addCell(cell)
		table.addCell(result)
	}
	def close() {
		report.add(table)
		report.close()
	}
}
