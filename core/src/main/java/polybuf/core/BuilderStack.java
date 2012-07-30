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

package polybuf.core;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import polybuf.core.config.ReaderConfig;
import polybuf.core.config.RootMessage;
import polybuf.core.util.CharacterRange;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message.Builder;

/**
 * A stack-like utility for constructing structured messages.
 * <p>
 * Some serializations, like json, provide structure information. The representation distinguishes between scalar
 * values, objects and arrays. A serialization like xml does not explicitly provide such structure.
 * <p>
 * A {@code BuilderStack} maintains a stack of builders to handle fields that are themselves messages and does so in a
 * way that supports serializations both where the structure is known and where it is not.
 * <p>
 * In strict mode, if the structure is provided and differs from that of the protobuf descriptor, a
 * {@link ParseException} is raised. In compatible mode, compatible differences are allowed. See
 * {@link ScalarParser#parse} for compatible parsing behavior.
 */
public class BuilderStack {
  private final ReaderConfig config;
  private final LinkedList<Entry> stack = new LinkedList<Entry>();
  private final ScalarParser scalarParser;

  /**
   * Create a new stack.
   * <p>
   * The created stack may be reused for multiple messages as long as it is empty before pushing a new root.
   * 
   * @param config
   * @param scalarParser
   */
  public BuilderStack(ReaderConfig config, ScalarParser scalarParser) {
    this.config = Preconditions.checkNotNull(config);
    this.scalarParser = Preconditions.checkNotNull(scalarParser);
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  public void clear() {
    stack.clear();
  }

  /**
   * Push the root message of the builder. The stack must be empty to push a new root.
   * 
   * @param builder
   * @throws ParseException if the stack is not empty
   */
  public void pushRootBuilder(Builder builder) {
    if (!stack.isEmpty()) {
      throw new ParseException("root can only be pushed to emtpy stack");
    }
    stack.push(new RealEntry(builder));
  }

  /**
   * Pops the builder.
   * 
   * @return the root builder
   * @throws NoSuchElementException if the stack is empty
   * @throws ParseException if the root still has fields on the stack
   * @see #isRootPoppable
   */
  public Builder popRootBuilder() {
    if (stack.size() == 0) {
      throw new NoSuchElementException();
    }
    if (!isRootPoppable()) {
      throw new ParseException("trying to pop unfinished root");
    }
    return stack.removeFirst().completedBuilder();
  }

  /**
   * Push a root or field.
   * <p>
   * If the stack is empty, the serialized name will be interpreted as a root message name and will behave as
   * {@link #pushRoot}. Otherwise it will be treated as a serialized field name and will behave as {@link #pushField}
   * with an unspecified context
   * 
   * @param serializedName
   */
  public void pushRootOrField(String serializedName) {
    if (stack.isEmpty()) {
      pushRoot(serializedName);
    }
    else {
      pushField(serializedName, StructureContext.UNSPECIFIED);
    }
  }

  /**
   * Pops a root or field with no specified content. This is typically used to pop embedded message fields. If is leaves
   * the stack empty, then return the root builder. Use {@link #popRootOrField(String, CharacterRange, ScalarContext)}
   * to provide content for the popped element
   * 
   * @param serializedName
   * @return builder if root was popped, null if it still has fields being populated.
   * @throws ParseException the serialized name does't match expected name for field or message at top of the stack
   */
  public Builder popRootOrField(String serializedName) {
    return popRootOrField(serializedName, null, null);
  }

  /**
   * Confirm there is one builder on the stack and it does not have any fields currently being built.
   */
  public boolean isRootPoppable() {
    return stack.size() == 1 && !stack.getFirst().hasField();
  }

  /**
   * Pops a root or field with the specified content. Prefer {@link #popRootOrField(String)} when there isn't any
   * content to provide.
   * <p>
   * In compatible mode, when the current field is a message field and content is provided, it will attempt to interpret
   * the content as a base64 encoding of the message bytes.
   * 
   * @param serializedName
   * @param content
   * @param scalarContext context for the provided content
   * @return builder if root was popped, null if it still has fields being populated.
   * @throws ParseException the serialized name does't match expected name for field or message at the top of the stack,
   *           or if there is a problem parsing the provided content.
   */
  public Builder popRootOrField(String serializedName, CharacterRange content, ScalarContext scalarContext) {
    if (!isRootPoppable()) {
      popField(serializedName, content, scalarContext);
      return null;
    }
    // TOOD error if content not null?
    return popRoot(serializedName);
  }

  /**
   * Returns the descriptor for the field at the top of the stack, or {@code null} if none.
   */
  public FieldDescriptor.Type getCurrentFieldType() {
    Entry first = stack.getFirst();
    if (first == null) {
      return null;
    }
    return first.getFieldType();
  }

  /**
   * Push field with the specified content.
   * <p>
   * In compatible mode when this serialized name is not recognized, this and all subsequent pushed fields will be
   * ignored until this field is popped.
   * 
   * @param serializedName
   * @param context
   * @throws ParseException if the serialized name is not a valid field name for the current builder
   * @throws NoSuchElementException if stack is empty
   */
  public void pushField(String serializedName, StructureContext context) {
    Entry first = stack.getFirst();
    Entry child = first.pushField(serializedName, context);
    if (child == null) {
      return;
    }
    stack.push(child);
  }

  /**
   * Pops the field with no content. Use {@link #popField(String, CharacterRange, ScalarContext)} to provide content.
   * 
   * @param serializedName
   * @throws ParseException if there is no field in progress or the the serialized name does not match the current field
   */
  public void popField(String serializedName) {
    popField(serializedName, null, null);
  }

  /**
   * Pops the field with the specified content. Use {@link #popField(String)} when there is no content.
   * <p>
   * In compatible mode, when the current field is a message field and content is provided, it will attempt to interpret
   * the content as a base64 encoding of the message bytes.
   * 
   * @param serializedName
   * @param content
   * @param scalarContext context of the provided content
   * @throws ParseException if there is no field in progress, the the serialized name does not match the current field,
   *           or there are problems parsing the content
   */
  public void popField(String serializedName, CharacterRange content, ScalarContext scalarContext) {
    Entry first = stack.getFirst();
    if (!first.hasField()) {
      // maybe an error from caller, or maybe child message is done
      // and it is time to set it in parent
      if (stack.size() == 1) {
        throw new ParseException("trying to pop empty field");
      }
      Entry parent = stack.get(1);
      if (parent.popChild(serializedName, content, scalarContext, first)) {
        stack.removeFirst();
        return;
      }
      throw new ParseException("trying to pop empty field");
    }
    first.popField(serializedName, content, scalarContext);
  }

  /**
   * Push a root builder with the provided serialized root message name. Stack must be empty to push a new root.
   * 
   * @param serializedName
   * @throws ParseException if the serialized name is not recognized or if there is already a root builder.
   */
  public void pushRoot(String serializedName) {
    RootMessage root = config.messageForSerializedName(serializedName);
    if (root == null) {
      throw new ParseException("unknown root name: " + serializedName);
    }
    pushRootBuilder(root.newBuilder());
  }

  /**
   * Pop a root with the provided serialized name.
   * 
   * @param serializedName
   * @return builder
   * @throws NoSuchElement if the stack is empty
   * @throws ParseException if the serialized name does not match that of the builder.
   */
  public Builder popRoot(String serializedName) {
    if (stack.size() == 0) {
      throw new NoSuchElementException();
    }
    if (!isRootPoppable()) {
      throw new ParseException("trying to pop unfinished root");
    }
    if (!stack.getFirst().isPoppableAs(serializedName)) {
      throw new ParseException("attept to pop root that does not match pushed: " + serializedName);
    }
    return stack.removeFirst().completedBuilder();
  }

  /**
   * Set the scalar field or, in the case of a repeated field, add it with the provided contents.
   * <p>
   * In compatible mode, when the current field is a message field, it will attempt to interpret the content as a base64
   * encoding of the message bytes.
   * 
   * @param serializedName
   * @param structureContext
   * @param content
   * @param scalarContext
   * @throws ParseException if there is no such field name in the current builder or there are problems parsing the
   *           content
   * @throws NoSuchElement if the stack is empty
   */
  public void addOrSetScalarField(String serializedName, StructureContext structureContext, CharacterRange content,
      ScalarContext scalarContext) {
    stack.getFirst().addOrSetScalarField(serializedName, content, structureContext, scalarContext);
  }

  /**
   * Clears the scalar field. Used, for example, when a json parser sees a {@code null} value.
   * 
   * @param serializedName
   * @throws ParseException if there is no such field name in the current builder or there are problems parsing the
   *           content
   * @throws NoSuchElement if the stack is empty
   */
  public void clearScalarField(String serializedName) {
    stack.getFirst().clearScalarField(serializedName);
  }

  private interface Entry {

    Entry pushField(String serializedName, StructureContext context);

    boolean hasField();

    FieldDescriptor.Type getFieldType();

    boolean isPoppableAs(String serializedName);

    void popField(String serializedName);

    void popField(String serializedName, CharacterRange content, ScalarContext scalarContext);

    boolean popChild(String serializedName, CharacterRange content, ScalarContext scalarContext, Entry child);

    Builder completedBuilder();

    void clearScalarField(String serializedName);

    void addOrSetScalarField(String serializedName, CharacterRange content, StructureContext structureContext,
        ScalarContext scalarContext);
  }

  private class RealEntry implements Entry {
    private final Builder builder;
    private FieldDescriptor field;

    public RealEntry(Builder builder) {
      this.builder = builder;
    }

    @Override
    public Entry pushField(String serializedName, StructureContext context) {
      if (this.field != null) {
        throw new ParseException("existing field must be popped before pushing new field");
      }
      FieldDescriptor field = fieldDescriptor(serializedName);
      if (field == null) {
        if (!config.isStrict()) {
          return new UnknownEntry(serializedName);
        }
        throw new ParseException("unknown field name: " + serializedName);
      }

      validateFieldStructure(field, context);

      this.field = field;

      if (FieldDescriptor.Type.MESSAGE == field.getType()) {
        return new RealEntry(builder.newBuilderForField(field));
      }
      return null;
    }

    @Override
    public void popField(String serializedName) {
      popField(serializedName, null, null);
    }

    @Override
    public boolean hasField() {
      return field != null;
    }

    @Override
    public boolean isPoppableAs(String serializedName) {
      return field == null && config.serializedNameForMessage(builder.getDescriptorForType()).equals(serializedName);
    }

    @Override
    public FieldDescriptor.Type getFieldType() {
      if (field == null) {
        return null;
      }
      return field.getType();
    }

    @Override
    public boolean popChild(String serializedName, CharacterRange content, ScalarContext scalarContext, Entry child) {
      if (field == null && child instanceof UnknownEntry) {
        return ((UnknownEntry) child).isComplete(serializedName);
      }
      else if (field != null && field.getType() == FieldDescriptor.Type.MESSAGE
          && field.equals(fieldDescriptor(serializedName))) {
        if (content != null) {
          // this could be message specified in compatible mode as base64 string.
          popField(serializedName, content, scalarContext);
        }
        else {
          addOrSetField(field, ((RealEntry) child).builder.build());
          popField(serializedName);
        }
        return true;
      }
      return false;
    }

    @Override
    public void popField(String serializedName, CharacterRange content, ScalarContext scalarContext) {
      if (field == null) {
        throw new ParseException("trying to pop empty field");
      }
      FieldDescriptor field = fieldDescriptor(serializedName);
      if (field == null) {
        throw new ParseException("unknown field name: " + serializedName);
      }
      if (field != this.field) {
        throw new ParseException("attept to pop field that does not match pushed: " + serializedName);
      }
      if (content != null) {
        addOrSetScalarField(field, content, scalarContext);
      }
      this.field = null;
    }

    @Override
    public Builder completedBuilder() {
      if (hasField()) {
        throw new ParseException("builder is not complete");
      }
      return builder;
    }

    @Override
    public void clearScalarField(String serializedName) {
      FieldDescriptor field = fieldDescriptor(serializedName);
      if (field == null) {
        throw new ParseException("unknown field name: " + serializedName);
      }
      builder.clearField(field);
    }

    @Override
    public void addOrSetScalarField(String serializedName, CharacterRange content, StructureContext structureContext,
        ScalarContext scalarContext) {
      FieldDescriptor field = fieldDescriptor(serializedName);
      if (field == null) {
        if (!config.isStrict()) {
          return;
        }
        throw new ParseException("unknown field name: " + serializedName);
      }
      validateFieldStructure(field, structureContext);
      addOrSetScalarField(field, content, scalarContext);
    }

    private void addOrSetScalarField(FieldDescriptor field, CharacterRange content, ScalarContext scalarContext) {
      Object value = scalarParser.parse(field, content.toString(), scalarContext, config.isStrict());
      if (FieldDescriptor.Type.MESSAGE == field.getType()) {
        assert value instanceof ByteString;
        if (config.isStrict()) {
          throw new IncompatibleFieldParseException("message cannot be parse as bytes in strict mode", field);
        }
        Builder child = builder.newBuilderForField(field);
        try {
          child.mergeFrom((ByteString) value);
        }
        catch (InvalidProtocolBufferException ex) {
          throw new ParseException(ex);
        }
        value = child.build();
      }
      addOrSetField(field, value);
    }

    private void addOrSetField(FieldDescriptor field, Object value) {
      // TODO check overwrite on strict
      if (field.isRepeated()) {
        if (value == null) {
          throw new IncompatibleFieldParseException("Cannot have null in repeated field", field);
        }
        else {
          builder.addRepeatedField(field, value);
        }
      }
      else {
        if (value == null) {
          builder.clearField(field);
        }
        else {
          builder.setField(field, value);
        }
      }
    }

    private void validateFieldStructure(FieldDescriptor field, StructureContext context) {
      if (config.isStrict() && !context.canRepresent(field)) {
        throw new IncompatibleFieldParseException("field does not allow structure " + context, field);
      }
    }

    private FieldDescriptor fieldDescriptor(String serializedName) {
      return config.fieldDescriptor(builder.getDescriptorForType(), serializedName);
    }
  }

  // This type of entry is used for unrecognized fields in compatible mode.
  // It gobbles up all fields pushed into it, yet still checks that the
  // field push/pop names match
  private class UnknownEntry implements Entry {
    private final LinkedList<String> serializedNames = new LinkedList<String>();

    public UnknownEntry(String serializedName) {
      serializedNames.push(serializedName);
    }

    @Override
    public Entry pushField(String serializedName, StructureContext context) {
      serializedNames.push(serializedName);
      return null;
    }

    @Override
    public void popField(String serializedName) {
      if (!serializedNames.pop().equals(serializedName)) {
        throw new ParseException("attept to pop field that does not match pushed: " + serializedName);
      }
    }

    @Override
    public void popField(String serializedName, CharacterRange content, ScalarContext scalarContext) {
      this.popField(serializedName);
    }

    @Override
    public boolean isPoppableAs(String serizliedName) {
      return false; // Unknown can never be root
    }

    public boolean isComplete(String serializedName) {
      return serializedNames.size() == 1 && serializedNames.peek().equals(serializedName);
    }

    @Override
    public boolean hasField() {
      return serializedNames.size() > 1;
    }

    @Override
    public FieldDescriptor.Type getFieldType() {
      return null;
    }

    @Override
    public Builder completedBuilder() {
      if (hasField()) {
        throw new ParseException("builder is not complete");
      }
      return null;
    }

    @Override
    public void clearScalarField(String serializedName) {
    }

    @Override
    public void addOrSetScalarField(String serializedName, CharacterRange content, StructureContext structureContext,
        ScalarContext scalarContext) {
    }

    @Override
    public boolean popChild(String serializedName, CharacterRange content, ScalarContext scalarContext, Entry child) {
      return false;
    }
  }
}
