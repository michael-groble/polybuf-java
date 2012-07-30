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

package polybuf.core.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import polybuf.core.BuilderStack;
import polybuf.core.MessageReader;
import polybuf.core.ScalarParser;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

/**
 * Immutable configuration class encapsulating information about the protobuf messages that can be read and written.
 * <p>
 * By convention, each configuration is limited to a single class (meaning single proto file), but other root messages
 * and base extensions can be added explicitly using {@link Builder}.
 * <p>
 * Root messages are detected based on options in the proto file, if they are found. The options are defined in
 * polybuf.proto.
 * <h2>File options</h2>
 * Polybuf currently has one file option, {@code namespace_uri} which is used to explicitly provide the XML namespace
 * URI of the generated XML schema. It can be specified with a file option in the protobuf file as follows:
 * 
 * <pre>
 * import "polybuf.proto";
 * option (polybuf.file) = {namespace_uri : "http://www.example.org/messages"};
 * </pre>
 * 
 * If not specified in the file, a namespace will be generated based on the class name. The namespace can be explicitly
 * set using {@link Builder#setNamespaceUri}.
 * 
 * <h2>Message options</h2>
 * There is currently only one message option, {@code rootable} which is used to tag those messages which can be
 * recognized by name. It is specified as follows:
 * 
 * <pre>
 * import "polybuf.proto";
 * 
 * message A {
 *   option (polybuf.message) = {rootable : true};
 *   required string id = 1;
 *   extensions 100 to 200;
 * }
 * </pre>
 * 
 * This is similar to the concept of elements in an XML schema. Root messages registered this way are automatically
 * recognized by {@link MessageReader#mergeRootFrom} and {@link MessageReader#mergeRepeatedRootsFrom}. This only works
 * in general if the root message names are {@linkplain SerializerConfig#hasAmbiguousSerializedRootNames unambiguous}.
 * <p>
 * Roots may also be explicitly added with {@link Builder#addRoot}.
 * 
 * <h2>Importing {@code polybuf.proto}</h2>
 * If you make use of the polybuf options, you need to import the {@code polybuf.proto} file. Even if you don't use the
 * option, the polybuf core library needs the generated class from that file. If you don't wish to compile it yourself,
 * it is included in the {@code polybuf-java-proto} module. If you want to compile it yourself, don't include the
 * {@code polybuf-java-proto} module in your classpath, but make sure you set your {@code protoc} include path such that
 * polybuf is included as follows - no leading directory paths.
 * 
 * <pre>
 * import "polybuf.proto";
 * </pre>
 * 
 * If you don't, you will get the dreaded {@code DescriptorValidationException} from the protobuf java library. Just
 * make sure the include path is set correctly and you should be able to use the pre-built jar or your own generated
 * class without problems.
 * <p>
 * Note that the <a href="http://code.google.com/p/protobuf-dt/">{@code protobuf-dt}</a> eclipse plugin builds relative
 * to the project source directory so cross-project imports need to look like {@code "main/proto/imported.proto"}. That
 * path won't work for Polybuf. The plugin is really nice plugin for editing proto files, but you should build your own
 * generated files if you have proto files spanning multiple projects.
 * 
 * @see Builder
 */
public class SerializerConfig {
  private final GeneratedOuterClass outer;
  private final RootMessageRegistry rootMessageRegistry;
  private final ExtensionRegistry extensionRegistry;
  private final SortedSetMultimap<String, FieldDescriptor> extensionFields;
  private final RootMessageNamingStrategy rootNamingStrategy;
  private final FieldNamingStrategy fieldNamingStrategy;
  private final String namespaceUri;
  private final boolean isStrict;
  private final boolean hasAmbiguousSerializedRootNames;
  private final ReaderConfig readerConfig;

  private SerializerConfig(GeneratedOuterClass outer, RootMessageNamingStrategy rootNamingStrategy,
      FieldNamingStrategy fieldNamingStrategy, SortedSetMultimap<String, FieldDescriptor> extensionFields,
      String namespaceUri, RootMessageRegistry rootMessageRegistry, ExtensionRegistry extensionRegistry,
      boolean isStrict) {
    this.outer = outer;
    this.rootNamingStrategy = rootNamingStrategy;
    this.fieldNamingStrategy = fieldNamingStrategy;
    this.extensionFields = Multimaps.unmodifiableSortedSetMultimap(extensionFields);
    this.namespaceUri = namespaceUri;
    this.rootMessageRegistry = rootMessageRegistry;
    this.extensionRegistry = extensionRegistry.getUnmodifiable();
    this.isStrict = isStrict;
    this.hasAmbiguousSerializedRootNames = determineAmbiguousSerializedRootNames();
    this.readerConfig = new MyReaderConfig();
  }

  /**
   * The XML namespace URI of the root protobuf class.
   * 
   * @see Builder#SerializerConfig.Builder
   */
  public String getNamespaceUri() {
    return namespaceUri;
  }

  /**
   * Return the reader config corresponding to this configuration.
   */
  public ReaderConfig readerConfig() {
    return readerConfig;
  }

