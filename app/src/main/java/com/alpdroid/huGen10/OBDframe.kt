package com.alpdroid.huGen10

import com.alpdroid.huGen10.util.clearBitsSlice
import com.alpdroid.huGen10.util.getBit
import com.alpdroid.huGen10.util.getBitsSlice

class OBDframe (var canID:Int, private var multiframetype:Int, private var frameData:ByteArray) {

    private val TAG = OBDframe::class.java.name

    constructor(canID: Int, type:Int, size:Int) : this(canID, type, ByteArray(size))

    private var multiframeLong: Int=0

    private var dlc_Long: Int=0

    // dlc is first frame size data
    private var dlc = frameData[0].toInt()

    private var dlc_offset = 0

    var serviceDir : Int = 0

    var servicePID : Int  = 0

    var serviceData : ByteArray

    private var previousframe:Int=0


    private var datatonum: Long = 0

    // creating a view of bytearray - for bits usage

    init {


        if (multiframetype==0)
        {
            serviceDir = frameData[1].toInt()

            if ((serviceDir != 0x7F) && (serviceDir and 0xBF > 0x21) && (serviceDir and 0xBF<0x2F)) {
                servicePID = (frameData[2].toUByte() * 256u + frameData[3].toUByte()).toInt()
                dlc_offset = 1
            } else {
                servicePID = frameData[2].toInt()
                dlc_offset = 0
                dlc_Long = 0
            }

         }
        else
        {
           if (multiframetype==1)
            {
                serviceDir = frameData[2].toInt()
                servicePID = frameData[3].toInt()
                multiframeLong = (frameData[0].toInt() and 0x0F) shl 8
                multiframeLong += frameData[1].toInt()
                dlc_offset = 1
                dlc_Long = multiframeLong-7
            }
        }


        serviceData = frameData.copyOfRange(3+dlc_offset,8)

        require (serviceData.size <= 5) { "Too many bytes for PID content! Max size is 5" }
        for (i in serviceData.indices) {
            datatonum = datatonum or (serviceData[i].toLong() and 0xFF shl (32-dlc_offset*8)-(i*8))
        }


    }

    fun addOBDdata(data2add:ByteArray, framenumber:Int):Boolean
    {
        if (data2add.size>8)
            return false//todo : throw exception

        if (framenumber!=previousframe+1)
            return false

        previousframe=framenumber

        dlc_Long-=7


        if(dlc_Long>=0) {
            serviceData += data2add.copyOfRange(0, data2add.size)
        }
        else
            if (dlc_Long>=-6) {
                serviceData += data2add.copyOfRange(0, dlc_Long + 8)

            }

        return true
    }

    fun getByte(pos: Int): Byte {
        return this.serviceData[pos]
    }

    fun getValue(pos: Int, len: Int): Int {

        if (pos+len > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        return datatonum.getBitsSlice((40-dlc_offset*8)-(pos+len), (39-dlc_offset*8)-pos).toInt()
    }

    fun getBit(offset: Int): Boolean {

        if (offset > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        return this.datatonum.getBit(40-(dlc_offset*8) - offset).toInt()==1
    }

    @Synchronized
    fun setBitRange(offset: Int, len: Int, value: Int) {

        val maskinv:Long

        if (offset+len > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        datatonum=datatonum.clearBitsSlice((40-dlc_offset*8)-(offset+len),(39-dlc_offset*8)-offset)

        (value.toLong() shl ((40-dlc_offset*8)-offset-len)).also { maskinv = it }

        datatonum = datatonum or maskinv

    }


    fun clear()
    {
        datatonum=0
    }

    fun setBytes(pos: Int, value: Byte) {
        this.serviceData[pos] = value

    }



    fun toCanframe():CanFrame
    {
     val canframe = CanFrame(1,canID,8)


     canframe.dlc=8

     canframe.data[0]=(this.dlc+2+dlc_offset).toByte()

     canframe.data[1]=serviceDir.toByte()

     if (dlc_offset==1) {
         canframe.data[2] = (this.servicePID / 256).toByte()
         canframe.data[3] = (this.servicePID).toByte()
         canframe.data[4] = (this.datatonum shr 24).toByte()
         canframe.data[5] = (this.datatonum shr 16).toByte()
         canframe.data[6] = (this.datatonum shr 8).toByte()
         canframe.data[7] = (this.datatonum shr 0).toByte()
     }
        else
     {
         canframe.data[2] = (this.servicePID ).toByte()
         canframe.data[3] = (this.datatonum shr 32).toByte()
         canframe.data[4] = (this.datatonum shr 24).toByte()
         canframe.data[5] = (this.datatonum shr 16).toByte()
         canframe.data[6] = (this.datatonum shr 8).toByte()
         canframe.data[7] = (this.datatonum shr 0).toByte()
     }

     return canframe

    }


    override fun toString():String
    {

        var dataFrame: String


        if (multiframetype==0)
            dataFrame = String.format("%04X%1d%02X", this.canID, this.dlc, this.serviceDir)
        else
            dataFrame = String.format("%04X%02X%02X", this.canID, this.multiframeLong, this.serviceDir)


        if (dlc_offset==1)
            dataFrame = String.format("%s%02X", dataFrame, (servicePID/256).toByte())

        dataFrame = String.format("%s%02X", dataFrame, servicePID.toByte())

        if (multiframetype==0) {
            for (i in 0..(dlc - 3 - dlc_offset))
                frameData[i] = (datatonum shr (32 - (dlc_offset * 8) - i * 8)).toByte()

            for (i in 0..(4 - dlc_offset)) {
                if (i < this.dlc - 2 - dlc_offset)
                    dataFrame = String.format("%s%02X", dataFrame, frameData[i])
                else
                    dataFrame = String.format("%s%02X", dataFrame, 0x55)
            }
        }
        else
        {
            for (i in 0 until serviceData.size) {
                    dataFrame = String.format("%s%02X", dataFrame, serviceData[i])

                 }

        }

        return dataFrame

    }


}