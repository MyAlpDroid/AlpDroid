package com.alpdroid.huGen10

import `in`.rmkrishna.mlog.MLog
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe


class AlpdroidApplication : Application() {

    private val TAG = AlpdroidApplication::class.java.name
    
    lateinit var alpdroidData : VehicleServices

    var alpineCanFrame : CanframeBuffer = CanframeBuffer()

    var alpdroidServices : CanFrameServices = CanFrameServices()

    var isBound = false
    var isStarted = false

    private var sharedPreferences: SharedPreferences? = null




    private val alpineConnection = object : ServiceConnection {

        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {

            val binder = service as CanFrameServices.MyLocalBinder
            alpdroidServices = binder.getService()
            isBound = true

        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }


        override fun onNullBinding(name: ComponentName) {

            super.onNullBinding(name)

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate() {

        super.onCreate()
        if(BuildConfig.DEBUG)
            StrictMode.enableDefaults();

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build()
            )
        }

        val oldPolicy = StrictMode.getThreadPolicy()
        StrictMode.allowThreadDiskReads()
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }


        eventBus.register(this)
     //   initLog.
        MLog.i(TAG,"OnCreate ")
    }


    fun startListenerService() {
        if (ListenerService.isNotificationAccessEnabled(this)) {
            startService(Intent(this, ListenerService::class.java))

        }
        else
            Log.d("%s : Listener not started", TAG)

    }

   fun startVehicleServices() {


       actionOnService(Actions.START)

       try {

               bindService(
               Intent(
                   this,
                   CanFrameServices::class.java
               ), alpineConnection, BIND_AUTO_CREATE
           )


           }
           catch (e : Exception)
           {
               Log.d("Echec binding CanframeServices : ",e.toString())
           }

            isStarted = true
            isBound = true

   }


    private fun actionOnService(action: Actions) {

        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return

        Intent(this, CanFrameServices::class.java).also {
            it.action = action.name
            startForegroundService(it)
        }
    }

    private fun stopListenerService()
    {
        stopService(Intent(this, ListenerService::class.java))
        Log.d("ListenerServices stopped : ", TAG)
    }

    fun stopVehicleServices() {

        isBound=false
        // Detach the service connection.
        actionOnService(Actions.STOP)
        isStarted=false
        alpdroidServices.isServiceStarted=false

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    fun close() {
       val preferences = getSharedPreferences()
        val editor = preferences!!.edit()
        editor.apply()
        /*
        stopListenerService()
        stopVehicleServices()
        alpdroidData.onClose()*/
    }


    fun getSharedPreferences(): SharedPreferences? {
        return sharedPreferences
    }

    private fun getplayerType(player: String):Int {

        var playerType:Int = 4
        if ((player.contains("radio", true) or player.contains("tuner",true))) playerType =
            1 else if ((player.contains("com.syu.bt", true)) or player.contains("bluetooth", true)) playerType =
            7
        return playerType
    }

    @Subscribe
    fun onNowPlayingChange(event: NowPlayingChangeEvent) {

        lastEvent = event

        if (alpdroidServices.isServiceStarted) {

            if (lastEvent.track().album().isPresent) {
                alpdroidServices.setalbumName(lastEvent.track().album().get().toString())
            } else
                alpdroidServices.setalbumName("--")

            if (lastEvent.track().albumArtist().isPresent) {
                alpdroidServices.setalbumArtist(lastEvent.track().albumArtist().get().toString())
            } else
                alpdroidServices.setalbumArtist("--")

            if (lastEvent.track().artist().isNotEmpty()) {
                alpdroidServices.setartistName(lastEvent.track().artist().toString())
                alpdroidServices.setaudioSource(getplayerType(lastEvent.source()))
            }
            else {
                alpdroidServices.setartistName("--")
                alpdroidServices.setaudioSource(0)
            }

            if (lastEvent.track().track().isNotEmpty())
                alpdroidServices.settrackName(lastEvent.track().track().toString())
            else
                alpdroidServices.settrackName("--")

        }
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

        lateinit var app : AlpdroidApplication

        fun setContext(con: AlpdroidApplication) {
            app=con
        }

    }


}