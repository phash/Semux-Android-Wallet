package de.phash.manuel.asw

import android.content.ContentValues
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.semux.Key
import kotlinx.android.synthetic.main.activity_create_account.*
import org.bouncycastle.util.encoders.Hex

class CreateAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_account)
    }

    fun onCreateKey(view: View) {
        val key = Key()
        val pk = key.getPrivateKey()
        privateKey.text = Hex.toHexString(pk)
        publicKey.text = Hex.toHexString(key.getPublicKey())
        createdaddress.text = key.toAddressString()

        val semuxAddress = SemuxAddress(null, key.toAddressString(), Hex.toHexString(pk), Hex.toHexString(key.getPublicKey()))
        val values = ContentValues()
        values.put("address", key.toAddressString())
        values.put("publickey", Hex.toHexString(key.getPublicKey()))
        values.put("privatekey", Hex.toHexString(pk))


        database.use { insert(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME, null, values) }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.balancesMenu -> balanceActivity(this)
            R.id.createAccout -> createActivity(this)

        }
        return super.onOptionsItemSelected(item)
    }
}
