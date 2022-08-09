package com.alpdroid.huGen10

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe

class AlpdroidApplication : Application() {

    lateinit var alpineCanFrame : CanframeBuffer

    lateinit var alpdroidData : VehicleServices

    lateinit var alpdroidServices: CanFrameServices


    var isBound = false
    var isStarted = false


    private var sharedPreferences: SharedPreferences? = null
    private var intent: Intent? = null


    private val alpineConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder
        ) {

            val binder = service as CanFrameServices.MyLocalBinder
            alpdroidServices = binder.getService()
            isBound = true
            isStarted = true

        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            isStarted = true
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        eventBus.register(this)

    }


    fun startListenerService() {
        if (ListenerService.isNotificationAccessEnabled(this)) {
            startService(Intent(this, ListenerService::class.java))
            Log.d("Application", "Listener started")
        }
        else
            Log.d("Application", "Listener not started")

    }

    fun startVehicleServices() {
        Log.d("Application", "Trying to start CanFrameListener")
       if (startService(Intent(this, CanFrameServices::class.java))!=null)
       {

           bindService(
               Intent(
                   this,
                   CanFrameServices::class.java
               ), alpineConnection, BIND_AUTO_CREATE
           )
            isStarted = true
            isBound = true
            Log.d("Application", "CanFrameListener started")
        }
        else
           Log.d("Application", "CanFrameListener not started")
    }


    fun stopListenerService() {
        stopService(Intent(this, ListenerService::class.java))
    }

    fun stopVehicleServices() {
        stopService(Intent(this, CanFrameServices::class.java))
        isStarted=false
        Log.d("Application", "VehicleListener stopped")
        // Detach the service connection.
            isBound=false
            unbindService(alpineConnection)

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    fun close() {
        val preferences = getSharedPreferences()
        val editor = preferences!!.edit()
        editor.apply()
        stopListenerService()
        if (isBound)
            stopVehicleServices()
    }



    fun getSharedPreferences(): SharedPreferences? {
        return sharedPreferences
    }

    @Subscribe
    fun onNowPlayingChange(event: NowPlayingChangeEvent) {

        lastEvent = event

        if (lastEvent.track().album().isPresent) {
            alpdroidData.setalbumName(lastEvent.track().album().get().toString())

        }
        else
            alpdroidData.setalbumName("--")

        if (lastEvent.track().artist().isNotEmpty())
            alpdroidData.setartistName(lastEvent.track().artist().toString())
        else
            alpdroidData.setartistName("--")

            Log.d(" Artist", lastEvent.track().artist().toString())
        if (lastEvent.track().track().isNotEmpty())
            alpdroidData.settrackName(lastEvent.track().track().toString())
        else
            alpdroidData.settrackName("--")
            Log.d("Track", lastEvent.track().track().toString())

    }


    fun getEventBus(): EventBus? {
        return eventBus
    }

    companion object {

        // Media streaming Event
        val eventBus: EventBus = EventBus()
        var lastEvent = NowPlayingChangeEvent.builder().source("").track(Track.empty()).build()

        fun getLastNowPlayingChangeEvent(): NowPlayingChangeEvent? {
            return lastEvent
        }


    }

}