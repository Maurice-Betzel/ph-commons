/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
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
package com.helger.commons.compare;

import java.util.function.ToLongFunction;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;

/**
 * Abstract comparator that handles values that can be represented as long
 * values.
 *
 * @author Philip Helger
 * @param <DATATYPE>
 *        The data type to be compared. Must somehow have a value that can be
 *        compared as a long value.
 */
@NotThreadSafe
@Deprecated
public class LongComparator <DATATYPE> extends AbstractComparator <DATATYPE>
{
  private final ToLongFunction <DATATYPE> m_aExtractor;

  public LongComparator (@Nonnull final ToLongFunction <DATATYPE> aExtractor)
  {
    m_aExtractor = ValueEnforcer.notNull (aExtractor, "Extractor");
  }

  @Override
  protected final int mainCompare (@Nonnull final DATATYPE aElement1, @Nonnull final DATATYPE aElement2)
  {
    final long n1 = m_aExtractor.applyAsLong (aElement1);
    final long n2 = m_aExtractor.applyAsLong (aElement2);
    return CompareHelper.compare (n1, n2);
  }
}
