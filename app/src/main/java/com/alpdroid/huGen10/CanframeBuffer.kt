package com.alpdroid.huGen10


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap


class CanframeBuffer {

    private var mapFrame : ConcurrentHashMap<Int, CanFrame> = ConcurrentHashMap<Int, CanFrame>(100)
    private var queueoutFrame : LinkedHashMap<Int, CanFrame> = LinkedHashMap(50)
    private var sendingSwitch : Boolean = false
    private var mutexadd : Mutex = Mutex()
    private var mutexpush : Mutex = Mutex()



    @Synchronized
    fun addFrame(frame: CanFrame) {

        CoroutineScope(Dispatchers.IO).launch {
            mutexadd.withLock {
                val frame2test= this@CanframeBuffer.mapFrame.get(frame.id shl (frame.bus*0x10))
                if (frame2test!=null)
                    this@CanframeBuffer.mapFrame.replace(frame.id shl (frame.bus*0x10), frame)
                else  this@CanframeBuffer.mapFrame[frame.id shl (frame.bus*0x10)] = frame
            }
        }
    }


    fun setSending()
    {
        sendingSwitch=true
    }

    fun unsetSending()
    {
        sendingSwitch=false
    }

    fun isFrametoSend() : Boolean
    {
        return sendingSwitch
    }

    fun getFrame(candID:Int): CanFrame? {
        try {
            return this.mapFrame[candID]
        }
        catch (e:Exception) {
            return null
        }
    }

    fun getFrameFromBus(bus: Int, candID: Int): CanFrame? {

        try {

        return this.mapFrame[candID shl (bus*0x10)]
        }

            catch (e:Exception) {
                return null
            }
    }

    @Synchronized
    fun pushFifoFrame(bus:Int,candID: Int)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mutexpush.withLock {  // Push frame to send into FiFO queue
            getFrameFromBus(bus,candID ).also {
            if (it!=null) this@CanframeBuffer.queueoutFrame[candID shl (bus*0x10)] = it
            }
          }
        }
    }

    fun getKeys(): Set<Int> {
        return queueoutFrame.keys
    }

    fun getMapKeys() : MutableSet<Int> {
        return mapFrame.keys
    }

    fun isNotEmpty(): Boolean {
        return queueoutFrame.isNotEmpty()

    }

    fun get(next: Int): CanFrame? {
        try {
            return queueoutFrame.get(next)
        }
        catch (e:Exception) {
            return null
        }
    }

    fun remove(id: Int) {
        queueoutFrame.remove(id)
    }


    fun flush()
    {
        CoroutineScope(Dispatchers.IO).launch {
            mutexpush.withLock {
                queueoutFrame.clear()
            }
       }
    }


}


