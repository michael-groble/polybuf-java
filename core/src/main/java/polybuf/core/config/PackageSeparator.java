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

import java.util.regex.Pattern;

/**
 * Allowed package separators for creating serialized root message names or extended field names.
 * <p>
 * The default separator is '.', just as in protobuf. Some serializations do not support that character, or it can
 * potentially lead to confusion with standard expected meaning of that separator for other purposes. JSON processing in
 * JavaScript, for example can be confusing when property names have dots in them.
 * <p>
 * Each of these separators is safe to use, in the sense that they are not valid characters in package segments in
 * protocol buffers.
 * 
 * @see FieldNamingStrategy
 * @see RootMessageNamingStrategy
 */
public enum PackageSeparator {
  /**
   * The {@code '.'} separator
   */
  DOT('.'), 
  /**
   * The {@code '-'} separator
   */
  DASH('-'), 
  /**
   * The {@code ':'} separator
   */
  COLON(':'), 
  /**
   * The {@code '$'} separator
   */
  DOLLAR('$');

  private final char separator;
  private final Pattern serializedNamePattern;

  private PackageSeparator(char separator) {
    this.separator = separator;
    // from google::protobuf::io::Tokenizer, identifiers match
    // [A-Za-z_][A-Za-z0-9_]*
    this.serializedNamePattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*(?:[\\" + separator
        + "][A-Za-z_][A-Za-z0-9_]*)*");
  }

  /**
   * Convert the serialized name using this package separator to the standard protobuf full name.
   */
  public String fullName(String serializedName) {
    if (serializedName == null) {
      return null;
    }
    return serializedName.replace(separator, '.');
  }

  /**
   * Convert the standard protobuf full name to one using this package separator.
   */
  public String serializedName(String fullName) {
    if (fullName == null) {
      return null;
    }
    return fullName.replace('.', separator);
  }

  /**
   * Determine if the serialized name uses this package separator. Returns true if there are no package separators, i.e.
   * the name matches {@code [A-Za-z_][A-Za-z0-9_]*}
   */
  public boolean isValidSerializedName(String serializedName) {
    return serializedNamePattern.matcher(serializedName).matches();
  }

  /**
   * Determines the first separator which reports {@link #isValidSerializedName} or {@code null} if none do.
   */
  public static String fullNameForUnknownSerailization(String serializedName) {
    if (serializedName == null) {
      return null;
    }
    for (PackageSeparator s : values()) {
      if (s.isValidSerializedName(serializedName)) {
        return s.fullName(serializedName);
      }
    }
    return null;
  }
}
