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
            Log.d(TAG,"On Null Binding Invoke")
            super.onNullBinding(name)
            Log.d(TAG,"On Null Binding Trying to restart")
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate() {

        MLog.init(this,true,true)
        MLog.d(TAG, "OnCreate")

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
        MLog.i(TAG,"OnCreate Fin")
    }


    fun startListenerService() {
        if (ListenerService.isNotificationAccessEnabled(this)) {
            startService(Intent(this, ListenerService::class.java))
            Log.d("%s : Listener started", TAG)
        }
        else
            Log.d("%s : Listener not started", TAG)

    }

   fun startVehicleServices() {
       Log.d("CanFrameServices start phase : ", TAG)

       actionOnService(Actions.START)

       try {
               Log.d("CanFrameServices binding phase : ", TAG)
               bindService(
               Intent(
                   this,
                   CanFrameServices::class.java
               ), alpineConnection, BIND_AUTO_CREATE
           )

           Log.d("CanFrameServices binding phase : ", alpineConnection.toString())
           }
           catch (e : Exception)
           {
               Log.d("Echec binding CanframeServices : ",e.toString())
           }

            isStarted = true
            isBound = true
            Log.d("%s : CanFrameServices started", TAG)
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
        Log.d("CanFrameServices stop phase: ", TAG)
        isBound=false
        // Detach the service connection.
        actionOnService(Actions.STOP)
        isStarted=false
        alpdroidServices.isServiceStarted=false
        Log.d("CanFrameServices stopped : ", TAG)
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

    @Subscribe
    fun onNowPlayingChange(event: NowPlayingChangeEvent) {

        lastEvent = event

        if (alpdroidServices.isServiceStarted) {

            if (lastEvent.track().album().isPresent) {
                alpdroidServices.setalbumName(lastEvent.track().album().get().toString())

            } else
                alpdroidServices.setalbumName("--")

            if (lastEvent.track().artist().isNotEmpty())
                alpdroidServices.setartistName(lastEvent.track().artist().toString())
            else
                alpdroidServices.setartistName("--")

            if (lastEvent.track().track().isNotEmpty())
                alpdroidServices.settrackName(lastEvent.track().track().toString())
            else
                alpdroidServices.settrackName("--")
        }
    }


    fun getEventBus(): EventBus? {
        return eventBus
    }

 /*   override fun osmandMissing() {
        // something to do is missing
        Log.d(TAG, "osmAND is missing, no navigation info available")
    }
*/

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