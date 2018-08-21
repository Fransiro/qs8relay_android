package fsiles.name.qsrelay.feature.log

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

class AppLog {
    companion object {
        private const val QS_RELAY_STORE_SHARED_PREFERENCES_KEY = "QS_RELAY_STORE"
        private const val LOG_STORE_SHARED_PREFERENCES_KEY = "logStore"
        private const val DEFAULT_LOG_STORE_SHARED_PREFERENCES_VALUE = ""

        fun getLogStore(c: Context): LogStore? {
            val settings: SharedPreferences? = getSharedPreferences(c)
            val logStoreString = settings?.getString(
                    LOG_STORE_SHARED_PREFERENCES_KEY,
                    DEFAULT_LOG_STORE_SHARED_PREFERENCES_VALUE)
            var toReturn: LogStore? = null
            if (logStoreString != null && !logStoreString.isEmpty()) {
                val gson = Gson()
                try {
                    toReturn = gson.fromJson(logStoreString, LogStore::class.java)
                } catch (e: Exception) {
                    Log.e("LogStore", "Cannot unmarshal log store", e)
                }
            }
            return toReturn
        }

        private fun getSharedPreferences(c: Context): SharedPreferences? {
            return c.getSharedPreferences(
                    QS_RELAY_STORE_SHARED_PREFERENCES_KEY, 0)
        }

        fun appendToLog(c: Context, logMessage: String): Boolean{
            var logStore = getLogStore(c)
            var toReturn = false
            if(logStore == null){
                logStore = LogStore()
            }
            val logLine = LogLine()
            logLine.message = logMessage
            logStore.logLines.add(logLine)
            try {
                val gson = Gson()
                val toSave = gson.toJson(logStore)
                val settings: SharedPreferences? = getSharedPreferences(c)
                val editor = settings!!.edit()
                editor.putString(LOG_STORE_SHARED_PREFERENCES_KEY, toSave)
                toReturn = editor.commit()
            }catch (e: Exception){
                Log.e("LogStore","Cannot marshal or save log store", e)
            }
            return toReturn
        }

        fun clearLogs(context: Context):Boolean {
            val logStore = LogStore()
            var toReturn = false
            try {
                val gson = Gson()
                val toSave = gson.toJson(logStore)
                val settings: SharedPreferences? = getSharedPreferences(context)
                val editor = settings!!.edit()
                editor.putString(LOG_STORE_SHARED_PREFERENCES_KEY, toSave)
                toReturn = editor.commit()
            }catch (e: Exception){
                Log.e("LogStore","Cannot marshal or save log store", e)
            }
            return toReturn
        }
    }
}