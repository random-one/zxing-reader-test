
import java.io.File
import javax.imageio.*
import java.awt.image.BufferedImage
import groovy.io.FileType;
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader


class ZxingReaderTest {

	static main(args) {
		def reader = new QRCodeReader()
		def path = new File('images')

		if (!path.exists())
			throw new Exception("Directory '$path.name' does not exist")

		path.eachFile FileType.FILES, {
			try {
				def image = ImageIO.read(new File(it.canonicalPath))
				def binaryMap = new BinaryBitmap(new GlobalHistogramBinarizer(new BufferedImageLuminanceSource(image)))
				Result result = reader.decode(binaryMap)
				println "$it.name $result"
			}catch (Exception e) {
				e.printStackTrace()
			}
		}
	}
}