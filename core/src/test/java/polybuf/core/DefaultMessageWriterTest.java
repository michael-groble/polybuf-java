package polybuf.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import polybuf.core.config.DefaultFieldNamingStrategy;
import polybuf.core.test.Coverage;
import polybuf.core.test.ExtensionsBase;
import polybuf.core.test.ExtensionsExt;
import polybuf.core.test.NoOptions;
import polybuf.core.util.Reflection;

import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;


public class DefaultMessageWriterTest {

  private StringBuilder log;
  private DefaultMessageWriter<StringBuilder> writer;
  
  @Before
  public void init() {
    log = new StringBuilder();
    writer = new DefaultMessageWriter<StringBuilder>(LoggingEncoder.factory(), new DefaultFieldNamingStrategy());
  }
  
  @Test
  public void emptyMessage() throws IOException {
    NoOptions.Message2 empty = NoOptions.Message2.newBuilder().build();
    writer.writeTo("empty", empty, log);
    assertEquals(
        "startRootMessage empty\n"+
        "endRootMessage empty\n",log.toString());
  }

  @Test
  public void emptyMessageList() throws IOException {
    writer.writeTo("emptyList", new ArrayList<Message>(0), log);
    assertEquals(
        "startRootList emptyList\n"+
        "endRootList emptyList\n",log.toString());
  }
  
  @Test
  public void listOfEmptyMessages() throws IOException {
    NoOptions.Message2 empty = NoOptions.Message2.newBuilder().build();
    writer.writeTo("listOfEmpties", Arrays.asList(empty,empty), log);
    assertEquals(
        "startRootList listOfEmpties\n"+
        "startRepeatedRoot listOfEmpties\n"+
        "endRepeatedRoot listOfEmpties\n"+
        "startRepeatedRoot listOfEmpties\n"+
        "endRepeatedRoot listOfEmpties\n"+
        "endRootList listOfEmpties\n",log.toString());
  }
  
  @Test
  public void singleAndRepeatedScalarFields() throws IOException {
    Coverage.Bool message = Coverage.Bool.newBuilder().setRequired(true).addRepeated(false).addRepeated(true).build();
    writer.writeTo("bool", message, log);
    assertEquals(
        "startRootMessage bool\n"+
        "scalarBooleanField required true\n"+
        "startRepeatedField repeated\n"+
        "repeatedScalarBooleanField repeated false\n"+
        "repeatedScalarBooleanField repeated true\n"+
        "endRepeatedField repeated\n"+
        "endRootMessage bool\n", log.toString());
  }

  
  @Test
  public void singleAndRepeatedMessageFields() throws IOException {
    Coverage.Bool boolTrue = Coverage.Bool.newBuilder().setRequired(true).build();
    Coverage.Bool boolFalse = Coverage.Bool.newBuilder().setRequired(false).build();
    Coverage.Message message = Coverage.Message.newBuilder().setRequired(boolFalse).addRepeated(boolFalse).addRepeated(boolTrue).build();
    writer.writeTo("message", message, log);
    assertEquals(
        "startRootMessage message\n"+
        "startMessageField required\n"+
        "scalarBooleanField required false\n"+
        "endMessageField required\n"+
        "startRepeatedField repeated\n"+
        "startRepeatedMessageField repeated\n"+
        "scalarBooleanField required false\n"+
        "endRepeatedMessageField repeated\n"+
        "startRepeatedMessageField repeated\n"+
        "scalarBooleanField required true\n"+
        "endRepeatedMessageField repeated\n"+
        "endRepeatedField repeated\n"+
        "endRootMessage message\n", log.toString());
  }
  
  @Test
  public void extensionField() throws IOException {
    ExtensionsBase.Message1 message = ExtensionsBase.Message1.newBuilder().setId("idstring").setExtension(ExtensionsExt.id, 10).build();
    writer.writeTo("extension", message, log);
    assertEquals(
        "startRootMessage extension\n"+
        "scalarStringField id idstring\n"+
        "scalarIntegerField ext.id 10\n"+
        "endRootMessage extension\n", log.toString());
  }
  
  
  // OK, now make sure all the scalar field types are covered
  
