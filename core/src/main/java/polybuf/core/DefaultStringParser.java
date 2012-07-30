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

import org.apache.commons.codec.binary.Base64;

import com.google.protobuf.ByteString;

/**
 * Parser that handles both strict string parsing and very minimal compatible parsing of strings.
 * <p>
 * The {@link DefaultMessageWriter} writes all binary types as Base64 encoded. If the proto file is changed in a
 * compatible way from a {@code string} field to a {@code bytes} field, this means code using the new proto file will
 * send values in Base64. If this default string parser sees such a message when configured with the old proto file, it
 * will not decode the string and will pass it unchanged to the caller.
 * <p>
 * See {@link HeuristicStringParser} for a parser that attempts to handle the conversion more completely.
 */
public class DefaultStringParser implements StringParser {

  /**
   * Parses bytes from the string.
   * <p>
   * Assumes the string is Base64 encoded and decodes to generate the returned byte string.
   */
  @Override
  public ByteString asStrictBytes(String string) {
    return ByteString.copyFrom(Base64.decodeBase64(string));
  }

  /**
   * Parse the incoming string with a target field type of String. Performs no conversion on the incoming bytes. This
   * means the application must handle the following cases that might have been sent:
   * <ul>
   * <li>utf-8 string. String will match what was sent, no further processing required</li>
   * <li>Base64 encoded utf-8 bytes. String will still be encoded, application will need to decode</li>
   * <li>Base64 encoded non-utf-8 bytes (e.g. message). Incompatible conversion, application will need to detect and
   * resolve</li>
   * </ul>
   */
  @Override
  public String asCompatibleString(String string) {
    return string;
  }

  /**
   * Parse the incoming string with a target field type of ByteString. Performs no conversion on the incoming bytes,
   * filling it with the UTF-8 encoded version of the string. This means the application must handle the following cases
   * that might have been sent:
   * <ul>
   * <li>utf-8 string. Bytes will match the bytes from the sent string, no further processing required</li>
   * <li>Base64 encoded utf-8 bytes. Bytes will still be the sent Base64 encoded bytes, application will need to decode</li>
   * <li>Base64 encoded non-utf-8 bytes (e.g. message). Bytes are the Base64 encoded bytes, application will need to
   * decode</li>
   * </ul>
   */
  @Override
  public ByteString asCompatibleBytes(String string) {
    return ByteString.copyFromUtf8(string);
  }

  /**
   * Parse the incoming string with a target field type of ByteString.
   * <p>
   * Assumes the string is Base64 encoded and decodes to generate the returned byte string.
   */
  @Override
  public ByteString asCompatibleMessageBytes(String string) {
    return ByteString.copyFrom(Base64.decodeBase64(string));
  }
}
