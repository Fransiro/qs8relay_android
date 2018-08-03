package fsiles.name.qsrelay.feature

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.SimpleAdapter
import fsiles.name.qsrelay.feature.store.DeviceData
import fsiles.name.qsrelay.feature.store.StoreUtils
import fsiles.name.qsrelay.feature.utils.Constants


class MainActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mPairedDevices: Set<BluetoothDevice>

    //Companion object
    companion object {
        const val EXTRA_ADDRESS: String = "Device_address"
        const val EXTRA_BOOT_KEY: String = "is_booting"
        // Constants
        private const val REQUEST_ENABLE_BLUETOOTH = 1
        private const val REQUEST_PERMISSIONS_BLUETOOTH = 2
        private const val REQUEST_PERMISSIONS_BLUETOOTH_ADMIN = 3
        private const val ADAPTER_KEY_1 = "text1"
        private const val ADAPTER_KEY_2 = "text2"

        // Message constants
        private const val NOT_SUPP_BLUETOOTH_MESSAGE = "This device does not support bluetooth"
        private const val NOT_SUPP_BLUETOOTH_DURATION = Toast.LENGTH_LONG
        private const val ENABLE_BLUETOOTH_MESSAGE = "Bluetooth has been enabled"
        private const val ENABLE_BLUETOOTH_DURATION = Toast.LENGTH_SHORT
        private const val DISABLE_BLUETOOTH_MESSAGE = "Bluetooth has been disabled"
        private const val DISABLE_BLUETOOTH_DURATION = Toast.LENGTH_LONG
        private const val CANCELED_BLUETOOTH_MESSAGE = "Bluetooth enabling has been canceled"
        private const val CANCELED_BLUETOOTH_DURATION = Toast.LENGTH_LONG
        private const val NO_PAIRED_DEVICES_MESSAGE = "No paired bluetooth devices found"
        private const val NO_PAIRED_DEVICES_DURATION = Toast.LENGTH_LONG
        private const val BAD_REQUEST_PERMISSION_BLUETOOTH_MESSAGE =
                "Bad response for check bluetooth permission"
        private const val BAD_REQUEST_PERMISSION_BLUETOOTH_DURATION = Toast.LENGTH_LONG
        private const val BAD_REQUEST_PERMISSION_BLUETOOTH_ADMIN_MESSAGE =
                "Bad response for check bluetooth admin permission"
        private const val BAD_REQUEST_PERMISSION_BLUETOOTH_ADMIN_DURATION = Toast.LENGTH_LONG
        private const val NOT_GRANTED_PERMISSION_BLUETOOTH_ADMIN_MESSAGE =
                "If you not granted bluetooth admin permissions you cannot continue"
        private const val NOT_GRANTED_PERMISSION_BLUETOOTH_ADMIN_DURATION = Toast.LENGTH_LONG
        private const val NOT_GRANTED_PERMISSION_BLUETOOTH_MESSAGE =
                "If you not granted bluetooth permissions you cannot continue"
        private const val NOT_GRANTED_PERMISSION_BLUETOOTH_DURATION = Toast.LENGTH_LONG
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkBluetoothPermissions()
        checkBluetoothAdminPermissions()
        val tempAdapter = BluetoothAdapter.getDefaultAdapter()
        if(tempAdapter == null){
            val toast = Toast.makeText(applicationContext, NOT_SUPP_BLUETOOTH_MESSAGE,
                    NOT_SUPP_BLUETOOTH_DURATION)
            toast.show()
            return
        }else{
            this.mBluetoothAdapter = tempAdapter
        }
        if(!this.mBluetoothAdapter.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        refresh_devices.setOnClickListener{
            pairedDeviceList()
        }
        pairedDeviceList()
        startBackgroundService()
        if(intent.getBooleanExtra(EXTRA_BOOT_KEY, false)) {
            //Log.i("QSRELAY_LOG","MainActivity - Call moveTaskToBack(true)")
            moveTaskToBack(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        startBackgroundService()
        //Log.i("QSRELAY_LOG","MainActivity - Called on destroy")
    }

    private fun startBackgroundService() {
        val intent = Intent(Constants.QS_RELAY_SERVICE_ACTION_START)
        sendBroadcast(intent)
    }

    private fun checkBluetoothAdminPermissions() {
        val permissionBluetoothCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN)
        when (permissionBluetoothCheck) {
            PackageManager.PERMISSION_GRANTED -> {
                //Do nothing all is perfect
                Log.i("HasPermissions","This app has bluetooth admin permissions")
            }
            PackageManager.PERMISSION_DENIED -> {
                // Ask for permissions
                Log.i("HasNotPermissions","This app has not bluetooth admin permissions. " +
                        "Asking for grant permission.")
                ActivityCompat.requestPermissions(this,
                        Array(1) {Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_PERMISSIONS_BLUETOOTH_ADMIN)
            }
            else -> Toast.makeText(applicationContext, BAD_REQUEST_PERMISSION_BLUETOOTH_ADMIN_MESSAGE,
                    BAD_REQUEST_PERMISSION_BLUETOOTH_ADMIN_DURATION).show()
        }
    }

    private fun checkBluetoothPermissions() {
        val permissionBluetoothCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH)
        when (permissionBluetoothCheck) {
            PackageManager.PERMISSION_GRANTED -> {
                //Do nothing all is perfect
                Log.i("HasPermissions","This app has bluetooth permissions")
            }
            PackageManager.PERMISSION_DENIED -> {
                // Ask for permissions
                Log.i("HasNotPermissions","This app has not bluetooth permissions." +
                        "Asking for grant permission.")
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.BLUETOOTH),
                        REQUEST_PERMISSIONS_BLUETOOTH)
            }
            else -> Toast.makeText(applicationContext, BAD_REQUEST_PERMISSION_BLUETOOTH_MESSAGE,
                    BAD_REQUEST_PERMISSION_BLUETOOTH_DURATION).show()
        }
    }

    private fun pairedDeviceList(){
        mPairedDevices = mBluetoothAdapter.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()
        val listToShow : ArrayList<Map<String,String>> = ArrayList()
        val fromMapKey = arrayOf(ADAPTER_KEY_1, ADAPTER_KEY_2)
        val toLayoutId = IntArray(2)
        toLayoutId[0] = android.R.id.text1
        toLayoutId[1] = android.R.id.text2
        if(!mPairedDevices.isEmpty()){
            for(device: BluetoothDevice in mPairedDevices){
                list.add(device)
                val valuesMap: HashMap<String, String> = HashMap()
                valuesMap[ADAPTER_KEY_1] = getNameOfAddress(device.address, device.name)
                valuesMap[ADAPTER_KEY_2] = device.address
                listToShow.add(valuesMap)
                Log.i("DeviceAdded", "Device: $device")
            }
        }else{
            val toast = Toast.makeText(applicationContext, NO_PAIRED_DEVICES_MESSAGE,
                    NO_PAIRED_DEVICES_DURATION)
            toast.show()
        }

        val adapter = SimpleAdapter(this, listToShow, android.R.layout.simple_list_item_2,
                fromMapKey, toLayoutId)
        bt_devices.adapter = adapter
        bt_devices.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            startActivity(intent)
        }
    }

    private fun getNameOfAddress(address: String, default: String): String {
        var toReturn = default
        val deviceData: DeviceData? = StoreUtils.findDeviceData(applicationContext, address)
        if(deviceData?.name != null && !deviceData.name!!.isEmpty()){
            toReturn = deviceData.name!!
        }
        return toReturn
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS_BLUETOOTH -> {
                if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pairedDeviceList()
                    Log.i("Granted", "Permission Bluetooth is granted")
                } else {
                    Toast.makeText(applicationContext, NOT_GRANTED_PERMISSION_BLUETOOTH_ADMIN_MESSAGE,
                            NOT_GRANTED_PERMISSION_BLUETOOTH_ADMIN_DURATION).show()
                }
            }
            REQUEST_PERMISSIONS_BLUETOOTH_ADMIN -> {
                if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Granted", "Permission Bluetooth Admin is granted")
                } else {
                    Toast.makeText(applicationContext, NOT_GRANTED_PERMISSION_BLUETOOTH_MESSAGE,
                            NOT_GRANTED_PERMISSION_BLUETOOTH_DURATION).show()
                }
            }
        }
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
         super.onActivityResult(requestCode, resultCode, data)
         if(requestCode == REQUEST_ENABLE_BLUETOOTH){
             if(resultCode == Activity.RESULT_OK){
                 if(this.mBluetoothAdapter.isEnabled){
                     Toast.makeText(applicationContext, ENABLE_BLUETOOTH_MESSAGE,
                             ENABLE_BLUETOOTH_DURATION).show()
                 } else {
                     Toast.makeText(applicationContext, DISABLE_BLUETOOTH_MESSAGE,
                             DISABLE_BLUETOOTH_DURATION).show()
                 }
             }
         } else if (resultCode == Activity.RESULT_CANCELED){
             Toast.makeText(applicationContext, CANCELED_BLUETOOTH_MESSAGE,
                     CANCELED_BLUETOOTH_DURATION).show()
         }
     }


}
