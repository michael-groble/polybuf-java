package polybuf.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import polybuf.core.config.SerializerConfig;
import polybuf.core.test.Coverage;
import polybuf.core.test.ExtensionsBase;
import polybuf.core.test.ExtensionsExt;
import polybuf.core.test.ExtensionsExtSamePackage;
import polybuf.core.test.Nested;
import polybuf.core.test.NoOptions;
import polybuf.core.test.PackagesPkg1;
import polybuf.core.test.PackagesPkg2;
import polybuf.core.test.Services;

import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

public class SchemaWriterTest {

  private final static boolean laxAnyExtensions = true;
  
  private Schema schema(SerializerConfig config) throws Exception {
    return schema(config,false);
  }
  
  private Schema schema(SerializerConfig config, boolean useLaxAnyForExtensions) throws Exception {
    SchemaWriter writer = new SchemaWriter(config, useLaxAnyForExtensions);
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    writer.writeSchema(stream);
    //assertEquals("", stream.toString("UTF-8"));
    ByteArrayInputStream input = new ByteArrayInputStream(stream.toByteArray());
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    return factory.newSchema(new StreamSource(input));
  }
  
  private boolean validateXmlAgainstSchema(String xml, Schema schema) throws Exception {
    Validator validator = schema.newValidator();
    ByteArrayInputStream stream = new ByteArrayInputStream(xml.replace('`','"').getBytes(Charsets.UTF_8));
    SAXSource source = new SAXSource(new InputSource(stream));
    validator.validate(source);
    return true;
  }
  private final static String xml = "<?xml version=`1.0` encoding=`UTF-8`?>";
  private final static String coverageNs = "xmlns=`http://www.example.org/polybuf-test/coverage`";
  private final static String coveragePrefixedNs = "xmlns:c=`http://www.example.org/polybuf-test/coverage`";
  private final static String extensionsBaseNs = "xmlns=`http://www.example.org/polybuf-test/extensions/base`";
  private final static String extensionsExtNs = "xmlns=`http://www.example.org/polybuf-test/extensions/ext`";
  private final static String extensionsExtSamePackageNs = "xmlns=`http://www.example.org/polybuf-test/extensions/ext_same_package`";
  private final static String nestedNs = "xmlns=`http://www.example.org/polybuf-test/nested`";
  private final static String packagesPkg2Ns = "xmlns=`http://www.example.org/polybuf-test/packages/pkg2`";
  private final static String noOptionsNs = "xmlns=`http://example.com/NoOptions`";
  private final static String servicesNs = "xmlns=`http://example.com/Services`";
  
