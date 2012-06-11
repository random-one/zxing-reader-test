
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

import com.google.zxing.*

class ReportGenerator {
	private reportName = "zxing-reader-report.pdf"
	private PdfPTable table
	private Document report

	ReportGenerator() {
		report = new Document(PageSize.A4)
		PdfWriter writer = PdfWriter.getInstance(report, new FileOutputStream(reportName))
		report.open()
	}
	def addHeader(tableColumns) {
		table = new PdfPTable(tableColumns)
	}
	def addHeaderColumn(title, hAlign = Element.ALIGN_CENTER, bgColor = new BaseColor(204, 255, 230)) {
		PdfPCell cell = new PdfPCell(new Paragraph(title))
		cell.setHorizontalAlignment(hAlign)
		cell.setBackgroundColor(bgColor)
		cell.setPadding(10.0f)
		table.addCell(cell)
	}
	def addRow(name, Result result, imagePath) {
		table.addCell(new Paragraph(name))
		if (result)
			table.addCell(new Paragraph(result.text))
		else
			table.addCell(new Paragraph("null"))
		Image image = Image.getInstance(imagePath)
		table.addCell(image)
	}
	def close() {
		report.add(table)
		report.close()
	}
}
