package polybuf.xml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import polybuf.core.BaseSerializerTest;
import polybuf.core.config.SerializerConfig;
import polybuf.core.test.Coverage;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;

public class XmlDomSerializerTest extends BaseSerializerTest<Node,Node> {
  private static final String USE_GENERATED_NS = "generated";
  private String namespaceUri;
  private String namespacePrefix;
  
  private SerializerConfig config(String namespaceUri, boolean isStrict) throws Exception {
    SerializerConfig.Builder builder = SerializerConfig.builder(Coverage.class).setIsStrict(isStrict);
    if (namespaceUri != USE_GENERATED_NS) {
      builder.setNamespaceUri(namespaceUri);
    }
    return builder.build();
  }
  private void setUriAndPrefix(String namespaceUri, String prefix) throws Exception {
    SerializerConfig strictConfig = config(namespaceUri,true);
    this.namespaceUri = strictConfig.getNamespaceUri();
    this.namespacePrefix = prefix;
    strictSerializer = XmlDom.serializer(strictConfig, prefix);
    compatibleSerializer = XmlDom.serializer(config(namespaceUri,false), prefix);
  }
  
  @Test
  public void singleEmptyTag() throws Exception {
    setUriAndPrefix(USE_GENERATED_NS,"");
    assertEquals(Coverage.Bool.newBuilder().buildPartial(), 
        buildRoot(in("<?xml version=`1.0` encoding=`UTF-8`?><coverage.Bool />"), strict).buildPartial());
  }
  
  @Test
  public void emptyTagPair() throws Exception {
    setUriAndPrefix(USE_GENERATED_NS,"");
    assertEquals(Coverage.Bool.newBuilder().buildPartial(), 
        buildRoot(in("<coverage.Bool></coverage.Bool>"), strict).buildPartial());
  }
  
  @Test
  public void bools() throws Exception {
    setUriAndPrefix(USE_GENERATED_NS,"");
    Coverage.Bool expected = Coverage.Bool.newBuilder()
        .setRequired(true)
        .setOptional(false)
        .addRepeated(false).addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
          .append("<coverage.Bool xmlns=`"+namespaceUri+"`>")
          .append(  "<required>true</required>")
          .append(  "<optional>false</optional>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>true</repeated>")
          .append("</coverage.Bool>")
          .toString()));
  }
  
  @Test
  public void boolsNoNs() throws Exception {
    setUriAndPrefix("","");
    Coverage.Bool expected = Coverage.Bool.newBuilder()
        .setRequired(true)
        .setOptional(false)
        .addRepeated(false).addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
          .append("<coverage.Bool>")
          .append(  "<required>true</required>")
          .append(  "<optional>false</optional>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>true</repeated>")
          .append("</coverage.Bool>")
          .toString()));
  }
  
  @Test
  public void boolsNullNs() throws Exception {
    setUriAndPrefix(null,"");
    Coverage.Bool expected = Coverage.Bool.newBuilder()
        .setRequired(true)
        .setOptional(false)
        .addRepeated(false).addRepeated(true).addRepeated(false).addRepeated(true).addRepeated(true).build();
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
          .append("<coverage.Bool>")
          .append(  "<required>true</required>")
          .append(  "<optional>false</optional>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>false</repeated>")
          .append(  "<repeated>true</repeated>")
          .append(  "<repeated>true</repeated>")
          .append("</coverage.Bool>")
          .toString()));
  }

  @Test(expected=IllegalArgumentException.class)
  public void boolsNoNsWithPrefix() throws Exception {
    setUriAndPrefix("","pre");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void boolsNullNsWithPrefix() throws Exception {
    setUriAndPrefix(null,"pre");
  }
  
  @Test
  public void boolsListWithPrefix() throws Exception {
    String prefix = "c";
    setUriAndPrefix(USE_GENERATED_NS,prefix);
    
    List<Coverage.Bool> expected = Arrays.asList(
        Coverage.Bool.newBuilder().setRequired(true).build(),
        Coverage.Bool.newBuilder().setRequired(false).build()
        );
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
        .append("<roots xmlns:"+prefix+"=`"+namespaceUri+"`>")
        .append(  "<"+prefix+":coverage.Bool>")
        .append(    "<"+prefix+":required>true</"+prefix+":required>")
        .append(  "</"+prefix+":coverage.Bool>")
        .append(  "<"+prefix+":coverage.Bool>")
        .append(    "<"+prefix+":required>false</"+prefix+":required>")
        .append(  "</"+prefix+":coverage.Bool>")
        .append("</roots>")
     .toString()));
  }
  
  @Test
  public void boolsListNoNs() throws Exception {
    setUriAndPrefix("","");
    
    List<Coverage.Bool> expected = Arrays.asList(
        Coverage.Bool.newBuilder().setRequired(true).build(),
        Coverage.Bool.newBuilder().setRequired(false).build()
        );
    assertRootParseBothAndEncode(expected, 
        in(new StringBuilder("<?xml version=`1.0` encoding=`UTF-8`?>")
        .append("<roots>")
        .append(  "<coverage.Bool>")
        .append(    "<required>true</required>")
        .append(  "</coverage.Bool>")
        .append(  "<coverage.Bool>")
        .append(    "<required>false</required>")
        .append(  "</coverage.Bool>")
        .append("</roots>")
     .toString()));
  }

  protected Node in(String input) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(input.replace('`','"').getBytes(Charsets.UTF_8)));
    doc.getDocumentElement().normalize();
    return doc.getDocumentElement();
  }
  
  @Override
  protected void assertEncode(Node expectedAsInput, Message message) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    strictSerializer.writeTo(message, doc);
    assertEquals(dump(expectedAsInput), dump(doc));
   }

  @Override
  protected <T extends Message> void assertEncode(Node expectedAsInput, List<T> messages) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.newDocument();
    Element roots = doc.createElement("roots");
    if (namespaceUri != null && namespaceUri.length() > 0) {
      String ns = "xmlns";
      if (namespacePrefix != null && namespacePrefix.length() > 0) {
        ns = ns + ":" + namespacePrefix;
      }
      roots.setAttribute(ns,namespaceUri);
    }
    doc.appendChild(roots);
    strictSerializer.writeTo(messages, roots);
    assertEquals(dump(expectedAsInput), dump(doc));
  }
  
  private String dump(Node node) throws TransformerException {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // so we don't worry about standalone="no"
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    DOMSource source = new DOMSource(node);
    transformer.transform(source, result);
    return writer.toString();
  }
}
