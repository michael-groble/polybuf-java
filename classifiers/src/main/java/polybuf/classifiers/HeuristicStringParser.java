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

package polybuf.classifiers;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.apache.commons.codec.binary.Base64;

import polybuf.core.ParseException;
import polybuf.core.StringParser;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;

/**
 * Parser that handles both strict and compatible parsing of strings.
 * 
 */
public class HeuristicStringParser implements StringParser {

  private static final ThreadLocal<CharsetDecoder> strictUtf8Decoder = new ThreadLocal<CharsetDecoder>() {
    @Override
    protected CharsetDecoder initialValue() {
      CharsetDecoder decoder = Charsets.UTF_8.newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPORT);
      decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
      return decoder;
    }
  };

  /**
   * Parses bytes from the string.
   * <p>
   * We store all bytes as Base64 encoded strings so parsing requires us to unencode.
   * 
   * @throws ParseException if string is not strictly Base64 encoded
   */
  @Override
  public ByteString asStrictBytes(String string) {
    // Base64 decoder is not strict. Verify strict encoding
    byte[] bytes = string.getBytes(Charsets.UTF_8);
    if (Base64Classifier.isStrictBase64(bytes)) {
      return ByteString.copyFrom(Base64.decodeBase64(bytes));
    }
    throw new ParseException("Non-base64 encoding detected for bytes");
  }

  /**
   * Parse the incoming string with a target field type of String.
   * <p>
   * In compatible mode, this can be one of the following sent types
   * <ul>
   * <li>utf-8 string. No conversion required in this case</li>
   * <li>Base64 encoded utf-8 bytes. Decode Base64 and return bytes</li>
   * <li>Base64 encoded non-string bytes (e.g. message). This is an error</li>
   * </ul>
   * The problem is that the last one is an error, but we don't know deterministically whether the incoming bytes are a
   * plain string or a Base64 encoded one. If decoding from Base64 gives us invalid UTF-8 bytes, maybe we were wrong in
   * guessing the string was Base64 encoded in the first place.
   * <p>
   * For now, we will attempt to decode if it seems likely that it is Base64, but if that gives us non-UTF-8 bytes,
   * assume we were wrong and just return the incoming string.
   */
  @Override
  public String asCompatibleString(String string) {
    byte[] bytes = string.getBytes(Charsets.UTF_8);
    if (Base64Classifier.isLikelyBase64(bytes)) {
      byte[] decodedBytes = Base64.decodeBase64(bytes);
      try {
        string = strictUtf8Decoder.get().decode(ByteBuffer.wrap(decodedBytes)).toString();
      }
      catch (CharacterCodingException ex) {
        // For now, assume the previous Base64 guess was wrong and this really was a "plain" string to
        // begin with. Fall through and return it.
      }
    }
    return string;
  }

  /**
   * Parse the incoming string with a target field type of ByteString (protobuf binary).
   * <p>
   * In compatible mode, this can be one of the following sent types
   * <ul>
   * <li>utf-8 string. No conversion required in this case</li>
   * <li>Base64 encoded utf-8 bytes. Decode Base64 and return bytes</li>
   * <li>Base64 encoded non-string bytes (e.g. message). Decode Base64 and return bytes</li>
   * </ul>
   * Again, we don't know deterministically whether the incoming bytes are a plain string or a Base64 encoded one. If
   * this looks like it is likely Base64, meaning it is strictly Base64 encoded, and seems more like a Base64 string
   * than a plain UTF-8 string, decode it. Otherwise, pass through.
   * <p>
   */
  @Override
  public ByteString asCompatibleBytes(String string) {
    byte[] bytes = string.getBytes(Charsets.UTF_8);
    if (Base64Classifier.isLikelyBase64(bytes)) {
      return ByteString.copyFrom(Base64.decodeBase64(bytes));
    }
    return ByteString.copyFromUtf8(string);
  }

  /**
   * Parse the incoming string with a target field type of ByteString (protobuf Message type).
   * <p>
   * In compatible mode, this can be one of the following sent types
   * <ul>
   * <li>utf-8 string. error</li>
   * <li>Base64 encoded utf-8 bytes. error</li>
   * <li>Base64 encoded non-string bytes (e.g. message). Decode Base64 and return bytes</li>
   * </ul>
   * This method simply checks that the string is strictly Base64 encoded and decodes if so. Downstream processing will
   * determine if the resulting byte string can be correctly merged into the message field.
   * 
   * @throws ParseException if string is not strictly Base64 encoded
   */
  @Override
  public ByteString asCompatibleMessageBytes(String string) {
    // Base64 decoder is not strict. Verify strict encoding
    byte[] bytes = string.getBytes(Charsets.UTF_8);
    if (Base64Classifier.isStrictBase64(bytes)) {
      return ByteString.copyFrom(Base64.decodeBase64(bytes));
    }
    throw new ParseException("Non-base64 encoding detected for bytes");
  }
}
