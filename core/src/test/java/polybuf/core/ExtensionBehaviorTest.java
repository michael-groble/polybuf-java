package polybuf.core;

import static org.junit.Assert.*;

import org.junit.Test;

import polybuf.core.test.ExtensionsBase;
import polybuf.core.test.ExtensionsExt;
import polybuf.core.test.ExtensionsExtSamePackage;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class ExtensionBehaviorTest {

  private static String ID_STRING = "extended base";
  private static Integer ID_INT = 12;
  
  private static ExtensionsBase.Message1.Builder baseBuilder() {
    return ExtensionsBase.Message1.newBuilder()
        .setId(ID_STRING)
        .setExtension(ExtensionsExt.baseType, ExtensionsBase.Enum1.A);
  }
  
  private static ExtensionsBase.Message1.Builder extendedBuilder() {
    return baseBuilder()
        .setExtension(ExtensionsExt.id, ID_INT);
  }
  
  @Test
  public void extensionsAreAlwaysOptional() {
    assertTrue(baseBuilder().isInitialized()); // even though we extended with "required" id field and didn't populate it
    assertTrue(extendedBuilder().isInitialized());
  }
  
  @Test
  public void idExtension() {
    FieldDescriptor field = ExtensionsExt.getDescriptor().findExtensionByName("id");
    assertNotNull(field);
    assertEquals("ext.id", field.getFullName());
    assertEquals(100, field.getNumber());
    assertEquals(FieldDescriptor.Type.INT32, field.getType());
    assertEquals(ID_INT, extendedBuilder().getField(field));
  }
  
  @Test
  public void mergeBaseWithoutRegistry() throws InvalidProtocolBufferException {
    byte[] bytes = extendedBuilder().build().toByteArray();
    ExtensionsBase.Message1 message = ExtensionsBase.Message1.parseFrom(bytes);
    FieldDescriptor field = ExtensionsExt.getDescriptor().findExtensionByName("id");
    assertNotSame(ID_INT, field.getDefaultValue());
    assertEquals(field.getDefaultValue(), message.getField(field)); 
  }
  
  @Test
  public void mergeBaseWithRegistry() throws InvalidProtocolBufferException {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    ExtensionsExt.registerAllExtensions(registry);
    
    byte[] bytes = extendedBuilder().build().toByteArray();
    ExtensionsBase.Message1 message = ExtensionsBase.Message1.parseFrom(bytes,registry);
    FieldDescriptor field = ExtensionsExt.getDescriptor().findExtensionByName("id");
    assertEquals(ID_INT, message.getField(field));
  }
  
  @Test
  public void idField() {
    FieldDescriptor field = ExtensionsBase.Message1.getDescriptor().findFieldByName("id");
    assertNotNull(field);
    assertEquals("base.Message1.id", field.getFullName());
    assertEquals(1, field.getNumber());
    assertEquals(FieldDescriptor.Type.STRING, field.getType());
    assertEquals(ID_STRING, extendedBuilder().getField(field));
  }
  
  
  @Test
  public void typeExtension() {
    FieldDescriptor field = ExtensionsExt.getDescriptor().findExtensionByName("type");
    assertNotNull(field);
    assertEquals("ext.type", field.getFullName());
    assertEquals(101, field.getNumber());
    assertEquals(FieldDescriptor.Type.ENUM, field.getType());
  }
  
  @Test
  public void scopedExtension() {
    FieldDescriptor field = ExtensionsExt.getDescriptor().findExtensionByName("Scope.type");
    // not handled in google code
    assertNull(field); 
    
    // but is handled in registry
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    ExtensionsExt.registerAllExtensions(registry);
    assertEquals(ExtensionsExt.Scope.type.getDescriptor(), registry.findExtensionByName("ext.Scope.type").descriptor);
    //
    assertEquals("ext.Scope.type", ExtensionsExt.Scope.type.getDescriptor().getFullName());
  }
  
  @Test
  public void scopedBaseExtension() {
    FieldDescriptor field = ExtensionsExtSamePackage.getDescriptor().findExtensionByName("Scope.id");
    // not handled in google code
    assertNull(field); 
    
    // but is handled in registry
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    ExtensionsExtSamePackage.registerAllExtensions(registry);
    assertEquals(ExtensionsExtSamePackage.Scope.id.getDescriptor(), registry.findExtensionByName("base.Scope.id").descriptor);
    //
    assertEquals("base.Scope.id", ExtensionsExtSamePackage.Scope.id.getDescriptor().getFullName());
  }
}
