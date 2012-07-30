# polybuf-java


Polybuf is a library for converting Google [protocol buffers](http://code.google.com/p/protobuf/) to various formats with support for [packages](https://developers.google.com/protocol-buffers/docs/proto#packages), [extensions](https://developers.google.com/protocol-buffers/docs/proto#extensions) and [compatible upgrades](https://developers.google.com/protocol-buffers/docs/proto#updating).  It also can be used to generate an XML schema representation of a proto file to integrate protobuf-based systems with xml-based systems.
    
## Why another protobuf library?

### Decoupled development
[protobuf-java-format](http://code.google.com/p/protobuf-java-format/) and [protostuff](https://code.google.com/p/protostuff/) are great options for dealing with protocol buffers in your Java code.

My main motivation for creating a new option is to support a decoupled workflow between groups who use protocol buffers to build services and other groups who want to use those services without needing to know anything about protocol buffers.

Polybuf allows developers to use protocol buffers as the "primary" message definition language and generate an XML schema representation for other groups to use.  It also provides serialization support to convert incoming and outgoing messages compliant with that schema.

### Customizable and consistent naming
Another motivation was to support generated schemas and message formats that would be close to what a person would naturally write using something like XML or JSON.  This can be difficult to do when using the package and extension features of protocol buffers.

Polybuf provides a way to specify root message naming conventions and field naming conventions in a way that is customizable and consistent between generated schemas and message serializers.

Messages in a proto file can be designated as a "root" message.  The naming conventions allow Polybuf to pick the right builder for these incoming root message.  This is similar to the XML schema concept of root elements.   

### Compatible parsing
Finally, the protocol buffer format is designed to handle message format changes as  interfaces evolve over time.  Polybuf supports both _strict_ parsing, where the serialized messages must match the expected format exactly, or _compatible_ parsing 
where [compatible message updates](https://developers.google.com/protocol-buffers/docs/proto#updating) are allowed.  

## How does it work?

### Unmodified proto files
Polybuf can work with any proto file.  Google's [tutorial](https://developers.google.com/protocol-buffers/docs/javatutorial), for example, can be generated with the following code.

```java
    SerializerConfig config = SerializerConfig.builder(AddressBookProtos.class).addRoot(AddressBook.class).build();
    SchemaWriter schema = new SchemaWriter(config, true);
    schema.writeSchema(new FileOutputStream("AddressBook.xsl"));
    
    Person john = Person.newBuilder().setId(1234)
    .setName("John Doe")
    .setEmail("jdoe@example.com")
    .addPhone(
      Person.PhoneNumber.newBuilder()
        .setNumber("555-4321"))
    .build();
    
    Person jane = Person.newBuilder().setId(4321)
        .setName("Jane Doe")
        .setEmail("jane@example.com")
        .addPhone(
          Person.PhoneNumber.newBuilder()
            .setNumber("555-1234")
            .setType(Person.PhoneType.MOBILE))
        .build();
    
    AddressBook addresses = AddressBook.newBuilder().addPerson(jane).addPerson(john).build();

    String prefix = "";
    XmlStream.serializer(config,prefix).writeTo(addresses,  new FileOutputStream("addresses.xml"));
```

This generates the schema
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" targetNamespace="http://example.com/AddressBookProtos"
  xmlns:tns="http://example.com/AddressBookProtos" elementFormDefault="qualified">
  <xs:simpleType name="tutorial.Person.PhoneType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="MOBILE" />
      <xs:enumeration value="HOME" />
      <xs:enumeration value="WORK" />
    </xs:restriction>
  </xs:simpleType>
  <xs:complexType name="tutorial.Person.PhoneNumber">
    <xs:sequence>
      <xs:element name="number" type="xs:string" />
      <xs:element name="type" type="tns:tutorial.Person.PhoneType" minOccurs="0" default="HOME" />
    </xs:sequence>
  </xs:complexType>
  <xs:complexType name="tutorial.Person">
    <xs:sequence>
      <xs:element name="name" type="xs:string" />
      <xs:element name="id" type="xs:int" />
      <xs:element name="email" type="xs:string" minOccurs="0" />
      <xs:element name="phone" type="tns:tutorial.Person.PhoneNumber" minOccurs="0" maxOccurs="unbounded" />
    </xs:sequence>
  </xs:complexType>
  <xs:complexType name="tutorial.AddressBook">
    <xs:sequence>
      <xs:element name="person" type="tns:tutorial.Person" minOccurs="0" maxOccurs="unbounded" />
    </xs:sequence>
  </xs:complexType>
  <xs:element type="tns:tutorial.AddressBook" name="tutorial.AddressBook" />
</xs:schema>
```

And the sample file
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tutorial.AddressBook xmlns="http://example.com/AddressBookProtos">
  <person>
    <name>Jane Doe</name>
    <id>4321</id>
    <email>jane@example.com</email>
    <phone>
      <number>555-1234</number>
      <type>MOBILE</type>
    </phone>
  </person>
  <person>
    <name>John Doe</name>
    <id>1234</id>
    <email>jdoe@example.com</email>
    <phone>
      <number>555-4321</number>
    </phone>
  </person>
</tutorial.AddressBook>
```

We can use the same example to demonstrate parsing.
```java
    Serializer<InputStream,OutputStream> serializer = XmlStream.serializer(config,"");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    serializer.writeTo(addresses,  output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    Builder builder = serializer.mergeRootFrom(input);
    if (addresses.equals(builder.build())) {
      System.out.println("Matches!");
    }
```

### Protocol buffer options
Polybuf also defines options that can be used in you proto files to customize processing.

Polybuf currently has one file option, `namespace_uri` which is used to explicitly provide the XML namespace URI of the generated XML schema. It can be specified with a file option in the protobuf file as follows:

```proto
  import "polybuf.proto";
  option (polybuf.file) = {namespace_uri : "http://www.example.org/messages"};
``` 

There is currently one message option, `rootable` which is used to tag those messages which can be recognized by name. It is specified as follows:
```proto
  import "polybuf.proto";
  
 message A {
   option (polybuf.message) = {rootable : true};
   required string id = 1;
   extensions 100 to 200;
 }
```

### Details

The `SerializerConfig` class provides more configuration options and documentation.  The test cases include examples of both writing and parsing for each of the supported serializers.


## What formats are supported?

### XML
The XML package provides two serializers in addition to the schema generator:

   * `XMLStream` - reads from an `InputStream` and writes to an `OutputStream`
   * `XMLDom` - reads and writes to XML DOM `Node`s which can be useful for embedding the protobuf content in larger documents.

### JSON
The JSON package provides three serializers, all of which operate on input and output streams.  They are best described by example:

#### "Bare" JSON
The `BareJson` serializer generates a format with just the object content (and therefore cannot be used to automatically determine roots)
```json
{"person": [
  {"name": "John Doe",
   "id": 1234,
   "email": "jdoe@example.com",
   "phone": [{"number":"555-4321"}]
  }
 ]
}
```

#### "Named" JSON
The `NamedJson` serializer generates an object with one field that has the name of the root message type.
```json
{"tutorial.AddressBook": {
  "person": [
    {"name": "John Doe",
     "id": 1234,
     "email": "jdoe@example.com",
     "phone": [{"number":"555-4321"}]
    }
   ]
  }
}
```

#### SocketIO Event
The `SocketIoEvent` serializer generates a SocketIO format with `name` and `args` fields.
```json
{"name": "tutorial.AddressBook",
 "args" : [
  {"person": [
    {"name": "John Doe",
     "id": 1234,
     "email": "jdoe@example.com",
     "phone": [{"number":"555-4321"}]
    }
   ]
  }]
}
```

## Dependencies
 
Polybuf is built using maven and is provided in a number of modules:

   * `polybuf-java-core` - uses [protobuf-java](http://mvnrepository.com/artifact/com.google.protobuf/protobuf-java), [guava-libraries](http://code.google.com/p/guava-libraries) and [commons-codec](http://commons.apache.org/codec/)
   * `polybuf-java-json` - uses [jackson-core](https://github.com/FasterXML/jackson-core)
   * `polybuf-java-xml` - uses standard Java library
   * `polybuf-java-classifiers`
   * `polybuf-java-proto`

To use either XML or JSON, include the respective module, which will automatically pull in core.

If you are not using the Polybuf options, also include `polybuf-java-proto` which contains the generated code defining the options. If you do want to use the options, you can compile the `polybuf.proto` file yourself, or use the generated file in `polybuf-java-proto`.  Just make sure your `protoc` include path includes the directory containing `polybuf.proto` so it can be imported as

```proto
  import "polybuf.proto";
```

If you don't, you will see the dreaded `DescriptorValidationException` telling you the files haven't been compiled with the same include path.

The classifiers module is an optional module which provides more compatible parsing behavior than is supported by the core package at the expense of more processing time.  See the documentation of `HeuristicStringParser` for more details.
