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
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import polybuf.core.config.FieldNamingStrategy;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

/**
 * Default writer. Knows how to translate Protobuf message structure to appropriate calls to the encoder methods.
 * Implementors of new encodings should just need to implement a new encoder and use this writer as-is.
 * 
 * @param <O> encoder output type
 * @see Encoder
 */
public class DefaultMessageWriter<O> implements ExplicitMessageWriter<O> {
  private final FieldNamingStrategy fieldNamingStrategy;
  private final EncoderFactory<O> encoderFactory;

  public DefaultMessageWriter(EncoderFactory<O> factory, FieldNamingStrategy fieldNamingStrategy) {
    this.fieldNamingStrategy = fieldNamingStrategy;
    this.encoderFactory = factory;
  }

  @Override
  public void writeTo(String messageName, Message message, O output) throws IOException {
    Encoder encoder = encoderFactory.encoder(output);
    writeRootMessage(encoder, messageName, message);
  }

  @Override
  public <T extends Message> void writeTo(String messageName, List<T> messages, O output) throws IOException {
    Encoder encoder = encoderFactory.encoder(output);
    writeRootMessages(encoder, messageName, messages);
  }

  private void writeRootMessage(Encoder encoder, String messageName, Message message) throws IOException {
    encoder.startRootMessage(messageName);
    writeMessageContents(encoder, message);
    encoder.endRootMessage(messageName);
  }

  private <T extends Message> void writeRootMessages(Encoder encoder, String messageName, List<T> messages)
      throws IOException {
    encoder.startRootList(messageName);
    for (T message : messages) {
      encoder.startRepeatedRoot(messageName);
      writeMessageContents(encoder, message);
      encoder.endRepeatedRoot(messageName);
    }
    encoder.endRootList(messageName);
  }

  private void startMessageField(Encoder encoder, String serializedName, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.startRepeatedMessageField(serializedName);
    }
    else {
      encoder.startMessageField(serializedName);
    }
  }

  private void endMessageField(Encoder encoder, String serializedName, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.endRepeatedMessageField(serializedName);
    }
    else {
      encoder.endMessageField(serializedName);
    }
  }

  private void writeMessageField(Encoder encoder, String serializedName, Message message, boolean isRepeated)
      throws IOException {
    startMessageField(encoder, serializedName, isRepeated);
    writeMessageContents(encoder, message);
    endMessageField(encoder, serializedName, isRepeated);
  }

  private void writeMessageContents(Encoder encoder, Message message) throws IOException {
    for (Map.Entry<FieldDescriptor, Object> e : message.getAllFields().entrySet()) {
      writeField(encoder, e.getKey(), e.getValue());
    }
  }

  private void writeField(Encoder encoder, FieldDescriptor field, Object value) throws IOException {
    assert value != null;
    String serializedName = fieldNamingStrategy.serializedName(field);
    if (serializedName == null) {
      throw new WriteException("cannnot serialize field " + field.getFullName());
    }

    if (value instanceof List) {
      assert field.isRepeated();
      encoder.startRepeatedField(serializedName);
      for (Object o : (List<?>) value) {
        writeValue(encoder, field, serializedName, o);
      }
      encoder.endRepeatedField(serializedName);
    }
    else {
      writeValue(encoder, field, serializedName, value);
    }
  }

  private void writeValue(Encoder encoder, FieldDescriptor field, String serializedName, Object value)
      throws IOException {
    switch (field.getType()) {
    case INT32:
    case SINT32:
    case SFIXED32:
      assert value instanceof Integer;
      writeScalarField(encoder, serializedName, (Integer) value, field.isRepeated());
      return;

    case INT64:
    case SINT64:
    case SFIXED64:
      assert value instanceof Long;
      writeScalarField(encoder, serializedName, (Long) value, field.isRepeated());
      return;

    case FLOAT:
      assert value instanceof Float;
      writeScalarField(encoder, serializedName, (Float) value, field.isRepeated());
      return;

    case DOUBLE:
      assert value instanceof Double;
      writeScalarField(encoder, serializedName, (Double) value, field.isRepeated());
      return;

    case BOOL:
      assert value instanceof Boolean;
      writeScalarField(encoder, serializedName, (Boolean) value, field.isRepeated());
      return;

    case UINT32:
    case FIXED32:
      assert value instanceof Integer;
      writeScalarField(encoder, serializedName, UnsignedInteger.asUnsigned((Integer) value).longValue(),
          field.isRepeated());
      return;

    case UINT64:
    case FIXED64:
      assert value instanceof Long;
      writeScalarField(encoder, serializedName, UnsignedLong.asUnsigned((Long) value), field.isRepeated());
      return;

    case STRING:
      assert value instanceof String;
      writeScalarField(encoder, serializedName, (String) value, field.isRepeated());
      return;

    case BYTES:
      assert value instanceof ByteString;
      writeScalarField(encoder, serializedName, (ByteString) value, field.isRepeated());
      return;

    case ENUM:
      assert value instanceof EnumValueDescriptor;
      writeScalarField(encoder, serializedName, ((EnumValueDescriptor) value).getName(), field.isRepeated());
      return;

    case MESSAGE:
      assert value instanceof Message;
      writeMessageField(encoder, serializedName, (Message) value, field.isRepeated());
      return;

    case GROUP:
      throw new AssertionError("group not supported");

    default:
      throw new AssertionError("unknown type");
    }

  }

  private void writeScalarField(Encoder encoder, String fieldName, boolean value, boolean isRepeated)
      throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, int value, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, long value, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, UnsignedLong value, boolean isRepeated)
      throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, float value, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, double value, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, String value, boolean isRepeated) throws IOException {
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, value);
    }
    else {
      encoder.scalarField(fieldName, value);
    }
  }

  private void writeScalarField(Encoder encoder, String fieldName, ByteString value, boolean isRepeated)
      throws IOException {
    String encoded = Base64.encodeBase64String(value.toByteArray());
    if (isRepeated) {
      encoder.repeatedScalarField(fieldName, encoded);
    }
    else {
      encoder.scalarField(fieldName, encoded);
    }
  }
}
