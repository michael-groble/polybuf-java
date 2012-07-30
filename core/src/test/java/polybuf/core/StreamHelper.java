package polybuf.core;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import polybuf.core.Serializer;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;

public class StreamHelper {
  public static void assertEncodeStream(InputStream expectedAsInput, Message message, Serializer<InputStream,OutputStream> serializer) throws IOException
  {
    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    serializer.writeTo(message, actual);
    expectedAsInput.reset();
    byte[] expectedBytes = new byte[expectedAsInput.available()];
    assertEquals(expectedAsInput.available(), expectedAsInput.read(expectedBytes));
    assertEquals(new String(expectedBytes, Charsets.UTF_8).replaceAll("\\s+", ""), actual.toString("UTF-8").replaceAll("\\s+", ""));
  }

  public static <T extends Message> void assertEncodeStream(InputStream expectedAsInput, List<T> messages, Serializer<InputStream,OutputStream> serializer) throws IOException
  {
    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    serializer.writeTo(messages, actual);
    expectedAsInput.reset();
    byte[] expectedBytes = new byte[expectedAsInput.available()];
    assertEquals(expectedAsInput.available(), expectedAsInput.read(expectedBytes));
    assertEquals(new String(expectedBytes, Charsets.UTF_8).replaceAll("\\s+", ""), actual.toString("UTF-8").replaceAll("\\s+", ""));
  }
}
