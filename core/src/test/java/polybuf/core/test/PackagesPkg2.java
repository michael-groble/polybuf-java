// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: packages_pkg2.proto

package polybuf.core.test;

public final class PackagesPkg2 {
  private PackagesPkg2() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface Message1OrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // required int32 id = 1;
    boolean hasId();
    int getId();
    
    // required .pkg1.Enum1 type = 2;
    boolean hasType();
    polybuf.core.test.PackagesPkg1.Enum1 getType();
  }
  public static final class Message1 extends
      com.google.protobuf.GeneratedMessage
      implements Message1OrBuilder {
    // Use Message1.newBuilder() to construct.
    private Message1(Builder builder) {
      super(builder);
    }
    private Message1(boolean noInit) {}
    
    private static final Message1 defaultInstance;
    public static Message1 getDefaultInstance() {
      return defaultInstance;
    }
    
    public Message1 getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message1_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message1_fieldAccessorTable;
    }
    
    private int bitField0_;
    // required int32 id = 1;
    public static final int ID_FIELD_NUMBER = 1;
    private int id_;
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public int getId() {
      return id_;
    }
    
    // required .pkg1.Enum1 type = 2;
    public static final int TYPE_FIELD_NUMBER = 2;
    private polybuf.core.test.PackagesPkg1.Enum1 type_;
    public boolean hasType() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public polybuf.core.test.PackagesPkg1.Enum1 getType() {
      return type_;
    }
    
    private void initFields() {
      id_ = 0;
      type_ = polybuf.core.test.PackagesPkg1.Enum1.A;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasType()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeInt32(1, id_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeEnum(2, type_.getNumber());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, id_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(2, type_.getNumber());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message1 parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(polybuf.core.test.PackagesPkg2.Message1 prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements polybuf.core.test.PackagesPkg2.Message1OrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message1_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message1_fieldAccessorTable;
      }
      
      // Construct using polybuf.core.test.PackagesPkg2.Message1.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        id_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        type_ = polybuf.core.test.PackagesPkg1.Enum1.A;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return polybuf.core.test.PackagesPkg2.Message1.getDescriptor();
      }
      
      public polybuf.core.test.PackagesPkg2.Message1 getDefaultInstanceForType() {
        return polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance();
      }
      
      public polybuf.core.test.PackagesPkg2.Message1 build() {
        polybuf.core.test.PackagesPkg2.Message1 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private polybuf.core.test.PackagesPkg2.Message1 buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        polybuf.core.test.PackagesPkg2.Message1 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public polybuf.core.test.PackagesPkg2.Message1 buildPartial() {
        polybuf.core.test.PackagesPkg2.Message1 result = new polybuf.core.test.PackagesPkg2.Message1(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.id_ = id_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.type_ = type_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof polybuf.core.test.PackagesPkg2.Message1) {
          return mergeFrom((polybuf.core.test.PackagesPkg2.Message1)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(polybuf.core.test.PackagesPkg2.Message1 other) {
        if (other == polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance()) return this;
        if (other.hasId()) {
          setId(other.getId());
        }
        if (other.hasType()) {
          setType(other.getType());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasId()) {
          
          return false;
        }
        if (!hasType()) {
          
          return false;
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              id_ = input.readInt32();
              break;
            }
            case 16: {
              int rawValue = input.readEnum();
              polybuf.core.test.PackagesPkg1.Enum1 value = polybuf.core.test.PackagesPkg1.Enum1.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(2, rawValue);
              } else {
                bitField0_ |= 0x00000002;
                type_ = value;
              }
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required int32 id = 1;
      private int id_ ;
      public boolean hasId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public int getId() {
        return id_;
      }
      public Builder setId(int value) {
        bitField0_ |= 0x00000001;
        id_ = value;
        onChanged();
        return this;
      }
      public Builder clearId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        id_ = 0;
        onChanged();
        return this;
      }
      
      // required .pkg1.Enum1 type = 2;
      private polybuf.core.test.PackagesPkg1.Enum1 type_ = polybuf.core.test.PackagesPkg1.Enum1.A;
      public boolean hasType() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public polybuf.core.test.PackagesPkg1.Enum1 getType() {
        return type_;
      }
      public Builder setType(polybuf.core.test.PackagesPkg1.Enum1 value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000002;
        type_ = value;
        onChanged();
        return this;
      }
      public Builder clearType() {
        bitField0_ = (bitField0_ & ~0x00000002);
        type_ = polybuf.core.test.PackagesPkg1.Enum1.A;
        onChanged();
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:pkg2.Message1)
    }
    
    static {
      defaultInstance = new Message1(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:pkg2.Message1)
  }
  
  public interface Message2OrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional .pkg1.Message1 pkg1 = 1;
    boolean hasPkg1();
    polybuf.core.test.PackagesPkg1.Message1 getPkg1();
    polybuf.core.test.PackagesPkg1.Message1OrBuilder getPkg1OrBuilder();
    
    // optional .pkg2.Message1 pkg2 = 2;
    boolean hasPkg2();
    polybuf.core.test.PackagesPkg2.Message1 getPkg2();
    polybuf.core.test.PackagesPkg2.Message1OrBuilder getPkg2OrBuilder();
  }
  public static final class Message2 extends
      com.google.protobuf.GeneratedMessage
      implements Message2OrBuilder {
    // Use Message2.newBuilder() to construct.
    private Message2(Builder builder) {
      super(builder);
    }
    private Message2(boolean noInit) {}
    
    private static final Message2 defaultInstance;
    public static Message2 getDefaultInstance() {
      return defaultInstance;
    }
    
    public Message2 getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message2_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message2_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional .pkg1.Message1 pkg1 = 1;
    public static final int PKG1_FIELD_NUMBER = 1;
    private polybuf.core.test.PackagesPkg1.Message1 pkg1_;
    public boolean hasPkg1() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public polybuf.core.test.PackagesPkg1.Message1 getPkg1() {
      return pkg1_;
    }
    public polybuf.core.test.PackagesPkg1.Message1OrBuilder getPkg1OrBuilder() {
      return pkg1_;
    }
    
    // optional .pkg2.Message1 pkg2 = 2;
    public static final int PKG2_FIELD_NUMBER = 2;
    private polybuf.core.test.PackagesPkg2.Message1 pkg2_;
    public boolean hasPkg2() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public polybuf.core.test.PackagesPkg2.Message1 getPkg2() {
      return pkg2_;
    }
    public polybuf.core.test.PackagesPkg2.Message1OrBuilder getPkg2OrBuilder() {
      return pkg2_;
    }
    
    private void initFields() {
      pkg1_ = polybuf.core.test.PackagesPkg1.Message1.getDefaultInstance();
      pkg2_ = polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance();
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (hasPkg1()) {
        if (!getPkg1().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      if (hasPkg2()) {
        if (!getPkg2().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(1, pkg1_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeMessage(2, pkg2_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, pkg1_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, pkg2_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static polybuf.core.test.PackagesPkg2.Message2 parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(polybuf.core.test.PackagesPkg2.Message2 prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements polybuf.core.test.PackagesPkg2.Message2OrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message2_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return polybuf.core.test.PackagesPkg2.internal_static_pkg2_Message2_fieldAccessorTable;
      }
      
      // Construct using polybuf.core.test.PackagesPkg2.Message2.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          getPkg1FieldBuilder();
          getPkg2FieldBuilder();
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        if (pkg1Builder_ == null) {
          pkg1_ = polybuf.core.test.PackagesPkg1.Message1.getDefaultInstance();
        } else {
          pkg1Builder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        if (pkg2Builder_ == null) {
          pkg2_ = polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance();
        } else {
          pkg2Builder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return polybuf.core.test.PackagesPkg2.Message2.getDescriptor();
      }
      
      public polybuf.core.test.PackagesPkg2.Message2 getDefaultInstanceForType() {
        return polybuf.core.test.PackagesPkg2.Message2.getDefaultInstance();
      }
      
      public polybuf.core.test.PackagesPkg2.Message2 build() {
        polybuf.core.test.PackagesPkg2.Message2 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private polybuf.core.test.PackagesPkg2.Message2 buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        polybuf.core.test.PackagesPkg2.Message2 result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public polybuf.core.test.PackagesPkg2.Message2 buildPartial() {
        polybuf.core.test.PackagesPkg2.Message2 result = new polybuf.core.test.PackagesPkg2.Message2(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (pkg1Builder_ == null) {
          result.pkg1_ = pkg1_;
        } else {
          result.pkg1_ = pkg1Builder_.build();
        }
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        if (pkg2Builder_ == null) {
          result.pkg2_ = pkg2_;
        } else {
          result.pkg2_ = pkg2Builder_.build();
        }
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof polybuf.core.test.PackagesPkg2.Message2) {
          return mergeFrom((polybuf.core.test.PackagesPkg2.Message2)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(polybuf.core.test.PackagesPkg2.Message2 other) {
        if (other == polybuf.core.test.PackagesPkg2.Message2.getDefaultInstance()) return this;
        if (other.hasPkg1()) {
          mergePkg1(other.getPkg1());
        }
        if (other.hasPkg2()) {
          mergePkg2(other.getPkg2());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (hasPkg1()) {
          if (!getPkg1().isInitialized()) {
            
            return false;
          }
        }
        if (hasPkg2()) {
          if (!getPkg2().isInitialized()) {
            
            return false;
          }
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              polybuf.core.test.PackagesPkg1.Message1.Builder subBuilder = polybuf.core.test.PackagesPkg1.Message1.newBuilder();
              if (hasPkg1()) {
                subBuilder.mergeFrom(getPkg1());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setPkg1(subBuilder.buildPartial());
              break;
            }
            case 18: {
              polybuf.core.test.PackagesPkg2.Message1.Builder subBuilder = polybuf.core.test.PackagesPkg2.Message1.newBuilder();
              if (hasPkg2()) {
                subBuilder.mergeFrom(getPkg2());
              }
              input.readMessage(subBuilder, extensionRegistry);
              setPkg2(subBuilder.buildPartial());
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional .pkg1.Message1 pkg1 = 1;
      private polybuf.core.test.PackagesPkg1.Message1 pkg1_ = polybuf.core.test.PackagesPkg1.Message1.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          polybuf.core.test.PackagesPkg1.Message1, polybuf.core.test.PackagesPkg1.Message1.Builder, polybuf.core.test.PackagesPkg1.Message1OrBuilder> pkg1Builder_;
      public boolean hasPkg1() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public polybuf.core.test.PackagesPkg1.Message1 getPkg1() {
        if (pkg1Builder_ == null) {
          return pkg1_;
        } else {
          return pkg1Builder_.getMessage();
        }
      }
      public Builder setPkg1(polybuf.core.test.PackagesPkg1.Message1 value) {
        if (pkg1Builder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          pkg1_ = value;
          onChanged();
        } else {
          pkg1Builder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder setPkg1(
          polybuf.core.test.PackagesPkg1.Message1.Builder builderForValue) {
        if (pkg1Builder_ == null) {
          pkg1_ = builderForValue.build();
          onChanged();
        } else {
          pkg1Builder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder mergePkg1(polybuf.core.test.PackagesPkg1.Message1 value) {
        if (pkg1Builder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              pkg1_ != polybuf.core.test.PackagesPkg1.Message1.getDefaultInstance()) {
            pkg1_ =
              polybuf.core.test.PackagesPkg1.Message1.newBuilder(pkg1_).mergeFrom(value).buildPartial();
          } else {
            pkg1_ = value;
          }
          onChanged();
        } else {
          pkg1Builder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      public Builder clearPkg1() {
        if (pkg1Builder_ == null) {
          pkg1_ = polybuf.core.test.PackagesPkg1.Message1.getDefaultInstance();
          onChanged();
        } else {
          pkg1Builder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      public polybuf.core.test.PackagesPkg1.Message1.Builder getPkg1Builder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getPkg1FieldBuilder().getBuilder();
      }
      public polybuf.core.test.PackagesPkg1.Message1OrBuilder getPkg1OrBuilder() {
        if (pkg1Builder_ != null) {
          return pkg1Builder_.getMessageOrBuilder();
        } else {
          return pkg1_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          polybuf.core.test.PackagesPkg1.Message1, polybuf.core.test.PackagesPkg1.Message1.Builder, polybuf.core.test.PackagesPkg1.Message1OrBuilder> 
          getPkg1FieldBuilder() {
        if (pkg1Builder_ == null) {
          pkg1Builder_ = new com.google.protobuf.SingleFieldBuilder<
              polybuf.core.test.PackagesPkg1.Message1, polybuf.core.test.PackagesPkg1.Message1.Builder, polybuf.core.test.PackagesPkg1.Message1OrBuilder>(
                  pkg1_,
                  getParentForChildren(),
                  isClean());
          pkg1_ = null;
        }
        return pkg1Builder_;
      }
      
      // optional .pkg2.Message1 pkg2 = 2;
      private polybuf.core.test.PackagesPkg2.Message1 pkg2_ = polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance();
      private com.google.protobuf.SingleFieldBuilder<
          polybuf.core.test.PackagesPkg2.Message1, polybuf.core.test.PackagesPkg2.Message1.Builder, polybuf.core.test.PackagesPkg2.Message1OrBuilder> pkg2Builder_;
      public boolean hasPkg2() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public polybuf.core.test.PackagesPkg2.Message1 getPkg2() {
        if (pkg2Builder_ == null) {
          return pkg2_;
        } else {
          return pkg2Builder_.getMessage();
        }
      }
      public Builder setPkg2(polybuf.core.test.PackagesPkg2.Message1 value) {
        if (pkg2Builder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          pkg2_ = value;
          onChanged();
        } else {
          pkg2Builder_.setMessage(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder setPkg2(
          polybuf.core.test.PackagesPkg2.Message1.Builder builderForValue) {
        if (pkg2Builder_ == null) {
          pkg2_ = builderForValue.build();
          onChanged();
        } else {
          pkg2Builder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder mergePkg2(polybuf.core.test.PackagesPkg2.Message1 value) {
        if (pkg2Builder_ == null) {
          if (((bitField0_ & 0x00000002) == 0x00000002) &&
              pkg2_ != polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance()) {
            pkg2_ =
              polybuf.core.test.PackagesPkg2.Message1.newBuilder(pkg2_).mergeFrom(value).buildPartial();
          } else {
            pkg2_ = value;
          }
          onChanged();
        } else {
          pkg2Builder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000002;
        return this;
      }
      public Builder clearPkg2() {
        if (pkg2Builder_ == null) {
          pkg2_ = polybuf.core.test.PackagesPkg2.Message1.getDefaultInstance();
          onChanged();
        } else {
          pkg2Builder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      public polybuf.core.test.PackagesPkg2.Message1.Builder getPkg2Builder() {
        bitField0_ |= 0x00000002;
        onChanged();
        return getPkg2FieldBuilder().getBuilder();
      }
      public polybuf.core.test.PackagesPkg2.Message1OrBuilder getPkg2OrBuilder() {
        if (pkg2Builder_ != null) {
          return pkg2Builder_.getMessageOrBuilder();
        } else {
          return pkg2_;
        }
      }
      private com.google.protobuf.SingleFieldBuilder<
          polybuf.core.test.PackagesPkg2.Message1, polybuf.core.test.PackagesPkg2.Message1.Builder, polybuf.core.test.PackagesPkg2.Message1OrBuilder> 
          getPkg2FieldBuilder() {
        if (pkg2Builder_ == null) {
          pkg2Builder_ = new com.google.protobuf.SingleFieldBuilder<
              polybuf.core.test.PackagesPkg2.Message1, polybuf.core.test.PackagesPkg2.Message1.Builder, polybuf.core.test.PackagesPkg2.Message1OrBuilder>(
                  pkg2_,
                  getParentForChildren(),
                  isClean());
          pkg2_ = null;
        }
        return pkg2Builder_;
      }
      
      // @@protoc_insertion_point(builder_scope:pkg2.Message2)
    }
    
    static {
      defaultInstance = new Message2(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:pkg2.Message2)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_pkg2_Message1_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_pkg2_Message1_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_pkg2_Message2_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_pkg2_Message2_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023packages_pkg2.proto\022\004pkg2\032\rpolybuf.pro" +
      "to\032\023packages_pkg1.proto\"9\n\010Message1\022\n\n\002i" +
      "d\030\001 \002(\005\022\031\n\004type\030\002 \002(\0162\013.pkg1.Enum1:\006\222\223!\002" +
      "\010\001\"N\n\010Message2\022\034\n\004pkg1\030\001 \001(\0132\016.pkg1.Mess" +
      "age1\022\034\n\004pkg2\030\002 \001(\0132\016.pkg2.Message1:\006\222\223!\002" +
      "\010\001BJ\n\021polybuf.core.test\222\223!3\n1http://www." +
      "example.org/polybuf-test/packages/pkg2"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_pkg2_Message1_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_pkg2_Message1_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_pkg2_Message1_descriptor,
              new java.lang.String[] { "Id", "Type", },
              polybuf.core.test.PackagesPkg2.Message1.class,
              polybuf.core.test.PackagesPkg2.Message1.Builder.class);
          internal_static_pkg2_Message2_descriptor =
            getDescriptor().getMessageTypes().get(1);
          internal_static_pkg2_Message2_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_pkg2_Message2_descriptor,
              new java.lang.String[] { "Pkg1", "Pkg2", },
              polybuf.core.test.PackagesPkg2.Message2.class,
              polybuf.core.test.PackagesPkg2.Message2.Builder.class);
          com.google.protobuf.ExtensionRegistry registry =
            com.google.protobuf.ExtensionRegistry.newInstance();
          registerAllExtensions(registry);
          polybuf.core.proto.Polybuf.registerAllExtensions(registry);
          polybuf.core.test.PackagesPkg1.registerAllExtensions(registry);
          return registry;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          polybuf.core.proto.Polybuf.getDescriptor(),
          polybuf.core.test.PackagesPkg1.getDescriptor(),
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}