/*
 * Copyright (c) 2012 Michael Groble
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package polybuf.core;

import java.io.IOException;
import java.util.List;

import polybuf.core.config.SerializerConfig;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Default serializer implementation implements {@link MessageReader} and {@link MessageWriter} interfaces
 * given {@link MessageReaderFactory} and {@link EncoderFactory}.
 * 
 * @param <I>
 * @param <O>
 */
public class Serializer<I, O> implements MessageReader<I>, MessageWriter<O> {
  private final SerializerConfig config;
  private final MessageReader<I> reader;
  private final ExplicitMessageWriter<O> writer;

  public Serializer(SerializerConfig config, MessageReaderFactory<I> readerFactory, EncoderFactory<O> encoderFactory) {
    this.config = config;
    this.reader = readerFactory.reader(config.readerConfig());
    this.writer = new DefaultMessageWriter<O>(encoderFactory, config.getFieldNamingStrategy());
  }

  @Override
  public Message.Builder mergeRootFrom(I input) throws IOException {
    return reader.mergeRootFrom(input);
  }

  @Override
  public List<Builder> mergeRepeatedRootsFrom(I input) throws IOException {
    return reader.mergeRepeatedRootsFrom(input);
  }

  @Override
  public void mergeFrom(Builder builder, I input) throws IOException {
    reader.mergeFrom(builder, input);
  }

  @Override
  public <T extends Builder> List<T> mergeRepeatedFrom(T prototype, I input) throws IOException {
    return reader.mergeRepeatedFrom(prototype, input);
  }

  @Override
  public void writeTo(Message message, O output) throws IOException {
    writer.writeTo(serializedName(message), message, output);
  }

  @Override
  public void writeTo(String messageName, Message message, O output) throws IOException {
    writer.writeTo(messageName, message, output);
  }

  @Override
  public <T extends Message> void writeTo(List<T> messages, O output) throws IOException {
    writer.writeTo(serializedName(messages), messages, output);
  }

  @Override
  public <T extends Message> void writeTo(String messageName, List<T> messages, O output) throws IOException {
    writer.writeTo(messageName, messages, output);
  }

  private String serializedName(Message message) {
    return config.serializedName(message.getDescriptorForType());
  }

  private <T extends Message> String serializedName(List<T> messages) {
    String serializedName = null;
    for (T message : messages) {
      if (serializedName == null) {
        serializedName = serializedName(message);
        continue;
      }
      assert serializedName.equals(serializedName(message));
    }
    assert serializedName != null;
    return serializedName;
  }
}
