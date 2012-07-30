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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import polybuf.core.EncoderFactory;
import polybuf.core.MessageReaderFactory;
import polybuf.core.Serializer;
import polybuf.core.config.ReaderConfig;
import polybuf.core.config.SerializerConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.protobuf.Message.Builder;

/**
 * JSON format that does not use any structure, as is popular in some rest interfaces.
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
 * {"name" : "Jim", "id" : 10}
 * </pre>
 * 
 * and as a list of messages as
 * 
 * <pre>
 * [{"name" : "Tom", "id" : 7}, {"name" : "Jim", "id" : 10}]
 * </pre>
 * 
 * This format does not support root message lookup since there is no way to specify the root message in bare format.
 * 
 */
public class BareJson {

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
     * Writes a bare {@code '['}.
     */
    public void startRootList(String messageName) throws IOException {
      generator.writeStartArray();
    }

    /**
     * Writes a bare {@code ']'}.
     */
    public void endRootList(String messageName) throws IOException {
      generator.writeEndArray();
      generator.flush();
    }

    /**
     * Writes a bare <code>'{'</code>.
     */
    public void startRootMessage(String messageName) throws IOException {
      generator.writeStartObject();
    }

    /**
     * Writes a bare <code>'}'</code>.
     */
    public void endRootMessage(String messageName) throws IOException {
      generator.writeEndObject();
      generator.flush();
    }
  }

  public static class MessageReader extends BaseJsonMessageReader {

    public MessageReader(ReaderConfig config) {
      super(config);
    }

    /**
     * Always throws {@link UnsupportedOperationException}.
     */
    public Builder mergeRootFrom(InputStream stream) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void mergeFrom(Builder builder, InputStream stream) throws IOException {
      mergeObject(builder, newJsonParser(stream));
    }

    /**
     * Always throws {@link UnsupportedOperationException}.
     */
    public List<Builder> mergeRepeatedRootsFrom(InputStream stream) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Builder> List<T> mergeRepeatedFrom(T prototype, InputStream stream) throws IOException {
      return mergeArray(prototype, newJsonParser(stream));
    }
  }
}
