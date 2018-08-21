package fsiles.name.qsrelay.feature

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SimpleAdapter
import fsiles.name.qsrelay.feature.log.AppLog
import fsiles.name.qsrelay.feature.service.JobUtils
import kotlinx.android.synthetic.main.app_logs.*
import java.text.SimpleDateFormat
import java.util.*

class LogActivity : AppCompatActivity() {
    companion object {
        private const val ADAPTER_KEY_1 = "text1"
        private const val ADAPTER_KEY_2 = "text2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_logs)
        refreshServiceState()
        refresh_logs.setOnClickListener {
            reloadLog()
        }
        clear_logs.setOnClickListener {
            clearLogs()
        }
        start_service.setOnClickListener {
            startService()
        }
        stop_service.setOnClickListener {
            stopService()
        }
    }

    private fun clearLogs() {
        AppLog.clearLogs(applicationContext)
        reloadLog()
    }

    private fun startService(){
        JobUtils.scheduleJob(applicationContext, true)
        refreshServiceState()
    }

    private fun stopService(){
        JobUtils.cancelJob(applicationContext)
        refreshServiceState()
    }

    private fun refreshServiceState(){
        service_is_running.isChecked = JobUtils.isJobStarted(applicationContext)
    }

    private fun reloadLog(){
        refreshServiceState()
        val logStore = AppLog.getLogStore(applicationContext)
        val listToShow : ArrayList<Map<String,String>> = ArrayList()
        if(logStore != null){
            val logLines = logStore.logLines
            for(logLine in logLines){
                val valuesMap: HashMap<String, String> = HashMap()
                valuesMap[ADAPTER_KEY_1] = logLine.message
                valuesMap[ADAPTER_KEY_2] = formatDateToString(logLine.date)
                listToShow.add(valuesMap)
            }
            val fromMapKey = arrayOf(ADAPTER_KEY_1, ADAPTER_KEY_2)
            val toLayoutId = IntArray(2)
            toLayoutId[0] = android.R.id.text1
            toLayoutId[1] = android.R.id.text2
            val adapter = SimpleAdapter(this, listToShow, android.R.layout.simple_list_item_2,
                    fromMapKey, toLayoutId)
            log_lines.adapter = adapter
        }
    }

    private fun formatDateToString(date: Date): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS")
        return formatter.format(date)
    }
}
