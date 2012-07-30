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

class EncodedAsciiModel extends AbstractBase64Model implements ByteArrayModel {

  private static final float log2ProbEncodedAscii = -5; // in each slot, half of the choices are invalid, so log2(1/32)

  // Three bytes get mapped to four base64 bytes as follows.
  // When ASCII is encoded, the top bit of each byte is 0, meaning
  // the following base 64 bits must also be 0

  // /byte 1\/byte 2\/byte 3\
  // 876543218765432187654321
  // 0       0       0
  // 654321654321654321654321
  // \64-1/\64-2/\64-3/\64-4/
  @Override
  protected float log2FirstByte(byte b) {
    int i = Base64Characters.index(b);
    if (i < 0) {
      return Float.NEGATIVE_INFINITY;
    }
    return ((0x20 & i) == 0 ? log2ProbEncodedAscii : Float.NEGATIVE_INFINITY);
  }

  @Override
  protected float log2SecondByte(byte b) {
    int i = Base64Characters.index(b);
    if (i < 0) {
      return Float.NEGATIVE_INFINITY;
    }
    return ((0x08 & i) == 0 ? log2ProbEncodedAscii : Float.NEGATIVE_INFINITY);
  }

  @Override
  protected float log2ThirdByte(byte b) {
    int i = Base64Characters.index(b);
    if (i < 0) {
      return Float.NEGATIVE_INFINITY;
    }
    return ((0x02 & i) == 0 ? log2ProbEncodedAscii : Float.NEGATIVE_INFINITY);
  }

  @Override
  protected float log2FourthByte(byte b) {
    return log2AnyByte(b);
  }
}