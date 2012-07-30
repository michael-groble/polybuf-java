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

import java.util.regex.Pattern;

import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLongs;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * Base scalar parser.
 * <p>
 * This parser knows how to parse in both strict and compatible mode.  Implementers of message readers
 * should just need to subclass and implement the abstract methods specifying how to handle floating
 * point and boolean literals.
 * 
 * @see #parse
 */
public abstract class ScalarParser {
 
  // use pattern to disallow Java's hex strings
  private final static Pattern floatPattern = Pattern.compile(
      "[+-]?" + // optional leading sign
      "(?:\\d+(?:\\.\\d*)?|" + // integer or decimal including things like 5.
      "\\.\\d+)" + // no leading digit, e.g. -.5
      "(?:[eE][+-]?\\d+)?"); // optional trailing exponent
  
  private final StringParser stringParser;
  
  public ScalarParser() {
    this(new DefaultStringParser());
  }
  
  public ScalarParser(StringParser stringParser) {
    this.stringParser = stringParser;
  }

  /**
   * Determine if the input represents the positive infinity literal, e.g. "Infinity" in JSON.
   */
  protected abstract boolean isPositiveInfinityLiteral(String string);

  /**
   * Determine if the input represents the negative infinity literal, e.g. "-Infinity" in JSON.
   */
  protected abstract boolean isNegativeInfinityLiteral(String string);

  /**
   * Determine if the input represents the NaN literal, e.g. "NaN" in JSON
   */
  protected abstract boolean isNaNLiteral(String string);

  /**
   * Determine if the input represents the 'true' boolean literal.
   * <p>
   * The default implementation responds true for the strings {@code "true"} and {@code "1"}
   */
  protected boolean isTrueLiteral(String string) {
    return "true".equals(string) || "1".equals(string);
  }

  /**
   * Determine if the input represents the 'false' boolean literal.
   * <p>
   * The default implementation responds true for the strings {@code "false"} and {@code "0"}
   */
  protected boolean isFalseLiteral(String string) {
    return "false".equals(string) || "0".equals(string);
  }

  /**
   * Parse the string from the provided context to generate an object of the type expected by protobuf for the given
   * field.
   * <p>
   * When {@code isStrict} is true, all strings are parsed strictly corresponding to the associated Java type. From the
   * protobuf <a href="https://developers.google.com/protocol-buffers/docs/proto#updating">documentation</a>, there are
   * a number of compatible modifications that can be made to a descriptor. When {@code isStrict} is false, compatible
   * parsing is performed as follows:
   * <ul>
   * <li>{@code int32, uint32, int64, uint64} and {@code bool} are all compatible and "you will get the same effect as
   * if you had cast the number to that type in C++". These are parsed as longs or unsigned longs, depending on sign
   * (converting boolean literals to 0 and 1, as needed), and cast to the desired type</li>
   * <li>{@code sint32} and {@code sint64} are compatible. These are parsed as longs and cast to desired type.</li>
   * <li>{@code fixed32} and {@code sfixed32} are compatible. These are parsed as int or unsigned int, depending on
   * sign, and cast to desired type</li>
   * <li>{@code fixed64} and {@code sfixed64} are compatible. These are parsed as long or unsigned long, depending on
   * sign, and cast to desired type</li>
   * <li>strings are compatible with bytes "as long as bytes are utf-8". See {@link StringParser}</li>
   * <li>messages are compatible with bytes "if the bytes contain the encoded version of the message". See
   * {@link StringParser}</li>
   * </ul>
   */
  public Object parse(FieldDescriptor field, String string, ScalarContext context, boolean isStrict) {
    assert string != null;
    assert !string.isEmpty();
    return isStrict ? parseStrict(field, string, context) : parseCompatible(field, string, context);
  }

