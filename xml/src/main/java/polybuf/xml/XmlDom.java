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

import org.w3c.dom.Node;

import polybuf.core.EncoderFactory;
import polybuf.core.MessageReader;
import polybuf.core.MessageReaderFactory;
import polybuf.core.Serializer;
import polybuf.core.config.ReaderConfig;
import polybuf.core.config.SerializerConfig;

/**
 * XML Format when reading/writing DOM nodes. Useful when the protobuf messages are to be embedded in an XML document or
 * when you need a fragment.
 * 
 * @see XmlStream
 */
public class XmlDom {

  public static EncoderFactory<Node> encoderFactory(String namespaceUri, String namespacePrefix) {
    return new XmlDomEncoderFactory(namespaceUri, namespacePrefix);
  }

  public static MessageReaderFactory<Node> readerFactory() {
    return new MessageReaderFactory<Node>() {

      @Override
      public MessageReader<Node> reader(ReaderConfig config) {
        return new XmlDomMessageReader(config);
      }
    };
  }

  public static Serializer<Node, Node> serializer(SerializerConfig config, String namespacePrefix) {
    return new Serializer<Node, Node>(config, readerFactory(),
        encoderFactory(config.getNamespaceUri(), namespacePrefix));
  }
}
