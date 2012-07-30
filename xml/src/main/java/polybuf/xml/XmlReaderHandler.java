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
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import polybuf.core.BuilderStack;
import polybuf.core.ParseException;
import polybuf.core.ScalarContext;
import polybuf.core.config.ReaderConfig;
import polybuf.core.util.CharacterRange;
import polybuf.core.util.ContentBuffer;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;

/**
 * A {@link DefaultHandler} that can be used to populate a {@link BuilderStack}
 * 
 */
public class XmlReaderHandler extends DefaultHandler {
  private final ContentBuffer content = new ContentBuffer();
  private final BuilderStack builderStack;
  private final Builder overrideRoot;
  private final List<Builder> roots = new LinkedList<Builder>();
  private SaxParseException fatalException;

  /**
   * Create a new handler with the specified builder stack. The builder should be empty.
   * 
   * @param builderStack
   * @param overrideRoot prototype builder to use for roots. Use this to parse handle messages that aren't in the root
   *          registry or other cases where the caller already knows the type of message to be parsed. Use @ null} to
   *          specify the root should be determined by the element name using the configured root registry
   * @see ReaderConfig
   */
  public XmlReaderHandler(BuilderStack builderStack, Builder overrideRoot) {
    assert builderStack.isEmpty();
    this.builderStack = builderStack;
    this.overrideRoot = overrideRoot;
  }

  /**
   * Get the list of roots parsed by this handler.
   * 
   * @throws IOException A {@link SaxParseException} will wrap any {@link SAXException} fatal errors detected by the
   *           underlying XML parser.
   */
  public List<Builder> getRoots() throws IOException {
    if (fatalException != null) {
      throw fatalException;
    }
    return roots;
  }

  @Override
  public void characters(char[] chars, int offset, int length) throws SAXException {
    content.append(chars, offset, length);
  }

  @Override
  public void startDocument() throws SAXException {
    content.clear();
    builderStack.clear();
    roots.clear();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    content.clear();
    String serializedName = serializedElementName(uri, localName, qName);
    if (builderStack.isEmpty() && overrideRoot != null) {
      builderStack.pushRootBuilder(overrideRoot.clone());
    }
    else {
      builderStack.pushRootOrField(serializedName);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    content.clear();
    if (!builderStack.isEmpty()) {
      throw new ParseException("XML document ended, but message not completely built");
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    CharacterRange contentCharacters = content.getAndClear();
    String serializedName = serializedElementName(uri, localName, qName);

    if (FieldDescriptor.Type.MESSAGE.equals(builderStack.getCurrentFieldType())) {

      // we could have a couple different cases:
      //
      // Ignorable whitepsace seen between </field> and </message>
      // <message>
      // <field>value</field>
      // </message>
      //
      // No content, intepret as null or default? Assume null values are not written to
      // xml and therefore this is a message with default content.
      // <message></message>
      //
      // In compatibility mode, message can be represented as bytes (which get base64 encoded)
      // <message>9FmH3sp/d19+</message>

      if (contentCharacters == null || contentCharacters.length() == 0) {
        popRootOrField(serializedName);
        return;
      }
      if (contentCharacters.isIgnorableWhitespace()) {
        // whitespace is not valid base64 so this can't be a compatible byte representation
        // ignore it
        popRootOrField(serializedName);
        return;
      }
      // else drop through to process content
    }
    popRootOrField(serializedName, contentCharacters, ScalarContext.UNSPECIFIED);
  }

  @Override
  public void error(SAXParseException ex) throws SAXException {
    // doc says it is recoverable, so ignore
  }

  @Override
  public void fatalError(SAXParseException ex) throws SAXException {
    builderStack.clear();
    this.fatalException = new SaxParseException(ex);
  }

  private String serializedElementName(String uri, String localName, String qName) {
    return localName;
  }

  private void popRootOrField(String serializedName) throws SAXException {
    Builder root = overrideRoot != null && builderStack.isRootPoppable() ? builderStack.popRootBuilder() : builderStack
        .popRootOrField(serializedName);

    if (root != null) {
      roots.add(root);
    }
  }

  private void popRootOrField(String serializedName, CharacterRange content, ScalarContext scalarContext)
      throws SAXException {
    if (overrideRoot != null && builderStack.isRootPoppable()) {
      throw new ParseException("Cannot provide content to overriden root");
    }
    Builder root = builderStack.popRootOrField(serializedName, content, scalarContext);
    if (root != null) {
      roots.add(root);
    }
  }
}
