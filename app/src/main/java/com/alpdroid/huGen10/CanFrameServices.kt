package com.alpdroid.huGen10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.*
import androidx.core.app.NotificationCompat
import com.alpdroid.huGen10.ui.MainActivity
import com.alpdroid.huGen10.ui.MainActivity.logger
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


    private val mutex_read = Mutex()
    private val mutex_write = Mutex()


    /* TODO : Implement ECU & MCU class or list enum */
    /* ECU enum could be : Cand_ID, ECUParameters, bytes, offset, value, len, step, offset, unit */

    override fun onCreate() {
        logger.i(TAG, "Service onCreate")
        super.onCreate()
        isConnected=true
        arduino=Arduino(this, 115200)
        arduino.setArduinoListener(this)

        logger.i(TAG, "Service start pid GlobalScope")

        GlobalScope.launch(Dispatchers.Default) {
            logger.i(TAG, "Launching globalscope coroutines OnCreate")
            while (isConnected)   {
                try {
                    mutex_write.withLock {
                        if (application.alpineCanFrame.isFrametoSend()) {
                            application.alpineCanFrame.unsetSending()
                            // Adding Stop Frame
                            application.alpineCanFrame.pushFifoFrame(0xFFF)
                            sendFifoFrame()
                            // Adding Init for Next Block Queue
                            application.alpineCanFrame.pushFifoFrame(0xFFE)

                        }
                    }
                } catch (e: Exception) {
                    // No Frame , No Arduino or Bad Frame
                    logger.i(TAG, " : No Frame, no Arduino or Bad Frame")
                }
                delay((Math.random() *1500).toLong())
            }
        }

        logger.i(TAG, "OnStartCommand ForeGorund Services Settings")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service MyAlpDroid")
            .setContentText("working")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        logger.i(TAG, "OnStartCommand ForeGorund Services Notif Ok Trying to start foreground")
        this.startForeground(1, notification)
        logger.i(TAG, "OnStartCommand ForeGorund Services start foreground OK")
    }


    @OptIn(InternalCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        logger.i(TAG, "Service onStartCommand " + startId)

        // sending frame from FiFo queue every 125 ms due to unidirectionnal USB 2.0
        // with USB 3.0 port this could be change
        application= getApplication() as AlpdroidApplication

        if (!isConnected)
        {
            logger.i(TAG, "Service need to recconnect")
            isConnected=true
            arduino=Arduino(this, 115200)
            arduino.setArduinoListener(this)
        }


        logger.i(TAG, " : return Start Sticky")
        return START_STICKY
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        logger.i(TAG, "CanFrameServices onBind")
        logger.d(TAG, myBinder.toString())
        return null
    }
    private val myBinder = MyLocalBinder()

    inner class MyLocalBinder : Binder() {
        fun getService() : CanFrameServices {
            return this@CanFrameServices
        }
    }

    override fun stopService(name: Intent?): Boolean {
        logger.i(TAG, "CanFrameServices StopService")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(1)
        return super.stopService(name)
    }

    // A client has unbound from the service
    override fun onUnbind(intent: Intent?): Boolean {
        logger.i(TAG, "CanFrameServices onUnBind")
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        logger.i(TAG, "CanFrameServices onDestroy")
        arduino.unsetArduinoListener()
        arduino.close()
        isConnected=false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(1)
        super.onDestroy()
        logger.i(TAG, "CanFrameServices quit onDestroy")
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
                    logger.d(TAG, "CanFrameServices Bad Frame")
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
            logger.d("next_turn",next_turn.toString())
            logger.d("turn_type",turn_type.toString())
            logger.d("distance_2_turn",distance_2_turn.toString())
           for (key in extras.keySet()) {

               logger.d("key to read : ", key)
               logger.d("value read : ", extras[key].toString())
           }

      }
    }

        fun updateOsmData(extras:Bundle)
        {
            if (extras != null && extras.size() > 0) {
                var next_turn = extras.getBundle("next_turn")
                var turn_type = extras.getBundle("turn_type")
                var distance_2_turn = extras.getBundle("turn_distance")
                logger.d("next_turn",next_turn.toString())
                logger.d("turn_type",turn_type.toString())
                logger.d("distance_2_turn",distance_2_turn.toString())
                for (key in extras.keySet()) {

                    logger.d("key to read : ", key)
                    logger.d("value read : ", extras[key].toString())
                }

            }
        }


}