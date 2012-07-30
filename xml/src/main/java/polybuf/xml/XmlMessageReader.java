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
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import polybuf.core.MessageReader;
import polybuf.core.ParseException;
import polybuf.core.config.ReaderConfig;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;

public class XmlMessageReader implements MessageReader<InputStream> {
  private final ReaderConfig config;

  public XmlMessageReader(ReaderConfig config) {
    this.config = config;
  }

  @Override
  public Builder mergeRootFrom(InputStream stream) throws IOException {
    List<Builder> builders = parse(stream, null);

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
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeFrom(Builder builder, InputStream stream) throws IOException {
    List<Builder> builders = parse(stream, builder);

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
    throw new UnsupportedOperationException();
  }

  private List<Builder> parse(InputStream stream, Builder rootOverride) throws IOException {
    try {
      XMLReader reader = XMLReaderFactory.createXMLReader();
      XmlReaderHandler handler = new XmlReaderHandler(config.builderStack(new XmlScalarParser()), rootOverride);
      reader.setContentHandler(handler);
      reader.setErrorHandler(handler);
      reader.parse(new InputSource(stream));
      return handler.getRoots();
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }
}
