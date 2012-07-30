package polybuf.core;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.DefaultStringParser;
import polybuf.core.StringParser;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;

public class DefaultStringParserTest {
  public static final String invalidLengthBase64 = "abc";
  //'C' is 0x02 in base 64 and char prior to end pad must have 0 for bottom two bits  
  public static final String invalid1PadBase64 = "abC=";
  // 'I' is 0x08 in base 64 and char prior to end double pad must have 0 for bottom four bits 
  public static final String invalid2PadBase64 = "aI==";
  public static final String utf8String = "𝄞 E♭7(♯11) D7(♯9) ♬ ♪";
  public static final byte[] utf8Bytes = utf8String.getBytes(Charsets.UTF_8);
  public static final byte[] randomBytes;
  public static final byte[] invalidUtf8Bytes = new byte[] {(byte)0xfe, (byte)0xfe, (byte)0xff, (byte)0xff};
  
  // The probabilistic test we do for compatibility testing
  // fails for this word.  It is determined to be a base64 
  // encoding and therefore comes back as a decoding of the
  // original.
  // This "bug" is expected.  If the corresponding tests fail,
  // try to find a new word to highlight this undesirable behavior.  
  public static final String confusingBase64Word = "Mice";
  
  private final StringParser parser = new DefaultStringParser();
  
  static {
    randomBytes = new byte[512];
    new Random().nextBytes(randomBytes);
  }
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

  @Test
  public void invalidLengthStrictBytes() {
    // default parser doesn't know or care
    assertEquals(ByteString.copyFrom(Base64.decodeBase64(invalidLengthBase64)), parser.asStrictBytes(invalidLengthBase64));
  }
  
  @Test
  public void invalid1PadStrictBytes() {
    // default parser doesn't know or care
    assertEquals(ByteString.copyFrom(Base64.decodeBase64(invalid1PadBase64)), parser.asStrictBytes(invalid1PadBase64));
  }
  
  @Test
  public void invalid2PadStrictBytes()  {
    // default parser doesn't know or care
    assertEquals(ByteString.copyFrom(Base64.decodeBase64(invalid2PadBase64)), parser.asStrictBytes(invalid2PadBase64));
  }
  
  @Test
  public void compatibleStringFromUtf8String() {
    assertEquals(utf8String, parser.asCompatibleString(utf8String));
  }
  
  @Test
  public void compatibleStringFromBase64EncodedUtf8() {
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(base64Encoding, parser.asCompatibleString(base64Encoding));
  }
  
  @Test
  public void compatibleStringFromConfusingString() {
    assertTrue(confusingBase64Word.equals(parser.asCompatibleString(confusingBase64Word)));
  }
  
  @Test
  public void compatibleStringFromBase64InvalidUtf8() {
    String base64Encoding = Base64.encodeBase64String(invalidUtf8Bytes);
    assertEquals(base64Encoding,parser.asCompatibleString(base64Encoding));
  }
  
  @Test
  public void compatibleBytesFromUtf8String() {
    assertEquals(ByteString.copyFrom(utf8Bytes), parser.asCompatibleBytes(utf8String));
  }
  
  @Test
  public void compatibleBytesFromBase64EncodedUtf8() {
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(ByteString.copyFromUtf8(base64Encoding), parser.asCompatibleBytes(base64Encoding));
  }
  
  @Test
  public void compatibleBytesFromBase64RandomBytes() {
    String base64Encoding = Base64.encodeBase64String(randomBytes);
    assertEquals(ByteString.copyFromUtf8(base64Encoding), parser.asCompatibleBytes(base64Encoding));
  }
  
  @Test
  public void compatibleMessageBytesFromUtf8String() {
    assertEquals(ByteString.copyFrom(Base64.decodeBase64(utf8Bytes)), parser.asCompatibleMessageBytes(utf8String));
  }
  
  @Test
  public void compatibleMessageBytesFromBase64EncodedUtf8() {
    // should detect this is base64 encoding and decode, the downstream message parsing may fail, but this step just decodes
    String base64Encoding = Base64.encodeBase64String(utf8Bytes);
    assertEquals(ByteString.copyFrom(utf8Bytes), parser.asCompatibleMessageBytes(base64Encoding));
  }
  
  @Test
  public void compatibleMessageBytesFromBase64RandomBytes() {
    String base64Encoding = Base64.encodeBase64String(randomBytes);
    assertEquals(ByteString.copyFrom(randomBytes), parser.asCompatibleMessageBytes(base64Encoding));
  }
}
