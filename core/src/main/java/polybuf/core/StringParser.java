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

import com.google.protobuf.ByteString;

/**
 * Protobuf allows two types of compatiblity for strings:
 * <ul>
 * <li>strings are compatible with bytes "as long as bytes are utf-8"</li>
 * <li>messages are compatible with bytes "if the bytes contain the encoded version of the message"</li>
 * </ul>
 * 
 * Taken together, these present a challenge. The default behavior is to store binary types as Base64 encoded strings
 * which allows them to be easily serialized in text formats like XML and JSON. But this means that "plain" strings can
 * look the same as encoded bytes. Distinguishing between compatible bytes and strings is potentially complex. This
 * interface allows different parser implementations to trade off complexity vs. accuracy in distinguishing betwee the
 * two cases.
 */
public interface StringParser {

  /**
   * Strictly parse bytes from the string.
   */
  ByteString asStrictBytes(String string);

  /**
   * Parse the incoming string with a target field type of String (a protobuf string).
   * 
   * @throws ParseExcpetion if the incoming string does not look like it is a valid string
   */
  String asCompatibleString(String string);

  /**
   * Parse the incoming string with a target field type of ByteString (a protobuf binary).
   * 
   * @throws ParseExcpetion if the incoming string does not look like it is a valid byte string
   */
  ByteString asCompatibleBytes(String string);

  /**
   * Parse the incoming string with a target field type of ByteString (a protobuf Message). This byte string will then
   * be merged into a builder for the corresponding message type.
   * 
   * @throws ParseExcpetion if the incoming string cannot be used to generate the message
   */
  ByteString asCompatibleMessageBytes(String string);
}