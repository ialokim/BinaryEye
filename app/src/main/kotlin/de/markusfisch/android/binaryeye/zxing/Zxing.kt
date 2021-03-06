package de.markusfisch.android.binaryeye.zxing

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

import android.graphics.Bitmap

import java.util.Arrays
import java.util.EnumMap
import java.util.EnumSet

class Zxing {
	private val multiFormatReader: MultiFormatReader = MultiFormatReader()

	init {
		val decodeFormats = EnumSet.noneOf<BarcodeFormat>(
			BarcodeFormat::class.java
		)
		decodeFormats.addAll(
			EnumSet.copyOf(
				Arrays.asList(
					BarcodeFormat.AZTEC,
					BarcodeFormat.CODABAR,
					BarcodeFormat.CODE_39,
					BarcodeFormat.CODE_93,
					BarcodeFormat.CODE_128,
					BarcodeFormat.DATA_MATRIX,
					BarcodeFormat.EAN_8,
					BarcodeFormat.EAN_13,
					BarcodeFormat.ITF,
					BarcodeFormat.MAXICODE,
					BarcodeFormat.PDF_417,
					BarcodeFormat.QR_CODE,
					BarcodeFormat.RSS_14,
					BarcodeFormat.RSS_EXPANDED,
					BarcodeFormat.UPC_A,
					BarcodeFormat.UPC_E,
					BarcodeFormat.UPC_EAN_EXTENSION
				)
			)
		)

		val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats)

		multiFormatReader.setHints(hints)
	}

	fun decode(
		yuvData: ByteArray,
		width: Int,
		height: Int,
		invert: Boolean = false
	): Result? {
		val source = PlanarYUVLuminanceSource(
			yuvData,
			width,
			height,
			0,
			0,
			width,
			height,
			false
		)
		return decodeLuminanceSource(
			if (invert) {
				source.invert()
			} else {
				source
			}
		)
	}

	private fun decodeLuminanceSource(source: LuminanceSource): Result? {
		val bitmap = BinaryBitmap(HybridBinarizer(source))
		return try {
			multiFormatReader.decodeWithState(bitmap)
		} catch (e: ReaderException) {
			null
		} finally {
			multiFormatReader.reset()
		}
	}

	companion object {
		private const val BLACK = 0xff000000.toInt()
		private const val WHITE = 0xffffffff.toInt()

		fun encodeAsBitmap(
			text: String,
			format: BarcodeFormat,
			width: Int,
			height: Int
		): Bitmap? {
			val result = MultiFormatWriter().encode(
				text, format,
				width, height, null
			)
			val w = result.getWidth()
			val h = result.getHeight()
			val pixels = IntArray(w * h)
			var offset = 0
			for (y in 0..h - 1) {
				for (x in 0..w - 1) {
					pixels[offset + x] = if (result.get(x, y))
						BLACK
					else
						WHITE
				}
				offset += w
			}
			val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
			bitmap.setPixels(pixels, 0, width, 0, 0, w, h)
			return bitmap
		}
	}
}