  @Test
  public void coverageBool() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Bool ").append(coverageNs).append(">")
        .append(  "<required>true</required>")
        .append(  "<defaulted>false</defaulted>")
        .append(  "<repeated>true</repeated>")
        .append(  "<repeated>false</repeated>")
        .append("</coverage.Bool>").toString(), schema);
  }

  @Test
  public void coverageBoolNullSchema() throws Exception {
    Schema schema = schema(SerializerConfig.builder(Coverage.class).setNamespaceUri(null).build());
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Bool>")
        .append(  "<required>true</required>")
        .append(  "<defaulted>false</defaulted>")
        .append(  "<repeated>true</repeated>")
        .append(  "<repeated>false</repeated>")
        .append("</coverage.Bool>").toString(), schema);
  }
  
  @Test
  public void coverageBoolPrefixedSchema() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<c:coverage.Bool ").append(coveragePrefixedNs).append(">")
        .append(  "<c:required>true</c:required>")
        .append(  "<c:defaulted>false</c:defaulted>")
        .append(  "<c:repeated>true</c:repeated>")
        .append(  "<c:repeated>false</c:repeated>")
        .append("</c:coverage.Bool>").toString(), schema);
  }
  
  @Test(expected=SAXException.class)
  public void coverageBoolBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Bool ").append(coverageNs).append(">")
        .append(  "<required>bad</required>")
        .append("</coverage.Bool>").toString(), schema);
  }
  
  @Test
  public void coverageBytes() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Bytes ").append(coverageNs).append(">")
        .append(  "<required>abcd</required>")
        .append(  "<defaulted>1234</defaulted>")
        .append(  "<repeated>+/+/</repeated>")
        .append(  "<repeated>1g==</repeated>")
        .append("</coverage.Bytes>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageBytesBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Bytes ").append(coverageNs).append(">")
        .append(  "<required>bad</required>") // doesn't accept odd-length
        .append("</coverage.Bytes>").toString(), schema);
  }
  
  @Test
  public void coverageDouble() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Double ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-5.84757</defaulted>")
        .append(  "<repeated>1.e-9</repeated>")
        .append(  "<repeated>INF</repeated>")
        .append("</coverage.Double>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageDoubleBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Double ").append(coverageNs).append(">")
        .append(  "<required>5 5</required>")
        .append("</coverage.Double>").toString(), schema);
  }
  
  @Test
  public void coverageEnum() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Enum ").append(coverageNs).append(">")
        .append(  "<required>A</required>")
        .append(  "<optional>B</optional>")
        .append(  "<repeated>A</repeated>")
        .append(  "<repeated>C</repeated>")
        .append("</coverage.Enum>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageEnumBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Enum ").append(coverageNs).append(">")
        .append(  "<required>D</required>")
        .append("</coverage.Enum>").toString(), schema);
  }
  
  @Test
  public void coverageNestedEnum() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.NestedEnum ").append(coverageNs).append(">")
        .append(  "<required>X</required>")
        .append(  "<optional>Y</optional>")
        .append(  "<repeated>Z</repeated>")
        .append(  "<repeated>Y</repeated>")
        .append("</coverage.NestedEnum>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageNestedEnumBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.NestedEnum ").append(coverageNs).append(">")
        .append(  "<required>W</required>")
        .append("</coverage.NestedEnum>").toString(), schema);
  }
  
  @Test
  public void coverageFixed32() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Fixed32 ").append(coverageNs).append(">")
        .append(  "<required>1</required>")
        .append(  "<optional>0</optional>")
        .append(  "<repeated>3734646</repeated>")
        .append(  "<repeated>"+UnsignedInteger.MAX_VALUE+"</repeated>")
        .append("</coverage.Fixed32>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageFixed32BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Fixed32 ").append(coverageNs).append(">")
        .append(  "<required>-1</required>")
        .append("</coverage.Fixed32>").toString(), schema);
  }

  @Test
  public void coverageFixed64() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Fixed64 ").append(coverageNs).append(">")
        .append(  "<required>1</required>")
        .append(  "<optional>0</optional>")
        .append(  "<repeated>3734646857463</repeated>")
        .append(  "<repeated>"+UnsignedLong.MAX_VALUE+"</repeated>")
        .append("</coverage.Fixed64>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageFixed64BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Fixed64 ").append(coverageNs).append(">")
        .append(  "<required>-1</required>")
        .append("</coverage.Fixed64>").toString(), schema);
  }

  @Test
  public void coverageFloat() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Float ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-5.84757</defaulted>")
        .append(  "<repeated>NaN</repeated>")
        .append(  "<repeated>-INF</repeated>")
        .append("</coverage.Float>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageFloatBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Float ").append(coverageNs).append(">")
        .append(  "<required>a</required>")
        .append("</coverage.Float>").toString(), schema);
  }
  
  @Test
  public void coverageInt32() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Int32 ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-3874</defaulted>")
        .append(  "<repeated>"+Integer.MIN_VALUE+"</repeated>")
        .append(  "<repeated>"+Integer.MAX_VALUE+"</repeated>")
        .append("</coverage.Int32>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageInt32BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Int32 ").append(coverageNs).append(">")
        .append(  "<required>1"+Integer.MAX_VALUE+"</required>")
        .append("</coverage.Int32>").toString(), schema);
  }
  
  @Test
  public void coverageInt64() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Int64 ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-3874</defaulted>")
        .append(  "<repeated>"+Long.MIN_VALUE+"</repeated>")
        .append(  "<repeated>"+Long.MAX_VALUE+"</repeated>")
        .append("</coverage.Int64>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageInt64BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Int64 ").append(coverageNs).append(">")
        .append(  "<required>1"+Long.MAX_VALUE+"</required>")
        .append("</coverage.Int64>").toString(), schema);
  }
  
  @Test
  public void coverageMessage() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    assertTrue(validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Message ").append(coverageNs).append(">")
        .append(  "<required>")
        .append(    "<required>true</required>")
        .append(    "<defaulted>false</defaulted>")
        .append(    "<repeated>true</repeated>")
        .append(    "<repeated>false</repeated>")
        .append(  "</required>")
        .append(  "<optional>")
        .append(    "<required>true</required>")
        .append(  "</optional>")
        .append(  "<repeated>")
        .append(    "<required>false</required>")
        .append(  "</repeated>")
        .append(  "<repeated>")
        .append(    "<required>true</required>")
        .append(  "</repeated>")
        .append("</coverage.Message>").toString(), schema));
  }

  @Test(expected=SAXException.class)
  public void coverageMessageBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    assertTrue(validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Message ").append(coverageNs).append(">")
        .append(  "<required>true</required>")
        .append("</coverage.Message>").toString(), schema));
  }
  
  @Test
  public void coverageNestedMessage() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    assertTrue(validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.NestedMessage ").append(coverageNs).append(">")
        .append(  "<required>")
        .append(    "<s>a</s>")
        .append(  "</required>")
        .append(  "<optional>")
        .append(    "<s>b</s>")
        .append(  "</optional>")
        .append(  "<repeated>")
        .append(    "<s>c</s>")
        .append(  "</repeated>")
        .append(  "<repeated>")
        .append(    "<s>d</s>")
        .append(  "</repeated>")
        .append("</coverage.NestedMessage>").toString(), schema));
  }

  @Test(expected=SAXException.class)
  public void coverageNestedMessageBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    assertTrue(validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Message ").append(coverageNs).append(">")
        .append(  "<required>")
        .append(    "<s>") // s is a string so any structure should cause exception
        .append(    "<required>a</required>")
        .append(    "</s>")
        .append(	"</required>")
        .append("</coverage.Message>").toString(), schema));
  }
  
  @Test
  public void coverageSfixed32() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sfixed32 ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-3874</defaulted>")
        .append(  "<repeated>"+Integer.MIN_VALUE+"</repeated>")
        .append(  "<repeated>"+Integer.MAX_VALUE+"</repeated>")
        .append("</coverage.Sfixed32>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageSfixed32BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sfixed32 ").append(coverageNs).append(">")
        .append(  "<required>1"+Integer.MAX_VALUE+"</required>")
        .append("</coverage.Sfixed32>").toString(), schema);
  }
  
  @Test
  public void coverageSfixed64() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sfixed64 ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-3874</defaulted>")
        .append(  "<repeated>"+Long.MIN_VALUE+"</repeated>")
        .append(  "<repeated>"+Long.MAX_VALUE+"</repeated>")
        .append("</coverage.Sfixed64>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageSfixed64BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sfixed64 ").append(coverageNs).append(">")
        .append(  "<required>1"+Long.MAX_VALUE+"</required>")
        .append("</coverage.Sfixed64>").toString(), schema);
  }

  @Test
  public void coverageSint32() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sint32 ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-3874</defaulted>")
        .append(  "<repeated>"+Integer.MIN_VALUE+"</repeated>")
        .append(  "<repeated>"+Integer.MAX_VALUE+"</repeated>")
        .append("</coverage.Sint32>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageSint32BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sint32 ").append(coverageNs).append(">")
        .append(  "<required>1"+Integer.MAX_VALUE+"</required>")
        .append("</coverage.Sint32>").toString(), schema);
  }
  
  @Test
  public void coverageSint64() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sint64 ").append(coverageNs).append(">")
        .append(  "<required>1234</required>")
        .append(  "<defaulted>-3874</defaulted>")
        .append(  "<repeated>"+Long.MIN_VALUE+"</repeated>")
        .append(  "<repeated>"+Long.MAX_VALUE+"</repeated>")
        .append("</coverage.Sint64>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageSint64BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Sint64 ").append(coverageNs).append(">")
        .append(  "<required>1"+Long.MAX_VALUE+"</required>")
        .append("</coverage.Sint64>").toString(), schema);
  }

  @Test
  public void coverageString1() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.String1 ").append(coverageNs).append(">")
        .append(  "<required>1fd s f asdjfasdf asdjf</required>")
        .append(  "<defaulted>werwsd</defaulted>")
        .append(  "<repeated>asdsdf</repeated>")
        .append(  "<repeated>psdofasdk</repeated>")
        .append("</coverage.String1>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageString1BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.String1 ").append(coverageNs).append(">")
        .append(  "<required>")
        .append(    "<required>a</required>")
        .append(  "</required>")
        .append("</coverage.String1>").toString(), schema);
  }
  
  @Test
  public void coverageUint32() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Uint32 ").append(coverageNs).append(">")
        .append(  "<required>1</required>")
        .append(  "<optional>0</optional>")
        .append(  "<repeated>3734646</repeated>")
        .append(  "<repeated>"+UnsignedInteger.MAX_VALUE+"</repeated>")
        .append("</coverage.Uint32>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageUint32BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Uint32 ").append(coverageNs).append(">")
        .append(  "<required>-1</required>")
        .append("</coverage.Uint32>").toString(), schema);
  }

  @Test
  public void coverageUint64() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Uint64 ").append(coverageNs).append(">")
        .append(  "<required>1</required>")
        .append(  "<optional>0</optional>")
        .append(  "<repeated>3734646857463</repeated>")
        .append(  "<repeated>"+UnsignedLong.MAX_VALUE+"</repeated>")
        .append("</coverage.Uint64>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void coverageUint64BadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(Coverage.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<coverage.Uint64 ").append(coverageNs).append(">")
        .append(  "<required>-1</required>")
        .append("</coverage.Uint64>").toString(), schema);
  }

  @Test
  public void extensionBaseStrict() throws Exception {
    Schema schema = schema(SerializerConfig.of(ExtensionsBase.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsBaseNs).append(">")
        .append(  "<id>base id</id>")
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test(expected=SAXException.class)
  public void extensionBaseStrictBadValue() throws Exception {
    Schema schema = schema(SerializerConfig.of(ExtensionsBase.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsBaseNs).append(">")
        .append(  "<id>base id</id>")
        .append(  "<blah>0</blah>") // only accepts items in extension registry
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test
  public void extensionBaseLax() throws Exception {
    Schema schema = schema(SerializerConfig.of(ExtensionsBase.class), laxAnyExtensions);
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsBaseNs).append(">")
        // defined in proto
        .append(  "<id>base id</id>")
         // accepted since Message1 defines lax any extensions
        .append(  "<blah>0</blah>")
        .append(  "<a.b.c>3.141</a.b.c>")
        .append(  "<a><b>c</b></a>")
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test(expected=SAXException.class)
  public void extensionBaseLaxWrongOrder() throws Exception {
    Schema schema = schema(SerializerConfig.of(ExtensionsBase.class), laxAnyExtensions);
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsBaseNs).append(">")
        .append(  "<blah>0</blah>")
        .append(  "<id>base id</id>") // defined members need to come before lax extensions
        .append("</base.Message1>").toString(), schema);
  }
  
  
  @Test
  public void extensionExtStrict() throws Exception {
    Schema schema = schema(SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class).build());
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsExtNs).append(">")
        .append(  "<id>base id</id>")
        .append(  "<ext.id>1</ext.id>")
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test(expected=SAXException.class)
  public void extensionExtStrictUnknownExtension() throws Exception {
    Schema schema = schema(SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class).build());
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsExtNs).append(">")
        .append(  "<id>base id</id>")
        .append(  "<ext.bad>s</ext.bad>")
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test
  public void extensionExtLax() throws Exception {
    Schema schema = schema(SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class).build(), laxAnyExtensions);
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsExtNs).append(">")
        .append(  "<id>base id</id>")
        .append(  "<ext.unknown><a>1</a></ext.unknown>")
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test(expected=SAXException.class)
  public void extensionExtLaxWrongOrder() throws Exception {
    Schema schema = schema(SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class).build(), laxAnyExtensions);
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsExtNs).append(">")
        .append(  "<ext.unknown><a>1</a></ext.unknown>") // extensions must come after known fields
        .append(  "<id>base id</id>")
        .append("</base.Message1>").toString(), schema);
  }
  
  @Test
  public void extensionExtSamePackageStrict() throws Exception {
    Schema schema = schema(SerializerConfig.builder(ExtensionsExtSamePackage.class)
        .addRoot(ExtensionsBase.Message1.class).build());
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<base.Message1 ").append(extensionsExtSamePackageNs).append(">")
        .append(  "<id>base id</id>")
        .append(  "<base.Scope.id>1</base.Scope.id>")
        .append("</base.Message1>").toString(), schema);
  }


  @Test
  public void extensionNested() throws Exception {
    Schema schema = schema(SerializerConfig.of(Nested.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<nested.Message1.Message1A ").append(nestedNs).append(">")
        .append(  "<id>nested id</id>")
        .append("</nested.Message1.Message1A>").toString(), schema);
  }

  @Test(expected=SAXException.class)
  public void extensionNestedNonRoot() throws Exception {
    Schema schema = schema(SerializerConfig.of(Nested.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        // enclosing message is not a root so should not be accepted element
        .append("<nested.Message1 ").append(nestedNs).append(">")
        .append(  "<parts>")
        .append(    "<id>nested id</id>")
        .append(  "</parts>")
        .append("</nested.Message1>").toString(), schema);
  }
  
  @Test
  public void packagesPkg1() throws Exception {
    Schema schema = schema(SerializerConfig.of(PackagesPkg1.class));
    assertNotNull(schema);
  }
  
  @Test
  public void packagesPkg2() throws Exception {
    Schema schema = schema(SerializerConfig.of(PackagesPkg2.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        // enclosing message is not a root so should not be accepted element
        .append("<pkg2.Message2 ").append(packagesPkg2Ns).append(">")
        .append(  "<pkg1>")
        .append(    "<id>pkg1 id</id>")
        .append(  "</pkg1>")
        .append(  "<pkg2>")
        .append(    "<id>1</id>")
        .append(    "<type>A</type>")
        .append(  "</pkg2>")
        .append("</pkg2.Message2>").toString(), schema);
  }
  
  @Test
  public void noOptions() throws Exception {
    Schema schema = schema(SerializerConfig.of(NoOptions.class));
    assertNotNull(schema);
  }
  
  @Test(expected=SAXException.class)
  public void noOptionsRoot() throws Exception {
    Schema schema = schema(SerializerConfig.of(NoOptions.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        // enclosing message is not a root so should not be accepted element
        .append("<Message1 ").append(noOptionsNs).append(">")
        .append(  "<id>id</id>")
        .append("</Message1>").toString(), schema);
  }
  
  @Test
  public void noOptionsExplicitRoot() throws Exception {
    Schema schema = schema(SerializerConfig.builder(NoOptions.class).addRoot(NoOptions.Message1.class).build());
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<Message1 ").append(noOptionsNs).append(">")
        .append(  "<id>id</id>")
        .append("</Message1>").toString(), schema);
  }
  
  @Test
  public void services() throws Exception {
    Schema schema = schema(SerializerConfig.of(Services.class));
    assertNotNull(schema);
  }
  
  @Test(expected=SAXException.class)
  public void servicesRoot() throws Exception {
    Schema schema = schema(SerializerConfig.of(Services.class));
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        // enclosing message is not a root so should not be accepted element
        .append("<Message1 ").append(servicesNs).append(">")
        .append(  "<id>id</id>")
        .append("</Message1>").toString(), schema);
  }
  
  @Test
  public void servicesExplicitRoot() throws Exception {
    Schema schema = schema(SerializerConfig.builder(Services.class).addRoot(NoOptions.Message1.class).build());
    assertNotNull(schema);
    validateXmlAgainstSchema(
        new StringBuilder(xml)
        .append("<Message1 ").append(servicesNs).append(">")
        .append(  "<id>id</id>")
        .append("</Message1>").toString(), schema);
  }

}
