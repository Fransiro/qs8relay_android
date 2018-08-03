package fsiles.name.qsrelay.feature

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import fsiles.name.qsrelay.feature.store.DeviceData
import fsiles.name.qsrelay.feature.store.RelayData
import fsiles.name.qsrelay.feature.store.StoreUtils
import kotlinx.android.synthetic.main.config_device.*

class ConfigDeviceActivity: AppCompatActivity() {

    private var inputTextArray = arrayListOf<EditText>()
    private lateinit var mAddress: String
    private lateinit var deviceDataToSave: DeviceData
    companion object {
        private const val CANNOT_SAVE_TO_STORE_MESSAGE =
                "Can not save to store. Android problem?"
        private const val CANNOT_SAVE_TO_STORE_DURATION = Toast.LENGTH_LONG
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.config_device)
        save_config.setOnClickListener {saveDeviceConfig()}
        mAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS)
        initComponentsViewArray()
        obtainDataFromStoreAndUpdate()
    }

    private fun obtainDataFromStoreAndUpdate() {
        val deviceData = StoreUtils.findDeviceData(applicationContext, mAddress)
        if(deviceData != null){
            if(deviceData.name!=null && !deviceData.name!!.isEmpty()){
                config_deviceName.text = Editable.Factory.getInstance().newEditable(deviceData.name)
            }
            for(relay in deviceData.relays){
                if(relay.index >= 0 && relay.index < inputTextArray.size){
                    if(!relay.name.isEmpty()){
                        inputTextArray[relay.index].text =
                                Editable.Factory.getInstance().newEditable(relay.name)
                    }
                }
            }
            deviceDataToSave = deviceData
        }else{
            deviceDataToSave = DeviceData()
            deviceDataToSave.relays = ArrayList()
            for(i in 0 until inputTextArray.size){
                val relayData = RelayData()
                relayData.state = false
                relayData.index = i
                relayData.name = ""
                deviceDataToSave.relays.add(relayData)
            }
        }
    }

    private fun initComponentsViewArray() {
        inputTextArray.add(config_relay1 as EditText)
        inputTextArray.add(config_relay2 as EditText)
        inputTextArray.add(config_relay3 as EditText)
        inputTextArray.add(config_relay4 as EditText)
        inputTextArray.add(config_relay5 as EditText)
        inputTextArray.add(config_relay6 as EditText)
        inputTextArray.add(config_relay7 as EditText)
        inputTextArray.add(config_relay8 as EditText)
    }

    private fun saveDeviceConfig() {
        if(config_deviceName.text != null && !config_deviceName.text.isEmpty()){
            deviceDataToSave.name = config_deviceName.text.toString()
        }
        for(i in 0 until inputTextArray.size){
            if(inputTextArray[i].text!=null && !inputTextArray[i].text.isEmpty()) {
                deviceDataToSave.relays[i].name = inputTextArray[i].text.toString()
            }
        }
        val result =
                StoreUtils.saveDeviceData(applicationContext, mAddress, deviceDataToSave)
        if(!result){
            Toast.makeText(applicationContext, CANNOT_SAVE_TO_STORE_MESSAGE,
                    CANNOT_SAVE_TO_STORE_DURATION).show()
        }
        finish()
    }
}