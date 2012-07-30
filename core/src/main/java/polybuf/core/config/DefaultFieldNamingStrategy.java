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

import com.google.common.base.CaseFormat;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistry.ExtensionInfo;

/**
 * Field parsers need to differentiate between declared fields and extension fields. This naming strategy uses fully
 * qualified names for extension fields and "short" or regular names for declared fields.
 * <p>
 * Consider the following example.
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
 * The field tags have the corresponding serialized names
 * <ul>
 * <li>{@code   1: "id"}</li>
 * <li>{@code 100: "pkg2.type"}</li>
 * <li>{@code 101: "pkg2.Scope.type}</li>
 * </ul>
 * <p>
 * This naming convention is unambiguous since protobuf does not allow extensions with the same qualified name as a
 * declared field.
 * <p>
 * This naming strategy also assumes that the protobuf fields are always defined in lower-case underscore notation (e.g.
 * {@code 'message_type'}) and serialized names are converted to camel case ({@code 'messageType'}) following common
 * JSON and XML practice. Note this is only done for the last field name part, no modification is made to package or
 * message names. In other words, the fully qualified protobuf extension {@code my_package.MyMessage.long_field} becomes
 * {@code my_package.MyMessage.longField}
 * <p>
 * Finally, some serializations reserve special meaning for the default package separator {@code '.'}. This class allows
 * the package separator to be configured to avoid conflicts.
 */
public class DefaultFieldNamingStrategy implements FieldNamingStrategy {
  private final PackageSeparator packageSeparator;
  private final boolean deserializeAnySeparator;
  private final CaseFormat serializedFieldFormat;
  private final CaseFormat protobufFieldFormat = CaseFormat.LOWER_UNDERSCORE;

  /**
   * 
   * @param packageSeparator specified package separator
   * @param deserializeAnySeparator if true, it will accept any valid package separator on parsing
   */
  public DefaultFieldNamingStrategy(PackageSeparator packageSeparator, boolean deserializeAnySeparator) {
    this.packageSeparator = packageSeparator;
    this.deserializeAnySeparator = deserializeAnySeparator;
    this.serializedFieldFormat = CaseFormat.LOWER_CAMEL;
  }

  /**
   * Default constructor uses the dot separator and accepts any valid package separator on parsing.
   */
  public DefaultFieldNamingStrategy() {
    this(PackageSeparator.DOT, true);
  }

  /**
   * Use the specified separator and accept any separator on parsing.
   */
  public DefaultFieldNamingStrategy(PackageSeparator packageSeparator) {
    this(packageSeparator, true);
  }

  @Override
  public String serializedName(FieldDescriptor field) {
    if (field.isExtension()) {
      return serializedNameOfFullName(field.getFullName());
    }
    return serializedNameOfBareFieldName(field.getName());
  }

  @Override
  public FieldDescriptor fieldForSerializedName(Descriptor message, String serializedName, ExtensionRegistry registry) {
    String bareFieldName = bareFieldNameOfSerializedName(serializedName);
    FieldDescriptor descriptor = message.findFieldByName(bareFieldName);

    if (descriptor != null) {
      return descriptor;
    }

    String fullName = fullNameOfSerializedName(serializedName);
    if (fullName == null) {
      return null;
    }
    ExtensionInfo info = registry.findExtensionByName(fullName);
    if (info == null) {
      return null;
    }
    return info.descriptor;
  }

  private String serializedNameOfFullName(String fullName) {
    // we apply serializedFieldFormat to last segment of full name
    // for consistency with other field names
    int index = fullName.lastIndexOf('.');
    if (index >= 0) {
      fullName = fullName.substring(0, index) + serializedNameOfBareFieldName(fullName.substring(index));
    }
    return packageSeparator.serializedName(fullName);
  }

  private String fullNameOfSerializedName(String serializedName) {
    String fullName = deserializeAnySeparator ? PackageSeparator.fullNameForUnknownSerailization(serializedName)
        : packageSeparator.fullName(serializedName);

    int index = fullName.lastIndexOf('.');
    if (index >= 0) {
      fullName = fullName.substring(0, index) + bareFieldNameOfSerializedName(fullName.substring(index));
    }
    return fullName;
  }

  private String serializedNameOfBareFieldName(String fieldName) {
    // TODO verify protobuf field format is actually what we think it is
    // this won't work right if someone uses both camelCase and lower_underscore
    // for thier actuall field names
    return protobufFieldFormat.to(serializedFieldFormat, fieldName);
  }

  private String bareFieldNameOfSerializedName(String serializedName) {
    return serializedFieldFormat.to(protobufFieldFormat, serializedName);
  }
}
