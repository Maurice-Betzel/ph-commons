package com.helger.commons.collection.ext;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;

public class CommonsLinkedHashMap <KEYTYPE, VALUETYPE> extends LinkedHashMap <KEYTYPE, VALUETYPE>
                                  implements ICommonsMap <KEYTYPE, VALUETYPE>
{
  public CommonsLinkedHashMap ()
  {}

  public CommonsLinkedHashMap (final int nInitialCapacity)
  {
    super (nInitialCapacity);
  }

  public CommonsLinkedHashMap (final int nInitialCapacity, final float fLoadFactor)
  {
    super (nInitialCapacity, fLoadFactor);
  }

  public CommonsLinkedHashMap (@Nonnull final Map <? extends KEYTYPE, ? extends VALUETYPE> aMap)
  {
    super (aMap);
  }

  public CommonsLinkedHashMap (final int nInitialCapacity, final float fLoadFactor, final boolean bAccessOrder)
  {
    super (nInitialCapacity, fLoadFactor, bAccessOrder);
  }

  @Nonnull
  @ReturnsMutableCopy
  public CommonsLinkedHashMap <KEYTYPE, VALUETYPE> getClone ()
  {
    return new CommonsLinkedHashMap <> (this);
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsSet <KEYTYPE> copyOfKeySet ()
  {
    // Must be ordered here!
    return CollectionHelper.newOrderedSet (keySet ());
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsSet <Map.Entry <KEYTYPE, VALUETYPE>> copyOfEntrySet ()
  {
    // Must be ordered here!
    return CollectionHelper.newOrderedSet (entrySet ());
  }
}
