package fsiles.name.qsrelay.feature.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fsiles.name.qsrelay.feature.MainActivity

class StartAutoUpdateReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
       // Log.i("QSRELAY_LOG","StartAutoUpdateReceiver - Called with intent: "+intent.action)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON"){
            val intentMain = Intent(context, MainActivity::class.java)
            intentMain.putExtra(MainActivity.EXTRA_BOOT_KEY,true)
            context.startActivity(intentMain)
        }else{
            val serviceIntent = Intent(context, AutoUpdateService::class.java)
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startService(serviceIntent)
        }
    }
}