  private Object parseStrict(FieldDescriptor field, String string, ScalarContext context) {
    boolean allowMessageAsBytes = false;
    if (!context.canRepresent(field.getType(), allowMessageAsBytes)) {
      throw new IncompatibleFieldParseException("Incompatible context " + context, field);
    }

    switch (field.getType()) {
    case INT32:
    case SINT32:
    case SFIXED32:
      return Integer.parseInt(string);

    case INT64:
    case SINT64:
    case SFIXED64:
      return Long.parseLong(string);

    case FLOAT:
      return parseFloat(string);

    case DOUBLE:
      return parseDouble(string);

    case BOOL:
      return parseBool(string);

    case UINT32:
    case FIXED32:
      return UnsignedInts.parseUnsignedInt(string);

    case UINT64:
    case FIXED64:
      return UnsignedLongs.parseUnsignedLong(string);

    case STRING:
      return string;

    case BYTES:
      return stringParser.asStrictBytes(string);

    case ENUM:
      EnumValueDescriptor value = field.getEnumType().findValueByName(string);
      if (value == null) {
        throw new IncompatibleFieldParseException("Invalid enumerator " + string, field);
      }
      return value;

    case MESSAGE:
    case GROUP:
      throw new AssertionError("non-scalar");

    default:
      throw new AssertionError("unknown type");
    }

  }

  private Long parseCompatibleIntegralLiteral(String string) {
    if (isTrueLiteral(string)) {
      return 1L;
    }
    if (isFalseLiteral(string)) {
      return 0L;
    }
    if (string.startsWith("-")) {
      return Long.parseLong(string);
    }
    return UnsignedLongs.parseUnsignedLong(string);
  }

  private Object parseCompatible(FieldDescriptor field, String string, ScalarContext context) {
    if (!context.canRepresent(field.getType(), true)) {
      throw new IncompatibleFieldParseException("Incompatible context " + context, field);
    }

    switch (field.getType()) {

    // these are all compatible "you will get the same effect as if you had cast the number to that type in C++"
    case INT32:
    case UINT32:
      return parseCompatibleIntegralLiteral(string).intValue();
    case INT64:
    case UINT64:
      return parseCompatibleIntegralLiteral(string);
    case BOOL:
      return parseCompatibleIntegralLiteral(string) == 0 ? false : true;

      // these are compatible "but are not compatible with the other integer types"
    case SINT32:
      return Long.valueOf(string).intValue();
    case SINT64:
      return Long.valueOf(string);

      // strings are compatible with bytes "as long as the bytes are valid UTF-8"
      // messages are compatible with bytes "if the bytes contain an encoded version of the message"
    case STRING:
      return stringParser.asCompatibleString(string);
    case BYTES:
      return stringParser.asCompatibleBytes(string);
    case MESSAGE:
      return stringParser.asCompatibleMessageBytes(string);

      // these are compatible
    case FIXED32:
    case SFIXED32:
      return string.startsWith("-") ? Integer.parseInt(string) : UnsignedInts.parseUnsignedInt(string);

      // these are compatible
    case FIXED64:
    case SFIXED64:
      return string.startsWith("-") ? Long.parseLong(string) : UnsignedLongs.parseUnsignedLong(string);

    case FLOAT:
      return parseFloat(string);

    case DOUBLE:
      return parseDouble(string);

    case ENUM:
      EnumValueDescriptor value = field.getEnumType().findValueByName(string);
      // if (value == null) {
      // return field.getDefaultValue();
      // }
      return value;

    case GROUP:
      throw new AssertionError("non-scalar");

    default:
      throw new AssertionError("unknown type");
    }
  }

  private Boolean parseBool(String string) {
    if (isTrueLiteral(string)) {
      return true;
    }
    else if (isFalseLiteral(string)) {
      return false;
    }
    else {
      throw new NumberFormatException("strict parser found unsupported string for BOOL " + string);
    }
  }

  private Float parseFloat(String string) {
    if (isPositiveInfinityLiteral(string)) {
      return Float.POSITIVE_INFINITY;
    }
    else if (isNegativeInfinityLiteral(string)) {
      return Float.NEGATIVE_INFINITY;
    }
    else if (isNaNLiteral(string)) {
      return Float.NaN;
    }
    else if (floatPattern.matcher(string).matches()) {
      return Float.parseFloat(string);
    }
    throw new NumberFormatException("invalid float " + string);
  }

  private Double parseDouble(String string) {
    if (isPositiveInfinityLiteral(string)) {
      return Double.POSITIVE_INFINITY;
    }
    else if (isNegativeInfinityLiteral(string)) {
      return Double.NEGATIVE_INFINITY;
    }
    else if (isNaNLiteral(string)) {
      return Double.NaN;
    }
    else if (floatPattern.matcher(string).matches()) {
      return Double.parseDouble(string);
    }
    throw new NumberFormatException("invalid double " + string);
  }
}
