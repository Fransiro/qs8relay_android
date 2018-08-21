package fsiles.name.qsrelay.feature.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fsiles.name.qsrelay.feature.MainActivity
import fsiles.name.qsrelay.feature.log.AppLog

class StartAutoUpdateReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("QS-RELAY_LOG","StartAutoUpdateReceiver - Called with intent: "+intent.action)
        AppLog.appendToLog(context, "[StartAutoUpdateService] onReceive: "+intent.action)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
                    intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
                intent.action == Intent.ACTION_REBOOT ||
                intent.action == Intent.ACTION_PACKAGE_FIRST_LAUNCH ||
                intent.action == Intent.ACTION_DREAMING_STOPPED ||
                intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
                intent.action == Intent.ACTION_MY_PACKAGE_UNSUSPENDED ||
                intent.action == Intent.ACTION_SCREEN_ON ||
                intent.action == Intent.ACTION_USER_UNLOCKED) {
            if(JobUtils.checkOldAndroidVersions()) {
                val intentMain = Intent(context, MainActivity::class.java)
                intentMain.putExtra(MainActivity.EXTRA_BOOT_KEY, true)
                context.startActivity(intentMain)
            }else{
                if(!JobUtils.isJobStarted(context)) {
                    AppLog.appendToLog(context, "[StartAutoUpdateService] On boot called")
                    JobUtils.scheduleJob(context, true)
                }
            }
        } else {
            if(JobUtils.checkOldAndroidVersions()) {
                val serviceIntent = Intent(context, AutoUpdateService::class.java)
                serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startService(serviceIntent)
            }else{
                if(!JobUtils.isJobStarted(context)) {
                    AppLog.appendToLog(context, "[StartAutoUpdateService] On boot called else")
                    JobUtils.scheduleJob(context, true)
                }
            }
        }
    }
}