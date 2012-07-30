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

package polybuf.core;

import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * Context for parsing scalar values.
 * 
 * @see ScalarParser
 */
public enum ScalarContext {
  /**
   * Scalar is seen in a quoted context. For example, in the following JSON, the field {@code 'a'} is in a quoted
   * context while {@code 'b'} is not.
   * 
   * <pre>
   * { "a": "true", "b": true}
   * </pre>
   */
  QUOTED(true, false),

  /**
   * Scalar is seen in an unquoted, or 'literal' context. For example, in the following JSON, the field {@code 'b'} is
   * in an unquoted context while {@code 'a'} is not.
   * 
   * <pre>
   * { "a": "true", "b": true}
   * </pre>
   */
  UNQUOTED(false, true),

  /**
   * Unspecified context. Used in XML, for example, where there typically aren't distinctions between quoted and
   * unquoted or literal values
   */
  UNSPECIFIED(true, true);

  private final boolean doesAllowQuoted;
  private final boolean doesAllowUnquoted;

  private ScalarContext(boolean doesAllowQuoted, boolean doesAllowUnquoted) {
    this.doesAllowQuoted = doesAllowQuoted;
    this.doesAllowUnquoted = doesAllowUnquoted;
  }

  /**
   * Determine if the context can represent the specified field type.
   * <p>
   * 
   * @param allowMessageAsBytes if the field is a message field, a true value for this parameter means to reply with
   *          true when the context is quoted (or unspecified) since message bytes are sent as base64-encoded strings.
   */
  public boolean canRepresent(FieldDescriptor.Type type, boolean allowMessageAsBytes) {
    switch (type) {
    case INT32:
    case SINT32:
    case SFIXED32:
    case INT64:
    case SINT64:
    case SFIXED64:
    case FLOAT:
    case DOUBLE:
    case BOOL:
    case UINT32:
    case FIXED32:
    case UINT64:
    case FIXED64:
      return doesAllowUnquoted;

    case STRING:
    case BYTES:
    case ENUM:
      return doesAllowQuoted;

    case MESSAGE:
      return allowMessageAsBytes && doesAllowQuoted;

    case GROUP:
      return false;

    default:
      throw new AssertionError("unknown type");
    }
  }
}
