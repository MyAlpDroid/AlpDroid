package com.alpdroid.huGen10

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class OBDframeBuffer {

    private var mapFrame : ConcurrentHashMap<Int, OBDframe> = ConcurrentHashMap<Int, OBDframe>(100)
    private var mutex_add : Mutex = Mutex()



    @Synchronized
    fun addFrame(frame: OBDframe) {

        CoroutineScope(Dispatchers.IO).launch {
            mutex_add.withLock {
                if (this@OBDframeBuffer.mapFrame.replace(frame.servicePID, frame) == null)
                    this@OBDframeBuffer.mapFrame[frame.servicePID] = frame
            }
        }
    }



    fun getFrame(service:Int): OBDframe? {
        return try {
            this.mapFrame[service]
        } catch (e:Exception) {
            null
        }
    }





}