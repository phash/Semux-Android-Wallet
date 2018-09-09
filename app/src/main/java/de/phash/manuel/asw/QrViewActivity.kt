package de.phash.manuel.asw

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_qr_view.*

class QrViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_view)
        qrAddressAddress.text = intent.getStringExtra("address")
        createQR(intent.getStringExtra("address"))
    }

    private fun createQR(address: String) {
        val multiFormatWriter = MultiFormatWriter()

        val bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, 300, 300)
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.createBitmap(bitMatrix)
        qrAddressBigImageView.setImageBitmap(bitmap)

    }
}
