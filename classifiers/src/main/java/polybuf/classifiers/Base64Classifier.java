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

class Base64Classifier {
  private static final ByteArrayModel base64Model = new Base64Model();
  private static final ByteArrayModel encodedAsciiModel = new EncodedAsciiModel();

  private static final ByteArrayModel textModel = new PlainTextModel();
  private static final ByteArrayModel sameCaseTextModel = new SameCaseTextModel();
  private static final ByteArrayModel numberModel = new NumberModel();

  public static boolean isStrictBase64(byte[] bytes) {
    return !Float.isInfinite(base64Model.log2Prob(bytes));
  }

  public static boolean isStrictBase64(byte[] bytes, int offset, int length) {
    return !Float.isInfinite(base64Model.log2Prob(bytes));
  }

  public static boolean isLikelyBase64(byte[] bytes) {
    float log2ProbBase64 = base64Model.log2Prob(bytes);
    if (Float.isInfinite(log2ProbBase64)) {
      return false;
    }

    return Floats.max(log2ProbBase64, encodedAsciiModel.log2Prob(bytes)) > Floats.max(textModel.log2Prob(bytes),
        sameCaseTextModel.log2Prob(bytes), numberModel.log2Prob(bytes));
  }
}
