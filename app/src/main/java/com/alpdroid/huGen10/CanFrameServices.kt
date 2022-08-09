package com.alpdroid.huGen10

import android.app.Service
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.*
import android.util.Log
import com.alpdroid.huGen10.ui.MainActivity
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*


// Main CLass for Arduino and Canframe handling, as a service, listening to Arduino, sending to arduino and giving Frame value

class CanFrameServices : Service(), ArduinoListener {

    private val TAG = CanFrameServices::class.java.name

    private lateinit var arduino : Arduino

    val application  = MainActivity.application

    var isConnected : Boolean = false
    var isBad : Boolean = false

    private lateinit var jobCan : Job

    private lateinit var mBinder: CanFrameServices

    private lateinit var coroutineScope:CoroutineScope

    /* TODO : Implement ECU & MCU class or list enum */
    /* ECU enum could be : Cand_ID, ECUParameters, bytes, offset, value, len, step, offset, unit */

    override fun onCreate() {
        Log.i(TAG, "Service onCreate")
        super.onCreate()
        isConnected=true
        arduino=Arduino(this, 115200)
        arduino.setArduinoListener(this)
    /**    if (alpine2Cluster==null)
            alpine2Cluster = ClusterInfo(this) */
        Log.d(TAG, "Arduino Listener started")

        // sending frame from FiFo queue every 125 ms due to unidirectionnal USB 2.0
        // with USB 3.0 port this could be change

        coroutineScope = CoroutineScope(newSingleThreadContext("Alpine Service Thread"))

        jobCan = coroutineScope.launch(Dispatchers.IO) {

            Log.d("CanFrameServices running is own thread  : ", Thread.currentThread().name)
        while (true)
            {
                try {

                    sendFifoFrame()

                }
                catch (e: Exception) {
                    Log.d("CanFrameServices sending frame : ","Exception")

                }
                delay(125)
            }
       }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "Service onStartCommand " + startId)

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Service onBind")
        return myBinder
    }
    private val myBinder = MyLocalBinder()

    inner class MyLocalBinder : Binder() {
        fun getService() : CanFrameServices {
            return this@CanFrameServices
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")
        super.onDestroy()
        arduino.unsetArduinoListener()
        arduino.close()
        isConnected=false
        jobCan.cancel("Service Destroy")
        Log.d(TAG, "Arduino Listener Destroy")
        Log.d(TAG, "CanFrame Services Destroy")

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


    @Synchronized
    override fun onArduinoMessage(bytes: ByteArray?) {

   //     coroutineScope.launch(Dispatchers.IO) {
            //receive frame as Gson message
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
                Log.d("CanFrame Services", "CanFrameServices Bad Frame")
                isBad = true
            }
     //   }

    }

    override fun onArduinoOpened() {}

    override fun onUsbPermissionDenied() {
        Looper.myLooper()?.let { Handler(it).postDelayed({ arduino.reopen() }, 3000) }
    }

    fun sendFifoFrame()
    {
        val keys: Set<Int> = application.alpineCanFrame.getKeys()
        val iterator = keys.iterator()
        val key2fifo:CanFrame

        //Unqueue frame : first in first out
        if (application.alpineCanFrame.isNotEmpty()) {
            key2fifo = application.alpineCanFrame.get(iterator.next())!!
            sendFrame(key2fifo.id)
            application.alpineCanFrame.remove(key2fifo.id)

        }

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

    // Receive Data from OsmAnd

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