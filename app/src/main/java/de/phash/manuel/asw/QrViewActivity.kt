package de.phash.manuel.asw

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.phash.manuel.asw.util.createQRCode
import kotlinx.android.synthetic.main.activity_qr_view.*

class QrViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_view)
        qrAddressAddress.text = intent.getStringExtra("address")
        createQR(intent.getStringExtra("address"))
    }

    private fun createQR(address: String) {
        qrAddressBigImageView.setImageBitmap(createQRCode(address, 300))
    }


}
