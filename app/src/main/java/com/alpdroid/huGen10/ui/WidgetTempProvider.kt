package com.alpdroid.huGen10.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.widget.RemoteViews
import android.widget.Toast
import com.alpdroid.huGen10.R
import com.alpdroid.huGen10.WidgetTempService


class WidgetTempProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {


        for (appWidgetId in appWidgetIds) {
            val rv = RemoteViews(context.packageName, R.layout.widget_temp_screen)


            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view:
           // rv.setEmptyView(R.id.widget_list, R.id.empty_view)

            // Setup intent which points to the WidgetService which will provide the views for this collection.
            val intent = Intent(context, WidgetTempService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
           // widget_list is part of widget_layout
       //     Log.d("RemoteViews : Update Loop", "Remote Adapter set  Widget Layout Item :"+appWidgetId.toString())

            rv.setRemoteAdapter(R.id.widget_temp_list, intent)

            // Setup refresh scheduler :

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            val toastIntent = Intent(context, WidgetTempProvider::class.java)
            toastIntent.action = LIST_ITEM_CLICKED_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val toastPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT+PendingIntent.FLAG_MUTABLE
            )
            rv.setPendingIntentTemplate(R.id.widget_temp_list, toastPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, rv)



        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            LIST_ITEM_CLICKED_ACTION -> {
                val clickedFilePath = intent.getStringExtra(EXTRA_CLICKED_FILE).toString()
                val biggerText = SpannableStringBuilder("-> $clickedFilePath")
                biggerText.setSpan(RelativeSizeSpan(3.35f), 0, "-> $clickedFilePath".length, 0)
                val toast = Toast.makeText(
                    context,
                    biggerText, Toast.LENGTH_SHORT
                )


                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                toast.show()
            }

            ACTION_WIDGET_UPDATE -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        WidgetTempProvider::class.java
                    )
                )
                // On envoi en ressource widget_list part of widget_layout

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_temp_list)

            }
        }
    }

    companion object {
        const val EXTRA_CLICKED_FILE = "EXTRA_CLICKED_FILE"
        const val ACTION_WIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE"
        private const val LIST_ITEM_CLICKED_ACTION = "LIST_ITEM_CLICKED_ACTION"
        private const val REFRESH_WIDGET_ACTION = "REFRESH_WIDGET_ACTION"
    }
}