package com.alpdroid.huGen10


import java.util.concurrent.ConcurrentHashMap


class CanframeBuffer () {

    private var mapFrame : ConcurrentHashMap<Int, CanFrame> = ConcurrentHashMap<Int, CanFrame>(100)
    private var queueoutFrame : LinkedHashMap<Int, CanFrame> = LinkedHashMap(50)



    @Synchronized
    fun addFrame(frame: CanFrame) {

        if (this.mapFrame.replace(frame.id,frame)==null)
            this.mapFrame[frame.id] = frame

    }


    fun getFrame(candID:Int): CanFrame? {
        try {
            return this.mapFrame[candID]
        }
        catch (e:Exception) {
            return null
        }
    }


    fun pushFifoFrame(candID: Int)
    {
        // Push frame to send into FiFO queue
        getFrame(candID).also { if (it!=null)
            this.queueoutFrame.put(candID,it)
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
        return queueoutFrame.get(next)
    }

    fun remove(id: Int) {
        queueoutFrame.remove(id)
    }


}


