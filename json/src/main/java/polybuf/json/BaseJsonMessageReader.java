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
import java.util.List;

import polybuf.core.MessageReader;
import polybuf.core.config.ReaderConfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.protobuf.Message.Builder;

/**
 * Base JSON Message reader.
 */
public abstract class BaseJsonMessageReader implements MessageReader<InputStream> {

  private final JsonParserReader reader;

  public BaseJsonMessageReader(ReaderConfig config) {
    this.reader = new JsonParserReader(config);
  }

  protected ReaderConfig config() {
    return reader.config();
  }

  protected <T extends Builder> List<T> mergeArray(T prototype, JsonParser parser) throws IOException {
    return reader.mergeRepeatedFrom(prototype, parser);
  }

  protected void mergeObject(Builder builder, JsonParser parser) throws IOException {
    reader.mergeFrom(builder, parser);
  }

  protected JsonParser newJsonParser(InputStream stream) throws IOException {
    return JsonParserReader.newJsonParser(stream);
  }

  protected JsonToken expectNextToken(JsonParser parser, JsonToken expected) throws IOException {
    return JsonParserReader.expectNextToken(parser, expected);
  }

  protected void expectCurrentToken(JsonParser parser, JsonToken expected) throws IOException {
    JsonParserReader.expectCurrentToken(parser, expected);
  }
}
