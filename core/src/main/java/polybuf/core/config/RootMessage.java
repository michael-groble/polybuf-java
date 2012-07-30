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

import polybuf.core.proto.Polybuf;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

public abstract class RootMessage implements Comparable<RootMessage> {

  protected final Descriptor descriptor;

  public static boolean isAnnotatedRoot(Descriptor descriptor) {
    Polybuf.MessageOptions options = descriptor.getOptions().getExtension(Polybuf.message);
    if (options != null && options.getRootable()) {
      return true;
    }
    return false;
  }

  protected RootMessage(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  public Descriptor getDescriptor() {
    return descriptor;
  }

  public abstract Message.Builder newBuilder();

  protected boolean isValid() {
    if (descriptor == null) {
      return false;
    }
    // test early so we fail when created instead of later when first used
    if (newBuilder() == null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof RootMessage)) {
      return false;
    }
    RootMessage other = (RootMessage) obj;
    if (descriptor == null) {
      return other.descriptor == null;
    }
    else if (!descriptor.equals(other.descriptor)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(RootMessage other) {
    assert other != null;
    return descriptor.getFullName().compareTo(other.getDescriptor().getFullName());
  }

}