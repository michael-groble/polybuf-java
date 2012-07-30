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

package polybuf.core.config;

import polybuf.core.BuilderStack;
import polybuf.core.MessageReader;
import polybuf.core.ScalarParser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * Subset of configuration information and convenience methods for implementing {@link MessageReader}.
 */
public interface ReaderConfig {

  /**
   * Determine if the reader is configured in strict, as opposed to compatible, mode.
   */
  boolean isStrict();

  /**
   * Construct a new builder stack configured to use the specified parser.
   */
  BuilderStack builderStack(ScalarParser scalarParser);

  /**
   * Determine the message corresponding to this serialized name. Returns {@code null} if a root cannot be determined.
   * 
   * @see RootMessageNamingStrategy
   */
  RootMessage messageForSerializedName(String serializedName);

  /**
   * Get the configured field naming strategy.
   */
  FieldNamingStrategy getFieldNamingStrategy();

  /**
   * Determine the field descriptor corresponding to the serialized name in the specified message. Returns {@code null}
   * if the field cannot be determined.
   * 
   * @see FieldNamingStrategy
   */
  FieldDescriptor fieldDescriptor(Descriptor messageDescriptor, String serializedName);

  /**
   * Get the serialized root message name for the specified message.
   * 
   * @see RootMessageNamingStrategy
   */
  String serializedNameForMessage(Descriptor messageDescriptor);
}
