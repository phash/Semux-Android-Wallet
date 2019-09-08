package de.phash.manuel.asw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.key.Network
import org.json.JSONException
import org.json.JSONObject

class ScanActivity : AppCompatActivity() {

    private var address: String = ""
    private var targetAddressString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        address = intent.getStringExtra("address")
        targetAddress = findViewById(R.id.targetAddress)
   //     txtSiteName = findViewById(R.id.site_name)

        btnScan = findViewById(R.id.btnScan)
        btnScan!!.setOnClickListener { performAction() }

        qrScanIntegrator = IntentIntegrator(this)
        setTitle(if (APIService.NETWORK == Network.MAINNET)  R.string.semuxMain else R.string.semuxTest)

    }

    internal var targetAddress: TextView? = null

    internal var btnScan: Button? = null

    internal var qrScanIntegrator: IntentIntegrator? = null

    fun onSendClick(view: View) {
        sendActivity(this, address, targetAddressString)
    }


    private fun performAction() {
        qrScanIntegrator?.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                Toast.makeText(this,"contents null\n"+  getString(R.string.result_not_found), Toast.LENGTH_LONG).show()
            } else {
                // If QRCode contains data.
                try {
                    // Converting the data to json format
                //    val obj = JSONObject(result.contents)
                //    Toast.makeText(this, "real contents:\n"+ result.contents, Toast.LENGTH_LONG).show()
                    // Show values in UI.
                    targetAddress?.text = result.contents //obj.getString("name")
                    targetAddressString = result.contents //obj.getString("name")
             //       txtSiteName?.text = obj.getString("site_name")

                } catch (e: JSONException) {
                    e.printStackTrace()

                    // Data not in the expected format. So, whole object as toast message.
                    Toast.makeText(this, "Fehler: \n"+ result.contents, Toast.LENGTH_LONG).show()
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
