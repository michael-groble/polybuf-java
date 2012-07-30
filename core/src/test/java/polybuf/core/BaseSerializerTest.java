package polybuf.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import polybuf.core.ParseException;
import polybuf.core.Serializer;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

public abstract class BaseSerializerTest<I,O> {
  protected Serializer<I,O> strictSerializer;
  protected Serializer<I,O> compatibleSerializer;
  protected static final boolean strict = true;
  protected static final boolean compatible = false;

  protected void assertRootParse(Message expected, I input, boolean isStrict) throws IOException {
    Builder builder = buildRoot(input, isStrict);
    assertNotNull(builder);
    Message parsed = builder.build();
    assertEquals(expected, parsed);
  }

  protected <T extends Message> void assertRootParse(List<T> expected, I input, boolean isStrict) throws IOException {
    List<Builder> builders = buildRepeatedRoot(input, isStrict);
    assertNotNull(builders);
    assertEquals(expected.size(), builders.size());
    for (int i = 0; i < builders.size(); ++i) {
      Builder builder = builders.get(i);
      assertNotNull(builder);
      Message parsed = builder.build();
      assertEquals(expected.get(i), parsed);
    }
  }

  protected void assertParse(Message expected, Builder builder, I input, boolean isStrict) throws IOException {
    Builder populated = builder.clone();
    build(populated, input, isStrict);
    Message parsed = populated.build();
    assertEquals(expected, parsed);
  }

  protected <T extends Message> void assertParse(List<T> expected, Builder prototype, I input, boolean isStrict) throws IOException {
    List<Builder> builders = buildRepeated(prototype, input, isStrict);
    assertNotNull(builders);
    assertEquals(expected.size(), builders.size());
    for (int i = 0; i < builders.size(); ++i) {
      Builder builder = builders.get(i);
      assertNotNull(builder);
      Message parsed = builder.build();
      assertEquals(expected.get(i), parsed);
    }
  }
  
  protected abstract void assertEncode(I expectedAsInput, Message message) throws Exception;
  protected abstract <T extends Message> void assertEncode(I expectedAsInput, List<T> messages) throws Exception;
  
  protected void assertRootParseBoth(Message expected, I input) throws IOException {
    assertRootParse(expected, input, strict);
    assertRootParse(expected, input, compatible);
  }

  protected <T extends Message> void assertRootParseBoth(List<T> expected, I input) throws IOException {
    assertRootParse(expected, input, strict);
    assertRootParse(expected, input, compatible);
  }
  
  protected void assertParseBoth(Message expected, Builder builder, I input) throws IOException {
    assertParse(expected, builder, input, strict);
    assertParse(expected, builder, input, compatible);
  }

  protected <T extends Message> void assertParseBoth(List<T> expected, Builder prototype, I input) throws IOException {
    assertParse(expected, prototype, input, strict);
    assertParse(expected, prototype, input, compatible);
  }
  
  protected void assertRootParseBothAndEncode(Message message, I input) throws Exception {
    assertRootParseBoth(message, input);
    assertEncode(input,message);
  }
  
  protected <T extends Message> void assertRootParseBothAndEncode(List<T> messages, I input) throws Exception {
    assertRootParseBoth(messages, input);
    assertEncode(input,messages);
  }
  
  protected void assertParseBothAndEncode(Message message, Builder builder, I input) throws Exception {
    assertParseBoth(message, builder, input);
    assertEncode(input,message);
  }
  
  protected <T extends Message> void assertParseBothAndEncode(List<T> messages, Builder prototype, I input) throws Exception {
    assertParseBoth(messages, prototype, input);
    assertEncode(input,messages);
  }
  
  protected void assertRootCompatibleOnly(Message expected, I input) throws IOException {
    assertRootParse(expected, input, compatible);
    assertRootException(input, strict);
  }

  protected void assertCompatibleOnly(Message expected, Builder builder, I input) throws IOException {
    assertParse(expected, builder, input, compatible);
    assertException(builder, input, strict);
  }
  
  protected void assertRootException(I input) throws IOException {
    assertRootException(input, strict);
    assertRootException(input, compatible);
  }

  protected void assertRepeatedRootException(I input) throws IOException {
    assertRepeatedRootException(input, strict);
    assertRepeatedRootException(input, compatible);
  }
  
  protected void assertException(Builder builder, I input) throws IOException {
    assertException(builder.clone(), input, strict);
    assertException(builder.clone(), input, compatible);
  }

  protected void assertRepeatedException(Builder prototype, I input) throws IOException {
    assertRepeatedException(prototype, input, strict);
    assertRepeatedException(prototype, input, compatible);
  }
  
  protected void assertRootUnsupported(I input) throws IOException {
    try {
      buildRoot(input, true);
    }
    catch (UnsupportedOperationException ex) {
      return;
    }
    fail();
  }

  protected void assertRepeatedRootUnsupported(I input) throws IOException {
    try {
      buildRepeatedRoot(input, true);
    }
    catch (UnsupportedOperationException ex) {
      return;
    }
    fail();
  }
  
  
  protected void assertRootException(I input, boolean isStrict) throws IOException {
    try {
      buildRoot(input, isStrict);
    }
    catch (ParseException ex) {
      return;
    }
    catch (NumberFormatException ex) {
      return;
    }
    fail();
  }

  protected void assertRepeatedRootException(I input, boolean isStrict) throws IOException {
    try {
      buildRepeatedRoot(input, isStrict);
    }
    catch (ParseException ex) {
      return;
    }
    catch (NumberFormatException ex) {
      return;
    }
    fail();
  }
  
  protected void assertException(Builder builder, I input, boolean isStrict) throws IOException {
    try {
      build(builder, input, isStrict);
    }
    catch (ParseException ex) {
      return;
    }
    catch (NumberFormatException ex) {
      return;
    }
    fail();
  }

  protected void assertRepeatedException(Builder prototype, I input, boolean isStrict) throws IOException {
    try {
      buildRepeated(prototype, input, isStrict);
    }
    catch (ParseException ex) {
      return;
    }
    catch (NumberFormatException ex) {
      return;
    }
    fail();
  }
  
  protected Builder buildRoot(I input, boolean isStrict) throws IOException {
    if (input instanceof InputStream) {
      ((InputStream)input).reset();
    }
    Serializer<I,O> serializer = isStrict ? strictSerializer : compatibleSerializer;
    return (Builder) serializer.mergeRootFrom(input);
  }
  
  protected List<Builder> buildRepeatedRoot(I input, boolean isStrict) throws IOException {
    if (input instanceof InputStream) {
      ((InputStream)input).reset();
    }
    Serializer<I,O> serializer = isStrict ? strictSerializer : compatibleSerializer;
    return serializer.mergeRepeatedRootsFrom(input);
  }
  
  protected void build(Builder builder, I input, boolean isStrict) throws IOException {
    if (input instanceof InputStream) {
      ((InputStream)input).reset();
    }
    Serializer<I,O> serializer = isStrict ? strictSerializer : compatibleSerializer;
    serializer.mergeFrom(builder,input);
  }
  
  protected <T extends Builder> List<T> buildRepeated(T prototype, I input, boolean isStrict) throws IOException {
    if (input instanceof InputStream) {
      ((InputStream)input).reset();
    }
    Serializer<I,O> serializer = isStrict ? strictSerializer : compatibleSerializer;
    return serializer.mergeRepeatedFrom(prototype,input);
  }
}
