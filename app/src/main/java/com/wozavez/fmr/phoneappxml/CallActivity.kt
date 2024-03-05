package com.wozavez.fmr.phoneappxml

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.wozavez.fmr.phoneappxml.databinding.ActivityCallBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.Locale
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.time.Duration.Companion.seconds

class CallActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()
    private lateinit var number: String
    private lateinit var binding: ActivityCallBinding
    private val timer = Timer()
    private var time = 0

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

        binding.answer.setOnClickListener {
            OngoingCall.answer()
        }

        binding.hangup.setOnClickListener {
            OngoingCall.hangup()
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

        timer.scheduleAtFixedRate(timerTask {
            time++
            //updateTime()
            binding.textView.post { binding.textView.text = time.toString() }
        }, 0, 1000)
    }

    private fun updateUi(state: Int) {
        binding.callInfo.text = "${state.asString().lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}\n$number\n${OngoingCall.call?.details?.state?.seconds}"

        binding.answer.isVisible = state == Call.STATE_RINGING
        binding.hangup.isVisible = state in listOf(
            Call.STATE_DIALING,
            Call.STATE_RINGING,
            Call.STATE_ACTIVE
        )
        if (state == Call.STATE_ACTIVE) binding.textView.isVisible = true
        if (state == Call.STATE_DISCONNECTED) timer.cancel()
    }

//    private fun updateTime() {
//        binding.textView.text = time.toString()
//    }

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