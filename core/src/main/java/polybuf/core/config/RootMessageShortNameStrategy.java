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

import java.util.Set;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;

/**
 * A naming strategy that uses the short, unqualified, message name. This strategy is only appropriate if the proto file
 * does not have ambiguous root message names in separate packages or nested scopes.
 * 
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
 * <li>{@code "A"}</li>
 * <li>{@code "B"}</li>
 * <ul>
 * <p>
 * Some serializations reserve special meaning for the default package separator {@code '.'}. This class allows the
 * package separator to be configured to avoid conflicts.
 * 
 * @see RootMessageFullNameStrategy
 * @see SerializerConfig#hasAmbiguousSerializedRootNames
 */
public class RootMessageShortNameStrategy implements RootMessageNamingStrategy {

  @Override
  public String serializedName(Descriptor rootMessage) {
    return rootMessage.getName();
  }

  @Override
  public RootMessage messageForSerializedName(FileDescriptor fileDescriptor, String serializedName,
      RootMessageRegistry registry) {
    Set<RootMessage> roots = registry.messagesForName(serializedName);
    if (roots.size() == 1) {
      return roots.iterator().next();
    }
    return null;
  }
}
