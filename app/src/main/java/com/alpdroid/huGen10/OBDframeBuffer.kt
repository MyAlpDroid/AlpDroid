package com.alpdroid.huGen10

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class OBDframeBuffer {

    private var mapFrame : ConcurrentHashMap<Long, OBDframe> = ConcurrentHashMap<Long, OBDframe>(100)
    private var mutex_add : Mutex = Mutex()

    private var previousOBDpid:Long = 0


    @Synchronized
    fun addFrame(framecan: CanFrame, multiframetype:Int) {



        CoroutineScope(Dispatchers.IO).launch {
            mutex_add.withLock {

                val frame:OBDframe

                if (multiframetype==2)
                   {
                    if (this@OBDframeBuffer.mapFrame.get(previousOBDpid)!=null) {
                        frame = this@OBDframeBuffer.mapFrame.get(previousOBDpid)!!
                        if (frame.addOBDdata(
                                framecan.data.copyOfRange(1, 8),
                                framecan.data[0].toInt() - 0x20
                            )
                        )
                            this@OBDframeBuffer.mapFrame[previousOBDpid] = frame
                    }
                   }
                else {
                    frame = OBDframe(framecan.id, multiframetype, framecan.data)

                    if (multiframetype == 1) {

                        previousOBDpid = frame.canID*0x100000000+frame.serviceDir * 0x10000 + frame.servicePID


                        this@OBDframeBuffer.mapFrame[previousOBDpid] = frame


                    } else {
                        if (this@OBDframeBuffer.mapFrame.replace(
                                (frame.canID*0x100000000+frame.serviceDir * 0x10000 + frame.servicePID),
                                frame
                            ) == null
                        )
                            this@OBDframeBuffer.mapFrame[frame.canID*0x100000000+ frame.serviceDir * 0x10000 + frame.servicePID] =
                                frame
                    }
                }
                }
           }
    }


    fun getFrame(service:Int, dir:Int, canID:Int): OBDframe? {


    //    Log.d("OBD Buffer", "this is looking pDI : "+String.format("%012X",canID*0x100000000 + dir * 0x10000 + service ))

        return try {
            this@OBDframeBuffer.mapFrame[canID*0x100000000 + dir * 0x10000 + service]
        } catch (e: Exception) {
            //         Log.d("OBDFrameBuffer", "frame not found")
            null
        }

    }
    fun getKeys(): Set<Long> {
        return mapFrame.keys
    }

    fun get(next: Long): OBDframe? {
        return try {
            mapFrame.get(next)
        } catch (e:Exception) {
            null
        }
    }

    fun remove(service:Int, dir:Int, canID:Int) {
        mapFrame.remove(canID*0x100000000 + dir * 0x10000 + service)
    }


}