  private void assertScalar(String expectedType, String expectedValue, Class<? extends GeneratedMessage> messageClass, Object value) throws IOException {
      Descriptor descriptor = Reflection.invokeStaticGetter(messageClass, "getDescriptor", Descriptor.class);
      Message.Builder builder = Reflection.invokeStaticGetter(messageClass,"newBuilder",Message.Builder.class);
      builder.setField(descriptor.findFieldByNumber(1),value);
      builder.addRepeatedField(descriptor.findFieldByNumber(4), value);
      writer.writeTo("scalar", builder.build(), log);
      assertEquals(
          "startRootMessage scalar\n"+
          "scalar"+expectedType+"Field required "+ expectedValue+ "\n"+
          "startRepeatedField repeated\n"+
          "repeatedScalar"+expectedType+"Field repeated "+ expectedValue+ "\n"+
          "endRepeatedField repeated\n"+
          "endRootMessage scalar\n", log.toString());
  }
  
  @Test
  public void bytes() throws IOException {
    assertScalar("String", Base64.encodeBase64String("hello".getBytes(Charsets.UTF_8)),
        Coverage.Bytes.class, ByteString.copyFromUtf8("hello"));
  }
  
  @Test
  public void doubles() throws IOException {
    assertScalar("Double", "NaN",
        Coverage.Double.class, Double.NaN);
  }

  @Test
  public void enums() throws IOException {
    assertScalar("String", "A",
        Coverage.Enum.class, Coverage.Enum1.A.getValueDescriptor());
  }

  @Test
  public void fixed32s() throws IOException {
    // this is unsigned, so gets called as long
    assertScalar("Long", UnsignedInteger.MAX_VALUE.toString(),
        Coverage.Fixed32.class, UnsignedInteger.MAX_VALUE.intValue());
  }
  
  @Test
  public void fixed64s() throws IOException {
    // this is unsigned, so gets called as UnsignedLong
    assertScalar("UnsignedLong", UnsignedLong.MAX_VALUE.toString(),
        Coverage.Fixed64.class, UnsignedLong.MAX_VALUE.longValue());
  }
  
  @Test
  public void floats() throws IOException {
    assertScalar("Float", "Infinity",
        Coverage.Float.class, Float.POSITIVE_INFINITY);
  }
  
  @Test
  public void int32s() throws IOException {
    assertScalar("Integer", String.valueOf(Integer.MAX_VALUE),
        Coverage.Int32.class, Integer.MAX_VALUE);
  }
  
  @Test
  public void int64s() throws IOException {
    assertScalar("Long", String.valueOf(Long.MAX_VALUE),
        Coverage.Int64.class, Long.MAX_VALUE);
  }
  
  @Test
  public void sfixed32s() throws IOException {
    assertScalar("Integer", String.valueOf(Integer.MIN_VALUE),
        Coverage.Sfixed32.class, Integer.MIN_VALUE);
  }
  
  @Test
  public void sfixed64s() throws IOException {
    assertScalar("Long", String.valueOf(Long.MIN_VALUE),
        Coverage.Sfixed64.class, Long.MIN_VALUE);
  }
  
  @Test
  public void sint32s() throws IOException {
    assertScalar("Integer", String.valueOf(Integer.MAX_VALUE),
        Coverage.Sint32.class, Integer.MAX_VALUE);
  }
  
  @Test
  public void sint64s() throws IOException {
    assertScalar("Long", String.valueOf(Long.MAX_VALUE),
        Coverage.Sint64.class, Long.MAX_VALUE);
  }
  
  @Test
  public void strings() throws IOException {
    assertScalar("String", "hello",
        Coverage.String1.class, "hello");
  }

  @Test
  public void uint32s() throws IOException {
    // this is unsigned, so gets called as long
    assertScalar("Long", UnsignedInteger.MAX_VALUE.toString(),
        Coverage.Uint32.class, UnsignedInteger.MAX_VALUE.intValue());
  }
  
  @Test
  public void uint64s() throws IOException {
    // this is unsigned, so gets called as UnsignedLong
    assertScalar("UnsignedLong", UnsignedLong.MAX_VALUE.toString(),
        Coverage.Uint64.class, UnsignedLong.MAX_VALUE.longValue());
  }
}
