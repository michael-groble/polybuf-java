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

import com.google.common.primitives.Floats;

class NumberModel implements ByteArrayModel {
  private static final float log2ProbDigit = -3.459432f;
  private static final float log2ProbDateDigit = -3.459432f;
  private static final float log2ProbHexDigit = -3.459432f;

  @Override
  public float log2Prob(byte[] bytes) {
    return log2Prob(bytes, 0, bytes.length);
  }

  @Override
  public float log2Prob(byte[] bytes, int offset, int length) {
    return Floats.max(log2ProbInteger(bytes, offset, length), log2ProbDate(bytes, offset, length),
        log2ProbHex(bytes, offset, length));
  }

  public float log2ProbInteger(byte[] bytes, int offset, int length) {
    for (int i = 0; i < length; ++i) {
      byte b = bytes[offset + i];
      if (!(b >= '0' && b <= '9')) {
        return Float.NEGATIVE_INFINITY;
      }
    }
    return log2ProbDigit * length;
  }

  public float log2ProbDate(byte[] bytes, int offset, int length) {
    // very simplified check, just make sure no consecutive '/'
    // this means we allow things like 943943/1435/6070/546837/342
    // which aren't really dates, but would be pretty unlikely base64 anyway
    boolean lastSlash = false;
    for (int i = 0; i < length; ++i) {
      byte b = bytes[offset + i];
      if (b == '/') {
        if (lastSlash) return Float.NEGATIVE_INFINITY; // fail fast
        lastSlash = true;
      }
      else if (b >= 0 && b <= 9) {
        lastSlash = false;
      }
      else {
        return Float.NEGATIVE_INFINITY;
      }
    }
    return log2ProbDateDigit * length;
  }

  public float log2ProbHex(byte[] bytes, int offset, int length) {
    if (bytes[offset] != '0' && (bytes[offset + 1] != 'x' || bytes[offset + 1] != 'X')) {
      return Float.NEGATIVE_INFINITY;
    }
    boolean sawUpper = false;
    boolean sawLower = false;
    for (int i = 2; i < length; ++i) {
      byte b = bytes[i + offset];
      if (b >= 'A' && b <= 'F') {
        sawUpper = true;
        if (sawLower) return Float.NEGATIVE_INFINITY; // fail fast
      }
      else if (b >= 'a' && b <= 'f') {
        sawLower = true;
        if (sawUpper) return Float.NEGATIVE_INFINITY; // fail fast
      }
      else if (b < '0' || b > '9') {
        return Float.NEGATIVE_INFINITY;
      }
    }
    return log2ProbHexDigit * (length - 2);
  }
}
