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

import polybuf.core.BuilderStack;
import polybuf.core.ScalarContext;
import polybuf.core.StructureContext;
import polybuf.core.util.CharacterRange;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

class JsonMessageParser {
  private final BuilderStack builderStack;
  private final JsonParser parser;

  public JsonMessageParser(BuilderStack builderStack, JsonParser parser) {
    this.builderStack = builderStack;
    this.parser = parser;
  }

  public void parse() throws IOException {
    parseMessageFields();
  }

  private void parseMessage(String serializedName, StructureContext context) throws IOException {
    builderStack.pushField(serializedName, context);
    parseMessageFields();
    builderStack.popField(serializedName);
  }

  private void parseMessageFields() throws IOException {
    assertCurrentToken(START_OBJECT);
    for (JsonToken token = parser.nextToken(); FIELD_NAME == token; token = parser.nextToken()) {
      String name = parser.getCurrentName();
      parser.nextToken();
      parseFieldValue(name, StructureContext.OBJECT);
    }
    expectCurrentToken(END_OBJECT);
  }

  private void parseFieldValue(String serializedName, StructureContext context) throws IOException {
    if (parser.getCurrentToken().isScalarValue()) {
      parseScalarFieldValue(serializedName, context);
    }
    else if (START_ARRAY == parser.getCurrentToken()) {
      if (StructureContext.ARRAY == context) {
        // no way in protobuf to have nested arrays field = [[1,3,4],[3,1,3],...]
        throw new JsonParseException("Nested arrays are not allowed", parser);
      }
      parseArrayFieldValues(serializedName);
    }
    else if (START_OBJECT == parser.getCurrentToken()) {
      parseMessage(serializedName, context);
    }
    else {
      assert false;
    }
  }

  private void parseArrayFieldValues(String serializedName) throws IOException {
    assertCurrentToken(START_ARRAY);
    StructureContext context = StructureContext.ARRAY;
    for (JsonToken token = parser.nextToken(); END_ARRAY != token; token = parser.nextToken()) {
      parseFieldValue(serializedName, context);
    }
    expectCurrentToken(END_ARRAY);
  }

  private void parseScalarFieldValue(String serializedName, StructureContext context) throws IOException {
    if (VALUE_NULL == parser.getCurrentToken()) {
      if (StructureContext.ARRAY == context) {
        throw new JsonParseException("null values not allowed in array", parser);
      }
      builderStack.clearScalarField(serializedName);
      return;
    }
    CharacterRange range = new CharacterRange(parser.getTextCharacters(), parser.getTextOffset(),
        parser.getTextLength());
    builderStack.addOrSetScalarField(serializedName, context, range, scalarContext());
  }

  private ScalarContext scalarContext() {
    return parser.getCurrentToken() == VALUE_STRING ? ScalarContext.QUOTED : ScalarContext.UNQUOTED;
  }

  private void assertCurrentToken(JsonToken expected) throws IOException {
    assert parser.getCurrentToken() == expected;
  }

  @SuppressWarnings("unused")
  private void expectNextToken(JsonToken expected) throws IOException {
    parser.nextToken();
    expectCurrentToken(expected);
  }

  private void expectCurrentToken(JsonToken expected) throws IOException {
    if (expected != parser.getCurrentToken()) {
      throw new JsonParseException("Expected token: " + expected, parser);
    }
  }
}