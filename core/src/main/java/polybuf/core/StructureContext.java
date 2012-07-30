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
 * Context for parsing structured messages. If known, this helps detect errors between the serialized format and the
 * protobuf definition of a field, especially with respect to optional and repeated fields.
 * 
 * @see BuilderStack
 * @see Encoder
 */
public enum StructureContext {
  /**
   * The item is seen in an object context. For example, in the following JSON, the field {@code 'a'} is in an object
   * context.
   * 
   * <pre>
   * { "a": "true"}
   * </pre>
   */
  OBJECT(true, false),

  /**
   * The item is seen in an array context. For example, in the following JSON, the embedded message containing
   * {@code 'b'} is in an array context.
   * 
   * <pre>
   * { "a": [{ "b": "true"}]}
   * </pre>
   */
  ARRAY(false, true),

  /**
   * The parser or serialization format does not distinguish between objects or arrays.
   */
  UNSPECIFIED(true, true);

  private final boolean hasSingleValue;
  private final boolean hasMultipleValues;

  private StructureContext(boolean hasSingleValue, boolean hasMultipleValues) {
    this.hasSingleValue = hasSingleValue;
    this.hasMultipleValues = hasMultipleValues;
  }

  /**
   * Determine if the context can represent the specified field type.
   */
  public boolean canRepresent(FieldDescriptor field) {
    return field.isRepeated() ? hasMultipleValues : hasSingleValue;
  }
}
