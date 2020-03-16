package ru.otus.demo.readsms

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            printSmsList()
            printContactList()
        }
    }

    private fun printSmsList() {
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_SMS permission is not granted. You should grant it for this app")
            return
        }

        val selection = Telephony.TextBasedSmsColumns.ADDRESS + " = ?"
        val selArgs = arrayOf("+79035555555")
        val order = "address DESC" //
        val uri = Uri.parse(INBOX)
        val contR = contentResolver
        val cursor1 = contR.query(uri, null, selection, selArgs, order)

        cursor1?.close()

        val cursor = contentResolver.query(Uri.parse(INBOX), null, null, null, null)
        if (cursor == null) {
            Log.d(TAG, "Unable to read sms!")
            return
        }
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                val msgData = StringBuilder()
                for (i in 0 until cursor.columnCount) {
                    msgData.append(" ").append(cursor.getColumnName(i)).append(":").append(cursor.getString(i))
                }
                msgData.append("\n")
                // use msgData
                Log.d(TAG, "read sms:$msgData")
            } while (cursor.moveToNext())
        } else {
            // empty box, no SMS
        }
        cursor.close()
    }

    private fun printContactList() {
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_CONTACTS permission is not granted. You should grant it for this app")
            return
        }
        val cr = contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null)
        if (cursor?.count ?: 0 > 0) {
            while (cursor!!.moveToNext()) {
                val id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME))
                if (cursor.getInt(cursor.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val phoneCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                    while (phoneCur!!.moveToNext()) {
                        val phoneNo = phoneCur.getString(phoneCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Log.i(TAG, "Name: $name")
                        Log.i(TAG, "Phone Number: $phoneNo")
                    }
                    phoneCur.close()
                }
            }
        }
        cursor?.close()
    }

    companion object {
        private const val TAG = "sms_and_contacts"

        //class Telephony.Sms.Inbox.CONTENT_URI
        const val INBOX = "content://sms/inbox"
        const val SENT = "content://sms/sent"
        const val DRAFT = "content://sms/draft"
    }
}