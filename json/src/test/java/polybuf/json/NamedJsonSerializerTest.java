package polybuf.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import polybuf.core.BaseSerializerTest;
import polybuf.core.DefaultStringParserTest;
import polybuf.core.ParseException;
import polybuf.core.StreamHelper;
import polybuf.core.config.SerializerConfig;
import polybuf.core.test.Coverage;
import polybuf.core.util.Reflection;

import com.google.common.base.Charsets;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

public class NamedJsonSerializerTest extends BaseSerializerTest<InputStream, OutputStream> {

  @Before
  public void setRegistry() throws Exception {
    strictSerializer = NamedJson.serializer(SerializerConfig.builder(Coverage.class).setIsStrict(true).build());
    compatibleSerializer = NamedJson.serializer(SerializerConfig.builder(Coverage.class).setIsStrict(false).build());
  }

  private void assertStructuralCompliance(Class<? extends GeneratedMessage> klass, Object goodValue, String goodString)
      throws IOException {
    String name = klass.getSimpleName();
    Builder builder = Reflection.invokeStaticGetter(klass, "newBuilder", Message.Builder.class);
    Message message = builder.setField(builder.getDescriptorForType().findFieldByNumber(1), goodValue).build();

    assertRootParseBoth(message, in("{`coverage." + name + "`: {`required`: " + goodString + ", `repeated`: null }}"));
    assertRootParseBoth(message, in("{`coverage." + name + "`: {`required`: " + goodString + ", `repeated`: [] }}"));

    // use builder, ignore message name from json
    assertParseBoth(message, builder, in("{`coverage.ignore`: {`required`: " + goodString + ", `repeated`: null }}"));
    assertParseBoth(message, builder, in("{`coverage.ignore`: {`required`: " + goodString + ", `repeated`: [] }}"));

    // make sure it works for repeated as well
    assertParseBoth(Arrays.asList(message), builder, in("{`coverage.ignore`: [{`required`: " + goodString + "}]}"));
    assertParseBoth(Arrays.asList(message), builder, in("{`coverage.ignore`: [{`required`: " + goodString + "}]}"));

    assertRootException(in("")); // must have message
    assertRootException(in("null")); // must have message
    assertRootException(in("{}")); // must have message
    assertRootException(in("{`coverage." + name + "`: null }")); // must have message
    assertRootException(in("{`coverage." + name + "`: [] }")); // can't be an empty list
    assertRootException(in("{`coverage." + name + "`: [{`required`: " + goodString + "}]")); // or a good list
    assertRootCompatibleOnly(message, in("{`coverage." + name + "`: {`required`: " + goodString + ", `bad`: "
        + goodString + "}}")); // unrecognized field
    assertRootException(in("{`coverage." + name + "`: {`required`: " + goodString + ", `repeated`: [" + goodString
        + ", null]}}")); // no null value in arrays
    assertRootException(in("{`coverage." + name + "`: {`required`: " + goodString + ", `repeated`: [[" + goodString
        + "]]}}")); // no nested arrays

    assertRepeatedRootException(in("{`coverage." + name + "`: null }")); // arrays must have message
    List<Message> empty = new ArrayList<Message>(0);
    assertRootParseBoth(empty, in("{`coverage." + name + "`: []}")); // but can be empty
  }

  @Test
  public void bools() throws Exception {
    Coverage.Bool expected = Coverage.Bool.newBuilder().setRequired(true).setOptional(false).addRepeated(false)
        .addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected,
        in("{`coverage.Bool`: {`required`: true, `optional`: false, `repeated`: [false, true, false, true, true]}}"));
    assertStructuralCompliance(Coverage.Bool.class, true, "true");
  }

  @Test
  public void boolsList() throws Exception {
    List<Coverage.Bool> expected = Arrays.asList(Coverage.Bool.newBuilder().setRequired(true).build(), Coverage.Bool
        .newBuilder().setRequired(false).build());
    assertRootParseBothAndEncode(expected, in("{`coverage.Bool`: [{`required`: true}, {`required`: false}]}"));
  }

  @Test
  public void messages() throws Exception {
    Coverage.Message expected = Coverage.Message
        .newBuilder()
        .setRequired(
            Coverage.Bool.newBuilder().setRequired(true).setDefaulted(false).addRepeated(true).addRepeated(false))
        .setOptional(Coverage.Bool.newBuilder().setRequired(true))
        .addRepeated(Coverage.Bool.newBuilder().setRequired(false))
        .addRepeated(Coverage.Bool.newBuilder().setRequired(true)).build();

    assertRootParseBothAndEncode(expected,
        in("{`coverage.Message`: {`required`: {`required`: true, `defaulted`: false, `repeated`: [true,false]},"
            + " `optional`: {`required`: true}, `repeated`: [{`required`: false}, {`required`: true}]}}"));

    assertStructuralCompliance(Coverage.Message.class, Coverage.Bool.newBuilder().setRequired(true).build(),
        "{`required`: true}");
  }

  @Test
  public void compatibleMessage() throws IOException {
    // in compatible mode, messages can be sent as bytes
    Coverage.Bool bool = Coverage.Bool.newBuilder().setRequired(true).setDefaulted(false).addRepeated(true)
        .addRepeated(false).build();

    assertRootCompatibleOnly(Coverage.Message.newBuilder().setRequired(bool).build(),
        in("{`coverage.Message`: {`required`: `" + Base64.encodeBase64String(bool.toByteArray()) + "`}}"));
  }

  @Test
  public void compatibleInvalidMessage() throws IOException {
    // but invalid Base64 should not be parsed
    assertRootException(in("{`coverage.Message`: {`required`: `" + DefaultStringParserTest.invalidLengthBase64 + "`}}"));
    assertRootException(in("{`coverage.Message`: {`required`: `" + DefaultStringParserTest.invalid1PadBase64 + "`}}"));
  }

  @Test(expected = ParseException.class)
  public void messageWithInvalidProtobufBytes() throws IOException {
    String invalidEncodedBytes = Base64.encodeBase64String(new byte[] { 0x0, 0x0, 0x0, 0x0 }); // field numbers can't be
                                                                                               // zero so this should
                                                                                               // fail
    buildRoot(in("{`coverage.Message`: {`required`: `" + invalidEncodedBytes + "`}}"), compatible);
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
