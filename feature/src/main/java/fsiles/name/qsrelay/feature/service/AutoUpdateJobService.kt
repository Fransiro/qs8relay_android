package fsiles.name.qsrelay.feature.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import fsiles.name.qsrelay.feature.log.AppLog

class AutoUpdateJobService: JobService() {
    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.i("QS-RELAY_LOG", "[AutoUpdateJobService] onStartJob")
        JobUtils.processService(applicationContext)
        JobUtils.scheduleJob(applicationContext,false)
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Log.i("QS-RELAY_LOG", "[AutoUpdateJobService] onStopJob")
        return true
    }
}