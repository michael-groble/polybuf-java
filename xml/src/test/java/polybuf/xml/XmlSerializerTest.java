package polybuf.xml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.BaseSerializerTest;
import polybuf.core.DefaultStringParserTest;
import polybuf.core.ParseException;
import polybuf.core.StreamHelper;
import polybuf.core.config.SerializerConfig;
import polybuf.core.test.Coverage;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;

public class XmlSerializerTest extends BaseSerializerTest<InputStream,OutputStream> {
  

  public void setRegistry(String namespaceUri, String prefix) throws Exception {
    strictSerializer = XmlStream.serializer(
        SerializerConfig.builder(Coverage.class).setIsStrict(true).setNamespaceUri(namespaceUri).build(), prefix);
    compatibleSerializer = XmlStream.serializer(SerializerConfig.builder(Coverage.class).setIsStrict(false)
        .setNamespaceUri(namespaceUri).build(), prefix);
  }

  @Test
  public void emptyMessage() throws Exception {
    setRegistry(null,null);
    assertRootException(in("")); // must have message
    assertRootException(in("<?xml version=`1.0` encoding=`UTF-8`?>")); // must have message
  }
  
  @Test
  public void noEndTag() throws Exception {
    setRegistry(null,null);
    assertRootException(in("<coverage.Bool>"));
  }
  
  @Test
  public void singleEmptyTag() throws Exception {
    setRegistry(null,null);
    assertEquals(Coverage.Bool.newBuilder().buildPartial(),
        buildRoot(in("<coverage.Bool />"), strict).buildPartial());
  }
  
  @Test
  public void emptyTagPair() throws Exception {
    setRegistry(null,null);
    assertEquals(Coverage.Bool.newBuilder().buildPartial(),
        buildRoot(in("<coverage.Bool></coverage.Bool>"), strict).buildPartial());
  }
  
  @Test
  public void unsupportedRootArray() throws Exception {
    setRegistry(null,null);
    assertRepeatedRootUnsupported(in("<roots></roots>"));
  }
  
  @Test
  public void bools() throws Exception {
    setRegistry(null,null);
    Coverage.Bool expected = Coverage.Bool.newBuilder()
        .setRequired(true)
        .setOptional(false)
        .addRepeated(false).addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
          .append("<coverage.Bool>")
          .append(  "<required>true</required>")
          .append(  "<optional>false</optional>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>true</repeated>")
          .append("</coverage.Bool>")
          .toString()));
  }
  
  @Test
  public void boolsNsUri() throws Exception {
    String uri = "http://example.com/test";
    setRegistry(uri,null);
    Coverage.Bool expected = Coverage.Bool.newBuilder()
        .setRequired(true)
        .setOptional(false)
        .addRepeated(false).addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
          .append("<coverage.Bool ").append("xmlns=`").append(uri).append("`>")
          .append(  "<required>true</required>")
          .append(  "<optional>false</optional>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>true</repeated>")
          .append("</coverage.Bool>")
          .toString()));
  }
  
  @Test
  public void boolsNsUriWithPrefix() throws Exception {
    String uri = "http://example.com/test";
    setRegistry(uri,"b");
    Coverage.Bool expected = Coverage.Bool.newBuilder()
        .setRequired(true)
        .setOptional(false)
        .addRepeated(false).addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
          .append("<b:coverage.Bool ").append("xmlns:b=`").append(uri).append("`>")
          .append(  "<b:required>true</b:required>")
          .append(  "<b:optional>false</b:optional>")
          .append(  "<b:repeated>false</b:repeated>")
          .append(  "<b:repeated>true</b:repeated>")
          .append(  "<b:repeated>false</b:repeated>")
          .append(  "<b:repeated>true</b:repeated>")
          .append(  "<b:repeated>true</b:repeated>")
          .append("</b:coverage.Bool>")
          .toString()));
  }
  @Test(expected=IllegalArgumentException.class)
  public void boolsNullUriWithPrefix() throws Exception {
    setRegistry(null,"b");
  }
  
  @Test
  public void messages() throws Exception {
    setRegistry(null,null);
    Coverage.Message expected = Coverage.Message.newBuilder()
        .setRequired(Coverage.Bool.newBuilder()
            .setRequired(true)
            .setDefaulted(false)
            .addRepeated(true).addRepeated(false))
        .setOptional(Coverage.Bool.newBuilder()
            .setRequired(true))
        .addRepeated(Coverage.Bool.newBuilder().setRequired(false))
        .addRepeated(Coverage.Bool.newBuilder().setRequired(true)).build();
    
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
    .append("<coverage.Message>")
    .append(  "<required>")
    .append(    "<required>true</required>")
    .append(    "<defaulted>false</defaulted>")
    .append(    "<repeated>true</repeated>")
    .append(    "<repeated>false</repeated>")
    .append(  "</required>")
    .append(  "<optional>")
    .append(    "<required>true</required>")
    .append(  "</optional>")
    .append(  "<repeated>")
    .append(    "<required>false</required>")
    .append(  "</repeated>")
    .append(  "<repeated>")
    .append(    "<required>true</required>")
    .append(  "</repeated>")
    .append("</coverage.Message>")
    .toString()));
  }
  
  @Test
  public void compatibleMessage() throws Exception {
    setRegistry(null,null);
    // in compatible mode, messages can be sent as bytes
    Coverage.Bool bool = Coverage.Bool.newBuilder()
    .setRequired(true)
    .setDefaulted(false)
    .addRepeated(true).addRepeated(false).build();
    
    assertRootCompatibleOnly(Coverage.Message.newBuilder().setRequired(bool).build(), 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
    .append("<coverage.Message>")
    .append(  "<required>")
    .append(     Base64.encodeBase64String(bool.toByteArray()))
    .append(  "</required>")
    .append("</coverage.Message>")
    .toString()));
  }
  
  @Test
  public void compatibleMessageInvalidLength() throws Exception {
    setRegistry(null,null);
    // but invalid Base64 should not be parsed
    assertRootException(
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
        .append("<coverage.Message>")
        .append(  "<required>")
        .append(     DefaultStringParserTest.invalidLengthBase64)
        .append(  "</required>")
        .append("</coverage.Message>").toString()));
  }
  
  @Test
  public void compatibleMessageInvalidPad() throws Exception {
    setRegistry(null,null);
    assertRootException(
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
        .append("<coverage.Message>")
        .append(  "<required>")
        .append(     DefaultStringParserTest.invalid1PadBase64)
        .append(  "</required>")
        .append("</coverage.Message>").toString()));
  }
  
  @Test(expected=ParseException.class)
  public void messageWithInvalidProtobufBytes() throws Exception {
    setRegistry(null,null);
    String invalidEncodedBytes = Base64.encodeBase64String(new byte[] {0x0,0x0,0x0,0x0}); // field numbers can't be
                                                                                          // zero so this should fail
    buildRoot(in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
    .append("<coverage.Message>")
    .append(  "<required>")
    .append(     invalidEncodedBytes)
    .append(  "</required>")
    .append("</coverage.Message>").toString()), compatible);
  }

  protected InputStream in(String input) {
    return new ByteArrayInputStream(input.replace('`', '"').getBytes(Charsets.UTF_8));
  }
  
  @Override
  protected void assertEncode(InputStream expectedAsInput, Message message) throws Exception {
    StreamHelper.assertEncodeStream(expectedAsInput, message, strictSerializer);
  }

  @Override
  protected <T extends Message> void assertEncode(InputStream expectedAsInput, List<T> messages) throws Exception {
    StreamHelper.assertEncodeStream(expectedAsInput, messages, strictSerializer);
  }
}
