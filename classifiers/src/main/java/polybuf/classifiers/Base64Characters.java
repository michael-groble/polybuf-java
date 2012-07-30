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

import com.google.common.base.Charsets;

class Base64Characters {
  private static final int[] indices = new int[128];

  static {
    for (int i = 0; i < 128; ++i) {
      indices[i] = -1;
    }

    byte[] bytes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(Charsets.US_ASCII);
    assert bytes.length == 64;
    for (int i = 0; i < 64; ++i) {
      indices[bytes[i]] = i;
    }
  }

  public static int index(byte b) {
    if (b < 0 || b >= indices.length) {
      return -1;
    }
    return indices[b];
  }

  public static boolean isValid(byte b) {
    return index(b) >= 0;
  }

  public static boolean isPad(byte b) {
    return b == '=';
  }

}
