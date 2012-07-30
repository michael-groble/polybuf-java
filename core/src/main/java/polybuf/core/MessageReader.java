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

import java.io.IOException;
import java.util.List;

import com.google.protobuf.Message;

/**
 * Primary interface for converters that can parse an input serialization and create corresponding protobuf message
 * builders.
 * 
 * @param <I> input type, e.g. {@code InputStream}
 */
public interface MessageReader<I> {

  /**
   * Create a builder from the input. The reader must determine the appropriate builder type from the input
   * serialization.
   */
  Message.Builder mergeRootFrom(I input) throws IOException;

  /**
   * Create a list of builders from the input. The reader must determine the appropriate builder type from the input
   * serialization.
   */
  List<Message.Builder> mergeRepeatedRootsFrom(I input) throws IOException;

  /**
   * Merge the input into the provided builder.
   */
  void mergeFrom(Message.Builder builder, I input) throws IOException;

  /**
   * Generate a list of builders from the input using the specified prototype builder. Implementations should use
   * {@link com.google.protobuf.Message.Builder#clone} to initialize the returned builders from the prototype.
   */
  <T extends Message.Builder> List<T> mergeRepeatedFrom(T prototype, I input) throws IOException;
}
