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

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XML Encoder for streams via a SAX {@link TransformerHandler}.
 * 
 * @see XmlDomEncoder
 */
public class XmlTransformerEncoder extends BaseXmlEncoder {
  private final static AttributesImpl noAttributes = new AttributesImpl();
  private final TransformerHandler handler;
  private final String namespaceUri;
  private final String namespacePrefix;
  private final String mappedPrefix;

  public XmlTransformerEncoder(String namespaceUri, String namespacePrefix, TransformerHandler handler)
      throws IOException {
    if (namespaceUri == null) {
      namespaceUri = "";
    }
    if (namespacePrefix == null) {
      namespacePrefix = "";
    }
    this.namespaceUri = namespaceUri;
    this.handler = handler;
    if (namespaceUri == "" && namespacePrefix != "") {
      throw new IllegalArgumentException("namespace prefix must be empty or null when namespace uri is empty or null");
    }
    this.mappedPrefix = namespacePrefix;
    if (namespacePrefix.length() > 0) {
      namespacePrefix = namespacePrefix + ":";
    }
    this.namespacePrefix = namespacePrefix;
  }

  @Override
  public void startRootList(String messageName) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void endRootList(String messageName) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startRepeatedRoot(String messageName) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void endRepeatedRoot(String messageName) throws IOException {
    throw new UnsupportedOperationException();
  }

  private void startElement(String elementName) throws IOException {
    try {
      handler.startElement(namespaceUri, elementName, namespacePrefix + elementName, noAttributes);
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  private void endElement(String elementName) throws IOException {
    try {
      handler.endElement(namespaceUri, elementName, namespacePrefix + elementName);
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  private void startDocument() throws IOException {
    try {
      handler.startDocument();
      handler.startPrefixMapping(mappedPrefix, namespaceUri);
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  private void endDocument() throws IOException {
    try {
      handler.endPrefixMapping(mappedPrefix);
      handler.endDocument();
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  private void characters(String content) throws IOException {
    try {
      // TODO use something else to avoid copy
      char[] chars = content.toCharArray();
      handler.characters(chars, 0, chars.length);
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  @Override
  public void startRootMessage(String messageName) throws IOException {
    startDocument();
    startElement(messageName);
  }

  @Override
  public void endRootMessage(String messageName) throws IOException {
    endElement(messageName);
    endDocument();
  }

  @Override
  public void startMessageField(String fieldName) throws IOException {
    startElement(fieldName);
  }

  @Override
  public void endMessageField(String fieldName) throws IOException {
    endElement(fieldName);
  }

  @Override
  public void startRepeatedMessageField(String fieldName) throws IOException {
    startMessageField(fieldName);
  }

  @Override
  public void endRepeatedMessageField(String fieldName) throws IOException {
    endMessageField(fieldName);
  }

  @Override
  public void scalarField(String fieldName, String fieldValue) throws IOException {
    startElement(fieldName);
    characters(fieldValue);
    endElement(fieldName);
  }

}
