package de.phash.manuel.asw

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import de.phash.manuel.asw.semux.DbWorkerThread
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.semux.SemuxAddressDatabase
import de.phash.semux.Key
import kotlinx.android.synthetic.main.activity_create_account.*
import org.bouncycastle.util.encoders.Hex

class CreateAccountActivity : AppCompatActivity() {
    private var mDb: SemuxAddressDatabase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDb = SemuxAddressDatabase.getInstance(this)
        setContentView(R.layout.activity_create_account)
    }

    fun onCreateKey(view: View) {
        val key = Key()
        val pk = key.getPrivateKey()
        privateKey.setText(Hex.toHexString(pk))
        publicKey.setText(Hex.toHexString(key.getPublicKey()))
        createdaddress.setText(key.toAddressString())

        val semuxAddress = SemuxAddress(key.toAddressString(),Hex.toHexString(key.getPublicKey() ))


    }

    private fun insertAddress(semuxAddress: SemuxAddress) {
        val task = Runnable { mDb?.semuxAddressDao()?.insert(semuxAddress) }
        mDbWorkerThread.postTask(task)
    }
}
