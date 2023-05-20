package com.alpdroid.huGen10

// import `in`.rmkrishna.mlog.MLog
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.manicben.physicaloid.lib.Boards
import com.manicben.physicaloid.lib.Physicaloid


class AlpdroidApplication : Application() {

    private val TAG = AlpdroidApplication::class.java.name
    
    lateinit var alpdroidData : VehicleServices

    var alpineCanFrame : CanframeBuffer = CanframeBuffer()

    var alpineOBDFrame : OBDframeBuffer = OBDframeBuffer()

    var alpdroidServices : CanFrameServices = CanFrameServices()

    var isBound = false
    var isStarted = false

    private var sharedPreferences: SharedPreferences? = null

    var mPhysicaloid:Physicaloid? = null



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


    override fun onCreate() {

        super.onCreate()
        if(BuildConfig.DEBUG)
            StrictMode.enableDefaults()

        if (BuildConfig.DEBUG && true) {
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

        // Check if the "update_arduino" preference is set to true
       val isArduinoEnabled = sharedPreferences!!.getBoolean(getString(R.string.arduino_update), true)


        if (isArduinoEnabled) {

            Log.d(TAG,"trying physicaloid stuff")
            // Call the specific function to perform physical tasks
            try {
            mPhysicaloid = Physicaloid(this)

            mPhysicaloid!!.setAutoDtr()

            mPhysicaloid!!.open()

            val timeout = 30000L // 30 secondes en millisecondes

            val startTime = System.currentTimeMillis()

            while (!mPhysicaloid!!.isOpened && System.currentTimeMillis() - startTime < timeout)
            {
                mPhysicaloid!!.open()
            }


                //****************************************************************
                // TODO : set board type and assets file.
                // TODO : copy .hex file to porject_dir/assets directory.


                mPhysicaloid!!.upload(
                    Boards.ARDUINO_UNO,
                    resources.assets.open("UNO_CODE.ino.hex")
                )

                // if no exception, seems to be OK
                sharedPreferences!!.edit().putBoolean(getString(R.string.isdownload_UNO), true).apply()

            } catch (e: RuntimeException) {
                Log.e("Physicaloid", e.toString())
            }

            // Update the "arduino" preference to false
            sharedPreferences!!.edit().putBoolean(getString(R.string.arduino_update), false).apply()

            mPhysicaloid!!.close()

            Log.d(TAG,"End Physicaloid Stuff")

        }

        Log.d(TAG, "OnCreate Application")

        eventBus.register(this)
     //   initLog.
      //  MLog.i(TAG,"OnCreate Fin")
    }


    fun startListenerService() {
        if (ListenerService.isNotificationAccessEnabled(this)) {
            startService(Intent(this, ListenerService::class.java))
            Log.d(TAG," Listener started")
        }
        else
            Log.d(TAG,"Listener not started")

    }

   fun startVehicleServices() {

       actionOnService(Actions.START)

       try {
               Log.d(TAG,"CanFrameServices binding phase  ")
               bindService(
               Intent(
                   this,
                   CanFrameServices::class.java
               ), alpineConnection, BIND_AUTO_CREATE
           )

           }
           catch (e : Exception)
           {
               Log.d(TAG,"Echec binding CanframeServices : "+e.toString())
           }

            isStarted = true
            isBound = true
            Log.d(TAG,"CanFrameServices started")
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
        Log.d(TAG,"ListenerServices stopped ")
    }

    fun stopVehicleServices() {

        isBound=false
        // Detach the service connection.
        actionOnService(Actions.STOP)
        isStarted=false
        alpdroidServices.isServiceStarted=false
        Log.d(TAG, "CanFrameServices stopped ")
    }


    fun close() {
       val preferences = getSharedPreferences()
        val editor = preferences!!.edit()
        editor.apply()

    }


    fun getSharedPreferences(): SharedPreferences? {
        return sharedPreferences
    }

    fun getplayerType(player: String):Int {
        var playerType = 4
        if ((player.contains("radio", true) or player.contains("tuner",true) or player.contains("zoulou",true))) playerType =
            1 else if (player.contains("com.syu.bt", true) or player.contains("carlink",true)) playerType =
            7
        return playerType
    }

    @Subscribe
    fun onNowPlayingChange(event: NowPlayingChangeEvent) {

        var isaudio = false

        lastEvent = event

        if (alpdroidServices.isServiceStarted) {

            if (lastEvent.track().album().isPresent) {
                alpdroidServices.setalbumName(lastEvent.track().album().get().toString())
                isaudio=true
            } else
                alpdroidServices.setalbumName("--")

            if (lastEvent.track().artist().isNotEmpty()) {
                alpdroidServices.setartistName(lastEvent.track().artist().toString())
                isaudio=true
            }
            else
                alpdroidServices.setartistName("--")


            if (lastEvent.track().track().isNotEmpty()) {
                alpdroidServices.settrackName(lastEvent.track().track().toString())
                isaudio=true
            }
            else
                alpdroidServices.settrackName("--")

            if (isaudio)
                alpdroidServices.setaudioSource(getplayerType(lastEvent.source()))
            else
                alpdroidServices.setaudioSource(0)
        }
    }


    fun getEventBus(): EventBus {
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