  /**
   * Based on the included roots messages and the configured {@link RootMessageNamingStrategy}, report if this
   * configuration results in serialized root message names that are ambiguous.
   * <p>
   * If so, message readers will likely have problems correctly parsing incoming root messages if the protobuf message
   * builders are not explicitly provided by the caller.
   * <p>
   * If the caller expects the message reader to determine the correct builder on its own, they should pick a
   * combination of root messages and naming strategy so the names are not ambiguous.
   */
  public boolean hasAmbiguousSerializedRootNames() {
    return hasAmbiguousSerializedRootNames;
  }

  private boolean determineAmbiguousSerializedRootNames() {
    Set<String> serializedNames = new HashSet<String>();
    for (RootMessage root : getRoots()) {
      String name = serializedName(root.getDescriptor());
      if (!serializedNames.add(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * The configured strict {@code true} versus {@code compatible} mode.
   */
  public boolean isStrict() {
    return isStrict;
  }

  /**
   * The configured field naming strategy.
   */
  public FieldNamingStrategy getFieldNamingStrategy() {
    return fieldNamingStrategy;
  }

  /**
   * The configured root message naming strategy.
   */
  public RootMessageNamingStrategy getRootNamingStrategy() {
    return rootNamingStrategy;
  }

  /**
   * Get the serialized name corresponding to the field.
   */
  public String serializedName(FieldDescriptor field) {
    return fieldNamingStrategy.serializedName(field);
  }

  /**
   * Get the serialized name corresponding to the message as a root message.
   */
  public String serializedName(Descriptor message) {
    return rootNamingStrategy.serializedName(message);
  }

  /**
   * Get all of the discovered and explicitly configured root messages that are recognized in this configuration.
   */
  public Collection<RootMessage> getRoots() {
    return rootMessageRegistry.roots();
  }

  /**
   * Find the field descriptor corresponding to the serialized name within the context of the specified message.
   * 
   * @return The descriptor, if known, or {@code null} if no such name is valid for the given message
   */
  public FieldDescriptor fieldForSerializedName(Descriptor message, String serializedName) {
    return fieldNamingStrategy.fieldForSerializedName(message, serializedName, extensionRegistry);
  }

  /**
   * Find the list of field descriptors that are known to be extensions of the extended message name.
   * <p>
   * Consider the following message definitions
   * <h2>base.proto</h2>
   * 
   * <pre>
   * package pkg1;
   * 
   * message A {
   *   required string id = 1;
   *   extensions 100 to 200;
   * }
   * </pre>
   * 
   * <h2>extended.proto</h2>
   * 
   * <pre>
   * import "base.proto";
   * package pkg2;
   * 
   * extend A {
   *   optional int32 type = 100;
   * }
   * 
   * message Scope {
   *   extend A {
   *     optional string type = 101;
   *   }
   * }
   * </pre>
   * 
   * If {@code A} is configured as a root and the extensions have been registered for the second file, then the full
   * name of {@code A} is {@code pkg1.A} and the extension fields will include {@code pkg2.type} and
   * {@code pkg2.Scope.type}
   * <p>
   * This information isn't needed for message readers or writers, but can be useful for schema generators.
   * 
   * @see Builder#registerAllExtensions(Class)
   */
  public Set<FieldDescriptor> extensionFieldsForFullExtendedMessageName(String fullName) {
    return extensionFields.get(fullName);
  }

  /**
   * Generate a customizeable configuration builder for the specified generated class.
   * 
   * @param generatedOuterClass The class generated by the protobuf compiler
   * @see Builder#SerializerConfig.Builder
   */
  public static Builder builder(Class<?> generatedOuterClass) throws Exception {
    return new Builder(generatedOuterClass);
  }

  /**
   * Generate a default configuration based on the specified generated class.
   * <p>
   * This is equivalent to {@code builder(klass).build()}
   * 
   * @param generatedOuterClass The class generated by the protobuf compiler
   * @see #builder
   * @see Builder#SerializerConfig.Builder
   */
  public static SerializerConfig of(Class<?> generatedOuterClass) throws Exception {
    return new Builder(generatedOuterClass).build();
  }

  /**
   * Helper class for incrementally building a serializer configuration.
   */
  public static final class Builder {
    private final GeneratedOuterClass outer;
    private final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
    private final RootMessageRegistry.Builder rootMessageBuilder = RootMessageRegistry.builder();
    private final SortedSetMultimap<String, FieldDescriptor> extensionFields = TreeMultimap.create();
    private String namespaceUri;
    private RootMessageNamingStrategy rootNamingStrategy;
    private FieldNamingStrategy fieldNamingStrategy;
    private boolean isStrict;

    /**
     * Create a modifiable builder for the top-level class generated by the protobuf compiler.
     * <p>
     * In addition to any information obtained via options in the proto file, the constructor configures the following
     * defaults
     * <ul>
     * <li>registers all extensions defined in the generated class</li>
     * <li>generates a default namespace URI if the corresponding option was not set in the proto file</li>
     * <li>sets the root message naming strategy to {@link RootMessageFullNameStrategy}</li>
     * <li>sets the field naming strateg to {@link DefaultFieldNamingStrategy}</li>
     * <li>configures strict, as opposed to {@linkplain ScalarParser#parse compatible}, processing</li>
     * </ul>
     * Extensions needed from other generated files (i.e. extensions from imported proto files) need to be explicitly
     * registered using {@link #registerAllExtensions}.
     */
    public Builder(Class<?> generatedOuterClass) throws Exception {
      this.outer = new GeneratedOuterClass(generatedOuterClass);
      String uri = outer.getAnnotatedNamespaceUri();
      this.namespaceUri = uri != null ? uri : "http://example.com/" + generatedOuterClass.getSimpleName();
      rootMessageBuilder.addDeclaredRoots(outer);
      handleExtensions(outer);
      this.rootNamingStrategy = new RootMessageFullNameStrategy();
      this.fieldNamingStrategy = new DefaultFieldNamingStrategy();
      this.isStrict = true;
    }

    /**
     * Registers extensions from the generated class.
     * <p>
     * This is typically needed for generated classes corresponding to imported proto files.
     */
    public Builder registerAllExtensions(Class<?> generatedOuterClass) {
      handleExtensions(new GeneratedOuterClass(generatedOuterClass));
      return this;
    }

    /**
     * Adds all the declared roots in the generated class (i.e. those with the polybuf message option {@code rootable}
     * set to {@code true}.
     */
    public Builder addDeclaredRoots(Class<?> generatedOuterClass) {
      rootMessageBuilder.addDeclaredRoots(new GeneratedOuterClass(generatedOuterClass));
      return this;
    }

    /**
     * Add a specific protobuf generated message class as a root.
     */
    public Builder addRoot(Class<? extends GeneratedMessage> messageClass) {
      rootMessageBuilder.addRoot(messageClass);
      return this;
    }

    /**
     * Set the namespace URI. If {@code null}, generated schemas will not have a target namespace and XML parsers will
     * use an empty string.
     */
    public Builder setNamespaceUri(String namespaceUri) {
      this.namespaceUri = namespaceUri;
      return this;
    }

    /**
     * Set the root naming strategy.
     * <p>
     * Provided strategies are {@link RootMessageFullNameStrategy} which uses the full name just as in protobuf files,
     * and {@link RootMessageShortNameStrategy} which uses just the last segment of the full name.
     * 
     * @see SerializerConfig#hasAmbiguousSerializedRootNames
     */
    public Builder setRootNamingStrategy(RootMessageNamingStrategy rootNamingStrategy) {
      this.rootNamingStrategy = rootNamingStrategy;
      return this;
    }

    /**
     * Set the field naming strategy.
     * <p>
     * Polybuf only provides one field naming strategy, {@link DefaultFieldNamingStrategy} which uses fully qualified
     * names for extension fields.
     */
    protected Builder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
      this.fieldNamingStrategy = fieldNamingStrategy;
      return this;
    }

    /**
     * Set strict {@code true}, versus compatible {@code false} processing.
     * 
     * @see ScalarParser#parse
     */
    public Builder setIsStrict(boolean isStrict) {
      this.isStrict = isStrict;
      return this;
    }

    /**
     * Generates an immutable serializer config.
     */
    public SerializerConfig build() throws Exception {
      RootMessageRegistry roots = rootMessageBuilder.build();
      // to save memory, remove all extensions that don't extend roots
      for (String fullName : extensionFields.asMap().keySet().toArray(new String[0])) {
        if (roots.messageForFullName(fullName) == null) {
          extensionFields.removeAll(fullName);
        }
      }
      return new SerializerConfig(outer, rootNamingStrategy, fieldNamingStrategy, extensionFields, namespaceUri,
          rootMessageBuilder.build(), extensionRegistry, isStrict);
    }

    private void handleExtensions(GeneratedOuterClass outer) {
      outer.visitGeneratedExtensions(new GeneratedExtensionVisitor() {

        @Override
        public void visit(GeneratedExtension<?, ?> extension) {
          FieldDescriptor field = extension.getDescriptor();
          extensionFields.put(field.getContainingType().getFullName(), field);
        }
      });
      outer.registerAllExtensions(extensionRegistry);
    }
  }

  private class MyReaderConfig implements ReaderConfig {
    @Override
    public boolean isStrict() {
      return isStrict;
    }

    @Override
    public FieldNamingStrategy getFieldNamingStrategy() {
      return fieldNamingStrategy;
    }

    @Override
    public BuilderStack builderStack(ScalarParser scalarParser) {
      return new BuilderStack(this, scalarParser);
    }

    @Override
    public RootMessage messageForSerializedName(String serializedName) {
      return rootNamingStrategy
          .messageForSerializedName(outer.getFileDescriptor(), serializedName, rootMessageRegistry);
    }

    @Override
    public FieldDescriptor fieldDescriptor(Descriptor messageDescriptor, String serializedName) {
      return fieldNamingStrategy.fieldForSerializedName(messageDescriptor, serializedName, extensionRegistry);
    }

    @Override
    public String serializedNameForMessage(Descriptor messageDescriptor) {
      return rootNamingStrategy.serializedName(messageDescriptor);
    }
  }

}
