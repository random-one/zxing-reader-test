

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.GridBagConstraints
import groovy.swing.SwingBuilder
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
		def swing = new SwingBuilder()

		def reader = new QRCodeReader()
		def imagePath
		def newline = System.getProperty("line.separator")

		JTextField inputDirField = new JTextField()
		inputDirField.setEditable(false)
		JTextField inputFileField = new JTextField()
		inputFileField.setEditable(false)

		swing.frame(title: 'ZXing Reader Test', defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE,
				size: [700, 800], show: true, locationRelativeTo: null) {
					lookAndFeel("system")
					panel(border:new EmptyBorder(0,0,1,1)) {

						gridBagLayout()
						label(text: "Input image directory:", constraints:gbc(ipady:0, gridx:0, gridy:0, fill:GridBagConstraints.HORIZONTAL, anchor:GridBagConstraints.PAGE_START))
						textField(inputDirField, text: "", constraints:gbc(ipady:0, gridx:1, gridy:0, fill:GridBagConstraints.HORIZONTAL))
						checkBox(id: "inputFileCheck", text: "Read from input file", constraints:gbc(gridx:0, gridy:1))
						textField(inputFileField, constraints:gbc(weightx:0.5,gridx:1, gridy:1,fill:GridBagConstraints.HORIZONTAL))
						button(text: "Browse", constraints:gbc(weightx: 0.05, gridx:2, gridy:1), actionPerformed: {
							fileChooser = new JFileChooser()
							fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
							fileChooser.showDialog(null, "Choose")
							textField(inputFileField, text: fileChooser.selectedFile, constraints:gbc(gridx:2, gridy: 1, fill:GridBagConstraints.HORIZONTAL))
						})
						textArea(rows: 10, columns: 500, constraints:gbc(fill:GridBagConstraints.BOTH,weighty:0.6, gridx:0, gridy:2, gridwidth:GridBagConstraints.REMAINDER))
					}
					menuBar() {
						menu(text: "File", mnemonic: 'F') {
							menuItem(text: "Open", mnemonic: 'O', actionPerformed: { fileChooser = new JFileChooser()
								fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
								fileChooser.showDialog(null, "Choose")
								imagePath = fileChooser.selectedFile
								textField(inputDirField, text: imagePath)
							})
							menuItem(text: "Exit", mnemonic: 'X', actionPerformed: {dispose() })
						}
						menu (text: "Action", mnemonic: 'T') {
							menuItem(text: "Decode", mnemonic: 'D', actionPerformed: {
								println imagePath
								def path = new File(imagePath.toString())

								if (!path.exists())
									throw new Exception("Directory '$path.name' does not exist")

								def current = 1
								def totalRead = 0
								def read = false
								def resultMap = [:]

								ReportGenerator report = new ReportGenerator("zxing-reader-report.pdf")
								report.addHeader(2)
								report.addHeaderColumn("File")
								report.addHeaderColumn("Decoded Text")

								def outputFile = new PrintStream("zxing-reader-report.txt")

								fileList = []

								if (swing.inputFileCheck.isSelected())
									f = new File(inputFileField.text).eachLine { fileList << it }
								else
									path.eachFile FileType.FILES, { fileList << it.canonicalPath }

								def total = fileList.size
								def start = System.currentTimeMillis()
								fileList.each  {
									try {
										Result result = decode(reader, it, false)
										file = new File(it).getName()
										resultMap[file] = result.text

										totalRead++
										read = true
									} catch (Exception e) {
										resultMap[file] = "Unable to decode"

										if (!read) {
											totalRead++
											read = false
										}
									}

									def out = "${current++} / $total : $file | "
									out = out + resultMap.get(file).replace("\n", "<br>") + newline

									report.addRow(file, resultMap.get(file), it)

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
								// decode here
							})
							menuItem(text: "Decode Iterative", mnemonic: "I", actionPerformed: {
								// forced decoding is done here
								def path = new File(imagePath.toString())

								if (!path.exists())
									throw new Exception("Directory '$path.name' does not exist")

								def current = 1
								def totalRead = 0
								def read = false
								def resultMap = [:]

								ReportGenerator report = new ReportGenerator("zxing-reader-report-force-decode.pdf")
								report.addHeader(2)
								report.addHeaderColumn("File")
								report.addHeaderColumn("Decoded Text")

								def outputFile = new PrintStream("zxing-reader-report-force-decode.txt")

								fileList = []

								if (swing.inputFileCheck.isSelected())
									f = new File(inputFileField.text).eachLine { fileList << it }
								else
									path.eachFile FileType.FILES, { fileList << it.canonicalPath }

								def total = fileList.size
								def start = System.currentTimeMillis()
								fileList.each {
									try {
										file = new File(it).getName()
										Result result = decode(reader, it, false)
										resultMap[file] = result.text

										totalRead++
										read = true
									} catch (Exception e) {
										Result result = decode(reader, it, true)

										if (result)
											resultMap[file] = result.text
										else
											resultMap[file] = "Unable to decode"

										if (!read) {
											totalRead++
											read = false
										}
									}

									def out = "${current++} / $total : $file | "
									out = out + resultMap.get(file).replace("\n", "<br>") + newline

									report.addRow(file, resultMap.get(file), it)

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
							})
						}
					}
				}
	}
}
