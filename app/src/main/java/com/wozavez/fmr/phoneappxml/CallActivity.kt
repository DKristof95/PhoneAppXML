package com.wozavez.fmr.phoneappxml

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
import android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
import android.media.AudioManager
import android.os.Bundle
import android.telecom.Call
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.wozavez.fmr.phoneappxml.databinding.ActivityCallBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class CallActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()
    private lateinit var number: String
    private lateinit var binding: ActivityCallBinding
    private val timer = Timer()
    private var time = 0
    private var isSpeakerOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        number = intent.data?.schemeSpecificPart ?: ""

    }

    override fun onStart() {
        super.onStart()

        binding.buttonEndCall.setOnClickListener {
            OngoingCall.hangup()
        }

        binding.buttonSpeaker.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.setMode(AudioManager.MODE_IN_CALL)

            if (isSpeakerOn) {
                audioManager.availableCommunicationDevices.firstOrNull { it.type == TYPE_BUILTIN_EARPIECE }
                    ?.let {
                        Timber.d("changed to earpiece")
                        val result = audioManager.setCommunicationDevice(it)
                        Timber.d(result.toString() + " " + audioManager.communicationDevice.toString())
                    }
                binding.buttonSpeaker.setBackgroundColor(Color.GRAY)
                isSpeakerOn = false
            } else {
                audioManager.availableCommunicationDevices.firstOrNull { it.type == TYPE_BUILTIN_SPEAKER }
                    ?.let {
                        Timber.d("changed to speaker")
                        val result = audioManager.setCommunicationDevice(it)
                        Timber.d(result.toString() + " " + audioManager.communicationDevice.toString())
                    }
                binding.buttonSpeaker.setBackgroundColor(Color.RED)
                isSpeakerOn = true
            }

        }

        OngoingCall.state
            .subscribe(::updateUi)
            .addTo(disposables)

        OngoingCall.state
            .filter { it == Call.STATE_DISCONNECTED }
            .delay(1, TimeUnit.SECONDS)
            .firstElement()
            .subscribe { finish() }
            .addTo(disposables)
    }

    private fun updateUi(state: Int) {
        if (state == Call.STATE_ACTIVE) {
            binding.textView.isVisible = true
            timer.scheduleAtFixedRate(timerTask {
                time++
                binding.textView.post { binding.textView.text = buildString {
                    if (time >= 3600) append((time/3600).toString() + ":") // hours
                    append((time%3600)/600) // 10 minutes
                    append((time%600)/60) // minutes
                    append(":")
                    append((time%60)/10) // 10 seconds
                    append(time%10) // seconds
                } }
            }, 0, 1000)
        }

        binding.callInfo.text = buildString {
            append(state.asString())
            append("\n" + number)
        }

        if (state == Call.STATE_DISCONNECTED) timer.cancel()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }
}