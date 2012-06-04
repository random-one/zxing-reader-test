
import java.io.File
import javax.imageio.*
import java.awt.image.BufferedImage
import groovy.io.FileType
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
// TODO: test which binarizer is better and decide to use it
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

class ZxingReaderTest {

	static decode(reader, file, useHints) {
		// TODO: rewrite this helper function in groovy style
		def image = ImageIO.read(new File(new String(file)))
		if (image == null)
			return

		if (useHints) {
			for (def x = 0; x < image.width; x+=50) {
				for (def y = 0; y < image.height; y+=50) {
					try {
						def roiWidth = 200, roiHeight = 200
						if (x + roiWidth > image.width)
							roiWidth = image.width - x
						if (y + roiHeight > image.height)
							roiHeight = image.height - y

						BinaryBitmap binaryMap = new BinaryBitmap(new GlobalHistogramBinarizer(new BufferedImageLuminanceSource(image, x, y, roiWidth, roiHeight)));
						return reader.decode(binaryMap)
					} catch (Exception e) {
					// TODO: handle checksum and format exceptions or at least log them as an error
					}
				}
			}
		}
		else {
			BinaryBitmap binaryMap = new BinaryBitmap(new GlobalHistogramBinarizer(new BufferedImageLuminanceSource(image)))
			return reader.decode(binaryMap)
		}
	}

	static main(args) {
		def reader = new QRCodeReader()
		def path = new File('images')

		if (!path.exists())
			throw new Exception("Directory '$path.name' does not exist")

		def current = 0
		def total = path.list().length
		def totalRead = 0
		def read = false
		def resultMap = [:]

		Document document = new Document(PageSize.A4)
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("zxing-reader-report.pdf"))
		document.open()
		PdfPTable table = new PdfPTable(3)
		PdfPCell cell = new PdfPCell(new Paragraph ("File"))
		cell.setHorizontalAlignment(Element.ALIGN_CENTER)
		cell.setBackgroundColor(new BaseColor(204, 255, 230))
		cell.setPadding(10.0f)
		table.addCell(cell)
		cell = new PdfPCell(new Paragraph("Result"))
		cell.setHorizontalAlignment(Element.ALIGN_CENTER)
		cell.setBackgroundColor(new BaseColor(204, 255, 230))
		cell.setPadding (10.0f)
		table.addCell(cell)
		cell = new PdfPCell(new Paragraph("Preview"))
		cell.setHorizontalAlignment(Element.ALIGN_CENTER)
		cell.setBackgroundColor(new BaseColor(204, 255, 230))
		cell.setPadding (10.0f)
		table.addCell(cell)

		path.eachFile FileType.FILES, {
			try {
				Result result = decode(reader, it.canonicalPath, false)
				resultMap[it.name] = result

				totalRead++
				read = true
			} catch (Exception e) {
				Result result = decode(reader, it.canonicalPath, true)

				resultMap[it.name] = result
				if (!read) {
					totalRead++
					read = false
				}
			}
			Image image = Image.getInstance(it.canonicalPath)
			table.addCell(new Paragraph("$it.name"))
			table.addCell(new Paragraph("${resultMap.get(it.name)}"))
			table.addCell(image)

			println "${++current} / $total : $it.name " + resultMap.get(it.name)
		}
		println "Total images read: $totalRead"
		document.add(table)
		document.close();
	}
}
