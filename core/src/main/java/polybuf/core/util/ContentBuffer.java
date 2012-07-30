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

import java.util.LinkedList;

/**
 * A simple convenience class for accumulating content for parsers that do not support NIO, such as the SAX parsing
 * interface.
 */
public class ContentBuffer {

  private final LinkedList<CharacterRange> ranges = new LinkedList<CharacterRange>();

  /**
   * append the specified characters to the current buffer.
   */
  public void append(char[] chars, int offset, int length) {
    CharacterRange range = new CharacterRange(chars, offset, length);
    if (ranges.isEmpty()) {
      ranges.add(range);
      return;
    }
    CharacterRange last = ranges.peekLast();
    if (last.isImmediatelyPreceding(range)) {
      last.append(range);
    }
    else {
      ranges.add(range);
    }
  }

  /**
   * clear the buffer.
   */
  public void clear() {
    ranges.clear();
  }

  /**
   * Get the current contents of the buffer as a single character range and clear the buffer before returning. Returns
   * {@code null} if nothing has been appended to this buffer.
   */
  public CharacterRange getAndClear() {
    if (ranges.size() == 0) {
      return null;
    }
    if (ranges.size() == 1) {
      return ranges.pop();
    }
    CharacterRange joined = CharacterRange.join(ranges);
    ranges.clear();
    return joined;
  }

  /**
   * Determine if this buffer consists entirely of ignorable whitespace.
   * 
   * @see CharacterRange#isIgnorableWhitespace
   */
  public boolean isIgnorableWhitespace() {
    for (CharacterRange range : ranges) {
      if (!range.isIgnorableWhitespace()) {
        return false;
      }
    }
    return true;
  }
}
