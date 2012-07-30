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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XML encoder for DOM nodes.
 * 
 * @see XmlTransformerEncoder
 */
public class XmlDomEncoder extends BaseXmlEncoder {
  private final Node parent;
  private final Document document;
  private final String namespaceUri;
  private final String prefix;
  private Node node;

  /**
   * Create an encoder with the specified parent node.
   * <p>
   * All protobuf elements are created as children of the specified parent which may be a document node. If the parent
   * node is document node and {@code namespaceUri} is not null, a {@code xmlns} attribute will be set accordingly for
   * the root element with the given prefix.
   * <p>
   * Also, if the parent is a document node, you should not call {@link #startRootList(String)} or attempt to write
   * multiple roots since documents only support one root element.
   * 
   * @param namespaceUri Namespace uri for the encoded nodes. Use {@code null} for no namespace.
   * @param prefix Prefix for generating qualified names.
   * @param parent Parent node.
   * @throws IOException
   */
  public XmlDomEncoder(String namespaceUri, String prefix, Node parent) throws IOException {
    this.namespaceUri = namespaceUri;
    if (namespaceUri == null) {
      assert prefix == null || prefix == "";
    }
    this.parent = parent;
    if (prefix == null) {
      prefix = "";
    }
    if (prefix.length() > 0) {
      prefix = prefix + ":";
    }
    this.prefix = prefix;
    this.document = parent.getNodeType() == Node.DOCUMENT_NODE ? (Document) parent : parent.getOwnerDocument();
  }

  private Element newNode(String serializedName) {
    return document.createElementNS(namespaceUri, qualifiedName(serializedName));
  }

  private String qualifiedName(String serializedName) {

    return prefix + serializedName;
  }

  @Override
  public void startRootMessage(String messageName) throws IOException {
    assert node == null;
    node = newNode(messageName);
    if (parent == document && namespaceUri != null) {
      String ns = "xmlns";
      if (prefix != null && prefix.length() > 0) {
        ns = ns + ":" + prefix;
      }
      ((Element) node).setAttribute(ns, namespaceUri);
    }
    parent.appendChild(node);
  }

  @Override
  public void endRootMessage(String messageName) throws IOException {
    assert node != null;
    assert node.getLocalName().equals(messageName);
    assert node.getParentNode() == parent;
    node = null;
  }

  @Override
  public void startMessageField(String fieldName) throws IOException {
    node.appendChild(newNode(fieldName));
    node = node.getLastChild();
  }

  @Override
  public void endMessageField(String fieldName) throws IOException {
    assert node.getLocalName().equals(fieldName);
    node = node.getParentNode();
    assert node != parent;
  }

  @Override
  public void startRepeatedRoot(String messageName) throws IOException {
    startRootMessage(messageName);
  }

  @Override
  public void endRepeatedRoot(String messageName) throws IOException {
    endRootMessage(messageName);
  }

  @Override
  public void startRepeatedMessageField(String messageName) throws IOException {
    startMessageField(messageName);
  }

  @Override
  public void endRepeatedMessageField(String messageName) throws IOException {
    endMessageField(messageName);
  }

  @Override
  public void scalarField(String fieldName, String fieldValue) throws IOException {
    Node child = newNode(fieldName);
    child.setTextContent(fieldValue);
    node.appendChild(child);
  }

}
