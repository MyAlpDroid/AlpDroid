package com.alpdroid.huGen10


import android.util.Log
import androidx.work.workDataOf
import com.alpdroid.huGen10.util.clearBitsSlice
import com.alpdroid.huGen10.util.getBit
import com.alpdroid.huGen10.util.getBitsSlice
import com.alpdroid.huGen10.util.setBitsSlice
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or


class CanFrame (var bus: Int, var id: Int, var data: ByteArray) {

    constructor(bus: Int, id: Int, size: Int) : this(bus, id, ByteArray(size))

    private var datatonum: Long = 0


    // creating a view of bytearray - for bits usage
    init {

        require (data.size <= 8) { "Too many bytes for frame content! Max size is 8" }
        for (i in data.indices) {
            datatonum = datatonum or (data[i].toLong() and 0xFF shl 56-(i*8))
        }

    }


    var dlc = data.size


    fun getByte(pos: Int): Byte {
        return this.data[pos]
    }

    fun getValue(pos: Int, len: Int): Int {

        if (pos+len > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        return datatonum.getBitsSlice(64-(pos+len), 63-pos).toInt()
    }

    fun getBit(offset: Int): Boolean {

        if (offset > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        return datatonum.getBit(64 - offset).toInt()==1
    }

    @Synchronized
    fun setBitRange(offset: Int, len: Int, value: Int) {

        val maskinv:Long

        if (offset+len > dlc*8) {
            throw IndexOutOfBoundsException("Offset+Len > ${dlc*8}")
        }

        datatonum=datatonum.clearBitsSlice(64-(offset+len),63-offset)

        maskinv = value.toLong() shl (64-offset-len)

       datatonum = datatonum or maskinv

    }


    fun clear()
    {
        datatonum=0
    }

    fun setBytes(pos: Int, value: Byte) {
        this.data[pos] = value

    }



    override fun toString(): String {

        var dataFrame: String


        dataFrame = String.format("%1d%04X%1d", this.bus, this.id, this.dlc)

        this.data[0]=(datatonum shr 56).toByte()
        this.data[1]=(datatonum shr 48).toByte()
        this.data[2]=(datatonum shr 40).toByte()
        this.data[3]=(datatonum shr 32).toByte()
        this.data[4]=(datatonum shr 24).toByte()
        this.data[5]=(datatonum shr 16).toByte()
        this.data[6]=(datatonum shr 8).toByte()
        this.data[7]=(datatonum shr 0).toByte()


        for (i in 0..7) {
            dataFrame = String.format("%s%02X", dataFrame, this.data[i])
        }

        return dataFrame

    }

    fun toByteArray(): ByteArray {

        val arrayByte = ByteArray(12)

        arrayByte[0] = this.bus.toByte()
        arrayByte[1] = (this.id).toByte()
        arrayByte[2] = (this.id / 256).toByte()
        arrayByte[3] = this.dlc.toByte()

        data[0]=(datatonum shr 56).toByte()
        data[1]=(datatonum shr 48).toByte()
        data[2]=(datatonum shr 40).toByte()
        data[3]=(datatonum shr 32).toByte()
        data[4]=(datatonum shr 24).toByte()
        data[5]=(datatonum shr 16).toByte()
        data[6]=(datatonum shr 8).toByte()
        data[7]=(datatonum shr 0).toByte()


        this.data.copyInto(arrayByte, 4, 0, 8)

        return arrayByte


    }

}


