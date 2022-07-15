package com.alpdroid.huGen10.util;

public final class BitUtil {

  private BitUtil() {
  }

  public static int getBitsSlice(int value, int startIndex, int stopIndex) {
    return IntegerUtil.getBitsSlice(value, startIndex, stopIndex);
  }

  public static int getBitsSliceSafe(int value, int startIndex, int stopIndex) {
    return IntegerUtil.getBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static long getBitsSlice(long value, int startIndex, int stopIndex) {
    return LongUtil.getBitsSlice(value, startIndex, stopIndex);
  }

  public static long getBitsSliceSafe(long value, int startIndex, int stopIndex) {
    return LongUtil.getBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static short getBitsSlice(short value, int startIndex, int stopIndex) {
    return ShortUtil.getBitsSlice(value, startIndex, stopIndex);
  }

  public static short getBitsSliceSafe(short value, int startIndex, int stopIndex) {
    return ShortUtil.getBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static int setBitsSlice(int value, int startIndex, int stopIndex) {
    return IntegerUtil.setBitsSlice(value, startIndex, stopIndex);
  }

  public static int setBitsSliceSafe(int value, int startIndex, int stopIndex) {
    return IntegerUtil.setBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static long setBitsSlice(long value, int startIndex, int stopIndex) {
    return LongUtil.setBitsSlice(value, startIndex, stopIndex);
  }

  public static long setBitsSliceSafe(long value, int startIndex, int stopIndex) {
    return LongUtil.setBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static short setBitsSlice(short value, int startIndex, int stopIndex) {
    return ShortUtil.setBitsSlice(value, startIndex, stopIndex);
  }

  public static short setBitsSliceSafe(short value, int startIndex, int stopIndex) {
    return ShortUtil.setBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static int clearBitsSlice(int value, int startIndex, int stopIndex) {
    return IntegerUtil.clearBitsSlice(value, startIndex, stopIndex);
  }

  public static int clearBitsSliceSafe(int value, int startIndex, int stopIndex) {
    return IntegerUtil.clearBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static long clearBitsSlice(long value, int startIndex, int stopIndex) {
    return LongUtil.clearBitsSlice(value, startIndex, stopIndex);
  }

  public static long clearBitsSliceSafe(long value, int startIndex, int stopIndex) {
    return LongUtil.clearBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static short clearBitsSlice(short value, int startIndex, int stopIndex) {
    return ShortUtil.clearBitsSlice(value, startIndex, stopIndex);
  }

  public static short clearBitsSliceSafe(short value, int startIndex, int stopIndex) {
    return ShortUtil.clearBitsSliceSafe(value, startIndex, stopIndex);
  }

  public static int getBit(int value, int index) {
    return IntegerUtil.getBit(value, index);
  }

  public static int getBitSafe(int value, int index) {
    return IntegerUtil.getBitSafe(value, index);
  }

  public static long getBit(long value, int index) {
    return LongUtil.getBit(value, index);
  }

  public static long getBitSafe(long value, int index) {
    return LongUtil.getBitSafe(value, index);
  }

  public static short getBit(short value, int index) {
    return ShortUtil.getBit(value, index);
  }

  public static short getBitSafe(short value, int index) {
    return ShortUtil.getBitSafe(value, index);
  }

  public static boolean isBitSet(int value, int index) {
    return IntegerUtil.isBitSet(value, index);
  }

  public static boolean isBitSetSafe(int value, int index) {
    return IntegerUtil.isBitSetSafe(value, index);
  }

  public static boolean isBitSet(long value, int index) {
    return LongUtil.isBitSet(value, index);
  }

  public static boolean isBitSetSafe(long value, int index) {
    return LongUtil.isBitSetSafe(value, index);
  }

  public static boolean isBitSet(short value, int index) {
    return ShortUtil.isBitSet(value, index);
  }

  public static boolean isBitSetSafe(short value, int index) {
    return ShortUtil.isBitSetSafe(value, index);
  }

  public static int setBit(int value, int index) {
    return IntegerUtil.setBit(value, index);
  }

  public static int setBitSafe(int value, int index) {
    return IntegerUtil.setBitSafe(value, index);
  }

  public static long setBit(long value, int index) {
    return LongUtil.setBit(value, index);
  }

  public static long setBitSafe(long value, int index) {
    return LongUtil.setBitSafe(value, index);
  }

  public static int setBit(short value, int index) {
    return ShortUtil.setBit(value, index);
  }

  public static int setBitSafe(short value, int index) {
    return ShortUtil.setBitSafe(value, index);
  }

  public static int clearBit(int value, int index) {
    return IntegerUtil.clearBit(value, index);
  }

  public static int clearBitSafe(int value, int index) {
    return IntegerUtil.clearBitSafe(value, index);
  }

  public static long clearBit(long value, int index) {
    return LongUtil.clearBit(value, index);
  }

  public static long clearBitSafe(long value, int index) {
    return LongUtil.clearBitSafe(value, index);
  }

  public static short clearBit(short value, int index) {
    return ShortUtil.clearBit(value, index);
  }

  public static short clearBitSafe(short value, int index) {
    return ShortUtil.clearBitSafe(value, index);
  }

  public static int clearHighBytes(int value, int numBytesToLeave) {
    return IntegerUtil.clearHighBytes(value, numBytesToLeave);
  }

  public static int clearHighBytesSafe(int value, int numBytesToLeave) {
    return IntegerUtil.clearHighBytesSafe(value, numBytesToLeave);
  }

  public static long clearHighBytes(long value, int numBytesToLeave) {
    return LongUtil.clearHighBytes(value, numBytesToLeave);
  }

  public static long clearHighBytesSafe(long value, int numBytesToLeave) {
    return LongUtil.clearHighBytesSafe(value, numBytesToLeave);
  }

  public static short clearHighBytes(short value, int numBytesToLeave) {
    return ShortUtil.clearHighBytes(value, numBytesToLeave);
  }

  public static short clearHighBytesSafe(short value, int numBytesToLeave) {
    return ShortUtil.clearHighBytesSafe(value, numBytesToLeave);
  }

  public static int numberOfBytes(int value) {
    return IntegerUtil.numberOfBytes(value);
  }

  public static int numberOfBytes(long value) {
    return LongUtil.numberOfBytes(value);
  }

  public static int numberOfBytes(short value) {
    return ShortUtil.numberOfBytes(value);
  }

  public static byte getByte(int value, int index) {
    return IntegerUtil.getByte(value, index);
  }

  public static byte getByteSafe(int value, int index) {
    return IntegerUtil.getByteSafe(value, index);
  }

  public static byte getByte(long value, int index) {
    return LongUtil.getByte(value, index);
  }

  public static byte getByteSafe(long value, int index) {
    return LongUtil.getByteSafe(value, index);
  }

  public static byte getByte(short value, int index) {
    return ShortUtil.getByte(value, index);
  }

  public static byte getByteSafe(short value, int index) {
    return ShortUtil.getByteSafe(value, index);
  }
}
