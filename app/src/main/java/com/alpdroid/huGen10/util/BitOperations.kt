package com.alpdroid.huGen10.util

// Int extensions functions

fun Int.getBitsSlice(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.getBitsSlice(this, startIndex, stopIndex)

fun Int.getBitsSliceSafe(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.getBitsSliceSafe(this, startIndex, stopIndex)

fun Int.setBitsSlice(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.setBitsSlice(this, startIndex, stopIndex)

fun Int.setBitsSliceSafe(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.setBitsSliceSafe(this, startIndex, stopIndex)

fun Int.clearBitsSlice(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.clearBitsSlice(this, startIndex, stopIndex)

fun Int.clearBitsSliceSafe(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.clearBitsSliceSafe(this, startIndex, stopIndex)

fun Int.getBit(index: Int): Int = IntegerUtil.getBit(this, index)

fun Int.getBitSafe(index: Int): Int = IntegerUtil.getBitSafe(this, index)

fun Int.setBit(index: Int): Int = IntegerUtil.setBit(this, index)

fun Int.setBitSafe(index: Int): Int = IntegerUtil.setBitSafe(this, index)

fun Int.clearBit(index: Int): Int = IntegerUtil.clearBit(this, index)

fun Int.clearBitSafe(index: Int): Int = IntegerUtil.clearBitSafe(this, index)

fun Int.isBitSet(index: Int): Boolean = IntegerUtil.isBitSet(this, index)

fun Int.isBitSetSafe(index: Int): Boolean = IntegerUtil.isBitSetSafe(this, index)

fun Int.numberOfBytes(): Int = IntegerUtil.numberOfBytes(this)

fun Int.clearHighBytes(numBytesToLeave: Int): Int =
        IntegerUtil.clearHighBytes(this, numBytesToLeave)

fun Int.clearHighBytesSafe(numBytesToLeave: Int): Int =
        IntegerUtil.clearHighBytesSafe(this, numBytesToLeave)

fun Int.getByte(index: Int): Byte = IntegerUtil.getByte(this, index)

fun Int.getByteSafe(index: Int): Byte = IntegerUtil.getByteSafe(this, index)

// Int overloaded operators

operator fun Int.get(startIndex: Int, stopIndex: Int): Int =
        IntegerUtil.getBitsSlice(this, startIndex, stopIndex)

operator fun Int.get(index: Int): Int = IntegerUtil.getBit(this, index)

// Long extension functions

fun Long.getBitsSlice(startIndex: Int, stopIndex: Int): Long =
        LongUtil.getBitsSlice(this, startIndex, stopIndex)

fun Long.getBitsSliceSafe(startIndex: Int, stopIndex: Int): Long =
        LongUtil.getBitsSliceSafe(this, startIndex, stopIndex)

fun Long.setBitsSlice(startIndex: Int, stopIndex: Int): Long =
        LongUtil.setBitsSlice(this, startIndex, stopIndex)

fun Long.setBitsSliceSafe(startIndex: Int, stopIndex: Int): Long =
        LongUtil.setBitsSliceSafe(this, startIndex, stopIndex)

fun Long.clearBitsSlice(startIndex: Int, stopIndex: Int): Long =
        LongUtil.clearBitsSlice(this, startIndex, stopIndex)

fun Long.clearBitsSliceSafe(startIndex: Int, stopIndex: Int): Long =
        LongUtil.clearBitsSliceSafe(this, startIndex, stopIndex)

fun Long.getBit(index: Int): Long = LongUtil.getBit(this, index)

fun Long.getBitSafe(index: Int): Long = LongUtil.getBitSafe(this, index)

fun Long.setBit(index: Int): Long = LongUtil.setBit(this, index)

fun Long.setBitSafe(index: Int): Long = LongUtil.setBitSafe(this, index)

fun Long.clearBit(index: Int): Long = LongUtil.clearBit(this, index)

fun Long.clearBitSafe(index: Int): Long = LongUtil.clearBitSafe(this, index)

fun Long.isBitSet(index: Int): Boolean = LongUtil.isBitSet(this, index)

fun Long.isBitSetSafe(index: Int): Boolean = LongUtil.isBitSetSafe(this, index)

fun Long.numberOfBytes(): Int = LongUtil.numberOfBytes(this)

fun Long.clearHighBytes(numBytesToLeave: Int): Long =
        LongUtil.clearHighBytes(this, numBytesToLeave)

fun Long.clearHighBytesSafe(numBytesToLeave: Int): Long =
        LongUtil.clearHighBytesSafe(this, numBytesToLeave)

fun Long.getByte(index: Int): Byte = LongUtil.getByte(this, index)

fun Long.getByteSafe(index: Int): Byte = LongUtil.getByteSafe(this, index)

// Long overloaded operators

operator fun Long.get(startIndex: Int, stopIndex: Int): Long =
        LongUtil.getBitsSlice(this, startIndex, stopIndex)

operator fun Long.get(index: Int): Long = LongUtil.getBit(this, index)

// Short extension functions

fun Short.getBitsSlice(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.getBitsSlice(this, startIndex, stopIndex)

fun Short.getBitsSliceSafe(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.getBitsSliceSafe(this, startIndex, stopIndex)

fun Short.setBitsSlice(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.setBitsSlice(this, startIndex, stopIndex)

fun Short.setBitsSliceSafe(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.setBitsSliceSafe(this, startIndex, stopIndex)

fun Short.clearBitsSlice(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.clearBitsSlice(this, startIndex, stopIndex)

fun Short.clearBitsSliceSafe(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.clearBitsSliceSafe(this, startIndex, stopIndex)

fun Short.getBit(index: Int): Short = ShortUtil.getBit(this, index)

fun Short.getBitSafe(index: Int): Short = ShortUtil.getBitSafe(this, index)

fun Short.setBit(index: Int): Short = ShortUtil.setBit(this, index)

fun Short.setBitSafe(index: Int): Short = ShortUtil.setBitSafe(this, index)

fun Short.clearBit(index: Int): Short = ShortUtil.clearBit(this, index)

fun Short.clearBitSafe(index: Int): Short = ShortUtil.clearBitSafe(this, index)

fun Short.isBitSet(index: Int): Boolean = ShortUtil.isBitSet(this, index)

fun Short.isBitSetSafe(index: Int): Boolean = ShortUtil.isBitSetSafe(this, index)

fun Short.numberOfBytes(): Int = ShortUtil.numberOfBytes(this)

fun Short.clearHighBytes(numBytesToLeave: Int): Short =
        ShortUtil.clearHighBytes(this, numBytesToLeave)

fun Short.clearHighBytesSafe(numBytesToLeave: Int): Short =
        ShortUtil.clearHighBytesSafe(this, numBytesToLeave)

fun Short.getByte(index: Int): Byte = ShortUtil.getByte(this, index)

fun Short.getByteSafe(index: Int): Byte = ShortUtil.getByteSafe(this, index)

// Short overloaded operators

operator fun Short.get(startIndex: Int, stopIndex: Int): Short =
        ShortUtil.getBitsSlice(this, startIndex, stopIndex)

operator fun Short.get(index: Int): Short = ShortUtil.getBit(this, index)
