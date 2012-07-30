package polybuf.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import polybuf.core.BaseSerializerTest;
import polybuf.core.StreamHelper;
import polybuf.core.config.SerializerConfig;
import polybuf.core.test.Coverage;
import polybuf.core.util.Reflection;

import com.google.common.base.Charsets;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

public class BareJsonSerializerTest extends BaseSerializerTest<InputStream, OutputStream> {

  @Before
  public void setRegistry() throws Exception {
    strictSerializer = BareJson.serializer(SerializerConfig.builder(Coverage.class).setIsStrict(true).build());
    compatibleSerializer = BareJson.serializer(SerializerConfig.builder(Coverage.class).setIsStrict(false).build());
  }

  private void assertStructuralCompliance(Class<? extends GeneratedMessage> klass, Object goodValue, String goodString)
      throws IOException {
    Builder builder = Reflection.invokeStaticGetter(klass, "newBuilder", Message.Builder.class);
    Message message = builder.setField(builder.getDescriptorForType().findFieldByNumber(1), goodValue).build();

    // bare doesn't support roots
    assertRootUnsupported(in("{`required`: " + goodString + "}"));
    assertRepeatedRootUnsupported(in("[{`required`: " + goodString + "}]"));

    // but does support objects
    assertParseBoth(message, builder, in("{`required`: " + goodString + "}"));

    assertException(builder, in("")); // must be object
    assertException(builder, in("null")); // must be object
    assertParseBoth(builder.clone().buildPartial(), builder, in("{}")); // but can be empty
    assertException(builder, in("[{`required`: " + goodString + "}]")); // use repeated for lists

    List<Message> empty = new ArrayList<Message>(0);
    assertRepeatedException(builder, in("")); // must be array
    assertRepeatedException(builder, in("null")); // must be array
    assertParseBoth(empty, builder, in("[]")); // but can be empty
    assertRepeatedException(builder, in("[null]")); // arrays must have message
  }

  @Test
  public void bools() throws Exception {
    Coverage.Bool expected = Coverage.Bool.newBuilder().setRequired(true).setOptional(false).addRepeated(false)
        .addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertParseBothAndEncode(expected, Coverage.Bool.newBuilder(),
        in("{`required`: true, `optional`: false, `repeated`: [false, true, false, true, true]}"));
    assertStructuralCompliance(Coverage.Bool.class, true, "true");
  }

  @Test
  public void boolsList() throws Exception {
    List<Coverage.Bool> expected = Arrays.asList(Coverage.Bool.newBuilder().setRequired(true).build(), Coverage.Bool
        .newBuilder().setRequired(false).build());
    assertParseBothAndEncode(expected, Coverage.Bool.newBuilder(), in("[{`required`: true}, {`required`: false}]"));
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

    assertParseBothAndEncode(expected, Coverage.Message.newBuilder(),
        in("{`required`: {`required`: true, `defaulted`: false, `repeated`: [true,false]},"
            + " `optional`: {`required`: true}, `repeated`: [{`required`: false}, {`required`: true}]}"));

    assertStructuralCompliance(Coverage.Message.class, Coverage.Bool.newBuilder().setRequired(true).build(),
        "{`required`: true}");
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
