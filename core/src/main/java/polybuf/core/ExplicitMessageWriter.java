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
 * Message writer for 'explicit' messages, in other words messages whose serialized name are provided.
 * <p>
 * This is in contrast to {@link MessageWriter}, which also knows how to generate the seralized name for specified
 * messages.
 * 
 * @param <O> Output type, e.g. {@code OutputStream}
 * @see DefaultMessageWriter
 * @see Encoder
 */
public interface ExplicitMessageWriter<O> {
  /**
   * Write the message to the output using the provided serialized message name.
   */
  void writeTo(String messageName, Message message, O output) throws IOException;

  /**
   * Write a list of messages to the output using the provided serialized message name.
   */
  <T extends Message> void writeTo(String messageName, List<T> messages, O output) throws IOException;
}
