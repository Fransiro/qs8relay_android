package fsiles.name.qsrelay.feature

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import fsiles.name.qsrelay.feature.service.JobUtils
import android.content.Intent
import android.util.Log
import fsiles.name.qsrelay.feature.store.StoreUtils


/**
 * Implementation of App Widget functionality.
 */
class ServiceWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if(context!=null && intent!=null) {
            if (TURN_ON_CLICK_ID == intent.action) {
                JobUtils.scheduleJob(context, true)
            } else if (TURN_OFF_CLICK_ID == intent.action) {
                JobUtils.cancelJob(context)
            }
            if(TURN_ON_CLICK_ID == intent.action || TURN_OFF_CLICK_ID == intent.action) {
                var isStarted = JobUtils.isJobStarted(context)
                saveLastStateDefinedByUser(context, isStarted)
                callAutoUpdate(context, intent)
            }
        }
    }

    companion object {

        private const val TURN_ON_CLICK_ID = "name.fsiles.qsrelay.action.WIDGET_TURN_ON_BUTTON"
        private const val TURN_OFF_CLICK_ID = "name.fsiles.qsrelay.action.WIDGET_TURN_OFF_BUTTON"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.service_widget)
            val lastStateIsOn = getLastStateDefinedByUserIsOn(context)
            if(!JobUtils.isJobStarted(context) && lastStateIsOn){
                JobUtils.scheduleJob(context, true)
            }
            updateServiceState(views, JobUtils.isJobStarted(context))
            bindButtonsEvents(context, views)
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        internal fun callAutoUpdate(context: Context, intent: Intent){
            val intent = Intent(context, ServiceWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context.applicationContext,
                            ServiceWidget::class.java!!))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }

        private fun bindButtonsEvents(context: Context, views: RemoteViews) {
            views.setOnClickPendingIntent(R.id.turn_on,
                    getPendingSelfIntent(context, TURN_ON_CLICK_ID))
            views.setOnClickPendingIntent(R.id.turn_off,
                    getPendingSelfIntent(context, TURN_OFF_CLICK_ID))
        }


        private fun getPendingSelfIntent(context: Context, action: String): PendingIntent? {
            val intent = Intent(context, ServiceWidget::class.java)
            intent.action = action
            return PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        private fun getLastStateDefinedByUserIsOn(context: Context): Boolean {
            return StoreUtils.getLastWidgetState(context)
        }

        private fun saveLastStateDefinedByUser(context: Context, started: Boolean){
            StoreUtils.saveLastWidgetState(context, started)
        }

        private fun updateServiceState( views: RemoteViews, isStarted: Boolean){
            views.setViewVisibility(R.id.turn_on, getVisibilityFromBoolean(!isStarted))
            views.setViewVisibility(R.id.turn_off, getVisibilityFromBoolean(isStarted))
            views.setViewVisibility(R.id.text_started_service, getVisibilityFromBoolean(isStarted))
            views.setViewVisibility(R.id.text_stopped_service, getVisibilityFromBoolean(!isStarted))
        }

        private fun getVisibilityFromBoolean(visible: Boolean): Int {
            return if(visible){
                View.VISIBLE
            }else{
                View.GONE
            }
        }
    }
}

