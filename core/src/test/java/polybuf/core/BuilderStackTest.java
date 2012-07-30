package polybuf.core;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import polybuf.core.config.SerializerConfig;
import polybuf.core.test.Coverage;
import polybuf.core.util.CharacterRange;

import com.google.protobuf.Message.Builder;


public class BuilderStackTest {

  private final boolean strict = true;
  private final boolean compatible = false;
  
  private BuilderStack stack(boolean isStrict) throws Exception {
    return stack(isStrict, null);
  }
  
  private BuilderStack stack(boolean isStrict, Builder rootOverride) throws Exception {
    return new BuilderStack(SerializerConfig.builder(Coverage.class).setIsStrict(isStrict).build().readerConfig(), new TestParser(new DefaultStringParser()));
  }
  
  @Test
  public void emptyExplicitBuilder() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder instanceof Coverage.Message.Builder);
  }

  @Test
  public void emptyImplicitBuilder() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    Builder builder = stack.popRootOrField("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder instanceof Coverage.Message.Builder);
  }
  
  @Test(expected = NoSuchElementException.class)
  public void unpushedExplicitBuilder() throws Exception {
    stack(strict).popRoot("coverage.Message");
  }
  
  @Test(expected = NoSuchElementException.class)
  public void unpushedImplicitBuilder() throws Exception {
    stack(strict).popRootOrField("coverage.Message");
  }
  
  @Test
  public void simpleNestedExplicitMessage() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("required");
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder().setRequired(Coverage.Bool.newBuilder().setRequired(true)).build(),
        builder.build());
  }
  
  @Test
  public void simpleNestedImplicitMessage() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("required");
    stack.pushRootOrField("required");
    assertNull(stack.popRootOrField("required", new CharacterRange("true"), ScalarContext.UNQUOTED));
    assertNull(stack.popRootOrField("required"));
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder().setRequired(Coverage.Bool.newBuilder().setRequired(true)).build(),
        builder.build());
  }
  
  @Test
  public void isRootPoppable() throws Exception {
    BuilderStack stack = stack(strict);
    assertFalse(stack.isRootPoppable());
    stack.pushRoot("coverage.Message");
    assertTrue(stack.isRootPoppable());
    stack.pushField("required", StructureContext.UNSPECIFIED);
    assertFalse(stack.isRootPoppable());
    stack.addOrSetScalarField("required", StructureContext.UNSPECIFIED, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("required");
    assertTrue(stack.isRootPoppable());
    stack.popRoot("coverage.Message");
    assertFalse(stack.isRootPoppable());
  }
  
  @Test(expected=ParseException.class)
  public void explicitUnknownRootName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Bad");
  }
  
  @Test(expected=ParseException.class)
  public void explicitUnknownRootPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.popRoot("coverage.Bad");
  }
  
  @Test(expected=ParseException.class)
  public void explicitWrongRootPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.popRoot("coverage.Bool");
  }
  
  @Test(expected=ParseException.class)
  public void implicitUnknownRootName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Bad");
  }
  
  @Test(expected=ParseException.class)
  public void implicitUnknownRootPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.popRootOrField("coverage.Bad");
  }
  
  @Test(expected=ParseException.class)
  public void implicitWrongRootPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.popRootOrField("coverage.Bool");
  }
  
  
  @Test(expected=ParseException.class)
  public void explicitUnknownFieldName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("unknown", StructureContext.OBJECT);
  }
  
  @Test()
  public void explicitCompatibleUnknownFieldName() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    stack.pushField("unknown", StructureContext.OBJECT);
    stack.pushField("unknownChild", StructureContext.OBJECT);
    stack.popField("unknownChild");
    stack.popField("unknown");
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"),ScalarContext.UNQUOTED);
    stack.popField("required");
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder().setRequired(Coverage.Bool.newBuilder().setRequired(true)).build(),
        builder.build());
  }

  @Test(expected=ParseException.class)
  public void explicitCompatibleWrongUnknownFieldPopName() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    stack.pushField("unknown", StructureContext.OBJECT);
    stack.pushField("unknownChild", StructureContext.OBJECT);
    stack.popField("unknown");
  }

  
  @Test(expected=ParseException.class)
  public void explicitUnknownFieldPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("unknown");
  }
  
  @Test(expected=ParseException.class)
  public void explicitWrongFieldPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("optional");
  }
  
  @Test(expected=ParseException.class)
  public void implicitUnknownFieldName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("required");
    stack.pushRootOrField("unknown");
  }
  
  @Test(expected=ParseException.class)
  public void implicitUnknownFieldPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("required");
    stack.pushRootOrField("required");
    assertNull(stack.popRootOrField("unknown", new CharacterRange("true"), ScalarContext.UNQUOTED));
  }
  
  @Test(expected=ParseException.class)
  public void implicitWrongFieldPopName() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("required");
    stack.pushRootOrField("required");
    assertNull(stack.popRootOrField("optional", new CharacterRange("true"), ScalarContext.UNQUOTED));
  }

  public void implicitCompatibleUnknownFieldName() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("unknown");
    stack.pushRootOrField("unknownChild");
    stack.popRootOrField("unknownChild");
    stack.popRootOrField("unknown");
    stack.pushRootOrField("required");
    stack.pushRootOrField("required");
    assertNull(stack.popRootOrField("required", new CharacterRange("true"), ScalarContext.UNQUOTED));
    assertNull(stack.popRootOrField("required"));
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder().setRequired(Coverage.Bool.newBuilder().setRequired(true)).build(),
        builder.build());
  }

  @Test(expected=ParseException.class)
  public void implicitCompatibleWrongUnknownFieldPopName() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    stack.pushRootOrField("unknown");
    stack.pushRootOrField("unknownChild");
    stack.popRootOrField("unknown");
  }
  
  @Test(expected=ParseException.class)
  public void explicitPrematurePopRoot() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("required", StructureContext.OBJECT);
    stack.popRoot("coverage.Message");
  }
  
  @Test(expected=ParseException.class)
  public void implicitPrematurePopRoot() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("required");
    stack.popRootOrField("coverage.Message");
  }

  @Test(expected=ParseException.class)
  public void explicitCompatiblePrematurePopRoot() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    stack.pushField("unknown", StructureContext.OBJECT);
    stack.popRoot("coverage.Message");
  }
  
  @Test(expected=ParseException.class)
  public void implicitCompatiblePrematurePopRoot() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRootOrField("coverage.Message");
    stack.pushRootOrField("unknown");
    stack.popRootOrField("coverage.Message");
  }

  
  @Test
  public void explicitRepeatedFieldInArrayContext() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("required");
    
    stack.pushField("repeated", StructureContext.ARRAY);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("false"), ScalarContext.UNQUOTED);
    stack.popField("repeated");
    
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder()
        .setRequired(Coverage.Bool.newBuilder().setRequired(true))
        .addRepeated(Coverage.Bool.newBuilder().setRequired(false)).build(),
        builder.build());
  }

  @Test(expected=ParseException.class)
  public void explicitInvalidContextArrayForNonRepeated() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("required", StructureContext.OBJECT);
    stack.pushField("required", StructureContext.ARRAY);
  }
  
  @Test(expected=ParseException.class)
  public void explicitInvalidContextObjectForRepeated() throws Exception {
    BuilderStack stack = stack(strict);
    stack.pushRoot("coverage.Message");
    stack.pushField("repeated", StructureContext.OBJECT);
  }
  
  @Test
  public void explicitCompatibleOptionalInArrayContext() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("required");
    
    // in compatible mode, optional takes last of repeated
    stack.pushField("optional", StructureContext.ARRAY);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("optional");
    
    stack.pushField("optional", StructureContext.ARRAY);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("false"), ScalarContext.UNQUOTED);
    stack.popField("optional");
    
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder()
        .setRequired(Coverage.Bool.newBuilder().setRequired(true))
        .setOptional(Coverage.Bool.newBuilder().setRequired(false)).build(),
        builder.build());
  }
  
  @Test
  public void explicitCompatibleRepeatedInObjectContext() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    
    stack.pushField("required", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("true"), ScalarContext.UNQUOTED);
    stack.popField("required");
    
    // in compatible mode, repeated can accept optional
    stack.pushField("repeated", StructureContext.OBJECT);
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange("false"), ScalarContext.UNQUOTED);
    stack.popField("repeated");
    
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder()
        .setRequired(Coverage.Bool.newBuilder().setRequired(true))
        .addRepeated(Coverage.Bool.newBuilder().setRequired(false)).build(),
        builder.build());
  }

  @Test
  public void explicitCompatibleMessageAsBytes() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    
    // in compatible mode, messages can be sent as bytes
    Coverage.Bool bool1 = Coverage.Bool.newBuilder()
    .setRequired(true)
    .setDefaulted(false)
    .addRepeated(true).addRepeated(false).build();
    Coverage.Bool bool2 = Coverage.Bool.newBuilder()
    .setRequired(false)
    .addRepeated(false).build();
    
    String base64_1 = Base64.encodeBase64String(bool1.toByteArray());
    String base64_2 = Base64.encodeBase64String(bool2.toByteArray());
    
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange(base64_1), ScalarContext.QUOTED);
    stack.addOrSetScalarField("repeated", StructureContext.ARRAY, new CharacterRange(base64_2), ScalarContext.QUOTED);
    stack.addOrSetScalarField("repeated", StructureContext.ARRAY, new CharacterRange(base64_1), ScalarContext.QUOTED);
    
    Builder builder = stack.popRoot("coverage.Message");
    assertNotNull(builder);
    assertTrue(builder.isInitialized());
    assertEquals(Coverage.Message.newBuilder().setRequired(bool1).addRepeated(bool2).addRepeated(bool1).build(), builder.build());
  }
  
  @Test(expected = ParseException.class)
  public void explicitInvalidCompatibleMessageAsBytes() throws Exception {
    BuilderStack stack = stack(compatible);
    stack.pushRoot("coverage.Message");
    stack.addOrSetScalarField("required", StructureContext.OBJECT, new CharacterRange(DefaultStringParserTest.invalidLengthBase64), ScalarContext.QUOTED);
  }
}
