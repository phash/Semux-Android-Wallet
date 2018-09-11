package de.phash.manuel.asw

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.key.Hex
import de.phash.manuel.asw.semux.key.Key
import kotlinx.android.synthetic.main.activity_import_key.*

class ImportKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_key)
        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    fun importClick(view: View) {
        val pkey = importEditText.text.toString()
        if (pkey.isEmpty())
            Toast.makeText(this, "Key may not be empty", Toast.LENGTH_LONG).show()
        else {

            val key = Key(Hex.decode0x(pkey))
            importAddress.text = key.toAddressString()
            importPubKey.text = Hex.encode0x(key.publicKey)

            val values = ContentValues()
            values.put("address", key.toAddressString())
            values.put("publickey", org.bouncycastle.util.encoders.Hex.toHexString(key.publicKey))
            values.put("privatekey", org.bouncycastle.util.encoders.Hex.toHexString(key.privateKey))

            database.use { insert(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME, null, values) }

        }
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
