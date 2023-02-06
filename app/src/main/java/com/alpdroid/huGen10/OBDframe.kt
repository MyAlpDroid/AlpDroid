package com.alpdroid.huGen10

import com.alpdroid.huGen10.util.clearBitsSlice
import com.alpdroid.huGen10.util.getBit
import com.alpdroid.huGen10.util.getBitsSlice

class OBDframe (canID:Int, frameData:ByteArray) {

    var canID = canID
    var servicePID:Int = frameData[2]+frameData[3]*256
    private var serviceData = frameData.copyOfRange(4,8)
    // dlc is first frame size data
    private var dlc = frameData[0].toInt()-3


    private var datatonum: Long = 0


    // creating a view of bytearray - for bits usage
    init {

        require (serviceData.size <= 4) { "Too many bytes for PID content! Max size is 4" }
        for (i in serviceData.indices) {
            datatonum = datatonum or (serviceData[i].toLong() and 0xFF shl 24-(i*8))
        }

    }



    fun getByte(pos: Int): Byte {
        return this.serviceData[pos]
    }

    fun getValue(pos: Int, len: Int): Int {

        if (pos+len > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        return datatonum.getBitsSlice(32-(pos+len), 31-pos).toInt()
    }

    fun getBit(offset: Int): Boolean {

        if (offset > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        return datatonum.getBit(32 - offset).toInt()==1
    }

    @Synchronized
    fun setBitRange(offset: Int, len: Int, value: Int) {

        val maskinv:Long

        if (offset+len > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        datatonum=datatonum.clearBitsSlice(32-(offset+len),31-offset)

        maskinv = value.toLong() shl (32-offset-len)

        datatonum = datatonum or maskinv

    }


    fun clear()
    {
        datatonum=0
    }

    fun setBytes(pos: Int, value: Byte) {
        this.serviceData[pos] = value

    }



    fun toCanframe(write:Boolean):CanFrame
    {
     val canframe:CanFrame= CanFrame(1,canID,8)

     canframe.dlc=8

     canframe.data[0]=this.dlc.toByte()

     if (write)
            canframe.data[1]=0x2E
     else
            canframe.data[1]=0x22

     canframe.data[2]=(this.servicePID).toByte()
     canframe.data[3]=(this.servicePID/256).toByte()
     canframe.data[4]=(datatonum shr 24).toByte()
     canframe.data[5]=(datatonum shr 16).toByte()
     canframe.data[6]=(datatonum shr 8).toByte()
     canframe.data[7]=(datatonum shr 0).toByte()


     return canframe

    }




}