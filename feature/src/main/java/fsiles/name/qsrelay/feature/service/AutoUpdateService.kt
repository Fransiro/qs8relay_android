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
                processService()
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

    private fun processService(){
        try{
            val devicesStores = StoreUtils.getDevicesStore(applicationContext)
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if(adapter != null && adapter.isEnabled){
                adapter.cancelDiscovery()
                if(devicesStores!=null && !devicesStores.devices.isEmpty()){
                    for(address in devicesStores.devices.keys){
                        val deviceData = devicesStores.devices[address]
                        if(deviceData!=null){
                            processDevice(adapter, address, deviceData)
                        }
                    }
                }
            }
        }catch (e: Exception){
            Log.e("AutoUpdateService","A error occurred when processService.", e)
        }
    }

    private fun processDevice(bAdapter: BluetoothAdapter, mAddress:String, deviceData: DeviceData){
        try{
            if(!deviceData.relays.isEmpty()){
                val commands = ArrayList<ByteArray>()
                val commandIndex = ArrayList<Int>()
                for(relay in deviceData.relays){
                    if(checkRelay(relay)) {
                        commands.add(RelaysUtils.getCommand(relay.index, relay.state))
                        commandIndex.add(relay.index)
                    }
                }
                val maxTries = deviceData.maxTries
                val msTimeBetweenEachTry = deviceData.msTimeBetweenEachTry
                val results = RelaysUtils.sendCommands(bAdapter, mAddress,
                        maxTries, msTimeBetweenEachTry, commands)
                for(i in 0 until results.size){
                    if(!results[i]){
                        Log.e("AutoUpdateService",
                                "Failed to send a command with index:"+commandIndex[i]
                                + " and command "
                                        + getHexadecimalString(commands[i]))
                    }
                }
            }
        }catch (e: Exception){
            Log.e("AutoUpdateService",
                    "A error occurred when processDevice: $mAddress.",
                    e)
        }
    }

    private fun checkRelay(relay: RelayData): Boolean {
        var toReturn = true
        if(relay.index<0){
            toReturn = false
        }
        return toReturn
    }

    private fun getHexadecimalString(command: ByteArray): String{
        return BigInteger(1, command).toString(16).toUpperCase()
    }
}