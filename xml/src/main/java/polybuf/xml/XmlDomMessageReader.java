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
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import polybuf.core.BuilderStack;
import polybuf.core.MessageReader;
import polybuf.core.ParseException;
import polybuf.core.config.ReaderConfig;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;

public class XmlDomMessageReader implements MessageReader<Node> {
  private final static AttributesImpl noAttributes = new AttributesImpl();
  private final ReaderConfig config;

  public XmlDomMessageReader(ReaderConfig config) {
    this.config = config;
  }

  @Override
  public Builder mergeRootFrom(Node node) throws IOException {
    return parse(node, (Builder) null);
  }

  @Override
  public List<Builder> mergeRepeatedRootsFrom(Node parent) throws IOException {
    return parse(parent.getChildNodes(), (Builder) null);
  }

  @Override
  public void mergeFrom(Builder builder, Node node) throws IOException {
    Builder parsed = parse(node, builder);
    if (parsed == null) {
      return;
    }
    for (Map.Entry<FieldDescriptor, Object> e : parsed.getAllFields().entrySet()) {
      builder.setField(e.getKey(), e.getValue());
    }
  }

  @Override
  public <T extends Builder> List<T> mergeRepeatedFrom(T prototype, Node parent) throws IOException {
    return parse(parent.getChildNodes(), prototype);
  }

  private Builder parse(Node node, Builder rootOverride) throws IOException {
    try {
      BuilderStack builderStack = config.builderStack(new XmlScalarParser());
      XmlReaderHandler handler = new XmlReaderHandler(builderStack, rootOverride);
      handler.startDocument();
      parse(node, handler);
      handler.endDocument();
      List<Builder> builders = handler.getRoots();
      if (builders.size() == 0) {
        return null;
      }
      if (builders.size() == 1) {
        return builders.get(0);
      }
      throw new ParseException("multiple messages seen where 1 expected");
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  private <T extends Builder> List<T> parse(NodeList nodes, T rootOverride) throws IOException {
    try {
      BuilderStack builderStack = config.builderStack(new XmlScalarParser());
      XmlReaderHandler handler = new XmlReaderHandler(builderStack, rootOverride);
      handler.startDocument();
      parse(nodes, handler);
      handler.endDocument();
      @SuppressWarnings("unchecked")
      // should all be instances of T
      List<T> roots = (List<T>) handler.getRoots();
      return roots;
    }
    catch (SAXException ex) {
      throw new SaxParseException(ex);
    }
  }

  private void parse(Node node, XmlReaderHandler handler) throws IOException, SAXException {
    if (node.getNodeType() != Node.ELEMENT_NODE) {
      throw new ParseException("invalid element type");
    }
    // ignore attributes, assume they are xmlns or such

    String qName = ""; // we know it isn't used so don't bother making it
    handler.startElement(node.getNamespaceURI(), node.getLocalName(), qName, noAttributes);

    String text = singleChildText(node);
    if (text != null) {
      handler.characters(text.toCharArray(), 0, text.length());
    }
    else {
      parse(node.getChildNodes(), handler);
    }

    handler.endElement(node.getNamespaceURI(), node.getLocalName(), qName);
  }

  private void parse(NodeList nodes, XmlReaderHandler handler) throws IOException, SAXException {
    for (int i = 0; i < nodes.getLength(); ++i) {
      parse(nodes.item(i), handler);
    }
  }

  private String singleChildText(Node node) {
    if (node.hasChildNodes() && node.getChildNodes().getLength() == 1) {
      Node child = node.getFirstChild();
      if (child.getNodeType() == Node.TEXT_NODE) {
        return child.getNodeValue();
      }
    }
    return null;
  }

}
