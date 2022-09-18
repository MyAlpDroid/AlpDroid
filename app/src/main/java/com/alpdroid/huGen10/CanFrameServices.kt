package com.alpdroid.huGen10

import android.app.*
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

    var isConnected : Boolean = false
    var isBad : Boolean = false
    private var isServiceStarted = false

    private val mutex_read = Mutex()
    private val mutex_write = Mutex()

    private val myBinder = MyLocalBinder()
    private var wakeLock: PowerManager.WakeLock? = null

    private var countdown : Int =20;


    /* TODO : Implement ECU & MCU class or list enum */
    /* ECU enum could be : Cand_ID, ECUParameters, bytes, offset, value, len, step, offset, unit */

    override fun onCreate() {
        Log.i(TAG, "Service onCreate")
        application= getApplication() as AlpdroidApplication
        super.onCreate()

        // init Control Frame

        // Adding reset pool Frame Block
        application.alpineCanFrame.addFrame(
            CanFrame(
                3,
                0xFFD,
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

        application.alpineCanFrame.pushFifoFrame(0xFFD)

        Log.i(TAG, "Launching globalscope coroutines OnCreate")

        val notification = createNotification()

        Log.i(TAG, "OnCreate ForeGround Services Notif Ok Trying to start foreground")

        this.startForeground(1603, notification)

        Log.i(TAG, "OnCreate ForeGround Services start foreground OK")
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

    @OptIn(InternalCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        Log.i(TAG, "Service onStartCommand " + startId)

       application= getApplication() as AlpdroidApplication

        if (!isConnected)
        {
            Log.i(TAG, "Arduino Service need to reconnect")
            isConnected=true
            arduino=Arduino(this, 115200)
            arduino.setArduinoListener(this)
        }

        if (intent != null) {
            val action = intent.action
            Log.i(TAG,"using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> Log.i(TAG,"This should never happen. No action in the received intent")
            }
        } else {
            Log.i(TAG,
                "with a null intent. It has been probably restarted by the system."
            )
        }
        Log.i(TAG, " : return Start Sticky")
        return START_STICKY
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "CanFrameServices onBind return MyBinder")
        Log.d(TAG, myBinder.toString())
        return myBinder // or null ?
    }



    inner class MyLocalBinder : Binder() {
        fun getService() : CanFrameServices {
            return this@CanFrameServices
        }
    }

    override fun stopService(name: Intent?): Boolean {
        Log.i(TAG, "CanFrameServices StopService")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(1603)
        return super.stopService(name)
    }

    // A client has unbound from the service
    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "CanFrameServices onUnBind")
        return super.onUnbind(intent)
    }

    private fun startService() {

        if (!isConnected)
        {
            Log.i(TAG, "Arduino Service start or need to reconnect")
            isConnected=true
            arduino=Arduino(this, 115200)
            arduino.setArduinoListener(this)
        }

        if (isServiceStarted) return
        Log.d(TAG,"Starting the foreground service task")
     //   Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {

                    mutex_write.withLock {
                        countdown--;
                        if (countdown == 0)
                            application.alpineCanFrame.pushFifoFrame(0xFFD)
                        if (isConnected) {
                            try {
                                if (application.alpineCanFrame.isFrametoSend())
                                {

                                    Log.i(TAG, "Something to Send")
                                    application.alpineCanFrame.unsetSending()
                                    // Adding Stop Frame
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
            Log.d(TAG,"End of the loop for the service")
        }
    }

    private fun stopService() {
        Log.d(TAG, "Stopping CanFrameServices's foreground service")
        Toast.makeText(this, "Service MyAlpdroid stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(TAG,"Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    override fun onDestroy() {
        arduino.unsetArduinoListener()
        arduino.close()
        isConnected=false
        super.onDestroy()
        Log.i(TAG, "CanFrameServices quit onDestroy")
    }


    fun isCanFrameEnabled(): Boolean {
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
            sendFrame(key2fifo.id)
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
    fun sendFrame(candID: Int) {
        application.alpineCanFrame.getFrame(candID).also {
         //send frame as byte to serial port
            if (it != null) {
                arduino.send(it.toByteArray())

            }
        }
    }

    fun checkFrame(ecuAddrs:Int):Boolean {

        // check if the frame is valuable
        if (application.alpineCanFrame.getFrame(ecuAddrs)!=null) {
            return true
        }
        return false
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