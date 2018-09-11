package de.phash.manuel.asw

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import de.phash.manuel.asw.util.createQRCode
import kotlinx.android.synthetic.main.activity_qr_view.*

class QrViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_view)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        qrAddressAddress.text = intent.getStringExtra("address")
        createQR(intent.getStringExtra("address"))
    }

    private fun createQR(address: String) {
        qrAddressBigImageView.setImageBitmap(createQRCode(address, 300))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        startNewActivity(item, this)
        return super.onOptionsItemSelected(item)
    }


}
