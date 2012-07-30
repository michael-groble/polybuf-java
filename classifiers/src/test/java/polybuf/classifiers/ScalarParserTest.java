package polybuf.classifiers;

import static org.junit.Assert.*;
import static polybuf.core.DefaultStringParserTest.*;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.ParseException;
import polybuf.core.ScalarContext;
import polybuf.core.ScalarParser;
import polybuf.core.TestParser;
import polybuf.core.test.Coverage;
import polybuf.core.util.Reflection;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;

public class ScalarParserTest {

  private static ScalarParser parser = new TestParser(new HeuristicStringParser());
  private static boolean strict = true;
  private static boolean compatible = false;

  private FieldDescriptor requiredField(Class<? extends GeneratedMessage> messageClass) {
    Descriptor descriptor = Reflection.invokeStaticGetter(messageClass, "getDescriptor", Descriptor.class);
    return descriptor.findFieldByNumber(1);
  }

  private static void assertParseBoth(FieldDescriptor descriptor, Object expected, String string, ScalarContext context) {
    assertParse(descriptor, expected, string, context, strict);
    assertParse(descriptor, expected, string, context, compatible);
  }

  private static void assertParseCompatible(FieldDescriptor descriptor, Object expected, String string,
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

  private static void assertParseEquals(FieldDescriptor descriptor, Object expected, String string,
      ScalarContext expectedContext, boolean isStrict) {
    assertEquals(expected, parser.parse(descriptor, string, expectedContext, isStrict));
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
  public void bytes() {
    FieldDescriptor descriptor = requiredField(Coverage.Bytes.class);
    ScalarContext context = ScalarContext.QUOTED;
    assertParseBoth(descriptor, ByteString.copyFrom(utf8Bytes), Base64.encodeBase64String(utf8Bytes), context);
    assertParseBoth(descriptor, ByteString.copyFrom(randomBytes), Base64.encodeBase64String(randomBytes), context);
    // invalid base64 is error in strict, but passthrough as string in
    // compatible
    assertParseCompatible(descriptor, ByteString.copyFromUtf8(invalidLengthBase64), invalidLengthBase64, context);
    assertParseCompatible(descriptor, ByteString.copyFromUtf8(invalid1PadBase64), invalid1PadBase64, context);
    assertParseCompatible(descriptor, ByteString.copyFromUtf8(invalid2PadBase64), invalid2PadBase64, context);
  }

  @Test
  public void bytesNeedsFix() {
    FieldDescriptor descriptor = requiredField(Coverage.Bytes.class);
    ScalarContext context = ScalarContext.QUOTED;

    // this is a special case, this word gets detected as base64, not as a "plain" word
    // Normally, in compatible mode, we would want this to come through undecoded
    // assert the encoding since we expect the behavior given the existing code base
    assertParseEquals(descriptor, ByteString.copyFrom(Base64.decodeBase64(confusingBase64Word)), confusingBase64Word,
        context, strict);
    assertParseEquals(descriptor, ByteString.copyFrom(Base64.decodeBase64(confusingBase64Word)), confusingBase64Word,
        context, compatible);
    // should be this if the word was correctly classified
    // assertParseEquals(descriptor,ByteString.copyFromUtf8(confusingBase64Word), confusingBase64Word, context,
    // compatible);
  }

  @Test
  public void messages() {
    FieldDescriptor descriptor = requiredField(Coverage.Message.class);
    ScalarContext context = ScalarContext.QUOTED;

    Coverage.Bool field = Coverage.Bool.newBuilder().setRequired(true).setOptional(false).addRepeated(false)
        .addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    // nothing is valid in strict mode
    assertParseCompatible(descriptor, field.toByteString(), Base64.encodeBase64String(field.toByteArray()), context);
    // technically, downstage the random bytes should be validated to not match the descriptor, but parser doesn't check
    assertParseCompatible(descriptor, ByteString.copyFrom(randomBytes), Base64.encodeBase64String(randomBytes), context);

    assertParseException(descriptor, invalidLengthBase64, context);
  }

  @Test
  public void nestedMessages() {
    FieldDescriptor descriptor = requiredField(Coverage.NestedMessage.class);
    ScalarContext context = ScalarContext.QUOTED;

    Coverage.NestedMessage.Nest field = Coverage.NestedMessage.Nest.newBuilder().setS("test string").build();
    // nothing is valid in strict mode
    assertParseCompatible(descriptor, field.toByteString(), Base64.encodeBase64String(field.toByteArray()), context);
    // technically, downstage the random bytes should be validated to not match the descriptor, but parser doesn't check
    assertParseCompatible(descriptor, ByteString.copyFrom(randomBytes), Base64.encodeBase64String(randomBytes), context);

    assertParseException(descriptor, invalidLengthBase64, context);
  }

  @Test
  public void strings() {
    FieldDescriptor descriptor = requiredField(Coverage.String1.class);
    ScalarContext context = ScalarContext.QUOTED;

    assertParseBoth(descriptor, utf8String, utf8String, context);
    // invalid base64 should come through in strict and compatible
    assertParseBoth(descriptor, invalidLengthBase64, invalidLengthBase64, context);
    assertParseBoth(descriptor, invalid1PadBase64, invalid1PadBase64, context);
    assertParseBoth(descriptor, invalid2PadBase64, invalid2PadBase64, context);

    // in compatible mode, try to decode if it looks like base64, pass through in strict
    assertParseEquals(descriptor, Base64.encodeBase64String(utf8Bytes), Base64.encodeBase64String(utf8Bytes), context,
        strict);
    assertParseEquals(descriptor, utf8String, Base64.encodeBase64String(utf8Bytes), context, compatible);
  }

  @Test
  public void stringsNeedsFix() {
    FieldDescriptor descriptor = requiredField(Coverage.String1.class);
    ScalarContext context = ScalarContext.QUOTED;

    // this is a special case, this word gets detected as base64, not as a "plain" word
    // Normally, in compatible mode, we would want this to come through undecoded
    // assert the encoding since we expect the behavior given the existing code base
    assertParseEquals(descriptor, confusingBase64Word, confusingBase64Word, context, strict);
    assertParseEquals(descriptor, new String(Base64.decodeBase64(confusingBase64Word), Charsets.UTF_8),
        confusingBase64Word, context, compatible);
    // should be this if the word was correctly classified
    // assertParseEquals(descriptor,confusingBase64Word,confusingBase64Word, context, compatible);
  }
}
