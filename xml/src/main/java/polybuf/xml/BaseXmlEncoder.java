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

package polybuf.xml;

import java.io.IOException;

import polybuf.core.Encoder;

import com.google.common.primitives.UnsignedLong;

public abstract class BaseXmlEncoder implements Encoder {

  @Override
  public void startRootList(String messageName) throws IOException {
    // ignore, no list delimiters
  }

  @Override
  public void endRootList(String messageName) throws IOException {
    // ignore, no list delimiters
  }

  @Override
  public void startRepeatedField(String fieldName) throws IOException {
    // ignore, no list delimiters
  }

  @Override
  public void endRepeatedField(String fieldName) throws IOException {
    // ignore, no list delimiters
  }

  @Override
  public void scalarField(String fieldName, boolean fieldValue) throws IOException {
    scalarField(fieldName, fieldValue ? "true" : "false");
  }

  @Override
  public void scalarField(String fieldName, int fieldValue) throws IOException {
    scalarField(fieldName, String.valueOf(fieldValue));
  }

  @Override
  public void scalarField(String fieldName, long fieldValue) throws IOException {
    scalarField(fieldName, String.valueOf(fieldValue));
  }

  @Override
  public void scalarField(String fieldName, UnsignedLong fieldValue) throws IOException {
    scalarField(fieldName, fieldValue.toString());
  }

  @Override
  public void scalarField(String fieldName, float fieldValue) throws IOException {
    if (Float.NEGATIVE_INFINITY == fieldValue) {
      scalarField(fieldName, XmlScalarParser.NEGATIVE_INFINITY);
    }
    else if (Float.POSITIVE_INFINITY == fieldValue) {
      scalarField(fieldName, XmlScalarParser.POSITIVE_INFINITY);
    }
    else if (Float.isNaN(fieldValue)) {
      scalarField(fieldName, XmlScalarParser.NaN);
    }
    else {
      scalarField(fieldName, String.valueOf(fieldValue));
    }
  }

  @Override
  public void scalarField(String fieldName, double fieldValue) throws IOException {
    if (Double.NEGATIVE_INFINITY == fieldValue) {
      scalarField(fieldName, XmlScalarParser.NEGATIVE_INFINITY);
    }
    else if (Double.POSITIVE_INFINITY == fieldValue) {
      scalarField(fieldName, XmlScalarParser.POSITIVE_INFINITY);
    }
    else if (Double.isNaN(fieldValue)) {
      scalarField(fieldName, XmlScalarParser.NaN);
    }
    else {
      scalarField(fieldName, String.valueOf(fieldValue));
    }
  }

  @Override
  public void repeatedScalarField(String fieldName, boolean fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, int fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, long fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, UnsignedLong fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, float fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, double fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

  @Override
  public void repeatedScalarField(String fieldName, String fieldValue) throws IOException {
    scalarField(fieldName, fieldValue);
  }

}
