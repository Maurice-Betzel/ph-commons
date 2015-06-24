/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.commons.lang;

import java.security.AccessController;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.lang.priviledged.PrivilegedActionGetClassLoader;
import com.helger.commons.lang.priviledged.PrivilegedActionGetContextClassLoader;
import com.helger.commons.lang.priviledged.PrivilegedActionGetSystemClassLoader;

/**
 * {@link ClassLoader} utility methods.
 *
 * @author Philip Helger
 */
@Immutable
public final class ClassLoaderHelper
{
  @PresentForCodeCoverage
  private static final ClassLoaderHelper s_aInstance = new ClassLoaderHelper ();

  private ClassLoaderHelper ()
  {}

  @Nonnull
  public static ClassLoader getSystemClassLoader ()
  {
    if (System.getSecurityManager () == null)
      return ClassLoader.getSystemClassLoader ();

    return AccessController.doPrivileged (new PrivilegedActionGetSystemClassLoader ());
  }

  @Nonnull
  public static ClassLoader getContextClassLoader ()
  {
    if (System.getSecurityManager () == null)
      return Thread.currentThread ().getContextClassLoader ();

    return AccessController.doPrivileged (new PrivilegedActionGetContextClassLoader ());
  }

  @Nonnull
  public static ClassLoader getClassClassLoader (@Nonnull final Class <?> aClass)
  {
    if (System.getSecurityManager () == null)
      return aClass.getClassLoader ();

    return AccessController.doPrivileged (new PrivilegedActionGetClassLoader (aClass));
  }

  @Nonnull
  public static ClassLoader getDefaultClassLoader ()
  {
    ClassLoader ret = null;
    try
    {
      ret = getContextClassLoader ();
    }
    catch (final RuntimeException ex)
    {
      // e.g. security exception
    }

    // Fallback to class loader of this class
    if (ret == null)
      ret = getClassClassLoader (ClassLoaderHelper.class);

    return ret;
  }
}