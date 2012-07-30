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
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import polybuf.core.Encoder;
import polybuf.core.EncoderFactory;

/**
 * Factory for stream-based XML encoders.
 */
public class XmlEncoderFactory implements EncoderFactory<OutputStream> {
  private final SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
  private final String namespaceUri;
  private final String namespacePrefix;

  public XmlEncoderFactory(String namespaceUri, String namespacePrefix) {
    if (namespaceUri == null) {
      namespaceUri = "";
    }
    if (namespacePrefix == null) {
      namespacePrefix = "";
    }
    this.namespaceUri = namespaceUri;
    if (namespaceUri == "" && namespacePrefix != "") {
      throw new IllegalArgumentException("namespace prefix must be empty or null when namespace uri is empty or null");
    }
    this.namespacePrefix = namespacePrefix;
  }

  @Override
  public Encoder encoder(OutputStream stream) throws IOException {
    try {
      TransformerHandler handler = factory.newTransformerHandler();
      handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      handler.setResult(new StreamResult(stream));
      return new XmlTransformerEncoder(namespaceUri, namespacePrefix, handler);
    }
    catch (TransformerConfigurationException e) {
      throw new IOException(e); // TODO specific exception
    }
  }
}