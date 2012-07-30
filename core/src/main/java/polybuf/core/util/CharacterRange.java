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

package polybuf.core.util;

import java.util.List;

/**
 * Utility class for manipulating character array ranges.
 */
public class CharacterRange {
  private final char[] chars;
  private final int offset;
  private int length;

  public CharacterRange(char[] chars, int offset, int length) {
    this.chars = chars;
    this.offset = offset;
    this.length = length;
  }

  public CharacterRange(String s) {
    this.chars = s.toCharArray();
    this.offset = 0;
    this.length = this.chars.length;
  }

  /**
   * Append the specified character range to this one. The character range must have the same underlying character array
   * and start immediately after this range finishes.
   * 
   * @see #isImmediatelyPreceding
   * @see #join
   */
  public void append(CharacterRange other) {
    assert isImmediatelyPreceding(other);
    this.length += other.length;
  }

  /**
   * Determine if this range immediately precedes the input range.
   */
  public boolean isImmediatelyPreceding(CharacterRange other) {
    return this.chars == other.chars && this.offset + this.length == other.offset;
  }

  /**
   * Number of characters in this range.
   */
  public int length() {
    return length;
  }

  @Override
  public String toString() {
    return new String(chars, offset, length);
  }

  /**
   * Determine if this range consists solely of XML ignorable whitespace, in other words solely of {@code '\t'},
   * {@code '\n'} and {@code '\r'} characters.
   */
  public boolean isIgnorableWhitespace() {
    for (int i = 0; i < length; ++i) {
      char c = chars[offset + i];
      if (!(c == ' ' || c == '\t' || c == '\n' || c == '\r')) {
        return false;
      }
    }
    return true;
  }

  /**
   * Copies the list of ranges into a single range.
   * <p>
   * Use {@link #append} if the ranges are contiguous to avoid the copy overhead.
   */
  public static CharacterRange join(List<CharacterRange> ranges) {
    int length = 0;
    for (CharacterRange range : ranges) {
      length += range.length();
    }
    char[] joined = new char[length];

    int offset = 0;
    for (CharacterRange range : ranges) {
      System.arraycopy(range.chars, range.offset, joined, offset, range.length);
      offset += range.length();
    }
    return new CharacterRange(joined, 0, joined.length);
  }
}
