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
import java.util.LinkedList;
import java.util.List;

import polybuf.core.BuilderStack;
import polybuf.core.MessageReader;
import polybuf.core.config.ReaderConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.protobuf.Message.Builder;

public class JsonParserReader implements MessageReader<JsonParser> {
  private static final JsonFactory jsonFactory = new JsonFactory();

  private final ReaderConfig config;

  public JsonParserReader(ReaderConfig config) {
    this.config = config;
  }

  public static JsonParser newJsonParser(InputStream stream) throws IOException {
    JsonParser parser = jsonFactory.createJsonParser(stream);
    parser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    return parser;
  }

  public static JsonToken expectNextToken(JsonParser parser, JsonToken expected) throws IOException {
    JsonToken actual = parser.nextToken();
    expectCurrentToken(parser, expected);
    return actual;
  }

  public static void expectCurrentToken(JsonParser parser, JsonToken expected) throws IOException {
    if (expected != parser.getCurrentToken()) {
      throw new JsonParseException("Expected token: " + expected, parser);
    }
  }

  public ReaderConfig config() {
    return config;
  }

  @Override
  public Builder mergeRootFrom(JsonParser input) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Builder> mergeRepeatedRootsFrom(JsonParser input) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeFrom(Builder builder, JsonParser input) throws IOException {
    expectNextToken(input, START_OBJECT);
    BuilderStack stack = config.builderStack(new JsonScalarParser());
    stack.pushRootBuilder(builder);
    new JsonMessageParser(stack, input).parse();
    stack.popRootBuilder();
  }

  @Override
  public <T extends Builder> List<T> mergeRepeatedFrom(T prototype, JsonParser input) throws IOException {
    expectNextToken(input, START_ARRAY);
    List<T> builders = new LinkedList<T>();
    BuilderStack stack = config.builderStack(new JsonScalarParser());
    for (JsonToken token = input.nextToken(); END_ARRAY != token; token = input.nextToken()) {
      expectCurrentToken(input, START_OBJECT);
      @SuppressWarnings("unchecked")
      T builder = (T) prototype.clone();
      stack.pushRootBuilder(builder);
      new JsonMessageParser(stack, input).parse();
      stack.popRootBuilder();
      builders.add(builder);
    }
    expectCurrentToken(input, END_ARRAY);
    return builders;
  }
}
