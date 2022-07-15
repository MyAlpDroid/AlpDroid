package com.alpdroid.huGen10.util;

  public final class LongUtil {
  private LongUtil() {
  }

  private static void checkSliceIndexes(int startIndex, int stopIndex) {
    if (startIndex < 0 || startIndex >= Long.SIZE) {
      throw new IllegalArgumentException(
          "startIndex parameter must be in the range [0, 63], but it's equal to " + startIndex);
    }

    if ((stopIndex < 0) || (stopIndex < startIndex) || (stopIndex - startIndex) > Long.SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "stopIndex parameter must be in the range [%d, %d], but it's equal to %d",
              startIndex,
              startIndex + Long.SIZE,
              stopIndex));
    }
  }

  private static void checkBitIndex(int index) {
    if (index < 0 || index >= Long.SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "index parameter must be in the range[0, %d], but it's equal to %d",
              Long.SIZE - 1,
              index));
    }
  }

  public static long getBitsSlice(long value, int startIndex, int stopIndex) {
    return (value & (-1L >>> (Long.SIZE - stopIndex - 1))) >>> startIndex;
  }

  public static long getBitsSliceSafe(long value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);
    return getBitsSlice(value, startIndex, stopIndex);
  }

  public static long setBitsSlice(long value, int startIndex, int stopIndex) {
    final int length = stopIndex - startIndex + 1;
    return value | ((-1L >>> (Long.SIZE - length)) << startIndex);
  }

  public static long setBitsSliceSafe(long value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);
    return setBitsSlice(value, startIndex, stopIndex);
  }

  public static long clearBitsSlice(long value, int startIndex, int stopIndex) {
    final int length = stopIndex - startIndex + 1;
    return value & ~((-1L >>> (Long.SIZE - length)) << startIndex);
  }

  public static long clearBitsSliceSafe(long value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);
    return clearBitsSlice(value, startIndex, stopIndex);
  }

  public static long getBit(long value, int index) {
    return (value >>> index) & 1;
  }

  public static long getBitSafe(long value, int index) {
    checkBitIndex(index);
    return getBit(value, index);
  }

  public static boolean isBitSet(long value, int index) {
    return ((value >>> index) & 1) != 0;
  }

  public static boolean isBitSetSafe(long value, int index) {
    checkBitIndex(index);
    return isBitSet(value, index);
  }

  public static long setBit(long value, int index) {
    return value | (1L << index);
  }

  public static long setBitSafe(long value, int index) {
    checkBitIndex(index);
    return setBit(value, index);
  }

  public static long clearBit(long value, int index) {
    return value & (~(1L << index));
  }

  public static long clearBitSafe(long value, int index) {
    checkBitIndex(index);
    return clearBit(value, index);
  }

  public static int numberOfBytes(long value) {
    int numZeros = Long.numberOfLeadingZeros(value);
    switch (numZeros) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        return Long.BYTES;

      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
        return Long.BYTES - 1;

      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
        return Long.BYTES - 2;

      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
        return Long.BYTES - 3;

      case 32:
      case 33:
      case 34:
      case 35:
      case 36:
      case 37:
      case 38:
      case 39:
        return Long.BYTES - 4;

      case 40:
      case 41:
      case 42:
      case 43:
      case 44:
      case 45:
      case 46:
      case 47:
        return Long.BYTES - 5;

      case 48:
      case 49:
      case 50:
      case 51:
      case 52:
      case 53:
      case 54:
      case 55:
        return Long.BYTES - 6;

//                case 56:
//                case 57:
//                case 58:
//                case 59:
//                case 60:
//                case 61:
//                case 62:
//                case 63:
//                case 64:
      default:
        return 1; // Long.BYTES - 7
    }
  }

  public static byte getByte(long value, int index) {
     return (byte)(value >>> Byte.SIZE * index);
  }

  public static byte getByteSafe(long value, int index) {
    if (index < 0 || index >= Long.BYTES) {
      throw new IllegalArgumentException(
          "index parameter must be in the range [0, 7], but it's equal to " + index);
    }

    return getByte(value, index);
  }

  public static long clearHighBytes(long value, int numBytesToLeave) {

    switch (numBytesToLeave) {
      default:
      case 0:
        return 0L;

      case 1:
        return value & 0xFFL;

      case 2:
        return value & 0xFFFFL;


      case 3:
        return value & 0xFFFFFFL;

      case 4:
        return value & 0xFFFFFFFFL;

      case 5:
        return value & 0xFFFFFFFFFFL;

      case 6:
        return value & 0xFFFFFFFFFFFFL;

      case 7:
        return value & 0xFFFFFFFFFFFFFFL;

      case 8:
        return value;
    }
  }

  public static long clearHighBytesSafe(long value, int numBytesToLeave) {
    if (numBytesToLeave < 0 || numBytesToLeave > Long.BYTES) {
      throw new IllegalArgumentException(
          "numberOfBytesToLeave parameter must be in the range [0, 8], but it's equal to "
              + numBytesToLeave);
    }

    return clearHighBytes(value, numBytesToLeave);
  }
}

