package com.alpdroid.huGen10

import android.app.Application
import android.content.*
import android.os.Build
import android.os.IBinder
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import java.util.*


class AlpdroidApplication : Application() {
    var alpdroidService: VehicleServices?=null
    var isBound = false
    var isStarted = false

    private var sharedPreferences: SharedPreferences? = null
    private var intent: Intent? = null

    private val alpineConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder
        ) {

            val binder = service as VehicleServices.VehicleServicesBinder
            alpdroidService = binder.getService()
            isBound = true

        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false

        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        Log.d("Application", "onCreate done")
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        eventBus.register(this)

       val threadVehicleServices: Thread = object : Thread() {
            override fun run() {
                val intent = Intent(getApplicationContext(), VehicleServices::class.java)
                if (bindService(intent, alpineConnection, BIND_AUTO_CREATE)) {
                    Log.d("Application", "Thread VehicleServices started")
                    isStarted = true
                    isBound = true
                }
                else
                    Log.d("Application", "Thread VehicleServices not binded")

            }
        }

        threadVehicleServices.start()

    }

    fun startListenerService() {
        if (ListenerService.isNotificationAccessEnabled(this)) {
            startService(Intent(this, ListenerService::class.java))
            Log.d("Application", "Listener started")
        }
    }

    fun startVehicleServices() {
        intent=Intent(this, VehicleServices::class.java)
        startService(intent)
        if (alpdroidService?.bindService(intent, alpineConnection, Context.BIND_AUTO_CREATE) == true) {
            isStarted = true
            isBound = true
            Log.d("Application", "VehicleListener started")
        }
    }


    fun stopListenerService() {
        stopService(Intent(this, ListenerService::class.java))
    }

    fun stopVehicleServices() {
        alpdroidService?.stopService(Intent(alpdroidService, VehicleServices::class.java))
        alpdroidService?.unbindService(alpineConnection)
        isBound=false
        isStarted=false
        Log.d("Application", "VehicleListener stopped")
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    fun close() {
        val preferences = getSharedPreferences()
        val editor = preferences!!.edit()
        editor.apply()
        stopListenerService()
        stopVehicleServices()
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    fun resume() {

        intent=Intent(this, VehicleServices::class.java)

        isStarted = false
        isBound = false
        if (bindService(intent, alpineConnection, Context.BIND_AUTO_CREATE)) {
            alpdroidService?.bindService(intent, alpineConnection, Context.BIND_AUTO_CREATE)
            isStarted = true
            isBound = true
        }

        if (alpdroidService!=null) {
            Log.d("Application", "VehicleListener resume")
          }
        else
            Log.d("Application", "VehicleListener destroy and not resume")
    }


    fun pause() {
        intent=Intent(this, VehicleServices::class.java)

        isStarted = false
        isBound = false
        if (bindService(intent, alpineConnection, Context.BIND_AUTO_CREATE)) {
            alpdroidService?.bindService(intent, alpineConnection, Context.BIND_AUTO_CREATE)
            isStarted = true
            isBound = true
        }

        if (alpdroidService!=null) {
            Log.d("Application", "VehicleListener pause")
        }
        else
            Log.d("Application", "VehicleListener destroy and not pause")

    }


    fun getSharedPreferences(): SharedPreferences? {
        return sharedPreferences
    }

    @Subscribe
    fun onNowPlayingChange(event: NowPlayingChangeEvent) {

        lastEvent = event

        if (lastEvent.track().album().isPresent) {
            this.alpdroidService?.setalbumName(lastEvent.track().album().get().toString())

        }
        else
            this.alpdroidService?.setalbumName("--")

        if (lastEvent.track().artist().isNotEmpty())
            this.alpdroidService?.setartistName(lastEvent.track().artist().toString())
        else
            this.alpdroidService?.setartistName("--")

            Log.d(" Artist", lastEvent.track().artist().toString())
        if (lastEvent.track().track().isNotEmpty())
            this.alpdroidService?.settrackName(lastEvent.track().track().toString())
        else
            this.alpdroidService?.settrackName("--")
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