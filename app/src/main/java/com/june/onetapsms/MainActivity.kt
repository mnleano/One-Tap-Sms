package com.june.onetapsms

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.june.onetapsms.accessibility.Codes
import com.june.onetapsms.databinding.DialogPersonalInformationBinding
import com.june.onetapsms.databinding.DialogReminderBinding
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


// RECIPIENT NUMBER
const val SMART_RECIPIENT = "09213244493"
const val GLOBE_RECIPIENT = "09456079023"

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private fun recipient() =
        if (SharedPrefManager.network == 0 ||
            SharedPrefManager.network == 1
        ) SMART_RECIPIENT
        else GLOBE_RECIPIENT

    // MESSAGE SEND TO RECIPIENT
    private fun message() =
        "1 count confirmed booking ${SharedPrefManager.name}, " +
                "confirmation number=${SharedPrefManager.confirmationNumber}, " +
                "ra number=${SharedPrefManager.raNumber}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()
        setupInformation()

        registerReceiver(smsSentReceiver, IntentFilter(SMS_SENT))
        registerReceiver(smsDeliveredReceiver, IntentFilter(SMS_DELIVERED))
    }

    fun onSendMessageClick(view: View) {
        makeUSSDCall()
    }

    private fun setupPermissions() {
        if (hasCallAndSmsPermissions()) Log.d(
            TAG,
            "setupPermissions: Both permissions granted ignore"
        )
        else EasyPermissions.requestPermissions(
            this,
            getString(R.string.rationale_call_sms),
            RC_CALL_SEND,
            *CALL_SMS_PERMISSIONS
        )
    }

    private fun hasCallAndSmsPermissions() =
        EasyPermissions.hasPermissions(this, *CALL_SMS_PERMISSIONS)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.reminders) showReminder()
        return super.onOptionsItemSelected(item)
    }

    private fun showReminder() {
        val dialogBinding = DialogReminderBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.ok.setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0f)
        dialog.show()
    }

    private fun setupInformation() {
        Log.d(TAG, "setupInformation: isInitialSetup=${SharedPrefManager.isInitialSetup}")
        if (!SharedPrefManager.isInitialSetup) {
            val dialogBinding = DialogPersonalInformationBinding.inflate(LayoutInflater.from(this))

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogBinding.root)

            val alertDialog = builder.create()
            alertDialog.show()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.window?.setDimAmount(0f)

            dialogBinding.rgNetwork.setOnCheckedChangeListener { _, _ ->
                dialogBinding.btnSubmit.isEnabled = true
            }
            dialogBinding.btnSubmit.setOnClickListener {
                val name = dialogBinding.name.text.toString()
                val confirmationNumber = dialogBinding.confirmationNumber.text.toString()
                val raNumber = dialogBinding.raNumber.text.toString()
                val network = when (dialogBinding.rgNetwork.checkedRadioButtonId) {
                    R.id.rbSmart -> 0
                    R.id.rbTnt -> 1
                    R.id.rbGlobe -> 2
                    else -> 3
                }
                Log.d(
                    TAG,
                    "setupInformation->btnSubmit.onClick: name=$name, confirmationNumber=$confirmationNumber, raNumber=$raNumber, network=$network"
                )
                SharedPrefManager.name = name
                SharedPrefManager.confirmationNumber = confirmationNumber
                SharedPrefManager.raNumber = raNumber
                SharedPrefManager.network = network
                SharedPrefManager.isInitialSetup = true
                alertDialog.dismiss()
            }
        }
    }

    @AfterPermissionGranted(RC_CALL)
    private fun makeUSSDCall() {
        if (hasCallAndSmsPermissions()) {
            Codes.index = 0
            val ussd = "*143${Uri.encode("#")}"
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel: $ussd")))
            Handler(Looper.getMainLooper()).postDelayed({
                sendSms()
            }, SMS_DELAY)
        } else EasyPermissions.requestPermissions(
            this,
            getString(R.string.rationale_call_sms),
            RC_CALL,
            *CALL_SMS_PERMISSIONS
        )
    }

    @AfterPermissionGranted(RC_SMS)
    private fun sendSms() {
        if (hasCallAndSmsPermissions()) {
            Log.d(TAG, "onSendMessageClick: sendMessage")
            sendMessage()
        } else
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.rationale_sms),
                RC_SMS,
                Manifest.permission.SEND_SMS
            )
    }

    private fun sendMessage() {
        val sentPendingIntent = PendingIntent.getBroadcast(this, 0, Intent(SMS_SENT), 0)
        val deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, Intent(SMS_DELIVERED), 0)

        Log.d(
            TAG,
            "sendMessage: name=${SharedPrefManager.name}, network=${SharedPrefManager.network}, recipient=${recipient()}, message=${message()}"
        )
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            recipient(),
            null,
            message(),
            sentPendingIntent,
            deliveredPendingIntent
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted: requestCode: $requestCode, perms=$perms")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied: requestCode=$requestCode, perms=$perms")
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Log.d(TAG, "onRationaleAccepted: requestCode=$requestCode")
    }

    override fun onRationaleDenied(requestCode: Int) {
        Log.d(TAG, "onRationaleDenied: requestCode=$requestCode")
    }

    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                RESULT_OK -> Log.d(TAG, "$SMS_SENT RESULT_OK")
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.d(
                    TAG,
                    "$SMS_SENT RESULT_ERROR_GENERIC_FAILURE"
                )
                SmsManager.RESULT_ERROR_NO_SERVICE -> Log.d(
                    TAG,
                    "$SMS_SENT RESULT_ERROR_NO_SERVICE"
                )
                SmsManager.RESULT_ERROR_NULL_PDU -> Log.d(TAG, "$SMS_SENT RESULT_ERROR_NULL_PDU")
                SmsManager.RESULT_ERROR_RADIO_OFF -> Log.d(TAG, "$SMS_SENT RESULT_ERROR_RADIO_OFF")
                else -> Log.w(TAG, "$SMS_SENT unhandled result=$resultCode")
            }
        }
    }

    private val smsDeliveredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (resultCode) {
                RESULT_OK -> Log.d(TAG, "$SMS_DELIVERED RESULT_OK")
                RESULT_CANCELED -> Log.d(TAG, "$SMS_DELIVERED RESULT_CANCELED")
                else -> Log.w(TAG, "$SMS_DELIVERED unhandled result=$resultCode")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsSentReceiver)
        unregisterReceiver(smsDeliveredReceiver)
    }

    companion object {
        const val TAG = "MainActivity"
        const val RC_CALL_SEND = 100
        const val RC_CALL = 101
        const val RC_SMS = 102
        const val SMS_SENT = "SMS_SENT"
        const val SMS_DELIVERED = "SMS_DELIVERED"
        const val SMS_DELAY = 60L * 1000L

        private val CALL_SMS_PERMISSIONS =
            arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS)
    }
}