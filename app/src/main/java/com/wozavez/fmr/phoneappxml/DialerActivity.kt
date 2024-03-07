package com.wozavez.fmr.phoneappxml

import android.Manifest.permission.CALL_PHONE
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.TableRow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.checkSelfPermission
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
                                makeCall("*102#")
                                return@setOnLongClickListener true
                            }
                        }
                        else if (button.text.isNotEmpty() && button.text[0] == '2') {
                            button.setOnLongClickListener {
                                makeCall("*121#")
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
        binding.buttonCall.setOnClickListener {
            if (binding.phoneNumberTextView.text.length >= 10 &&
                (binding.phoneNumberTextView.text.startsWith("06")
                        || binding.phoneNumberTextView.text.startsWith("00")))
            makeCall(binding.phoneNumberTextView.text.toString())
        }
    }

    private fun makeCall(number: String) {
        if (checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            val s = Uri.encode(number)

            telecomManager.placeCall(Uri.parse("tel:$s"), null)
        } else {
            requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION)
        }
    }

    private fun offerReplacingDefaultDialer() {
        val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager

        activityResultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
    }

    companion object {
        const val REQUEST_PERMISSION = 0
    }
}