package polybuf.json;

import static org.junit.Assert.assertEquals;

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

public class SocketIoEventSerializerTest extends BaseSerializerTest<InputStream, OutputStream> {

  @Before
  public void setRegistry() throws Exception {
    strictSerializer = SocketIoEvent.serializer(SerializerConfig.builder(Coverage.class).setIsStrict(true).build());
    compatibleSerializer = SocketIoEvent
        .serializer(SerializerConfig.builder(Coverage.class).setIsStrict(false).build());
  }

  private void assertStructuralCompliance(Class<? extends GeneratedMessage> klass, Object goodValue, String goodString)
      throws IOException {
    String name = klass.getSimpleName();
    Builder builder = Reflection.invokeStaticGetter(klass, "newBuilder", Message.Builder.class);
    Message message = builder.setField(builder.getDescriptorForType().findFieldByNumber(1), goodValue).build();

    assertRootParseBoth(message, in("{`name`: `coverage." + name + "`, `args`: [{`required`: " + goodString + "}]}"));

    // use builder, ignore message name from json
    assertParseBoth(message, builder, in("{`name`: `coverage.ignore`, `args`: [{`required`: " + goodString + "}]}"));

    // make sure it works for repeated as well
    assertParseBoth(Arrays.asList(message), builder, in("{`name`: `coverage.ignore`, `args`: [{`required`: "
        + goodString + "}]}"));

    assertRootException(in("")); // must have message
    assertRootException(in("null")); // must have message
    assertRootException(in("{}")); // must have message
    assertRootException(in("{`name`: `coverage." + name + "`, `args`: null }")); // must have array
    assertEquals(null, buildRoot(in("{`name`: `coverage." + name + "`, `args`: []}"), true));

    assertRepeatedRootException(in("{`name`: `coverage." + name + "`, `args`: null }")); // repeated must also have
                                                                                         // array
    List<Message> empty = new ArrayList<Message>(0);
    assertRootParseBoth(empty, in("{`name`: `coverage." + name + "`, `args`: []}")); // but can be empty
  }

  @Test
  public void bools() throws Exception {
    Coverage.Bool expected = Coverage.Bool.newBuilder().setRequired(true).setOptional(false).addRepeated(false)
        .addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertParseBothAndEncode(
        expected,
        Coverage.Bool.newBuilder(),
        in("{`name`: `coverage.Bool`, `args`: [{`required`: true, `optional`: false, `repeated`: [false, true, false, true, true]}]}"));
    assertStructuralCompliance(Coverage.Bool.class, true, "true");
  }

  @Test
  public void boolsList() throws Exception {
    List<Coverage.Bool> expected = Arrays.asList(Coverage.Bool.newBuilder().setRequired(true).build(), Coverage.Bool
        .newBuilder().setRequired(false).build());
    assertParseBothAndEncode(expected, Coverage.Bool.newBuilder(),
        in("{`name`: `coverage.Bool`, `args`: [{`required`: true}, {`required`: false}]}"));
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

    assertParseBothAndEncode(
        expected,
        Coverage.Message.newBuilder(),
        in("{`name`: `coverage.Message`, `args`: [{`required`: {`required`: true, `defaulted`: false, `repeated`: [true,false]},"
            + " `optional`: {`required`: true}, `repeated`: [{`required`: false}, {`required`: true}]}]}"));

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
