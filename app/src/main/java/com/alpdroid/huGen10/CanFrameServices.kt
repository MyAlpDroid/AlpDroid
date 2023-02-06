package com.alpdroid.huGen10

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.os.*
import android.util.Log
import com.alpdroid.huGen10.ui.MainActivity
import com.alpdroid.huGen10.util.IntegerUtil
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

    var backalbumName:String = "--"
    var bcktrackName:String = "--"
    var backartistName:String = "--"
    var audioSource:Int=0


    var isConnected : Boolean = false
    var isBad : Boolean = false

    var isServiceStarted = false

    private val mutex_read = Mutex()
    private val mutex_write = Mutex()

    private val myBinder = MyLocalBinder()
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var globalScopeReporter : Job

    private val DEFAULT_BAUD_RATE = Constants.BAUDRATE

    var tx:Int = 0
    var rx:Int = 0

    /* TODO : Implement ECU & MCU class or list enum */
    /* ECU enum could be : Cand_ID, ECUParameters, bytes, offset, value, len, step, offset, unit */

    override fun onCreate() {
        application= getApplication() as AlpdroidApplication

        super.onCreate()

        isConnected=true
        arduino=Arduino(this)
        arduino.setArduinoListener(this)

        alpine2Cluster=ClusterInfo(application)

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
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        val builder: Notification.Builder = Notification.Builder(
            this,
            notificationChannelId
        )

        return builder
            .setContentTitle("MyAlpdroid Foreground Service")
            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("MFS ticker")
  //          .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


       application = getApplication() as AlpdroidApplication

        try {
            if (arduino.isOpened) {
                arduino.reopen()
                isConnected = true
            }
            else
            {
                arduino=Arduino(this, DEFAULT_BAUD_RATE)
                arduino.setArduinoListener(this)
                isConnected=true
            }
        }
        catch (e:Exception)
        {
            isConnected=true
            arduino=Arduino(this, DEFAULT_BAUD_RATE)
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
            else
            {
                arduino=Arduino(this, DEFAULT_BAUD_RATE)
                arduino.setArduinoListener(this)
                isConnected=true
                isServiceStarted=false
            }
            if (globalScopeReporter.isActive)
                isServiceStarted=true
        }
        catch (e:Exception)
        {
            arduino=Arduino(this, DEFAULT_BAUD_RATE)
            arduino.setArduinoListener(this)
            isConnected=true
            isServiceStarted=false
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
          //      launch(Dispatchers.Default) {
                    mutex_write.withLock {
                        if (isConnected) {
                            try {
                                    if (application.alpineCanFrame.isFrametoSend())
                                    {
                                        application.alpineCanFrame.unsetSending()
                                        // Adding Stop Frame
                                        sendFifoFrame()
                                    }

                            } catch (e: Exception) {
                                // No Frame , No Arduino or Bad Frame
                                Log.i(TAG, " : No Frame, no Arduino or Bad Frame")
                            }
                        }
                    }

           //     }
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
        return arduino.txWarning
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

                        if (frame.id>0x700)
                            application.alpineOBDFrame.addFrame(OBDframe(frame.id,frame.data))
                        else
                            application.alpineCanFrame.addFrame(frame)
                        isBad = false
                        rx+=frame.dlc
                } catch (e: Exception) {
                    //checking bad message
                    isBad = true
                }
            }
        }

    }

    override fun onArduinoOpened() {}

    override fun onUsbPermissionDenied() {
        Looper.myLooper()?.let { Handler(it).postDelayed({ arduino.reopen() }, 3000) }
    }


    fun sendFrame(frame: CanFrame) {

        val crcValue= IntegerUtil.GenerateChecksumCRC16(frame.toByteArray())

        val crcByte:ByteArray = ByteArray(2)

        crcByte[0]=crcValue.toByte()
        crcByte[1]=(crcValue/256).toByte()

        arduino.send("@@".toByteArray()+frame.toByteArray()+crcByte)
        // +crcframe.value.toString().toByteArray()
        tx+=frame.dlc
    }

    fun checkFrame(ecuAddrs:Int):Boolean {

        // check if the frame is valuable
        if (application.alpineCanFrame.getFrame(ecuAddrs)!=null) {
            return true
        }
        return false
    }

    @Synchronized
    fun setalbumName(albumname:String)
    {
        alpine2Cluster.albumName=albumname
        backalbumName=albumname
        alpine2Cluster.startIndexAlbum=0

    }

    @Synchronized
    fun setalbumArtist(albumartist:String)
    {
        alpine2Cluster.albumArtist=albumartist
    }

    fun getalbumName(): String? {
        return backalbumName
    }

    fun getartistName(): String? {
        return backartistName
    }

    fun gettrackName(): String? {
        return bcktrackName
    }


    @Synchronized
    fun settrackName(trackname:String)
    {

        alpine2Cluster.trackName=trackname
        bcktrackName=trackname

        alpine2Cluster.startIndexTrack=0
    }

    @Synchronized
    fun setartistName(artistname:String)
    {
        alpine2Cluster.artistName=artistname
        backartistName=artistname
        alpine2Cluster.startIndexArtist=0
    }

    @Synchronized
    fun settrackId(trackid:Int)
    {
        alpine2Cluster.trackId=trackid
    }

    @Synchronized
    fun settrackLengthInSec(tracklengthinsec:Int)
    {
        alpine2Cluster.trackLengthInSec=tracklengthinsec
    }

    @Synchronized
    fun setaudioSource(audioSource: Int) {
        this.audioSource =audioSource
        alpine2Cluster.audioSource=audioSource

    }


}