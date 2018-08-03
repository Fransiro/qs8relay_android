package fsiles.name.qsrelay.feature

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.Toast
import fsiles.name.qsrelay.feature.store.DeviceData
import fsiles.name.qsrelay.feature.store.RelayData
import fsiles.name.qsrelay.feature.store.StoreUtils
import fsiles.name.qsrelay.feature.utils.RelaysUtils
import kotlinx.android.synthetic.main.control_layout.*


class ControlActivity: AppCompatActivity() {

    private var relayArray = arrayListOf<Switch>()
    private var relayState = arrayListOf<Boolean>()

    companion object {
        private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()!!
        private lateinit var mAddress: String
        private const val CANNOT_SEND_COMMAND_TO_DEVICE_MESSAGE =
                "Can not send all data to selected device. Connection problem"
        private const val CANNOT_SEND_COMMAND_TO_DEVICE_DURATION = Toast.LENGTH_LONG
        private const val CANNOT_CONNECT_TO_DEVICE_MESSAGE =
                "Can not connect to selected device. Is On?"
        private const val CANNOT_CONNECT_TO_DEVICE_DURATION = Toast.LENGTH_LONG
        private const val CANNOT_SAVE_TO_STORE_MESSAGE =
                "Can not save to store. Android problem?"
        private const val CANNOT_SAVE_TO_STORE_DURATION = Toast.LENGTH_LONG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        mAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS)
        initComponentsViewArray()
        save_state.setOnClickListener {runSaveAllConfig()}
        Log.i("StartedControlActivity", "ControlActivity is started")
    }

    override fun onResume() {
        super.onResume()
        obtainRelaysDataFromStoreAndUpdate()
    }

    override fun onRestart() {
        super.onRestart()
        obtainRelaysDataFromStoreAndUpdate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val toReturn = super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.control_activity_menu, menu)
        return toReturn
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.configMenu -> {
            val intent = Intent(this, ConfigDeviceActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_ADDRESS, mAddress)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initComponentsViewArray() {
        relayArray.add(switch_relay_1 as Switch)
        relayArray.add(switch_relay_2 as Switch)
        relayArray.add(switch_relay_3 as Switch)
        relayArray.add(switch_relay_4 as Switch)
        relayArray.add(switch_relay_5 as Switch)
        relayArray.add(switch_relay_6 as Switch)
        relayArray.add(switch_relay_7 as Switch)
        relayArray.add(switch_relay_8 as Switch)
        obtainRelaysDataFromStoreAndUpdate()
        refreshRelayState()
    }

    private fun obtainRelaysDataFromStoreAndUpdate() {
        val deviceData = StoreUtils.findDeviceData(applicationContext, mAddress)
        if(deviceData != null){
            for(relay in deviceData.relays){
               if(relay.index >= 0 && relay.index < relayArray.size){
                   relayArray[relay.index].isChecked = relay.state
                   if(!relay.name.isEmpty()){
                       relayArray[relay.index].text = relay.name
                   }
               }
            }
        }
    }

    private fun saveActualStateToStore(){
        var deviceData = StoreUtils.findDeviceData(applicationContext, mAddress)
        if(deviceData == null){
            deviceData = DeviceData()
        }
        deviceData.relays = ArrayList()
        for(i in 0 until relayArray.size){
            val relayData = RelayData()
            relayData.index = i
            relayData.name = relayArray[i].text as String
            relayData.state = relayArray[i].isChecked
            deviceData.relays.add(relayData)
        }
        val result = StoreUtils.saveDeviceData(applicationContext, mAddress, deviceData)
        if(!result){
            showToastInMainLoop(CANNOT_SAVE_TO_STORE_MESSAGE, CANNOT_SAVE_TO_STORE_DURATION)
        }
    }

    private fun refreshRelayState(){
        if(relayState.isEmpty() || relayState.size != relayArray.size){
            relayState.clear()
            for(i in 0 until relayArray.size) {
                relayState.add(relayArray[i].isChecked)
            }
        }else{
            for(i in 0 until relayArray.size) {
                relayState[i] = relayArray[i].isChecked
            }
        }
    }

    private fun saveAllConfig(): java.util.ArrayList<Boolean> {
        val toSend = ArrayList<ByteArray>()
        for(i in 0 until relayArray.size) {
            val relay = relayArray[i]
            toSend.add(RelaysUtils.getCommand(i, relay.isChecked))
        }
        return RelaysUtils.sendCommands(mBluetoothAdapter,
                mAddress, toSend)
    }

    private fun showToastInMainLoop(message: String, duration: Int){
        Handler(applicationContext.mainLooper).post {
            Toast.makeText(applicationContext, message, duration).show()
        }
    }

    private fun runSaveAllConfig(){
        val callback:AsyncTaskCallback = object: AsyncTaskCallback {
            override fun doCallback(): ArrayList<Boolean> {
                return saveAllConfig()
            }
            override fun doPostPostExecute(statusArray: ArrayList<Boolean>) {
                processBluetoothResponse(statusArray)
                saveActualStateToStore()
            }
            override fun startLoading() {
                startLoadingWidget()
            }
            override fun stopLoading() {
                stopLoadingWidget()
            }
        }
        SaveConfigTask().init(callback).execute()
    }

    private fun startLoadingWidget(){
        loadingLayout!!.visibility = View.VISIBLE
        save_state!!.isEnabled = false
        for(relay in relayArray){
            relay.isEnabled = false
        }
    }

    private fun stopLoadingWidget(){
        loadingLayout!!.visibility = View.GONE
        save_state!!.isEnabled = true
        for(relay in relayArray){
            relay.isEnabled = true
        }
    }

    private fun processBluetoothResponse(statusArray: ArrayList<Boolean>){
        var failedCommands = 0
        for(i in 0 until statusArray.size) {
            if(!statusArray[i]) {
                val relay: Switch = relayArray[i]
                relay.isChecked = relayState[i]
                failedCommands++
            }
        }
        if (failedCommands > 0 && failedCommands < relayArray.size){
            showToastInMainLoop(CANNOT_SEND_COMMAND_TO_DEVICE_MESSAGE,
                    CANNOT_SEND_COMMAND_TO_DEVICE_DURATION)
        }else if(failedCommands > 0){
            showToastInMainLoop(CANNOT_CONNECT_TO_DEVICE_MESSAGE,
                     CANNOT_CONNECT_TO_DEVICE_DURATION)
        }
        refreshRelayState()
    }

    interface AsyncTaskCallback{
        fun doCallback(): ArrayList<Boolean>
        fun doPostPostExecute(statusArray: ArrayList<Boolean>)
        fun startLoading()
        fun stopLoading()
    }

    class SaveConfigTask: AsyncTask<Void,Void,ArrayList<Boolean>>(){

        private var callback: AsyncTaskCallback? = null

        fun init(callback: AsyncTaskCallback): SaveConfigTask{
            this.callback = callback
            return this
        }

        override fun onPreExecute() {
            super.onPreExecute()
            callback!!.startLoading()
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<Boolean> {
            return callback!!.doCallback()
        }

        override fun onPostExecute(statusArray: ArrayList<Boolean>) {
            super.onPostExecute(statusArray)
            callback!!.doPostPostExecute(statusArray)
            callback!!.stopLoading()
        }
    }
}