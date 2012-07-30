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

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import polybuf.core.config.DefaultFieldNamingStrategy;
import polybuf.core.config.FieldNamingStrategy;
import polybuf.core.config.RootMessage;
import polybuf.core.config.RootMessageFullNameStrategy;
import polybuf.core.config.RootMessageNamingStrategy;
import polybuf.core.config.RootMessageShortNameStrategy;
import polybuf.core.config.SerializerConfig;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * Generates an XML schema for a specified serializer configuration.
 * <p>
 * The best way to describe the generated schema conventions is with an example. The schema for Google's protobuf <a
 * href="https://developers.google.com/protocol-buffers/docs/javatutorial">tutorial</a> can be generated as follows
 * 
 * <pre>
 * SerializerConfig config = SerializerConfig.builder(AddressBookProtos.class).addRoot(AddressBook.class).build();
 * SchemaWriter schema = new SchemaWriter(config);
 * schema.writeSchema(new FileOutputStream(&quot;AddressBook.xsl&quot;));
 * </pre>
 * 
 * which generates the following
 * 
 * <pre>
 *  {@code
 *  <?xml version="1.0" encoding="UTF-8"?>
 *  <xs:schema xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema"
 *    targetNamespace="http://example.com/AddressBookProtos" xmlns:tns="http://example.com/AddressBookProtos"
 *    elementFormDefault="qualified">
 *    <xs:simpleType name="tutorial.Person.PhoneType">
 *      <xs:restriction base="xs:string">
 *        <xs:enumeration value="MOBILE" />
 *        <xs:enumeration value="HOME" />
 *        <xs:enumeration value="WORK" />
 *      </xs:restriction>
 *    </xs:simpleType>
 *    <xs:complexType name="tutorial.Person.PhoneNumber">
 *      <xs:sequence>
 *        <xs:element name="number" type="xs:string" />
 *        <xs:element name="type" type="tns:tutorial.Person.PhoneType" minOccurs="0" default="HOME" />
 *      </xs:sequence>
 *    </xs:complexType>
 *    <xs:complexType name="tutorial.Person">
 *      <xs:sequence>
 *        <xs:element name="name" type="xs:string" />
 *        <xs:element name="id" type="xs:int" />
 *        <xs:element name="email" type="xs:string" minOccurs="0" />
 *        <xs:element name="phone" type="tns:tutorial.Person.PhoneNumber" minOccurs="0" maxOccurs="unbounded" />
 *      </xs:sequence>
 *    </xs:complexType>
 *    <xs:complexType name="tutorial.AddressBook">
 *      <xs:sequence>
 *        <xs:element name="person" type="tns:tutorial.Person" minOccurs="0" maxOccurs="unbounded" />
 *      </xs:sequence>
 *    </xs:complexType>
 *    <xs:element type="tns:tutorial.AddressBook" name="tutorial.AddressBook" />
 *  </xs:schema>
 *  }
 * </pre>
 * 
 * Many of the details of the generated schema can be configured via the {@link SerializerConfig} including the target
 * namespace URI, the root elements themselves and the naming conventions for root elements and fields. Note the
 * generated schema for optional and repeated fields are very similar to protobuf conventions. No "wrapping" element is
 * created for the repeated elements. Rather the field can potentially be missing or repeated based on {@code minOccurs}
 * and {@code maxOccurs} constraints.
 * <p>
 * The generated schema will generate type definitions for all referenced types needed by the specified root elements.
 * In the tutorial example, we only added the {@code AddressBook} root, but all dependent types were generated as well.
 * <p>
 * The type names follow the protobuf package naming conventions. The root element naming strategy only applies to the
 * root {@code element} names. The example above uses {@link RootMessageFullNameStrategy}. Using
 * {@link RootMessageShortNameStrategy} would only change the element name from the full package name
 * {@code tutorial.AddressBook} to the short name {@code AddressBook} as follows:
 * 
 * <pre>
 * {@code
 *    <xs:element type="tns:tutorial.AddressBook" name="AddressBook" />
 * }
 * </pre>
 * <p>
 * If a message allows extension fields, the schema will include explicit field definitions for any of the registered
 * extensions. For example, if we modify the tutorial as follows:
 * 
 * <pre>
 * message AddressBook {
 *   repeated Person person = 1;
 *   extensions 100 to 200;
 * }
 * 
 * extend AddressBook {
 *   optional string name = 101;
 * }
 * </pre>
 * 
 * we will get a definition that includes the extension (using the fully qualified name as described in
 * {@link DefaultFieldNamingStrategy}
 * 
 * <pre>
 * {@code 
 *   <xs:complexType name="tutorial.AddressBook">
 *     <xs:sequence>
 *       <xs:element name="person" type="tns:tutorial.Person" minOccurs="0" maxOccurs="unbounded" />
 *       <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:element name="tutorial.name" type="xs:string" minOccurs="0" />
 *       </xs:choice>
 *     </xs:sequence>
 *   </xs:complexType>
 * }
 * </pre>
 * 
 * We can also configure the schema writer to generate a lax {@code xs:any} to support schema migration scenarios, in
 * which case, the generated schema would be
 * 
 * <pre>
 * {@code 
 *   <xs:complexType name="tutorial.AddressBook">
 *     <xs:sequence>
 *       <xs:element name="person" type="tns:tutorial.Person" minOccurs="0" maxOccurs="unbounded" />
 *       <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:any minOccurs="0" maxOccurs="unbounded" processContents="lax" />
 *       </xs:choice>
 *     </xs:sequence>
 *   </xs:complexType>
 * }
 * </pre>
 * 
 * Finally, if the serializer config is set to strict processing, the generated schema will use the most restrictive
 * type to represent integer fields, e.g. {@code xs:long} for the signed 64 bit types. If not strict, these will be the
 * more permissive {@code xs:integer}. Bools are always {@code xs:bool}, bytes are always {@code xs:base64Binary},
 * floats are always {@code xs:float} and doubles are always {@code xs:double}.
 * 
 * @see SerializerConfig
 * @see RootMessageNamingStrategy
 * @see FieldNamingStrategy
 */
public class SchemaWriter {
  private final static AttributesImpl noAttributes = new AttributesImpl();
  private final SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
  private final SerializerConfig config;
  private final boolean useLaxAnyForExtensions;
  private TransformerHandler handler;
  private Set<Descriptor> writtenMessages = new HashSet<Descriptor>();
  private Set<EnumDescriptor> writtenEnums = new HashSet<EnumDescriptor>();

  /**
   * Schema writer for the defined configuration that does not use {@code xs:any} for extensions.
   * 
   * @see #SchemaWriter(SerializerConfig, boolean)
   */
  public SchemaWriter(SerializerConfig config) {
    this(config, false);
  }

  /**
   * Schema writer for the defined configuration and extension behavior.
   * 
   * @param config
   * @param useLaxAnyForExtensions true if {@code xs:any} with lax processing should be used for extensions. If false,
   *          the message will be defined using all known extension fields from the configured extension registry.
   */
  public SchemaWriter(SerializerConfig config, boolean useLaxAnyForExtensions) {
    this.config = config;
    this.useLaxAnyForExtensions = useLaxAnyForExtensions;
  }

  public void writeSchema(OutputStream stream) throws TransformerConfigurationException, SAXException {
    this.handler = factory.newTransformerHandler();
    handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
    handler.setResult(new StreamResult(stream));
    writeRoots();
  }

  private void writeRoots() throws SAXException {
    startSchema();
    for (RootMessage root : config.getRoots()) {
      writeMessage(root.getDescriptor());
    }

    for (RootMessage root : config.getRoots()) {
      writeMessageElement(root.getDescriptor());
    }
    endSchema();
  }

  private void writeEnum(EnumDescriptor enumerator) throws SAXException {
    if (writtenEnums.contains(enumerator)) {
      return;
    }
    startEnum(enumerator);
    for (EnumValueDescriptor value : enumerator.getValues()) {
      writeEnumValue(value);
    }
    endEnum(enumerator);
    writtenEnums.add(enumerator);
  }

  private void writeMessage(Descriptor message) throws SAXException {
    if (writtenMessages.contains(message)) {
      return;
    }
    // write nested ones first
    for (Descriptor nested : message.getNestedTypes()) {
      writeMessage(nested);
    }
    for (EnumDescriptor nested : message.getEnumTypes()) {
      writeEnum(nested);
    }

    // write referenced
    for (FieldDescriptor field : message.getFields()) {
      if (FieldDescriptor.Type.MESSAGE == field.getType()) {
        writeMessage(field.getMessageType());
      }
      else if (FieldDescriptor.Type.ENUM == field.getType()) {
        writeEnum(field.getEnumType());
      }
    }

    for (FieldDescriptor field : config.extensionFieldsForFullExtendedMessageName(message.getFullName())) {
      if (FieldDescriptor.Type.MESSAGE == field.getType()) {
        writeMessage(field.getMessageType());
      }
      else if (FieldDescriptor.Type.ENUM == field.getType()) {
        writeEnum(field.getEnumType());
      }
    }

    startMessage(message);
    for (FieldDescriptor field : message.getFields()) {
      writeField(field);
    }
    if (message.toProto().getExtensionRangeCount() > 0) {
      writeExtendable(message);
    }
    endMessage(message);
    writtenMessages.add(message);
  }

  private void startMessage(Descriptor message) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "", "name", "CDATA", message.getFullName());
    handler.startElement("", "", "xs:complexType", attributes);
    handler.startElement("", "", "xs:sequence", noAttributes);
  }

  private void endMessage(Descriptor message) throws SAXException {
    handler.endElement("", "", "xs:sequence");
    handler.endElement("", "", "xs:complexType");
  }

  private void startEnum(EnumDescriptor enumerator) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "", "name", "CDATA", enumerator.getFullName());
    handler.startElement("", "", "xs:simpleType", attributes);
    attributes = new AttributesImpl();
    attributes.addAttribute("", "", "base", "CDATA", "xs:string");
    handler.startElement("", "", "xs:restriction", attributes);
  }

  private void endEnum(EnumDescriptor enumerator) throws SAXException {
    handler.endElement("", "", "xs:restriction");
    handler.endElement("", "", "xs:simpleType");
  }

  private void writeField(FieldDescriptor field) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "", "name", "CDATA", config.serializedName(field));
    attributes.addAttribute("", "", "type", "CDATA", fieldType(field));
    if (field.isOptional()) {
      attributes.addAttribute("", "", "minOccurs", "CDATA", "0");
    }
    if (field.isRepeated()) {
      attributes.addAttribute("", "", "minOccurs", "CDATA", "0");
      attributes.addAttribute("", "", "maxOccurs", "CDATA", "unbounded");
    }
    if (field.hasDefaultValue()) {
      Object defaultValue = field.getDefaultValue();
      if (defaultValue instanceof EnumValueDescriptor) {
        defaultValue = ((EnumValueDescriptor) defaultValue).getName();
      }
      else if (defaultValue instanceof ByteString) {
        defaultValue = Base64.encodeBase64String(((ByteString) defaultValue).toByteArray());
      }
      attributes.addAttribute("", "", "default", "CDATA", defaultValue.toString());
    }
    handler.startElement("", "", "xs:element", attributes);
    handler.endElement("", "", "xs:element");
  }

  private String fieldType(FieldDescriptor field) {
    return config.isStrict() ? restrictiveFieldType(field) : permissiveFieldType(field);
  }

  private String restrictiveFieldType(FieldDescriptor field) {
    switch (field.getType()) {

    case INT32:
    case SINT32:
    case SFIXED32:
      return "xs:int";

    case FIXED32:
    case UINT32:
      return "xs:unsignedInt";

    case INT64:
    case SINT64:
    case SFIXED64:
      return "xs:long";

    case FIXED64:
    case UINT64:
      return "xs:unsignedLong";

    case BOOL:
      return "xs:boolean";

    case STRING:
      return "xs:string";

    case BYTES:
      return "xs:base64Binary";

    case MESSAGE:
      return messageType(field.getMessageType());

    case FLOAT:
      return "xs:float";

    case DOUBLE:
      return "xs:double";

    case ENUM:
      return enumType(field.getEnumType());

    case GROUP:
      throw new AssertionError("group not supported");

    default:
      throw new AssertionError("unknown type");
    }
  }

  private String permissiveFieldType(FieldDescriptor field) {
    switch (field.getType()) {

    case INT32:
    case SINT32:
    case SFIXED32:
    case FIXED32:
    case UINT32:
    case INT64:
    case SINT64:
    case SFIXED64:
    case FIXED64:
    case UINT64:
      return "xs:integer";

    case BOOL:
      return "xs:boolean";

    case STRING:
      return "xs:string";

    case BYTES:
      return "xs:base64Binary";

    case MESSAGE:
      return messageType(field.getMessageType());

    case FLOAT:
      return "xs:float";

    case DOUBLE:
      return "xs:double";

    case ENUM:
      return enumType(field.getEnumType());

    case GROUP:
      throw new AssertionError("group not supported");

    default:
      throw new AssertionError("unknown type");
    }
  }

  private String tns() {
    if (config.getNamespaceUri() == null) {
      return "";
    }
    return "tns:";
  }
  
  private String messageType(Descriptor message) {
    assert writtenMessages.contains(message);
    return tns() + message.getFullName();
  }

  private String enumType(EnumDescriptor enumerator) {
    assert writtenEnums.contains(enumerator);
    return tns() + enumerator.getFullName();
  }

  private void writeEnumValue(EnumValueDescriptor enumValue) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "", "value", "CDATA", enumValue.getName());
    handler.startElement("", "", "xs:enumeration", attributes);
    handler.endElement("", "", "xs:enumeration");
  }

  private void writeMessageElement(Descriptor message) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "", "type", "CDATA", messageType(message));
    attributes.addAttribute("", "", "name", "CDATA", config.serializedName(message));
    handler.startElement("", "", "xs:element", attributes);
    handler.endElement("", "", "xs:element");
  }

  private void writeExtendable(Descriptor message) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    attributes.addAttribute("", "", "minOccurs", "CDATA", "0");
    attributes.addAttribute("", "", "maxOccurs", "CDATA", "unbounded");
    handler.startElement("", "", "xs:choice", attributes);
    if (useLaxAnyForExtensions) {
      attributes.addAttribute("", "", "processContents", "CDATA", "lax");
      handler.startElement("", "", "xs:any", attributes);
      handler.endElement("", "", "xs:any");
    }
    else {
      for (FieldDescriptor field : config.extensionFieldsForFullExtendedMessageName(message.getFullName())) {
        writeField(field);
      }
    }
    handler.endElement("", "", "xs:choice");
  }

  private void startSchema() throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    //attributes.addAttribute("", "", "xmlns", "CDATA", "http://www.w3.org/2001/XMLSchema");
    attributes.addAttribute("", "", "xmlns:xs", "CDATA", "http://www.w3.org/2001/XMLSchema");
    if (config.getNamespaceUri() != null) {
      attributes.addAttribute("", "", "targetNamespace", "CDATA", config.getNamespaceUri());
      attributes.addAttribute("", "", "xmlns:tns", "CDATA", config.getNamespaceUri());
    }
    attributes.addAttribute("", "", "elementFormDefault", "CDATA", "qualified");
    handler.startDocument();
    handler.startElement("", "", "xs:schema", attributes);
  }

  private void endSchema() throws SAXException {
    handler.endElement("", "", "xs:schema");
    handler.endDocument();
  }

}
