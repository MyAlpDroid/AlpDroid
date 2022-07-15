package com.alpdroid.huGen10.util;

public final class ShortUtil {
  private ShortUtil() {
  }

  private static void checkSliceIndexes(int startIndex, int stopIndex) {
    if (startIndex < 0 || startIndex >= Short.SIZE) {
      throw new IllegalArgumentException(
          "startIndex parameter must be in the range [0, 15], but it's equal to " + startIndex);
    }

    if ((stopIndex < 0) || (stopIndex < startIndex) || (stopIndex - startIndex) > Short.SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "stopIndex parameter must be in the range [%d, %d], but it's equal to %d",
              startIndex,
              startIndex + Short.SIZE,
              stopIndex));
    }
  }

  private static void checkBitIndex(int index) {
    if (index < 0 || index >= Short.SIZE) {
      throw new IllegalArgumentException(
          String.format(
              "index parameter must be in the range[0, %d], but it's equal to %d",
              Short.SIZE - 1,
              index));
    }
  }

  public static short getBitsSlice(short value, int startIndex, int stopIndex) {
    return (short) ((value & (-1 >>> (Integer.SIZE - stopIndex - 1))) >>> startIndex);
  }

  public static short getBitsSliceSafe(short value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);

    return getBitsSlice(value, startIndex, stopIndex);
  }

  public static short setBitsSlice(short value, int startIndex, int stopIndex) {
    final int length = stopIndex - startIndex + 1;
    return (short) (value | ((-1 >>> (Integer.SIZE - length)) << startIndex));
  }

  public static short setBitsSliceSafe(short value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);

    return setBitsSlice(value, startIndex, stopIndex);
  }

  public static short clearBitsSlice(short value, int startIndex, int stopIndex) {
    final int length = stopIndex - startIndex + 1;
    return (short) (value & ~((-1 >>> (Integer.SIZE - length)) << startIndex));
  }

  public static short clearBitsSliceSafe(short value, int startIndex, int stopIndex) {
    checkSliceIndexes(startIndex, stopIndex);

    return clearBitsSlice(value, startIndex, stopIndex);
  }

  public static short getBit(short value, int index) {
    return (short) ((value >>> index) & 1);
  }

  public static short getBitSafe(short value, int index) {
    checkBitIndex(index);
    return getBit(value, index);
  }

  public static boolean isBitSet(short value, int index) {
    return ((short)(value >>> index) & 1) != 0;
  }

  public static boolean isBitSetSafe(short value, int index) {
    checkBitIndex(index);
    return isBitSet(value, index);
  }

  public static short setBit(short value, int index) {
    return (short) (value | (1 << index));
  }

  public static short setBitSafe(short value, int index) {
    checkBitIndex(index);
    return setBit(value, index);
  }

  public static short clearBit(short value, int index) {
    return (short) (value & (~(1 << index)));
  }

  public static short clearBitSafe(short value, int index) {
    checkBitIndex(index);
    return clearBit(value, index);
  }

  public static int numberOfBytes(short value) {
    if (value < 0) {
      return 2;
    }

    if (value <= 0xFF) {
      return 1;
    }

    return 2;
  }

  public static short clearHighBytes(short value, int numBytesToLeave) {
    switch (numBytesToLeave) {
      default:
      case 0:
        return 0;

        case 1:
        return (short) (value & 0xFF);

      case 2:
        return (short) (value & 0xFFFF);
    }
  }

  public static short clearHighBytesSafe(short value, int numBytesToLeave) {
    if (numBytesToLeave < 0 || numBytesToLeave > Short.BYTES) {
      throw new IllegalArgumentException(
          "numberOfBytesToLeave parameter must be in the range [0, 8], but it's equal to "
              + numBytesToLeave);
    }

    return clearHighBytes(value, numBytesToLeave);
  }

  public static byte getByte(short value, int index) {
    if (index == 0) {
      return (byte) value;
    } else {
      return (byte) (value >>> Byte.SIZE);
    }
  }

  public static byte getByteSafe(short value, int index) {
    if (index < 0 || index >= Short.BYTES) {
      throw new IllegalArgumentException(
          "index parameter must be in the range [0, 1], but it's equal to " + index);
    }

    return getByte(value, index);
  }
}
