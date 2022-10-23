package com.alpdroid.huGen10

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.os.*
import android.util.Log
import android.widget.Toast
import com.alpdroid.huGen10.ui.MainActivity
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


// Main CLass for Arduino and Canframe handling, as a service, listening to Arduino, sending to arduino and giving Frame value

class CanFrameServices : Service(), ArduinoListener {

    private val TAG = CanFrameServices::class.java.name
    private val CHANNEL_ID = "ForegroundService MyAlpDroid"

    private lateinit var arduino : Arduino

    private lateinit var application: AlpdroidApplication

    lateinit var alpine2Cluster: ClusterInfo

    var isConnected : Boolean = false
    var isBad : Boolean = false
    var isServiceStarted = false

    private val mutex_read = Mutex()
    private val mutex_write = Mutex()

    private val myBinder = MyLocalBinder()
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var globalScopeReporter : Job

    /* TODO : Implement ECU & MCU class or list enum */
    /* ECU enum could be : Cand_ID, ECUParameters, bytes, offset, value, len, step, offset, unit */

    override fun onCreate() {
        application= getApplication() as AlpdroidApplication

        super.onCreate()

            isConnected=true
            arduino=Arduino(this, 115200)
            arduino.setArduinoListener(this)

            alpine2Cluster=ClusterInfo(application)

        // init Control Frame

        // Adding Start Block
        application.alpineCanFrame.addFrame(
            CanFrame(
                2,
                0xFFE,
                byteArrayOf(
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte()
                )
            ))

        // Adding Stop Block
        application.alpineCanFrame.addFrame(
            CanFrame(
                2,
                0xFFF,
                byteArrayOf(
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte()
                )
            ))


    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ALPDROID SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("MyAlpdroid Foreground Service")
            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


       application= getApplication() as AlpdroidApplication

        try {
            if (arduino.isOpened)
                isConnected=true
        }
        catch (e:Exception)
        {
            isConnected=true
            arduino=Arduino(this, 115200)
            arduino.setArduinoListener(this)
        }

        if (isServiceStarted) return START_STICKY

        val notification = createNotification()

        this.startForeground(1603, notification)

        if (intent != null) {
            when (intent.action) {
                Actions.START.name -> startService(intent)
                Actions.STOP.name -> stopService(intent)
                else -> Log.i(TAG,"This should never happen. No action in the received intent")
            }
        } else {
            Log.i(TAG,
                "with a null intent. It has been probably restarted by the system."
            )
            return START_REDELIVER_INTENT
        }
        Log.i(TAG,"CanFrame Service is started")

        return START_STICKY
    }


    override fun onBind(intent: Intent): IBinder? {
        return myBinder // or null ?
    }


    inner class MyLocalBinder : Binder() {
        fun getService() : CanFrameServices {
            return this@CanFrameServices
        }
    }

    override fun stopService(name: Intent?): Boolean {
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
        Log.d(TAG, "Stopping CanFrameServices's foreground service")
        Toast.makeText(this, "Service MyAlpdroid stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            Log.d(TAG,"Service stopped without being started: ${e.message}")
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(1603)

        return super.stopService(name)
    }

    // A client has unbound from the service
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun startService(name: Intent?): ComponentName? {

        try {
            if (arduino.isOpened)
                isConnected=true
            if (globalScopeReporter.isActive)
                isServiceStarted=true
        }
        catch (e:Exception)
        {
            isConnected=true
            isServiceStarted=false
            arduino=Arduino(this, 115200)
            arduino.setArduinoListener(this)
        }


        if (isServiceStarted) return startForegroundService(name)

        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire(10*60*1000L /*10 minutes*/)
                }
            }

        // we're starting a loop in a coroutine
        globalScopeReporter = CoroutineScope(Dispatchers.Default).launch {
            while (isServiceStarted) {
                launch(Dispatchers.Default) {
                        if (isConnected)
                            if (application.alpineCanFrame.isFrametoSend()) {
                               try {
                                mutex_write.withLock {
                                    application.alpineCanFrame.unsetSending()
                                    // Adding Stop Frame
                                    Log.i(TAG, " : Sending Frame")
                                    application.alpineCanFrame.pushFifoFrame(0xFFF)
                                    sendFifoFrame()
                                    // Adding Init for Next Block Queue
                                    application.alpineCanFrame.pushFifoFrame(0xFFE)
                                }
                            } catch (e: Exception) {
                                // No Frame , No Arduino or Bad Frame
                                Log.i(TAG, " : No Frame, no Arduino or Bad Frame")
                            }
                        }

                }
            }

        }

