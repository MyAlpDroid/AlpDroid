package com.alpdroid.huGen10

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import m.co.rh.id.alogger.AndroidLogger
import m.co.rh.id.alogger.CompositeLogger
import m.co.rh.id.alogger.FileLogger
import m.co.rh.id.alogger.ILogger
import java.io.File
import java.io.IOException


class AlpdroidApplication : Application() {

    private val TAG = AlpdroidApplication::class.java.name
    
    lateinit var alpdroidData : VehicleServices

    var alpineCanFrame : CanframeBuffer = CanframeBuffer()

    lateinit var alpdroidServices : CanFrameServices

  //  lateinit var mOsmAndHelper: OsmAndHelper

    var isBound = false
    var isStarted = false

    private var mLogger: ILogger? = null

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
            mLogger?.d(TAG,"On Null Binding Invoke")
            super.onNullBinding(name)
            mLogger?.d(TAG,"On Null Binding Trying to restart")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        eventBus.register(this)
        initLog()
    }


    fun  getLogger() : ILogger? {
        return mLogger
    }

    private fun initLog() {

        var loggerList : ArrayList<ILogger> = ArrayList<ILogger>()
        var defaultLogger:ILogger  =  AndroidLogger(ILogger.ERROR)
        loggerList.add(defaultLogger)
        try {
            val file:File = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "alogger/app.log")
            Log.d("files dir :", file.toString())
            val fileLogger =  FileLogger(ILogger.DEBUG, file)
            loggerList.add(fileLogger)
        } catch (e: IOException) {
            defaultLogger.e("Application", "Error instantiating file logger", e)
        }
        // might want to show ERROR only on toast
      //  var toastLogger:ToastLogger = ToastLogger(ILogger.ERROR, this)
      //  loggerList.add(toastLogger)
        mLogger  = CompositeLogger(loggerList)

    }


  

    fun startListenerService() {
        if (ListenerService.isNotificationAccessEnabled(this)) {
            startService(Intent(this, ListenerService::class.java))
            mLogger?.d("%s : Listener started", TAG)
        }
        else
            mLogger?.d("%s : Listener not started", TAG)

    }

   fun startVehicleServices() {
       mLogger?.d("CanFrameServices start phase : ", TAG)
       if (startService(Intent(this, CanFrameServices::class.java))!=null)
       {
           try {
               mLogger?.d("CanFrameServices binding phase : ", TAG)
               bindService(
               Intent(
                   this,
                   CanFrameServices::class.java
               ), alpineConnection, BIND_AUTO_CREATE
           )
               mLogger?.d("CanFrameServices binding phase : ", alpineConnection.toString())
           }
           catch (e : Exception)
           {
               mLogger?.d("Echec binding CanframeServices : ",e.toString())
           }
            isStarted = true
            isBound = true
            mLogger?.d("%s : CanFrameServices started", TAG)
        }
        else
           mLogger?.d("%s :CanFrameServices not started", TAG)
    }


    fun stopListenerService() {
        stopService(Intent(this, ListenerService::class.java))
        mLogger?.d("ListenerServices stopped : ", TAG)
    }

    fun stopVehicleServices() {
        mLogger?.d("CanFrameServices stop phase: ", TAG)
        isBound=false
        // Detach the service connection.
        unbindService(alpineConnection)
        stopService(Intent(this, CanFrameServices::class.java))
        isStarted=false
        mLogger?.d("CanFrameServices stopped : ", TAG)

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    fun close() {
        mLogger?.d("Application stop Phase initiate : ", TAG)
        val preferences = getSharedPreferences()
        val editor = preferences!!.edit()
        editor.apply()
        stopListenerService()
        stopVehicleServices()
        mLogger?.d("Application stop : ", TAG)
    }



    fun getSharedPreferences(): SharedPreferences? {
        return sharedPreferences
    }

    @Subscribe
    fun onNowPlayingChange(event: NowPlayingChangeEvent) {

        lastEvent = event

        if (this::alpdroidData.isInitialized) {

            if (lastEvent.track().album().isPresent) {
                alpdroidData.setalbumName(lastEvent.track().album().get().toString())

            } else
                alpdroidData.setalbumName("--")

            if (lastEvent.track().artist().isNotEmpty())
                alpdroidData.setartistName(lastEvent.track().artist().toString())
            else
                alpdroidData.setartistName("--")

            if (lastEvent.track().track().isNotEmpty())
                alpdroidData.settrackName(lastEvent.track().track().toString())
            else
                alpdroidData.settrackName("--")
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


    }

}