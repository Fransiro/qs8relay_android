package fsiles.name.qsrelay.feature

import android.app.Activity
import android.app.VoiceInteractor
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

class VoiceActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("QS-RELAY_LOG", "[VoiceActivity] se crea")
        onReceive(applicationContext, intent)
    }

    override fun onResume() {
        super.onResume()
        if (isVoiceInteraction)
        {
            val status = Bundle()
            val request: VoiceInteractor.Request = voiceInteractor.activeRequests[0]
        }
        Log.i("QS-RELAY_LOG", "[VoiceActivity] se resume")
        onReceive(applicationContext, intent)
        finish()
    }

    private fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH){
            Log.i("QS-RELAY_LOG", "[VoiceActivity] Accion search")
        } else if(intent.action == Intent.ACTION_VOICE_COMMAND){
            Log.i("QS-RELAY_LOG", "[VoiceActivity] Accion voice command")
        } else if (intent.action == "com.google.android.gms.actions.SEARCH_ACTION"){
            Log.i("QS-RELAY_LOG", "[VoiceActivity] Accion search action")
        } else {
            Log.i("QS-RELAY_LOG", "[VoiceActivity] Accion no reconocida: "+ intent.action)
        }
    }
}