        return startForegroundService(name)
    }


    override fun onDestroy() {
        if (isConnected) {
            arduino.unsetArduinoListener()
            arduino.close()
        }
        isConnected=false
        isServiceStarted = false
        super.onDestroy()
    }


    fun isCanFrameEnabled(): Boolean {
        return (isConnected && isServiceStarted)
    }

    fun isArduinoWorking():Boolean {
        return arduino.isOpened
    }

    override fun onArduinoAttached(device: UsbDevice?) {
        arduino.open(device)
    }

    override fun onArduinoDetached() {
        isConnected=false
        arduino.close()
    }

    fun sendFifoFrame()
    {

        val keys: Set<Int> = application.alpineCanFrame.getKeys()
        val iterator = keys.iterator()
        var key2fifo: CanFrame

        //Unqueue frame : first in first out
        while (iterator.hasNext()) {
            key2fifo = application.alpineCanFrame.get(iterator.next())!!
            sendFrame(key2fifo)
        }

        application.alpineCanFrame.flush()

    }

    override fun onArduinoMessage(bytes: ByteArray?) {

        CoroutineScope(Dispatchers.IO).launch {

            mutex_read.withLock {  //receive frame as Gson message
                val frame: CanFrame
                val buff = String(bytes!!)
                val gson = GsonBuilder()
                    .registerTypeAdapter(CanFrame::class.java, CanframeGsonDeserializer())
                    .create()

                try {
                    frame = gson.fromJson(buff, CanFrame::class.java)
                    if (frame != null) {
                        application.alpineCanFrame.addFrame(frame)
                        isBad = false
                    }
                } catch (e: Exception) {
                    //checking bad message
                    Log.d(TAG, "CanFrameServices Bad Frame")
                    isBad = true
                }
            }
        }

    }

    override fun onArduinoOpened() {}

    override fun onUsbPermissionDenied() {
        Looper.myLooper()?.let { Handler(it).postDelayed({ arduino.reopen() }, 3000) }
    }


    @Synchronized
    fun sendFrame(frame: CanFrame) {

            if (frame != null) {
                arduino.send(frame.toByteArray())
            }
    }

    fun checkFrame(ecuAddrs:Int):Boolean {

        // check if the frame is valuable
        if (application.alpineCanFrame.getFrame(ecuAddrs)!=null) {
            return true
        }
        return false
    }

    fun setalbumName(albumname:String)
    {
        alpine2Cluster.albumName=albumname
        // .substring(0,minOf(albumname.length, 16))
        alpine2Cluster.startIndexAlbum=0

    }

    fun getalbumName(): String? {
        return alpine2Cluster.albumName
    }
    fun settrackName(trackname:String)
    {

        alpine2Cluster.trackName=trackname

        //.substring(0,minOf(trackname.length, 16))
        alpine2Cluster.startIndexTrack=0
    }

    fun setartistName(artistname:String)
    {
        alpine2Cluster.artistName=artistname
        //  alpine2Cluster.prevartistName=artistname
        //.substring(0,minOf(artistname.length, 16))
        alpine2Cluster.startIndexArtist=0
    }

    fun settrackId(trackid:Int)
    {
        alpine2Cluster.trackId=trackid
    }
    fun settrackLengthInSec(tracklengthinsec:Int)
    {
        alpine2Cluster.trackLengthInSec=tracklengthinsec
    }
    // Receive Data from OsmAnd not here

    fun fromOsmData(extras:Bundle)
    {
      if (extras != null && extras.size() > 0) {
            var next_turn = extras.getBundle("next_turn")
            var turn_type = extras.getBundle("turn_type")
            var distance_2_turn = extras.getBundle("turn_distance")
            Log.d("next_turn",next_turn.toString())
            Log.d("turn_type",turn_type.toString())
            Log.d("distance_2_turn",distance_2_turn.toString())
           for (key in extras.keySet()) {

               Log.d("key to read : ", key)
               Log.d("value read : ", extras[key].toString())
           }

      }
    }

        fun updateOsmData(extras:Bundle)
        {
            if (extras != null && extras.size() > 0) {
                var next_turn = extras.getBundle("next_turn")
                var turn_type = extras.getBundle("turn_type")
                var distance_2_turn = extras.getBundle("turn_distance")
                Log.d("next_turn",next_turn.toString())
                Log.d("turn_type",turn_type.toString())
                Log.d("distance_2_turn",distance_2_turn.toString())
                for (key in extras.keySet()) {

                    Log.d("key to read : ", key)
                    Log.d("value read : ", extras[key].toString())
                }

            }
        }


}