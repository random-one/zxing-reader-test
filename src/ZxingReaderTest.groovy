
import java.io.File
import javax.imageio.*
import java.util.concurrent.TimeUnit
import java.awt.image.BufferedImage
import groovy.io.FileType
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
// TODO: test which binarizer is better and decide to use it
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

class ZxingReaderTest {

	static decode(reader, file, useHints) {
		// TODO: rewrite this helper function in groovy style
		def image = ImageIO.read(new File(new String(file)))
		if (image == null)
			return

		if (useHints) {
			def fib1 = 1, fib2 = 1
			def fib
			for (fib = fib1 + fib2; fib < 11; fib = fib1 + fib2) {
				def roiWidth =  (1 /fib) * image.width, roiHeight = (1 / fib) * image.height
				for (def x = 0; x < image.width; x+=50) {
					for (def y = 0; y < image.height; y+=50) {
						try {
							if (roiWidth > image.width)
								roiWidth = image.width - x
							if (roiHeight > image.height)
								roiHeight = image.height - y

							def tmp = fib1
							fib1 = fib
							fib2 = tmp
							BinaryBitmap binaryMap = new BinaryBitmap(new GlobalHistogramBinarizer(new BufferedImageLuminanceSource(image, x, y, roiWidth, roiHeight)));
							return reader.decode(binaryMap)
						} catch (Exception e) {
							// TODO: handle checksum and format exceptions or at least log them as an error
						}
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

		def current = 1
		def total = path.list().length
		def totalRead = 0
		def read = false
		def resultMap = [:]

		ReportGenerator report = new ReportGenerator("zxing-reader-report.pdf")
		report.addHeader(2)
		report.addHeaderColumn("File")
		report.addHeaderColumn("Decoded Text")

		def newline = System.getProperty("line.separator")

		def outputFile = new PrintStream("zxing-reader-report.txt")

		def start = System.currentTimeMillis()

		path.eachFile FileType.FILES, {
			try {
				Result result = decode(reader, it.canonicalPath, false)
				resultMap[it.name] = result.text

				totalRead++
				read = true
			} catch (Exception e) {
				Result result = decode(reader, it.canonicalPath, true)

				if (result)
					resultMap[it.name] = result.text
				else
					resultMap[it.name] = "Unable to decode"

				if (!read) {
					totalRead++
					read = false
				}
			}

			def out = "${current++} / $total : $it.name | "
			out = out + resultMap.get(it.name).replace("\n", "<br>") + newline

			report.addRow(it.name, resultMap.get(it.name), it.canonicalPath)

			outputFile.append(out)
			print out
		}
		total = "Total images read: $totalRead / $total"
		println total
		outputFile.append(total + newline)
		def end = System.currentTimeMillis()
		def elapsed = "Time elapsed: " + TimeUnit.SECONDS.convert(end - start, TimeUnit.MILLISECONDS) + " s"
		println elapsed
		outputFile.append(elapsed)
		report.table.addCell(total)
		report.table.addCell(elapsed)
		report.close()
	}
}
