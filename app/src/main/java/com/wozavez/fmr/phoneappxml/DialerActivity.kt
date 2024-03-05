package com.wozavez.fmr.phoneappxml

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER
import android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME
import android.widget.Button
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.wozavez.fmr.phoneappxml.databinding.ActivityDialerBinding

class DialerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDialerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        offerReplacingDefaultDialer()

        for (child in binding.tableLayout.children) {
            for (button in (child as TableRow).children) {
                try {
                    if ((button as Button).text.isNotEmpty() && button.text[0] in "0123456789") {
                        button.setOnClickListener {
                            binding.phoneNumberTextView.text = buildString {
                                append(binding.phoneNumberTextView.text.toString())
                                append(button.text[0])
                            }
                            if (binding.phoneNumberTextView.text.isNotEmpty()) binding.buttonBackspace.isVisible = true
                        }
                        if (button.text.isNotEmpty() && button.text[0] == '1') {
                            button.setOnLongClickListener {
                                val s = Uri.encode("*102#")
                                val callIntent = Intent(Intent.ACTION_CALL)
                                callIntent.setData(Uri.parse("tel:$s"))
                                startActivity(callIntent)
                                return@setOnLongClickListener true
                            }
                        }
                        else if (button.text.isNotEmpty() && button.text[0] == '2') {
                            button.setOnLongClickListener {
                                val s = Uri.encode("*121#")
                                val callIntent = Intent(Intent.ACTION_CALL)
                                callIntent.setData(Uri.parse("tel:$s"))
                                startActivity(callIntent)
                                return@setOnLongClickListener true
                            }
                        }
                    }
                } catch (_: ClassCastException) {}
            }
        }
        binding.buttonBackspace.setOnClickListener {
            binding.phoneNumberTextView.text = buildString {
                append(binding.phoneNumberTextView.text.toString().dropLast(1))
            }
            if (binding.phoneNumberTextView.text.isEmpty()) binding.buttonBackspace.isInvisible = true
        }
        binding.buttonBackspace.setOnLongClickListener {
            binding.phoneNumberTextView.text = ""
            binding.buttonBackspace.isInvisible = true
            return@setOnLongClickListener true
        }
        binding.buttonCall.setOnClickListener { makeCall() }
    }

    private fun makeCall() {
        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            val uri = "tel:${binding.phoneNumberTextView.text}".toUri()
            startActivity(Intent(Intent.ACTION_CALL, uri))
        } else {
            requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && PERMISSION_GRANTED in grantResults) {
            makeCall()
        }
    }

    private fun offerReplacingDefaultDialer() {
        if (getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName) {
//            Intent(ACTION_CHANGE_DEFAULT_DIALER)
//                .putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
//                .let(::startActivity)
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(intent, REQUEST_PERMISSION)
        }
        val intent = Intent(ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
        startActivityForResult(intent, REQUEST_PERMISSION)
    }

    companion object {
        const val REQUEST_PERMISSION = 0
    }
}