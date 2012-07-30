package polybuf.core;

import static org.junit.Assert.*;
import static polybuf.core.DefaultStringParserTest.*;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.test.Coverage;
import polybuf.core.util.Reflection;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;

public class ScalarParserTest {

  private static ScalarParser parser = new TestParser(new DefaultStringParser());
  private static boolean strict = true;
  private static boolean compatible = false;

  private static final String MAX_UNSIGNED_LONG_PLUS1 = "18446744073709551616";
  private static final String MAX_UNSIGNED_LONG = "18446744073709551615";
  private static final String MAX_SIGNED_LONG_PLUS1 = "9223372036854775808";
  private static final String MAX_SIGNED_LONG = "9223372036854775807";
  private static final String MIN_SIGNED_LONG = "-9223372036854775808";
  private static final String MIN_SIGNED_LONG_MINUS1 = "-9223372036854775809";
  private static final String MAX_SIGNED_INT_PLUS1 = "2147483648";
  private static final String MAX_SIGNED_INT = "2147483647";
  private static final String MIN_SIGNED_INT = "-2147483648";
  private static final String MIN_SIGNED_INT_MINUS1 = "-2147483649";
  private static final String MAX_UNSIGNED_INT_PLUS1 = "4294967296";
  private static final String MAX_UNSIGNED_INT = "4294967295";
 
  private FieldDescriptor requiredField(Class<? extends GeneratedMessage> messageClass) {
    Descriptor descriptor = Reflection.invokeStaticGetter(messageClass, "getDescriptor", Descriptor.class);
    return descriptor.findFieldByNumber(1);
  }

  private static void assertParseBoth(FieldDescriptor descriptor, Object expected, String string, ScalarContext context) {
    assertParse(descriptor, expected, string, context, strict);
    assertParse(descriptor, expected, string, context, compatible);
  }

  private static void assertParseCompatibleOnly(FieldDescriptor descriptor, Object expected, String string,
      ScalarContext context) {
    assertParse(descriptor, expected, string, context, compatible);
    assertParseException(descriptor, string, context, strict);
  }

  private static void assertParse(FieldDescriptor descriptor, Object expected, String string,
      ScalarContext expectedContext, boolean isStrict) {
    assert expectedContext != ScalarContext.UNSPECIFIED;
    assertEquals(expected, parser.parse(descriptor, string, expectedContext, isStrict));
    assertEquals(expected, parser.parse(descriptor, string, ScalarContext.UNSPECIFIED, isStrict));
    ScalarContext alternateContext = expectedContext == ScalarContext.QUOTED ? ScalarContext.UNQUOTED
        : ScalarContext.QUOTED;
    assertParseException(descriptor, string, alternateContext, isStrict);
  }

  private static void assertParseException(FieldDescriptor descriptor, String string, ScalarContext context) {
    assertParseException(descriptor, string, context, strict);
    assertParseException(descriptor, string, context, compatible);
  }

  private static void assertParseException(FieldDescriptor descriptor, String string, ScalarContext context,
      boolean isStrict) {
    try {
      parser.parse(descriptor, string, context, isStrict);
    }
    catch (ParseException ex) {
      return;
    }
    catch (NumberFormatException ex) {
      return;
    }
    fail();
  }

