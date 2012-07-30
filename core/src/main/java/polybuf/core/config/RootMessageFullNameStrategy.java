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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

/**
 * A naming strategy that uses the fully qualified message name. This strategy ensures root message names are not
 * ambiguous.
 * <p>
 * Consider the following example.
 * <h2>base.proto</h2>
 * 
 * <pre>
 * package pkg1;
 * 
 * message A {
 *   message B {
 *     required string name = 1;
 *     optional string tag = 2;
 *   }
 *   required string id = 1;
 *   repeated B catalog = 2;
 * }
 * </pre>
 * 
 * The messages have the corresponding serialized names
 * <ul>
 * <li>{@code "pkg1.A"}</li>
 * <li>{@code "pkg1.A.B"}</li>
 * <ul>
 * <p>
 * Some serializations reserve special meaning for the default package separator {@code '.'}. This class allows the
 * package separator to be configured to avoid conflicts.
 * 
 * @see RootMessageShortNameStrategy
 */
public class RootMessageFullNameStrategy implements RootMessageNamingStrategy {
  private final PackageSeparator packageSeparator;
  private final boolean deserializeAnySeparator;

  /**
   * 
   * @param packageSeparator specified package separator
   * @param deserializeAnySeparator if true, it will accept any valid package separator on parsing
   */
  public RootMessageFullNameStrategy(PackageSeparator packageSeparator, boolean deserializeAnySeparator) {
    this.packageSeparator = packageSeparator;
    this.deserializeAnySeparator = deserializeAnySeparator;
  }

  /**
   * Default constructor uses the dot separator and accepts any valid package separator on parsing.
   */
  public RootMessageFullNameStrategy() {
    this(PackageSeparator.DOT, true);
  }

  /**
   * Use the specified separator and accept any separator on parsing.
   */
  public RootMessageFullNameStrategy(PackageSeparator packageSeparator) {
    this(packageSeparator, true);
  }

  @Override
  public String serializedName(Descriptor rootMessage) {
    return serializedNameOfFullName(rootMessage.getFullName());
  }

  @Override
  public RootMessage messageForSerializedName(FileDescriptor fileDescriptor, String serializedName,
      RootMessageRegistry registry) {
    String fullName = fullNameOfSerializedName(serializedName);
    return registry.messageForFullName(fullName);
  }

  private String serializedNameOfFullName(String fullName) {
    return packageSeparator.serializedName(fullName);
  }

  private String fullNameOfSerializedName(String serializedName) {
    if (deserializeAnySeparator) {
      return PackageSeparator.fullNameForUnknownSerailization(serializedName);
    }
    return packageSeparator.fullName(serializedName);
  }

}
