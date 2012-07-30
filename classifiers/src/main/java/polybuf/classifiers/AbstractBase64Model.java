/*
 * Copyright (c) 2012 Michael Groble
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package polybuf.classifiers;

abstract class AbstractBase64Model implements ByteArrayModel {

  static final float log2ProbEncodedByte = -6; // log2(1/64)

  private static boolean isValidLength(int length) {
    if (length < 4 || length % 4 != 0) {
      return false;
    }
    return true;
  }

  @Override
  public float log2Prob(byte[] bytes) {
    return log2Prob(bytes, 0, bytes.length);
  }

  @Override
  public float log2Prob(byte[] bytes, int offset, int length) {
    if (!isValidLength(length)) {
      return Float.NEGATIVE_INFINITY;
    }

    float result = 0;
    for (int i = 0; i < bytes.length - 4; i += 4) {
      result += log2Prob(bytes, i + offset);
      if (Float.isInfinite(result)) {
        break;
      }
    }
    result += log2ProbEnding(bytes, offset, length);
    return result;
  }

  protected abstract float log2FirstByte(byte b);

  protected abstract float log2SecondByte(byte b);

  protected abstract float log2ThirdByte(byte b);

  protected abstract float log2FourthByte(byte b);

  protected float log2Prob(byte[] bytes, int i) {
    return log2FirstByte(bytes[i]) + log2SecondByte(bytes[i + 1]) + log2ThirdByte(bytes[i + 2])
        + log2FourthByte(bytes[i + 3]);
  }

  protected float log2AnyByte(byte b) {
    int i = Base64Characters.index(b);
    if (i < 0) {
      return Float.NEGATIVE_INFINITY;
    }
    return log2ProbEncodedByte;
  }

  protected float log2ProbEnding(byte[] bytes, int offset, int length) {
    int i = offset + length - 4;

    float result = log2FirstByte(bytes[i]);

    if (Base64Characters.isPad(bytes[i + 2])) {
      if (!Base64Characters.isPad(bytes[i + 3])) {
        return Float.NEGATIVE_INFINITY;
      }
      result += log2ProbBeforeDoublePad(bytes[i + 1]);
    }
    else {
      result += log2SecondByte(bytes[i + 1]);
      if (Base64Characters.isPad(bytes[i + 3])) {
        result += log2ProbBeforeSinglePad(bytes[i + 2]);
      }
      else {
        result += log2ThirdByte(bytes[i + 2]);
        result += log2FourthByte(bytes[i + 3]);
      }
    }
    return result;
  }

  protected float log2ProbBeforeSinglePad(byte b) {
    // A single pad is used when we don't have a third byte
    // so the previous base 64 must low-order 0 bits

    // /byte 1\/byte 2\/byte 3\
    // 876543218765432187654321
    //                 00000000
    // 654321654321654321654321
    // \64-1/\64-2/\64-3/\64-4/
    //  any   any    ?     =
    int index = Base64Characters.index(b);
    if (index < 0) {
      return Float.NEGATIVE_INFINITY;
    }

    // only 4 bits with unrestricted values
    return (0x03 & index) == 0 ? -4 : Float.NEGATIVE_INFINITY;
  }

  protected float log2ProbBeforeDoublePad(byte b) {
    // A double pad is used when we have only 1 byte to encode

    // /byte 1\/byte 2\/byte 3\
    // 876543218765432187654321
    //         0000000000000000
    // 654321654321654321654321
    // \64-1/\64-2/\64-3/\64-4/
    //  any    ?     =     =
    int index = Base64Characters.index(b);
    if (index < 0) {
      return Float.NEGATIVE_INFINITY;
    }

    // only 2 bits with unrestricted values
    return (0x0f & index) == 0 ? -2 : Float.NEGATIVE_INFINITY;
  }
}