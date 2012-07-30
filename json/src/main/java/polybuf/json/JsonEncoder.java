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

import polybuf.core.Encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.primitives.UnsignedLong;

/**
 * Base JSON encoder.
 * 
 */
public abstract class JsonEncoder implements Encoder {
  protected final JsonGenerator generator;

  protected JsonEncoder(JsonGenerator generator) {
    this.generator = generator;
  }

  @Override
  public void startMessageField(String fieldName) throws IOException {
    generator.writeObjectFieldStart(fieldName);
  }

  @Override
  public void endMessageField(String fieldName) throws IOException {
    generator.writeEndObject();
  }

  @Override
  public void startRepeatedMessageField(String messageName) throws IOException {
    generator.writeStartObject();
  }

  @Override
  public void endRepeatedMessageField(String messageName) throws IOException {
    generator.writeEndObject();
  }

  @Override
  public void startRepeatedRoot(String messageName) throws IOException {
    generator.writeStartObject();
  }

  @Override
  public void endRepeatedRoot(String messageName) throws IOException {
    generator.writeEndObject();
  }

  @Override
  public void startRepeatedField(String fieldName) throws IOException {
    generator.writeArrayFieldStart(fieldName);
  }

  @Override
  public void endRepeatedField(String fieldName) throws IOException {
    generator.writeEndArray();
  }

  @Override
  public void scalarField(String fieldName, boolean fieldValue) throws IOException {
    generator.writeBooleanField(fieldName, fieldValue);
  }

  @Override
  public void scalarField(String fieldName, int fieldValue) throws IOException {
    generator.writeNumberField(fieldName, fieldValue);
  }

  @Override
  public void scalarField(String fieldName, long fieldValue) throws IOException {
    generator.writeNumberField(fieldName, fieldValue);
  }

  @Override
  public void scalarField(String fieldName, UnsignedLong fieldValue) throws IOException {
    generator.writeFieldName(fieldName);
    generator.writeNumber(fieldValue.bigIntegerValue());
  }

  @Override
  public void scalarField(String fieldName, float fieldValue) throws IOException {
    generator.writeNumberField(fieldName, fieldValue);
  }

  @Override
  public void scalarField(String fieldName, double fieldValue) throws IOException {
    generator.writeNumberField(fieldName, fieldValue);
  }

  @Override
  public void scalarField(String fieldName, String fieldValue) throws IOException {
    generator.writeStringField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, boolean fieldValue) throws IOException {
    generator.writeBoolean(fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, int fieldValue) throws IOException {
    generator.writeNumber(fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, long fieldValue) throws IOException {
    generator.writeNumber(fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, UnsignedLong fieldValue) throws IOException {
    generator.writeNumber(fieldValue.bigIntegerValue());
  }

  @Override
  public void repeatedScalarField(String fieldName, float fieldValue) throws IOException {
    generator.writeNumber(fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, double fieldValue) throws IOException {
    generator.writeNumber(fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, String fieldValue) throws IOException {
    generator.writeString(fieldValue);
  }
}
