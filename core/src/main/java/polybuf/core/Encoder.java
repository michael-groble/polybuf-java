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

import com.google.common.primitives.UnsignedLong;

/**
 * Streaming-type interface that needs to be implemented to serialize a protobuf message into another encoding.
 * <p>
 * For convenience, the interface also allows the potential to write lists of root messages.
 */
public interface Encoder { 

  /**
   * Called at the start of an attempt to encode a list of messages. Encoders should throw
   * {@link UnsupportedOperationException} if it only supports encoding a single root A list of roots will see as a
   * sequence of calls such as follows, showing how a json and xml encoder might respond
   * 
   * <pre>
   *                                          |           json             | 
   *                                         _|_    (socket io event)     _|_  xml
   * encoder.startRootList("Message")         | { name: "Message", args:[  | {@code <roots>}
   * repeat for each root:                    |                            |
   *   encoder.startRootMessage("Message")    |  {                         | {@code <Message>}
   *   set fields                             |    ...                     |  ...
   *   encoder.endRootMessage("Message")      |  }                         | {@code </Message>}
   * encoder.endRootList("Message")           |  ]}                        | {@code </roots>}
   * </pre>
   */
  void startRootList(String messageName) throws IOException;

  /**
   * Called to end the list of messages started with {@link #startRootList}.
   */
  void endRootList(String messageName) throws IOException;

  /**
   * Called at the start of an attempt to encode a single message.
   */
  void startRootMessage(String messageName) throws IOException;

  /**
   * Called to end the message started with {@link #startRootMessage}.
   */
  void endRootMessage(String messageName) throws IOException;

  /**
   * Called to begin a root message within a root list.
   * @see #startRootList
   */
  void startRepeatedRoot(String messageName) throws IOException;

  /**
   * Called to end a root message within a root list.
   * @see #startRootList
   */
  void endRepeatedRoot(String messageName) throws IOException;
  
  /**
   * Called to start a non-repeating message field.
   */
  void startMessageField(String fieldName) throws IOException;

  /**
   * Called to end a non-repeating message field..
   */
  void endMessageField(String fieldName) throws IOException;

  /**
   * Called to start a repeated message field.
   * @see #startRepeatedField
   */
  void startRepeatedMessageField(String messageName) throws IOException;

  /**
   * Called to end a repeated message field.
   * @see #startRepeatedField
   */
  void endRepeatedMessageField(String messageName) throws IOException;

  /**
   * Called to start any repeated field within a message.
   * <p>
   * A repeated message field will appear as follow, showing how a json and xml encoder might respond. Note the json
   * encoder would need to maintain state to correctly insert commas between list and field elements.
   * 
   * <pre>
   *                                       |   json    |  xml
   * encoder.startRepeatedField("a")       | "a" : [   |  
   * for each message:                     |           |
   *   encoder.startRepeatedMessage("a")   | {         |  {@code <a>}
   *   set message fields                  |   ...     |    ...
   *   encoder.endRepeatedMessage("a")     | }         |  {@code </a>}
   * encoder.endRepeatedField("a")         | ]         |
   * </pre>
   * 
   * A repeated scalar field would appear as follows:
   * 
   * <pre>
   *                                         |  json    |     xml
   * encoder.startRepeatedField("a")         | "a" : [  |
   * encoder.repeatedScalarField("a",true)   | false    |  {@code <a>true</a>}
   * encoder.repeatedScalarField("a",false)  | , false  |  {@code <a>true</a>}
   * encoder.repeatedScalarField("a",false)  | , false  |  {@code <a>true</a>}
   * encoder.endRepeatedField("a")           | ]        |
   * </pre>
   */
  void startRepeatedField(String fieldName) throws IOException;

  /**
   * Called to end a repeated field.
   * @see #startRepeatedField
   */
  void endRepeatedField(String fieldName) throws IOException;

  void scalarField(String fieldName, boolean fieldValue) throws IOException;

  /**
   * Called for signed 32 bit protobuf types.
   */
  void scalarField(String fieldName, int fieldValue) throws IOException;

  /**
   * Called for signed 64 bit protobuf types and unsigned 32 bit types.
   */
  void scalarField(String fieldName, long fieldValue) throws IOException;

  /**
   * Called for unsigned 64 bit protobuf types.
   */
  void scalarField(String fieldName, UnsignedLong fieldValue) throws IOException;

  void scalarField(String fieldName, float fieldValue) throws IOException;

  void scalarField(String fieldName, double fieldValue) throws IOException;

  /**
   * Called for strings, binary and enum types.  For binary types, the string is
   * the Base64 encoding.
   */
  void scalarField(String fieldName, String fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, boolean)
   */
  void repeatedScalarField(String fieldName, boolean fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, int)
   */
  void repeatedScalarField(String fieldName, int fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, long)
   */
  void repeatedScalarField(String fieldName, long fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, UnsignedLong)
   */
  void repeatedScalarField(String fieldName, UnsignedLong fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, float)
   */
  void repeatedScalarField(String fieldName, float fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, double)
   */
  void repeatedScalarField(String fieldName, double fieldValue) throws IOException;

  /**
   * @see #startRepeatedField
   * @see #scalarField(String, String)
   */
  void repeatedScalarField(String fieldName, String fieldValue) throws IOException;
}
