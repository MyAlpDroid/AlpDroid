package com.alpdroid.huGen10

import android.util.Log
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

        var servicePIDtoUInt:Int

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

                        servicePIDtoUInt=(frame.servicePID.toUByte()+(frame.servicePID/256).toUByte()* 256u).toInt()

                    //    Log.d("servicePID replace:", String.format("%04X",servicePIDtoUInt.toString()))

                        previousOBDpid = (frame.canID*0x100000000+frame.serviceDir * 0x10000 + servicePIDtoUInt)
                        this@OBDframeBuffer.mapFrame[previousOBDpid] = frame

                    } else {
                        servicePIDtoUInt=(frame.servicePID.toUByte()+(frame.servicePID/256).toUByte()* 256u).toInt()

                 //       Log.d("servicePID add:", String.format("%04X",servicePIDtoUInt))

                        if (this@OBDframeBuffer.mapFrame.replace(
                                (frame.canID*0x100000000+frame.serviceDir * 0x10000 + servicePIDtoUInt),
                                frame
                            ) == null
                        )
                            this@OBDframeBuffer.mapFrame[frame.canID*0x100000000+ frame.serviceDir * 0x10000 + servicePIDtoUInt] = frame
                       //     Log.d("this pid :",servicePIDtoUInt.toString())
                    }
                }
                }
           }
    }


    fun getFrame(service:Int, dir:Int, canID:Int): OBDframe? {


        return try {

           val servicePIDtoUInt=(service.toUByte()+((service/256).toUByte()* 256u)).toInt()

            if (this@OBDframeBuffer.mapFrame[canID*0x100000000 + dir * 0x10000 + servicePIDtoUInt]!=null)
                this@OBDframeBuffer.mapFrame[canID*0x100000000 + dir * 0x10000 + servicePIDtoUInt]
            else null
        } catch (e: Exception) {
                    Log.d("OBDFrameBuffer", "frame not found")
            null
        }

    }
    fun getKeys(): Set<Long> {
        return mapFrame.keys
    }

    fun get(key: Long): OBDframe? {
        return try {
            mapFrame.get(key)
        } catch (e:Exception) {
            null
        }
    }

    fun remove(service:Int, dir:Int, canID:Int) {

        val servicePIDtoUInt=(service.toUByte()+(service/256).toUByte()* 256u).toInt()

        mapFrame.remove((canID*0x100000000 + dir * 0x10000 + servicePIDtoUInt))

    }


}