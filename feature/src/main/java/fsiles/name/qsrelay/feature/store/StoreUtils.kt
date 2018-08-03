package fsiles.name.qsrelay.feature.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

class StoreUtils {
    companion object {

        private const val QS_RELAY_STORE_SHARED_PREFERENCES_KEY = "QS_RELAY_STORE"
        private const val DEVICE_STORE_SHARED_PREFERENCES_KEY = "devicesStore"
        private const val DEFAULT_DEVICE_STORE_SHARED_PREFERENCES_VALUE = ""

        fun getDevicesStore(c: Context): DevicesStore? {
            val settings: SharedPreferences? = getSharedPreferences(c)
            val devicesStoreString = settings?.getString(
                    DEVICE_STORE_SHARED_PREFERENCES_KEY,
                    DEFAULT_DEVICE_STORE_SHARED_PREFERENCES_VALUE)
            var toReturn: DevicesStore? = null
            if(devicesStoreString!=null && !devicesStoreString.isEmpty()){
                val gson = Gson()
                try{
                    toReturn = gson.fromJson(devicesStoreString, DevicesStore::class.java)
                }catch (e: Exception){
                    Log.e("DeviceStore","Cannot unmarshal devices store", e)
                }
            }
            return toReturn
        }

        fun findDeviceData(c: Context, mAddress: String): DeviceData?{
            val devicesStore = getDevicesStore(c)
            var toReturn: DeviceData? = null
            if(devicesStore?.devices != null
                    && devicesStore.devices.containsKey(mAddress)){
                    toReturn = devicesStore.devices[mAddress]
            }
            return toReturn
        }

        fun saveDeviceData(c: Context, mAddress: String, deviceData: DeviceData): Boolean{
            var devicesStore = getDevicesStore(c)
            var toReturn = false
            if(devicesStore == null){
                devicesStore = DevicesStore()
                devicesStore.devices = HashMap()
            }
            devicesStore.devices[mAddress] = deviceData
            try {
                val gson = Gson()
                val toSave = gson.toJson(devicesStore)
                val settings: SharedPreferences? = getSharedPreferences(c)
                val editor = settings!!.edit()
                editor.putString(DEVICE_STORE_SHARED_PREFERENCES_KEY, toSave)
                toReturn = editor.commit()
            }catch (e: Exception){
                Log.e("DeviceStore","Cannot marshal or save devices store", e)
            }
            return toReturn
        }

        private fun getSharedPreferences(c: Context): SharedPreferences? {
            return c.getSharedPreferences(
                    QS_RELAY_STORE_SHARED_PREFERENCES_KEY, 0)
        }
    }
}