  @Test
  public void bools() {
    FieldDescriptor descriptor = requiredField(Coverage.Bool.class);
    ScalarContext context = ScalarContext.UNQUOTED;
    // allowed literals: json & xml schema
    assertParseBoth(descriptor, Boolean.TRUE, "true", context);
    assertParseBoth(descriptor, Boolean.FALSE, "false", context);
    // allowed literals: xml schema
    assertParseBoth(descriptor, Boolean.TRUE, "1", context);
    assertParseBoth(descriptor, Boolean.FALSE, "0", context);
    // in compatible mode, integers are cast to bool so non-zeros are true
    assertParseCompatibleOnly(descriptor, Boolean.TRUE, MAX_UNSIGNED_LONG, context);
    assertParseCompatibleOnly(descriptor, Boolean.TRUE, MIN_SIGNED_LONG, context);
    // case must match
    assertParseException(descriptor, "TRUE", context);
    assertParseException(descriptor, "FALSE", context);
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context);
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context);
  }

  @Test
  public void bytes() {
    FieldDescriptor descriptor = requiredField(Coverage.Bytes.class);
    ScalarContext context = ScalarContext.QUOTED;
    // with default parser, strict decodes base64 and compatible does no modification
    assertParse(descriptor, ByteString.copyFrom(utf8Bytes), Base64.encodeBase64String(utf8Bytes), context, strict);
    assertParse(descriptor, ByteString.copyFromUtf8(Base64.encodeBase64String(utf8Bytes)), Base64.encodeBase64String(utf8Bytes), context, compatible);
    // strict decodes, even if invalid base64
    assertParse(descriptor, ByteString.copyFrom(Base64.decodeBase64(invalidLengthBase64)), invalidLengthBase64, context, strict);
    assertParse(descriptor, ByteString.copyFromUtf8(invalidLengthBase64), invalidLengthBase64, context, compatible);
  }

  @Test
  public void doubles() {
    FieldDescriptor descriptor = requiredField(Coverage.Double.class);
    ScalarContext context = ScalarContext.UNQUOTED;
    // test all different formats since we have regexp protecting us from Java's hex parsing
    assertParseBoth(descriptor, Double.valueOf(Double.POSITIVE_INFINITY), "Infinity", context);
    assertParseBoth(descriptor, Double.valueOf(Double.NEGATIVE_INFINITY), "-Infinity", context);
    assertParseBoth(descriptor, Double.valueOf(Double.NaN), "NaN", context);
    assertParseBoth(descriptor, Double.valueOf(1.0), "1.0", context);
    assertParseBoth(descriptor, Double.valueOf(1.0), "1", context);
    assertParseBoth(descriptor, Double.valueOf(1.0), "1.", context);
    assertParseBoth(descriptor, Double.valueOf(-1.0), "-1", context);
    assertParseBoth(descriptor, Double.valueOf(-0.0), "-.0", context);
    assertParseBoth(descriptor, Double.valueOf(-1e-9), "-1.e-9", context);
    assertParseBoth(descriptor, Double.valueOf(-1e-9), "-.1e-8", context);
    assertParseBoth(descriptor, Double.valueOf(1e9), "1.e9", context);
    assertParseBoth(descriptor, Double.valueOf(1.1e9), "1.1e9", context);
    assertParseBoth(descriptor, Double.valueOf(1e9), ".1e10", context);
    assertParseBoth(descriptor, Double.valueOf(1e9), "1.e+9", context);
    assertParseBoth(descriptor, Double.valueOf(1.1e9), "1.1e+9", context);
    assertParseBoth(descriptor, Double.valueOf(1e9), ".1e+10", context);

    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
    assertParseException(descriptor, "+Infinity", context);
    assertParseException(descriptor, "infinity", context);
    assertParseException(descriptor, ".", context);
    assertParseException(descriptor, "-", context);
    assertParseException(descriptor, "1.1.", context);
    assertParseException(descriptor, "e9", context);
    assertParseException(descriptor, Double.toHexString(1.0), context);
  }
  
  private void assertEnumEquals(FieldDescriptor descriptor, EnumValueDescriptor expected, String string) {
    assertParseBoth(descriptor, expected, string, ScalarContext.QUOTED);
  }

  private void assertInvalidEnum(FieldDescriptor descriptor, String string) {
    assertParseException(descriptor, string, ScalarContext.QUOTED, strict);
    assertParseException(descriptor, string, ScalarContext.UNSPECIFIED, strict);
    assertParseException(descriptor, string, ScalarContext.UNQUOTED, strict);

    assertNull(parser.parse(descriptor, string, ScalarContext.QUOTED, compatible));
    assertNull(parser.parse(descriptor, string, ScalarContext.UNSPECIFIED, compatible));
    assertParseException(descriptor, string, ScalarContext.UNQUOTED, compatible);
  }

  @Test
  public void enums() {
    FieldDescriptor descriptor = requiredField(Coverage.Enum.class);
    assertEnumEquals(descriptor, Coverage.Enum1.A.getValueDescriptor(), "A");
    assertEnumEquals(descriptor, Coverage.Enum1.B.getValueDescriptor(), "B");
    assertEnumEquals(descriptor, Coverage.Enum1.C.getValueDescriptor(), "C");
    assertInvalidEnum(descriptor, "AA");
    assertInvalidEnum(descriptor, "D");
    assertInvalidEnum(descriptor, "a");
    assertInvalidEnum(descriptor, "b");
    assertInvalidEnum(descriptor, "c");
  }

  @Test
  public void nestedEnums() {
    FieldDescriptor descriptor = requiredField(Coverage.NestedEnum.class);
    assertEnumEquals(descriptor, Coverage.NestedEnum.Nest.X.getValueDescriptor(), "X");
    assertEnumEquals(descriptor, Coverage.NestedEnum.Nest.Y.getValueDescriptor(), "Y");
    assertEnumEquals(descriptor, Coverage.NestedEnum.Nest.Z.getValueDescriptor(), "Z");
    assertInvalidEnum(descriptor, "XX");
    assertInvalidEnum(descriptor, "W");
    assertInvalidEnum(descriptor, "x");
    assertInvalidEnum(descriptor, "y");
    assertInvalidEnum(descriptor, "z");
  }
  
  @Test
  public void fixed32s() {
    // these are unsigned, but in java get mapped to "top bit stored in the sign bit"
    // they are compatible with sfixed32
    FieldDescriptor descriptor = requiredField(Coverage.Fixed32.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should only accept positive integers in unsigned int range
    assertParseBoth(descriptor, Integer.valueOf(Integer.MAX_VALUE), MAX_SIGNED_INT, context);
    assertParseBoth(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_INT, context);
    // compatible should accept negative to min signed
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), "-1", context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(Integer.MIN_VALUE), MIN_SIGNED_INT, context);
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_INT_MINUS1, context);
    assertParseException(descriptor, MAX_UNSIGNED_INT_PLUS1, context);
    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
  }

  @Test
  public void fixed64s() {
    // these are unsigned, but in java get mapped to "top bit stored in the sign bit"
    // they are compatible with sfixed64
    FieldDescriptor descriptor = requiredField(Coverage.Fixed64.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should only accept positive integers in unsigned int range
    assertParseBoth(descriptor, Long.valueOf(Long.MAX_VALUE), MAX_SIGNED_LONG, context); // max signed long
    assertParseBoth(descriptor, Long.valueOf(-1), MAX_UNSIGNED_LONG, context); // max unsigned long
    // compatible should accept negative to min signed
    assertParseCompatibleOnly(descriptor, Long.valueOf(-1), "-1", context);
    assertParseCompatibleOnly(descriptor, Long.valueOf(Long.MIN_VALUE), MIN_SIGNED_LONG, context); // min signed
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context); // min - 1
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context); // max + 1
    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
  }

  @Test
  public void floats() {
    FieldDescriptor descriptor = requiredField(Coverage.Float.class);
    ScalarContext context = ScalarContext.UNQUOTED;
    // test all different formats since we have regexp protecting us from Java's hex parsing
    assertParseBoth(descriptor, Float.valueOf(Float.POSITIVE_INFINITY), "Infinity", context);
    assertParseBoth(descriptor, Float.valueOf(Float.NEGATIVE_INFINITY), "-Infinity", context);
    assertParseBoth(descriptor, Float.valueOf(Float.NaN), "NaN", context);
    assertParseBoth(descriptor, Float.valueOf(1.0f), "1.0", context);
    assertParseBoth(descriptor, Float.valueOf(1.0f), "1", context);
    assertParseBoth(descriptor, Float.valueOf(1.0f), "1.", context);
    assertParseBoth(descriptor, Float.valueOf(-1.0f), "-1", context);
    assertParseBoth(descriptor, Float.valueOf(-0.0f), "-.0", context);
    assertParseBoth(descriptor, Float.valueOf(-1e-9f), "-1.e-9", context);
    assertParseBoth(descriptor, Float.valueOf(-1e-9f), "-.1e-8", context);
    assertParseBoth(descriptor, Float.valueOf(1e9f), "1.e9", context);
    assertParseBoth(descriptor, Float.valueOf(1.1e9f), "1.1e9", context);
    assertParseBoth(descriptor, Float.valueOf(1e9f), ".1e10", context);
    assertParseBoth(descriptor, Float.valueOf(1e9f), "1.e+9", context);
    assertParseBoth(descriptor, Float.valueOf(1.1e9f), "1.1e+9", context);
    assertParseBoth(descriptor, Float.valueOf(1e9f), ".1e+10", context);

    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
    assertParseException(descriptor, "+Infinity", context);
    assertParseException(descriptor, "infinity", context);
    assertParseException(descriptor, ".", context);
    assertParseException(descriptor, "-", context);
    assertParseException(descriptor, "1.1.", context);
    assertParseException(descriptor, "e9", context);
    assertParseException(descriptor, Double.toHexString(1.0), context);
  }

  @Test
  public void int32s() {
    // these are signed, and are compatible with uint32, int64, uint64, and bool
    // "If a number is parsed from the wire which doesn't fit in the corresponding type,
    //  you will get the same effect as if you had cast the number to that type in C++" 
    FieldDescriptor descriptor = requiredField(Coverage.Int32.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should accept int range
    assertParseBoth(descriptor, Integer.valueOf(Integer.MAX_VALUE), MAX_SIGNED_INT, context);
    assertParseBoth(descriptor, Integer.valueOf(Integer.MIN_VALUE), MIN_SIGNED_INT, context);
    
    // compatible should accept from max unsigned long down to min long and boolean true/false
    assertParseCompatibleOnly(descriptor, Integer.valueOf(0), "false", context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(1), "true", context);
    // max signed int + 1:  0x7FFFFFFF + 0x01 -> 0x80000000 -> min signed int 
    assertParseCompatibleOnly(descriptor, Integer.valueOf(Integer.MIN_VALUE), MAX_SIGNED_INT_PLUS1, context);
    // min signed int - 1:  0x80000000 + 0xffffffff -> 0x7fffffff -> max signed int
    assertParseCompatibleOnly(descriptor, Integer.valueOf(Integer.MAX_VALUE), MIN_SIGNED_INT_MINUS1, context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_INT, context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_LONG, context);
    // min signed long: 0x8000000000000000 -> cast to 0x00000000
    assertParseCompatibleOnly(descriptor, Integer.valueOf(0), MIN_SIGNED_LONG, context);
    
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context); // min - 1
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context); // max + 1
  }
  
  @Test
  public void int64s() {
    // these are signed, and are compatible with uint32, int32, uint64, and bool
    // "If a number is parsed from the wire which doesn't fit in the corresponding type,
    //  you will get the same effect as if you had cast the number to that type in C++" 
    FieldDescriptor descriptor = requiredField(Coverage.Int64.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should accept long range
    assertParseBoth(descriptor, Long.valueOf(Long.MAX_VALUE), MAX_SIGNED_LONG, context);
    assertParseBoth(descriptor, Long.valueOf(Long.MIN_VALUE), MIN_SIGNED_LONG, context);
    
    // compatible should accept from max unsigned long down to min long and boolean true/false
    assertParseCompatibleOnly(descriptor, Long.valueOf(0), "false", context);
    assertParseCompatibleOnly(descriptor, Long.valueOf(1), "true", context);
    assertParseCompatibleOnly(descriptor, Long.valueOf(Long.MIN_VALUE), MAX_SIGNED_LONG_PLUS1, context);
    assertParseCompatibleOnly(descriptor, Long.valueOf(-1), MAX_UNSIGNED_LONG, context);
    
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context);
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context);
  }
    
  @Test
  public void messages() {
    FieldDescriptor descriptor = requiredField(Coverage.Message.class);
    ScalarContext context = ScalarContext.QUOTED;

    Coverage.Bool field = Coverage.Bool.newBuilder().setRequired(true).setOptional(false).addRepeated(false)
        .addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    
    // nothing is valid in strict mode
    assertParseCompatibleOnly(descriptor, field.toByteString(), Base64.encodeBase64String(field.toByteArray()), context);
    // technically, downstage the random bytes should be validated to not match the descriptor, but parser doesn't check
    assertParseCompatibleOnly(descriptor, ByteString.copyFrom(randomBytes), Base64.encodeBase64String(randomBytes), context);
    // even for invalid base 64
    assertParseCompatibleOnly(descriptor, ByteString.copyFrom(Base64.decodeBase64(invalidLengthBase64)), invalidLengthBase64, context);
  }
  
  @Test
  public void nestedMessages() {
    FieldDescriptor descriptor = requiredField(Coverage.NestedMessage.class);
    ScalarContext context = ScalarContext.QUOTED;

    Coverage.NestedMessage.Nest field = Coverage.NestedMessage.Nest.newBuilder().setS("test string").build();
    // nothing is valid in strict mode
    assertParseCompatibleOnly(descriptor, field.toByteString(), Base64.encodeBase64String(field.toByteArray()), context);
    // technically, downstage the random bytes should be validated to not match the descriptor, but parser doesn't check
    assertParseCompatibleOnly(descriptor, ByteString.copyFrom(randomBytes), Base64.encodeBase64String(randomBytes), context);
    // even for invalid base 64
    assertParseCompatibleOnly(descriptor, ByteString.copyFrom(Base64.decodeBase64(invalidLengthBase64)), invalidLengthBase64, context);
  }
  
  @Test
  public void sfixed32s() {
    // these are compatible with fixed32
    FieldDescriptor descriptor = requiredField(Coverage.Sfixed32.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should only accept signed int range
    assertParseBoth(descriptor, Integer.valueOf(Integer.MAX_VALUE), MAX_SIGNED_INT, context);
    assertParseBoth(descriptor, Integer.valueOf(Integer.MIN_VALUE), MIN_SIGNED_INT, context);
    // compatible should accept unsigned
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_INT, context);
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_INT_MINUS1, context);
    assertParseException(descriptor, MAX_UNSIGNED_INT_PLUS1, context);
    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
  }

  @Test
  public void sfixed64s() {
    // these are compatible with fixed32
    FieldDescriptor descriptor = requiredField(Coverage.Sfixed64.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should only accept signed int range
    assertParseBoth(descriptor, Long.valueOf(Long.MAX_VALUE), MAX_SIGNED_LONG, context);
    assertParseBoth(descriptor, Long.valueOf(Long.MIN_VALUE), MIN_SIGNED_LONG, context);
    // compatible should accept unsigned
    assertParseCompatibleOnly(descriptor, Long.valueOf(-1), MAX_UNSIGNED_LONG, context);
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context);
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context);
    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
  }
  
  @Test
  public void sint32s() {
    // these are compatible with sint64
    FieldDescriptor descriptor = requiredField(Coverage.Sint32.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should accept int range
    assertParseBoth(descriptor, Integer.valueOf(Integer.MAX_VALUE), MAX_SIGNED_INT, context);
    assertParseBoth(descriptor, Integer.valueOf(Integer.MIN_VALUE), MIN_SIGNED_INT, context);
    
    // compatible should accept from max signed long down to min signed long
    // max signed int + 1:  0x7FFFFFFF + 0x01 -> 0x80000000 -> min signed int 
    assertParseCompatibleOnly(descriptor, Integer.valueOf(Integer.MIN_VALUE), MAX_SIGNED_INT_PLUS1, context);
    // min signed int - 1:  0x80000000 + 0xffffffff -> 0x7fffffff -> max signed int
    assertParseCompatibleOnly(descriptor, Integer.valueOf(Integer.MAX_VALUE), MIN_SIGNED_INT_MINUS1, context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_INT, context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), MAX_SIGNED_LONG, context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(0), MIN_SIGNED_LONG, context);
    
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context);
    assertParseException(descriptor, MAX_SIGNED_LONG_PLUS1, context);
    assertParseException(descriptor, "true", context);
    assertParseException(descriptor, "false", context);
  }
  
  @Test
  public void sint64s() {
    // these are compatible with sint32
    FieldDescriptor descriptor = requiredField(Coverage.Sint64.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should accept long range
    assertParseBoth(descriptor, Long.valueOf(Long.MAX_VALUE), MAX_SIGNED_LONG, context);
    assertParseBoth(descriptor, Long.valueOf(Long.MIN_VALUE), MIN_SIGNED_LONG, context);
    
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context);
    assertParseException(descriptor, MAX_SIGNED_LONG_PLUS1, context);
  }
  
  @Test
  public void strings() {
    FieldDescriptor descriptor = requiredField(Coverage.String1.class);
    ScalarContext context = ScalarContext.QUOTED;
    // with default parser, strict and compatible pass through with no change
    assertParseBoth(descriptor, utf8String, utf8String, context);
    assertParseBoth(descriptor, invalidLengthBase64, invalidLengthBase64, context);
  }
  
  @Test
  public void unit32s() {
    // these are unsigned, and are compatible with int32, int64, uint64, and bool
    // "If a number is parsed from the wire which doesn't fit in the corresponding type,
    //  you will get the same effect as if you had cast the number to that type in C++" 
    FieldDescriptor descriptor = requiredField(Coverage.Uint32.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should accept int range
    assertParseBoth(descriptor, Integer.valueOf(Integer.MAX_VALUE), MAX_SIGNED_INT, context);
    assertParseBoth(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_INT, context);
    
    // compatible should accept from max unsigned long down to min long and boolean true/false
    assertParseCompatibleOnly(descriptor, Integer.valueOf(0), "false", context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(1), "true", context);
    // min signed int - 1:  0x80000000 + 0xffffffff -> 0x7fffffff -> max signed int
    assertParseCompatibleOnly(descriptor, Integer.valueOf(Integer.MAX_VALUE), MIN_SIGNED_INT_MINUS1, context);
    assertParseCompatibleOnly(descriptor, Integer.valueOf(-1), MAX_UNSIGNED_LONG, context);
    // min signed long: 0x8000000000000000 -> cast to 0x00000000
    assertParseCompatibleOnly(descriptor, Integer.valueOf(0), MIN_SIGNED_LONG, context);
    
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context); // min - 1
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context); // max + 1
  }
  
  @Test
  public void uint64s() {
    // these are compatible with uint32, int32, int64, and bool
    // "If a number is parsed from the wire which doesn't fit in the corresponding type,
    //  you will get the same effect as if you had cast the number to that type in C++" 
    FieldDescriptor descriptor = requiredField(Coverage.Uint64.class);
    ScalarContext context = ScalarContext.UNQUOTED;

    // both should accept long range
    assertParseBoth(descriptor, Long.valueOf(Long.MAX_VALUE), MAX_SIGNED_LONG, context);
    assertParseBoth(descriptor, Long.valueOf(-1), MAX_UNSIGNED_LONG, context);
    
    // compatible should accept from max unsigned long down to min long and boolean true/false
    assertParseCompatibleOnly(descriptor, Long.valueOf(0), "false", context);
    assertParseCompatibleOnly(descriptor, Long.valueOf(1), "true", context);
    
    // both should reject out of range
    assertParseException(descriptor, MIN_SIGNED_LONG_MINUS1, context);
    assertParseException(descriptor, MAX_UNSIGNED_LONG_PLUS1, context);
  }
}
