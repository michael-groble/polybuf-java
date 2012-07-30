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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import polybuf.core.util.Multimaps;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.protobuf.GeneratedMessage;

/**
 * Immutable registry of root messages.
 * 
 */
public class RootMessageRegistry {

  private final Map<String, RootMessage> rootsByFullName;
  private final SortedSetMultimap<String, RootMessage> rootsByName;
  private final boolean hasAmbiguousNames;

  /**
   * Empty registry.
   */
  public RootMessageRegistry() {
    rootsByFullName = ImmutableMap.of();
    SortedSetMultimap<String, RootMessage> empty = TreeMultimap.create();
    rootsByName = com.google.common.collect.Multimaps.unmodifiableSortedSetMultimap(empty);
    hasAmbiguousNames = false;
  }

  private RootMessageRegistry(Map<String, RootMessage> rootsByFullName,
      SortedSetMultimap<String, RootMessage> rootsByName) {
    this.rootsByFullName = Collections.unmodifiableMap(rootsByFullName);
    this.rootsByName = com.google.common.collect.Multimaps.unmodifiableSortedSetMultimap(rootsByName);
    this.hasAmbiguousNames = Multimaps.hasDuplicateKeys(rootsByName);
  }

  /**
   * Determine the message with the requested fully qualified name. Returns {@code null} if the name cannot be found
   */
  public RootMessage messageForFullName(String fullName) {
    RootMessage root = rootsByFullName.get(fullName);
    if (root == null) {
      return null;
    }
    return root;
  }

  /**
   * Immutable collection of roots in this registry.
   */
  public Collection<RootMessage> roots() {
    return rootsByFullName.values();
  }

  /**
   * The immutable set of potentially ambiguous roots for the requested short, or unqualified message name.
   */
  public Set<RootMessage> messagesForName(String name) {
    return rootsByName.get(name);
  }

  /**
   * True if more than one of the configured roots has the same short, unqualified name.
   */
  public boolean hasAmbiguousNames() {
    return hasAmbiguousNames;
  }

  /**
   * Create a new builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Modifiable class for building a RootMessageRegistry.
   */
  public static final class Builder {
    private final Map<String, RootMessage> rootsByFullName = new HashMap<String, RootMessage>();
    private final SortedSetMultimap<String, RootMessage> rootsByName = TreeMultimap.create();

    /**
     * Add all of the roots declared in the generated outer class.
     * 
     * @see SerializerConfig.Builder#SerializerConfig.Builder
     */
    public Builder addDeclaredRoots(GeneratedOuterClass outer) {
      outer.visitGeneratedMessages(new GeneratedMessageVisitor() {
        @Override
        public void visit(Class<? extends GeneratedMessage> messageClass) {
          if (GeneratedRootMessage.isAnnotatedRoot(messageClass)) {
            addRoot(messageClass);
          }
        }
      });

      return this;
    }

    /**
     * Explicitly add the generated message as a root.
     */
    public Builder addRoot(Class<? extends GeneratedMessage> messageClass) {
      addRoot(new GeneratedRootMessage(messageClass));

      return this;
    }

    /**
     * Generate the immutable registry.
     */
    public RootMessageRegistry build() {
      return new RootMessageRegistry(rootsByFullName, rootsByName);
    }

    private void addRoot(RootMessage root) {
      RootMessage existing = rootsByFullName.put(root.getDescriptor().getFullName(), root);
      if (existing != null) {
        // don't add duplicate short name for existing full name
        return;
      }
      boolean inserted = rootsByName.put(root.getDescriptor().getName(), root);
      assert inserted;
    }
  }
}
