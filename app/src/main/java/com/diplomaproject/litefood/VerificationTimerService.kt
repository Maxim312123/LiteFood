package com.diplomaproject.litefood

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import androidx.databinding.ObservableField

class VerificationTimerService : Service() {

    private lateinit var cutDownTimer: CountDownTimer
    private var time = 0


    companion object {
        var isServiceRunning = false
        var leftTimeInSeconds = ObservableField(0L)
    }


    //Этот метод вызывается методом bindService()
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Этот метод вызываетя методом startService()
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTimer()
        return START_NOT_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
    }

    private fun startTimer() {
        cutDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                leftTimeInSeconds.set(millisUntilFinished / 1000)
                time = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                leftTimeInSeconds.set(0)
                time = 0
                isServiceRunning = false
                stopSelf()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        cutDownTimer?.cancel()
    }
}