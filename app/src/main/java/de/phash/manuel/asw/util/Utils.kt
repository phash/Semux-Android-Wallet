package de.phash.manuel.asw.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

fun createQRCode(text: String, width: Int): Bitmap? {

    val multiFormatWriter = MultiFormatWriter()

    val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, width, width)
    val barcodeEncoder = BarcodeEncoder()
    return barcodeEncoder.createBitmap(bitMatrix)

}