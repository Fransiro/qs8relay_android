package fsiles.name.qsrelay.feature.service

import android.app.IntentService
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import fsiles.name.qsrelay.feature.store.DeviceData
import fsiles.name.qsrelay.feature.store.RelayData
import fsiles.name.qsrelay.feature.store.StoreUtils
import fsiles.name.qsrelay.feature.utils.RelaysUtils
import java.math.BigInteger
import java.util.*

class AutoUpdateService: IntentService("AutoUpdateService") {

    companion object {
        private var timer: Timer? = null
        private const val INITIAL_TASK_DELAY_MS: Long = 1000
        private const val NORMAL_TASK_DELAY_MS: Long = 30000
    }

    override fun onHandleIntent(p0: Intent?) {
        val startDate = Date()
        try{
            createTimer()
            timer!!.schedule(getTimerTask(), INITIAL_TASK_DELAY_MS)
        }catch (e: Exception){
            val runningTime = (Date().time - startDate.time)/1000.0
                    Log.e("QSRELAY_LOG","AutoUpdateService - A error occurred when auto-update"
                    + " after $runningTime seconds running", e)
        }
    }

    private fun getTimerTask(): TimerTask? {
        return object: TimerTask() {
            override fun run() {
                //Log.i("QSRELAY_LOG", "AutoUpdateService is running $counter secons")
                JobUtils.processService(applicationContext)
                if(AutoUpdateService.timer != null){
                    AutoUpdateService.timer!!.schedule(getTimerTask(), NORMAL_TASK_DELAY_MS)
                }
            }
        }
    }

    private fun createTimer() {
        if(timer!=null){
            timer!!.cancel()
            timer = null
        }
        timer = Timer()
    }
}