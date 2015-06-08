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

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotations.PresentForCodeCoverage;

/**
 * Misc helper methods for handling booleans.
 *
 * @author Philip Helger
 */
@Immutable
public final class BooleanHelper
{
  @PresentForCodeCoverage
  private static final BooleanHelper s_aInstance = new BooleanHelper ();

  private BooleanHelper ()
  {}

  /**
   * Get the primitive value of the passed object value.
   *
   * @param aObj
   *        The Boolean value to be converted
   * @param bDefault
   *        The default value to be returned, if the passed obj is
   *        <code>null</code>.
   * @return Either the primitive boolean value or the default value
   */
  public static boolean getBooleanValue (@Nullable final Boolean aObj, final boolean bDefault)
  {
    return aObj == null ? bDefault : aObj.booleanValue ();
  }
}
