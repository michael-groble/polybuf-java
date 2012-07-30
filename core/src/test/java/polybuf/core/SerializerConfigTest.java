package polybuf.core;

import static org.junit.Assert.*;

import org.junit.Test;

import polybuf.core.config.RootMessage;
import polybuf.core.config.RootMessageShortNameStrategy;
import polybuf.core.config.SerializerConfig;
import polybuf.core.test.ExtensionsBase;
import polybuf.core.test.ExtensionsExt;
import polybuf.core.test.ExtensionsExtSamePackage;
import polybuf.core.util.Reflection;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

public class SerializerConfigTest {

  private SerializerConfig config;
  
  private RootMessage root(String serializedName) {
    return config.readerConfig().messageForSerializedName(serializedName);
  }

  private FieldDescriptor field(Descriptor message, String serializedName) {
    return config.readerConfig().fieldDescriptor(message, serializedName);
  }
  
  private void assertRootMessage(Class<? extends GeneratedMessage> messageClass, RootMessage root) {
    assertNotNull(root);
    assertEquals(Reflection.invokeStaticGetter(messageClass, "getDescriptor", Descriptor.class), root.getDescriptor());
    assertEquals(Reflection.invokeStaticGetter(messageClass, "getDefaultInstance", Message.class), root.newBuilder().getDefaultInstanceForType());
  }
  
  @Test
  public void extensionsBaseRootFullName() throws Exception {
    config = SerializerConfig.of(ExtensionsBase.class);
    assertFalse(config.hasAmbiguousSerializedRootNames());
    assertRootMessage(ExtensionsBase.Message1.class, root("base.Message1"));
    assertRootMessage(ExtensionsBase.Message2.class, root("base.Message2"));
  }
  
  
  @Test
  public void extensionsBaseRootFullNameAnySeparator() throws Exception {
    config = SerializerConfig.of(ExtensionsBase.class);
    assertFalse(config.hasAmbiguousSerializedRootNames());
    assertRootMessage(ExtensionsBase.Message1.class, root("base.Message1"));
    assertRootMessage(ExtensionsBase.Message1.class, root("base-Message1"));
    assertRootMessage(ExtensionsBase.Message1.class, root("base:Message1"));
    assertRootMessage(ExtensionsBase.Message1.class, root("base$Message1"));
    assertNull(root("base_Message1")); // disallow '_' since it is a valid identifier character
  }
  
  
  @Test
  public void extensionsBaseRootShortName() throws Exception {
    config = SerializerConfig.builder(ExtensionsBase.class)
        .setRootNamingStrategy(new RootMessageShortNameStrategy())
        .build();
    assertFalse(config.hasAmbiguousSerializedRootNames());
    assertRootMessage(ExtensionsBase.Message1.class,root("Message1"));
    assertRootMessage(ExtensionsBase.Message2.class,root("Message2"));
  }

  @Test
  public void extensionsExtRootFullName() throws Exception {
    config = SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class)
        .build();
    assertFalse(config.hasAmbiguousSerializedRootNames());
    assertRootMessage(ExtensionsBase.Message1.class, root("base.Message1"));
    assertRootMessage(ExtensionsExt.Message1.class, root("ext.Message1"));
    assertNull(root("base.Message2")); // not rootable just from import
  }
  
  @Test
  public void extensionsExtRootShortName() throws Exception {
    config = SerializerConfig.builder(ExtensionsExt.class)
        .addRoot(ExtensionsBase.Message1.class)
        .setRootNamingStrategy(new RootMessageShortNameStrategy())
        .build();
    assertTrue(config.hasAmbiguousSerializedRootNames());
    assertNull(root("Message1"));
    assertNull(root("Message2")); // not rootable just from import
  }

  @Test
  public void extensionsExtSamePackageRootFullName() throws Exception {
    config = SerializerConfig.builder(ExtensionsExtSamePackage.class)
        .addRoot(ExtensionsBase.Message1.class)
        .build();
    assertFalse(config.hasAmbiguousSerializedRootNames());
    assertRootMessage(ExtensionsBase.Message1.class, root("base.Message1"));
  }

  @Test
  public void extensionsExtSamePackageRootShortName() throws Exception {
    config = SerializerConfig.builder(ExtensionsExtSamePackage.class)
        .addRoot(ExtensionsBase.Message1.class)
        .setRootNamingStrategy(new RootMessageShortNameStrategy())
        .build();
    assertFalse(config.hasAmbiguousSerializedRootNames());
    assertRootMessage(ExtensionsBase.Message1.class,root("Message1"));
  }

  @Test
  public void extensionsExtExtensions() throws Exception {
    config = SerializerConfig.of(ExtensionsExt.class);
    assertEquals(ExtensionsBase.Message1.getDescriptor().findFieldByNumber(ExtensionsBase.Message1.ID_FIELD_NUMBER),
        field(ExtensionsBase.Message1.getDescriptor(), "id"));
    assertEquals(ExtensionsExt.id.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext.id"));
    assertEquals(ExtensionsExt.type.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext.type"));
    assertEquals(ExtensionsExt.baseType.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext.baseType"));
    assertEquals(ExtensionsExt.Scope.type.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext.Scope.type"));
    assertNull(field(ExtensionsBase.Message1.getDescriptor(), "Scope.type")); // extensions must be fully qualified
    assertNull(field(ExtensionsBase.Message1.getDescriptor(), "type")); // extensions must be fully qualified
  }
  
  @Test
  public void extensionsExtExtensionsAnySeparator() throws Exception {
    config = SerializerConfig.of(ExtensionsExt.class);
    assertEquals(ExtensionsExt.baseType.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext.baseType"));
    assertEquals(ExtensionsExt.baseType.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext-baseType"));
    assertEquals(ExtensionsExt.baseType.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext:baseType"));
    assertEquals(ExtensionsExt.baseType.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "ext$baseType"));
    assertNull(field(ExtensionsBase.Message1.getDescriptor(), "ext_baseType")); // disallow '_' since it is a valid identifier character
  }

  @Test
  public void extensionsExtSamePackageExtensions() throws Exception {
    config = SerializerConfig.of(ExtensionsExtSamePackage.class);
    assertEquals(ExtensionsExtSamePackage.Scope.id.getDescriptor(), field(ExtensionsBase.Message1.getDescriptor(), "base.Scope.id"));
    assertNull(field(ExtensionsBase.Message1.getDescriptor(), "Scope.id")); // extensions must be fully qualified
    assertEquals(ExtensionsBase.Message1.getDescriptor().findFieldByNumber(ExtensionsBase.Message1.ID_FIELD_NUMBER),
        field(ExtensionsBase.Message1.getDescriptor(), "id"));  // id is also in base message so we get that
  }
}
