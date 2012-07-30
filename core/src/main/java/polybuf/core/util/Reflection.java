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

package polybuf.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflection {

  /**
   * Get a declared static method. Returns {@code null} if none exists or if the matching method is not static.
   */
  public static Method getStaticDeclaredMethod(Class<?> klass, String methodName, Class<?>... parameterClasses) {
    Method method = null;
    try {
      method = klass.getDeclaredMethod(methodName, parameterClasses);
      if (!Modifier.isStatic(method.getModifiers())) {
        method = null;
      }
    }
    catch (Exception ex) {
    }
    return method;
  }

  /**
   * Get a declared static parameterless method (e.g. "getter") with the specified return type. Returns {@code null} if
   * none exists, or the matching signature is not static.
   */
  public static Method getStaticDeclaredMethodReturning(Class<?> klass, String name, Class<?> returning) {
    Method method = getStaticDeclaredMethod(klass, name);
    if (method != null) {
      if (!returning.isAssignableFrom(method.getReturnType())) {
        method = null;
      }
    }
    return method;
  }

  /**
   * Invoke a static parameterless method (e.g. "getter") with the specified return type. Returns {@code null} if none
   * exists or if the matching signature is not static, otherwise returns the result of invoking the method.
   */
  public static <T> T invokeStaticGetter(Class<?> klass, String name, Class<? extends T> returning) {
    Method method = getStaticDeclaredMethodReturning(klass, name, returning);
    if (method == null) {
      return null;
    }
    try {
      return returning.cast(method.invoke(null));
    }
    catch (Exception ex) {
    }
    return null;
  }
}
