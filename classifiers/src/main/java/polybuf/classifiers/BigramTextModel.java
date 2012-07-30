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

import com.google.common.math.DoubleMath;

abstract class BigramTextModel implements ByteArrayModel {

  // assume in normal string, whitespace or punctuation shows up with
  // certain frequency. At some point, the probability of a real string
  // having so many characters without any whitespace or punctuation
  // becomes too small to even bother with the chi square test.
  //
  // If we assume whitespace or punctuation is 1 of every k characters,
  // then figure out how many consecutive chars are needed before
  // probability is under p.
  // In other words find smallest n such that ((k-1)/k)^n <= p which is
  // n = ceil(log(p) / log(1 - 1/k))
  //
  // for k = 8 and p = .001, we have n = 52
  // for k = 7 and p = .001, we have n = 45
  // Also, wikipedia says the longest english word "in a major dictionary" is 45
  // chars
  private static final int maxStringLength = 50;
  @SuppressWarnings("unused")
  private static final float log2ProbNoWhitespaceOrPunctuation = (float) DoubleMath.log2(7. / 8.);

  protected static void log2Normalize(float[] values) {
    float sum = 0;
    for (float v : values) {
      sum += v;
    }
    for (int i = 0; i < values.length; ++i) {
      values[i] = (float) DoubleMath.log2(values[i] / sum);
    }
  }

  protected static void log2Normalize(float[][] values) {
    for (int previous = 0; previous < values.length; ++previous) {
      float sum = 0;
      for (int current = 0; current < values.length; ++current) {
        sum += values[current][previous];
      }
      for (int current = 0; current < values.length; ++current) {
        values[current][previous] = (float) DoubleMath.log2(values[current][previous] / sum);
      }
    }
  }

  @Override
  public float log2Prob(byte[] bytes) {
    return log2Prob(bytes, 0, bytes.length);
  }

  @Override
  public float log2Prob(byte[] bytes, int offset, int length) {
    if (length > maxStringLength) {
      return Float.NEGATIVE_INFINITY;
    }

    int n = unpaddedLength(bytes, offset, length);

    float result = log2ProbStart(bytes[offset]) + log2ProbEnd(bytes[offset + n - 1]);

    for (int i = 1; i < n; ++i) {
      result += log2ProbBigram(bytes[offset + i], bytes[offset + i - 1]);
      // result += log2ProbNoWhitespaceOrPunctuation;
      if (Float.isInfinite(result)) {
        return Float.NEGATIVE_INFINITY;
      }
    }

    for (int i = n; i < length; ++i) {
      result += log2ProbPad();
    }
    return result;
  }

  protected abstract float log2ProbPad();

  protected abstract float log2ProbStart(byte current);

  protected abstract float log2ProbEnd(byte previous);

  protected abstract float log2ProbBigram(byte current, byte previous);

  private int unpaddedLength(byte[] bytes, int offset, int length) {
    if (Base64Characters.isPad(bytes[offset + length - 1])) {
      length -= 1;
      if (Base64Characters.isPad(bytes[offset + length - 1])) {
        length -= 1;
      }
    }
    return length;
  }
}
