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

package polybuf.json;

import java.io.IOException;
import java.io.OutputStream;

import polybuf.core.EncoderFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * JSON encoder factory using an underlying jackson {@link JsonFactory}.
 */
public abstract class JsonEncoderFactory implements EncoderFactory<OutputStream> {
  private static final JsonFactory noCloseFactory;

  static {
    noCloseFactory = new JsonFactory();
    noCloseFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
  }

  private final JsonFactory factory;

  /**
   * Encoder using the default JSON Factory.
   */
  public JsonEncoderFactory() {
    this(noCloseFactory);
  }

  /**
   * Encoder using the specified JSON factory.
   */
  public JsonEncoderFactory(JsonFactory factory) {
    this.factory = factory;
  }

  protected JsonGenerator generator(OutputStream stream) throws IOException {
    return factory.createJsonGenerator(stream);
  }
}
