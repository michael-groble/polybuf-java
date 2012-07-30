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

package polybuf.json;

import static com.fasterxml.jackson.core.JsonToken.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import polybuf.core.EncoderFactory;
import polybuf.core.MessageReaderFactory;
import polybuf.core.ParseException;
import polybuf.core.Serializer;
import polybuf.core.config.ReaderConfig;
import polybuf.core.config.RootMessage;
import polybuf.core.config.SerializerConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;

/**
 * JSON format that provides messages in a single enclosing JSON object conforming to SocketIO events.
 * <p>
 * A protobuf message like the following
 * 
 * <pre>
 * message A {
 *   required string name = 1;
 *   required int32 id = 2;
 * }
 * </pre>
 * 
 * Is serialized as a single message in JSON as
 * 
 * <pre>
 * {"name" : "A", "args" : [{"name" : "Jim", "id" : 10}]}
 * </pre>
 * 
 * and as a list of messages as
 * 
 * <pre>
 * {"name": "A", "args" : [{"name" : "Tom", "id" : 7}, {"name" : "Jim", "id" : 10}]}
 * </pre>
 * 
 */
public class SocketIoEvent {

  public static EncoderFactory<OutputStream> encoderFactory() {
    return new JsonEncoderFactory() {
      @Override
      public polybuf.core.Encoder encoder(OutputStream stream) throws IOException {
        return new Encoder(generator(stream));
      }
    };
  }

  public static EncoderFactory<OutputStream> encoderFactory(JsonFactory jsonFactory) {
    return new JsonEncoderFactory(jsonFactory) {
      @Override
      public polybuf.core.Encoder encoder(OutputStream stream) throws IOException {
        return new Encoder(generator(stream));
      }
    };
  }

  public static MessageReaderFactory<InputStream> readerFactory() {
    return new MessageReaderFactory<InputStream>() {

      @Override
      public polybuf.core.MessageReader<InputStream> reader(ReaderConfig config) {
        return new MessageReader(config);
      }
    };
  }

  public static Serializer<InputStream, OutputStream> serializer(SerializerConfig config) {
    return new Serializer<InputStream, OutputStream>(config, readerFactory(), encoderFactory());
  }

  public static class Encoder extends JsonEncoder {

    private Encoder(JsonGenerator generator) {
      super(generator);
    }

    /**
     * Starts an object with name and args fields, e.g. {@code ' "name": "messageName", "args": ['}.
     */
    public void startRootList(String messageName) throws IOException {
      generator.writeStartObject();
      generator.writeStringField("name", messageName);
      generator.writeArrayFieldStart("args");
    }

    /**
     * Writes <code>']}'</code>.
     */
    public void endRootList(String messageName) throws IOException {
      generator.writeEndArray();
      generator.writeEndObject();
      generator.flush();
    }

    /**
     * Starts an object with name and args fields, e.g. <code>' "name": "messageName", "args": [{'</code>.
     */
    public void startRootMessage(String messageName) throws IOException {
      startRootList(messageName);
      generator.writeStartObject();
    }

    /**
     * Writes <code>'}]}'</code>.
     */
    public void endRootMessage(String messageName) throws IOException {
      generator.writeEndObject();
      endRootList(messageName);
      generator.flush();
    }
  }

  public static class MessageReader extends BaseJsonMessageReader {

    public MessageReader(ReaderConfig config) {
      super(config);
    }

    @Override
    public Builder mergeRootFrom(InputStream stream) throws IOException {
      List<Builder> builders = mergeRepeatedRootsFrom(stream);
      if (builders.size() == 0) {
        return null;
      }
      if (builders.size() > 1) {
        throw new ParseException("multiple messages seen where 1 expected");
      }
      return builders.get(0);
    }

    @Override
    public List<Builder> mergeRepeatedRootsFrom(InputStream stream) throws IOException {
      JsonParser parser = newJsonParser(stream);
      expectNextToken(parser, START_OBJECT);
      Builder prototype = parseNameField(parser);
      List<Builder> builders = parseArgsField(prototype, parser);
      expectNextToken(parser, END_OBJECT);
      return builders;
    }

    @Override
    public void mergeFrom(Builder builder, InputStream stream) throws IOException {
      List<Builder> builders = mergeRepeatedFrom(builder, stream);
      if (builders.size() == 0) {
        return; // leave builder unchanged
      }
      if (builders.size() > 1) {
        throw new ParseException("multiple messages seen where 1 expected");
      }
      for (Map.Entry<FieldDescriptor, Object> e : builders.get(0).getAllFields().entrySet()) {
        builder.setField(e.getKey(), e.getValue());
      }
    }

    @Override
    public <T extends Builder> List<T> mergeRepeatedFrom(T prototype, InputStream stream) throws IOException {
      JsonParser parser = newJsonParser(stream);
      expectNextToken(parser, START_OBJECT);
      parseName(parser); // ignore and use explicit prototype
      List<T> builders = parseArgsField(prototype, parser);
      expectNextToken(parser, END_OBJECT);
      return builders;
    }

    private Builder parseNameField(JsonParser parser) throws IOException {
      String serializedName = parseName(parser);

      RootMessage root = config().messageForSerializedName(serializedName);
      if (root == null) {
        throw new ParseException("Cannot determine root message for serialized name " + serializedName);
      }
      return root.newBuilder();
    }

    private String parseName(JsonParser parser) throws IOException {
      expectNextToken(parser, FIELD_NAME);
      if (!"name".equals(parser.getCurrentName())) {
        throw new ParseException("SocketIO event should start with the field 'name', instead saw: "
            + parser.getCurrentName());
      }
      expectNextToken(parser, VALUE_STRING);
      return parser.getText();
    }

    private <T extends Builder> List<T> parseArgsField(T prototype, JsonParser parser) throws IOException {
      expectNextToken(parser, FIELD_NAME);
      if (!"args".equals(parser.getCurrentName())) {
        throw new ParseException("SocketIO event should have field 'args', instead saw: " + parser.getCurrentName());
      }
      return mergeArray(prototype, parser);
    }
  }
}
