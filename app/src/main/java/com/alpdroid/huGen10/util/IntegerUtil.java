package com.alpdroid.huGen10.util;

public final class IntegerUtil {
  private IntegerUtil() {
  }

  private static void checkSliceIndexes(int startIndex, int stopIndex) {
    if (startIndex < 0 || startIndex >= Integer.SIZE) {
      throw new IllegalArgumentException(
          "startIndex parameter must be in the range [0, 31], but it's equal to " + startIndex);
    }

    if ((stopIndex < 0) || (stopIndex < startIndex) || (stopIndex - startIndex) > Integer.SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "stopIndex parameter must be in the range [%d, %d], but it's equal to %d",
              startIndex,
              startIndex + Integer.SIZE,
              stopIndex));
    }
  }

  private static void checkBitIndex(int index) {
    if (index < 0 || index >= Integer.SIZE) {
      throw new IllegalArgumentException(
          "index parameter must be in the range[0, Integer.SIZE - 1], but it's equal to " + index);
    }
  }

  public static int getBitsSlice(int value, int startIndex, int stopIndex) {
    return (value & (-1 >>> (Integer.SIZE - stopIndex - 1))) >>> startIndex;
  }

  public static int getBitsSliceSafe(int value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);
    return getBitsSlice(value, startIndex, stopIndex);
  }

  public static int setBitsSlice(int value, int startIndex, int stopIndex) {
    final int length = stopIndex - startIndex + 1;
    return value | ((-1 >>> (Integer.SIZE - length)) << startIndex);
  }

  public static int setBitsSliceSafe(int value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);

    return setBitsSlice(value, startIndex, stopIndex);
  }

  public static int clearBitsSlice(int value, int startIndex, int stopIndex) {
    final int length = stopIndex - startIndex + 1;
    return value & ~((-1 >>> (Integer.SIZE - length)) << startIndex);
  }

  public static int clearBitsSliceSafe(int value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);
    return clearBitsSlice(value, startIndex, stopIndex);
  }

  public static int getBit(int value, int index) {
    return (value >>> index) & 1;
  }

  public static int getBitSafe(int value, int index) {
    checkBitIndex(index);
    return getBit(value, index);
  }

  public static boolean isBitSet(int value, int index) {
    return ((value >>> index) & 1) != 0;
  }

  public static boolean isBitSetSafe(int value, int index) {
    checkBitIndex(index);
    return isBitSet(value, index);
  }

  public static int setBit(int value, int index) {
    return value | (1 << index);
  }

  public static int setBitSafe(int value, int index) {
    checkBitIndex(index);
    return setBit(value, index);
  }

  public static int clearBit(int value, int index) {
    return value & (~(1 << index));
  }

  public static int clearBitSafe(int value, int index) {
    checkBitIndex(index);
    return clearBit(value, index);
  }

  public static int numberOfBytes(int value) {
    int highestBit = Integer.highestOneBit(value);
    switch (highestBit) {
      case 0x80000000:
      case 0x40000000:
      case 0x20000000:
      case 0x10000000:
      case 0x8000000:
      case 0x4000000:
      case 0x2000000:
      case 0x1000000:
        return 4;

      case 0x800000:
      case 0x400000:
      case 0x200000:
      case 0x100000:
      case 0x80000:
      case 0x40000:
      case 0x20000:
      case 0x10000:
        return 3;

      case 0x8000:
      case 0x4000:
      case 0x2000:
      case 0x1000:
      case 0x800:
      case 0x400:
      case 0x200:
      case 0x100:
        return 2;

//                case 0x80:
//                case 0x40:
//                case 0x20:
//                case 0x10:
//                case 0x8:
//                case 0x4:
//                case 0x2:
//                case 0x1:
//                case 0x0:
      default:
        return 1;
    }
  }

  public static int clearHighBytes(int value, int numBytesToLeave) {
    switch (numBytesToLeave) {
      default:
      case 0:
        return 0;

        case 1:
        return value & 0xFF;

      case 2:
        return value & 0xFFFF;


      case 3:
        return value & 0xFFFFFF;

      case 4:
        return value & 0xFFFFFFFF;
    }
  }

  public static int clearHighBytesSafe(int value, int numBytesToLeave) {
    if (numBytesToLeave < 0 || numBytesToLeave > Integer.BYTES) {
      throw new IllegalArgumentException(
          "numberOfBytesToLeave parameter must be in the range [0, 4], but it's equal to "
              + numBytesToLeave);
    }

    return clearHighBytes(value, numBytesToLeave);
  }

  public static byte getByte(int value, int index) {
    return (byte)(value >>> Byte.SIZE * index);
  }

  public static byte getByteSafe(int value, int index) {
    if (index < 0 || index >= Integer.BYTES) {
      throw new IllegalArgumentException(
          "index parameter must be in the range [0, 3], but it's equal to " + index);
    }

    return getByte(value, index);
  }
}
