package polybuf.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import polybuf.core.BaseSerializerTest;
import polybuf.core.StreamHelper;
import polybuf.core.config.SerializerConfig;
import polybuf.core.test.ExtensionsBase;
import polybuf.core.test.ExtensionsExt;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;

public class ExtensionsTest extends BaseSerializerTest<InputStream, OutputStream> {

  @Before
  public void setRegistry() throws Exception {
    strictSerializer = NamedJson.serializer(SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class).setIsStrict(true).build());
    compatibleSerializer = NamedJson.serializer(SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class).setIsStrict(false).build());
  }

  @Test
  public void baseMessage1() throws IOException {
    ExtensionsBase.Message1 expected = ExtensionsBase.Message1.newBuilder().setId("base id").build();
    assertRootParseBoth(expected, in("{`base.Message1`: {`id`: `base id`}}"));
  }

  @Test
  public void extendedBaseMessage1() throws IOException {
    ExtensionsBase.Message1 expected = ExtensionsBase.Message1.newBuilder().setId("base id")
        .setExtension(ExtensionsExt.id, 123).setExtension(ExtensionsExt.type, ExtensionsExt.Enum1.X)
        .setExtension(ExtensionsExt.baseType, ExtensionsBase.Enum1.A).build();
    assertRootParseBoth(expected,
        in("{`base.Message1`: {`id`: `base id`, `ext.id`: 123, `ext.type`: `X`, `ext.baseType`: `A`}}"));
  }

  protected InputStream in(String input) {
    return new ByteArrayInputStream(input.replace('`', '"').getBytes(Charsets.UTF_8));
  }

  @Override
  protected void assertEncode(InputStream expectedAsInput, Message message) throws IOException {
    StreamHelper.assertEncodeStream(expectedAsInput, message, strictSerializer);
  }

  @Override
  protected <T extends Message> void assertEncode(InputStream expectedAsInput, List<T> messages) throws IOException {
    StreamHelper.assertEncodeStream(expectedAsInput, messages, strictSerializer);
  }

}
