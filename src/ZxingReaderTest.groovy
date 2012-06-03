
import java.io.File
import javax.imageio.*
import java.awt.image.BufferedImage
import groovy.io.FileType
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader

class ZxingReaderTest {

	static decode(reader, file, useHints) {
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
			println "${++current} / $total : $it.name " + resultMap.get(it.name)
		}
		println "Total images read: $totalRead"
	}
}
