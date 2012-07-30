package polybuf.classifiers;

import static org.junit.Assert.*;
import static polybuf.core.DefaultStringParserTest.*;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.ParseException;
import polybuf.core.StringParser;

import com.google.protobuf.ByteString;

public class HeuristicStringParserTest {

  private final StringParser parser = new HeuristicStringParser();

  @Test
  public void utf8StrictBytes() {
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(ByteString.copyFrom(utf8Bytes), parser.asStrictBytes(base64Encoding));
  }

  @Test
  public void randomStrictBytes() {
    String base64Encoding = Base64.encodeBase64String(randomBytes);
    assertEquals(ByteString.copyFrom(randomBytes), parser.asStrictBytes(base64Encoding));
  }

  @Test(expected = ParseException.class)
  public void invalidLengthStrictBytes() {
    parser.asStrictBytes(invalidLengthBase64);
  }

  @Test(expected = ParseException.class)
  public void invalid1PadStrictBytes() {
    parser.asStrictBytes(invalid1PadBase64);
  }

  @Test(expected = ParseException.class)
  public void invalid2PadStrictBytes() {
    parser.asStrictBytes(invalid2PadBase64);
  }

  @Test
  public void compatibleStringFromUtf8String() {
    assertEquals(utf8String, parser.asCompatibleString(utf8String));
  }

  @Test
  public void compatibleStringFromBase64EncodedUtf8() {
    // should detect this is base64 encoding and decode
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(utf8String, parser.asCompatibleString(base64Encoding));
  }

  @Test
  public void compatibleStringFromConfusingStringShouldFail() {
    assertFalse(confusingBase64Word.equals(parser.asCompatibleString(confusingBase64Word)));
  }

  @Test
  public void compatibleStringFromBase64InvalidUtf8() {
    // should detect this is base64, but then realize can't decode to UTF-8, so leave it as original bytes
    String base64Encoding = Base64.encodeBase64String(invalidUtf8Bytes);
    assertEquals(base64Encoding, parser.asCompatibleString(base64Encoding));
  }

  @Test
  public void compatibleBytesFromUtf8String() {
    assertEquals(ByteString.copyFrom(utf8Bytes), parser.asCompatibleBytes(utf8String));
  }

  @Test
  public void compatibleBytesFromBase64EncodedUtf8() {
    // should detect this is base64 encoding and decode
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(ByteString.copyFrom(utf8Bytes), parser.asCompatibleBytes(base64Encoding));
  }

  @Test
  public void compatibleBytesFromBase64RandomBytes() {
    String base64Encoding = Base64.encodeBase64String(randomBytes);
    assertEquals(ByteString.copyFrom(randomBytes), parser.asCompatibleBytes(base64Encoding));
  }

  @Test(expected = ParseException.class)
  // not valid base64 so can't decode to message
  public void compatibleMessageBytesFromUtf8String() {
    parser.asCompatibleMessageBytes(utf8String);
  }

  @Test
  public void compatibleMessageBytesFromBase64EncodedUtf8() {
    // should detect this is base64 encoding and decode, the downstream message parsing may fail, but this step just
    // decodes
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(ByteString.copyFrom(utf8Bytes), parser.asCompatibleMessageBytes(base64Encoding));
  }

  @Test
  public void compatibleMessageBytesFromBase64RandomBytes() {
    String base64Encoding = Base64.encodeBase64String(randomBytes);
    assertEquals(ByteString.copyFrom(randomBytes), parser.asCompatibleMessageBytes(base64Encoding));
  }
}
