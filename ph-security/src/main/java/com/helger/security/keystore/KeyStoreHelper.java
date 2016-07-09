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
package com.helger.security.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resourceprovider.ClassPathResourceProvider;
import com.helger.commons.io.resourceprovider.FileSystemResourceProvider;
import com.helger.commons.io.resourceprovider.IReadableResourceProvider;
import com.helger.commons.io.resourceprovider.ReadableResourceProviderChain;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.string.StringHelper;

/**
 * Helper methods to access Java key stores of type JKS (Java KeyStore).
 *
 * @author PEPPOL.AT, BRZ, Philip Helger
 */
@Immutable
public final class KeyStoreHelper
{
  public static final String KEYSTORE_TYPE_JKS = "JKS";
  public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";

  private static final IReadableResourceProvider s_aRRP = new ReadableResourceProviderChain (new ClassPathResourceProvider (),
                                                                                             new FileSystemResourceProvider ());

  @PresentForCodeCoverage
  private static final KeyStoreHelper s_aInstance = new KeyStoreHelper ();

  private KeyStoreHelper ()
  {}

  @Nonnull
  public static KeyStore getJKSKeyStore () throws KeyStoreException
  {
    return KeyStore.getInstance (KEYSTORE_TYPE_JKS);
  }

  @Nonnull
  public static KeyStore getSimiliarKeyStore (@Nonnull final KeyStore aOther) throws KeyStoreException
  {
    return KeyStore.getInstance (aOther.getType (), aOther.getProvider ());
  }

  /**
   * Load a key store from a resource.
   *
   * @param sKeyStorePath
   *        The path pointing to the key store. May not be <code>null</code>.
   * @param sKeyStorePassword
   *        The key store password. May be <code>null</code> to indicate that no
   *        password is required.
   * @return The Java key-store object.
   * @see KeyStore#load(InputStream, char[])
   * @throws GeneralSecurityException
   *         In case of a key store error
   * @throws IOException
   *         In case key store loading fails
   * @throws IllegalArgumentException
   *         If the keystore path is invalid
   */
  @Nonnull
  public static KeyStore loadKeyStoreDirect (@Nonnull final String sKeyStorePath,
                                             @Nullable final String sKeyStorePassword) throws GeneralSecurityException,
                                                                                       IOException
  {
    return loadKeyStoreDirect (sKeyStorePath, sKeyStorePassword == null ? null : sKeyStorePassword.toCharArray ());
  }

  /**
   * Load a key store from a resource.
   *
   * @param sKeyStorePath
   *        The path pointing to the key store. May not be <code>null</code>.
   * @param aKeyStorePassword
   *        The key store password. May be <code>null</code> to indicate that no
   *        password is required.
   * @return The Java key-store object.
   * @see KeyStore#load(InputStream, char[])
   * @throws GeneralSecurityException
   *         In case of a key store error
   * @throws IOException
   *         In case key store loading fails
   * @throws IllegalArgumentException
   *         If the keystore path is invalid
   */
  @Nonnull
  public static KeyStore loadKeyStoreDirect (@Nonnull final String sKeyStorePath,
                                             @Nullable final char [] aKeyStorePassword) throws GeneralSecurityException,
                                                                                        IOException
  {
    ValueEnforcer.notNull (sKeyStorePath, "KeyStorePath");

    // Open the resource stream
    final IReadableResource aRes = s_aRRP.getReadableResource (sKeyStorePath);
    InputStream aIS = null;
    if (aRes != null)
      aIS = aRes.getInputStream ();
    if (aIS == null)
      throw new IllegalArgumentException ("Failed to open key store '" + sKeyStorePath + "'");

    try
    {
      final KeyStore aKeyStore = getJKSKeyStore ();
      aKeyStore.load (aIS, aKeyStorePassword);
      return aKeyStore;
    }
    catch (final KeyStoreException ex)
    {
      throw new IllegalStateException ("No provider can handle JKS key stores! Very weird!", ex);
    }
    finally
    {
      StreamHelper.close (aIS);
    }
  }

  /**
   * Create a new key store based on an existing key store
   *
   * @param aBaseKeyStore
   *        The source key store. May not be <code>null</code>
   * @param sAliasToCopy
   *        The name of the alias in the source key store that should be put in
   *        the new key store
   * @param aAliasPassword
   *        The optional password to access the alias in the source key store.
   *        If it is not <code>null</code> the same password will be used in the
   *        created key store
   * @return The created in-memory key store
   * @throws GeneralSecurityException
   *         In case of a key store error
   * @throws IOException
   *         In case key store loading fails
   */
  @Nonnull
  public static KeyStore createKeyStoreWithOnlyOneItem (@Nonnull final KeyStore aBaseKeyStore,
                                                        @Nonnull final String sAliasToCopy,
                                                        @Nullable final char [] aAliasPassword) throws GeneralSecurityException,
                                                                                                IOException
  {
    ValueEnforcer.notNull (aBaseKeyStore, "BaseKeyStore");
    ValueEnforcer.notNull (sAliasToCopy, "AliasToCopy");

    final KeyStore aKeyStore = getSimiliarKeyStore (aBaseKeyStore);
    // null stream means: create new key store
    aKeyStore.load (null, null);

    // Do we need a password?
    ProtectionParameter aPP = null;
    if (aAliasPassword != null)
      aPP = new PasswordProtection (aAliasPassword);

    aKeyStore.setEntry (sAliasToCopy, aBaseKeyStore.getEntry (sAliasToCopy, aPP), aPP);
    return aKeyStore;
  }

  /**
   * Load the provided keystore in a safe manner.
   *
   * @param sKeyStorePath
   *        Path to the keystore. May not be <code>null</code> to succeed.
   * @param sKeyStorePassword
   *        Password for the keystore. May not be <code>null</code> to succeed.
   * @return The keystore loading result. Never <code>null</code>.
   */
  @Nonnull
  public static LoadedKeyStore loadKeyStore (@Nullable final String sKeyStorePath,
                                             @Nullable final String sKeyStorePassword)
  {
    // Get the parameters for the key store
    if (StringHelper.hasNoText (sKeyStorePath))
      return new LoadedKeyStore (null, EKeyStoreLoadError.KEYSTORE_NO_PATH);

    KeyStore aKeyStore = null;
    // Try to load key store
    try
    {
      aKeyStore = loadKeyStoreDirect (sKeyStorePath, sKeyStorePassword);
    }
    catch (final IllegalArgumentException ex)
    {
      return new LoadedKeyStore (null,
                                 EKeyStoreLoadError.KEYSTORE_LOAD_ERROR_NON_EXISTING,
                                 sKeyStorePath,
                                 ex.getMessage ());
    }
    catch (final Exception ex)
    {
      final boolean bInvalidPW = ex instanceof IOException && ex.getCause () instanceof UnrecoverableKeyException;

      return new LoadedKeyStore (null,
                                 bInvalidPW ? EKeyStoreLoadError.KEYSTORE_INVALID_PASSWORD
                                            : EKeyStoreLoadError.KEYSTORE_LOAD_ERROR_FORMAT_ERROR,
                                 sKeyStorePath,
                                 ex.getMessage ());
    }

    // Finally success
    return new LoadedKeyStore (aKeyStore, null);
  }

  @Nonnull
  private static <T extends KeyStore.Entry> LoadedKey <T> _loadKey (@Nonnull final KeyStore aKeyStore,
                                                                    @Nonnull final String sKeyStorePath,
                                                                    @Nullable final String sKeyStoreKeyAlias,
                                                                    @Nullable final char [] aKeyStoreKeyPassword,
                                                                    @Nonnull final Class <T> aTargetClass)
  {
    ValueEnforcer.notNull (aKeyStore, "KeyStore");
    ValueEnforcer.notNull (sKeyStorePath, "KeyStorePath");

    if (StringHelper.hasNoText (sKeyStoreKeyAlias))
      return new LoadedKey<> (null, EKeyStoreLoadError.KEY_NO_ALIAS);

    if (aKeyStoreKeyPassword == null)
      return new LoadedKey<> (null, EKeyStoreLoadError.KEY_NO_PASSWORD);

    // Try to load the key.
    T aKeyEntry = null;
    try
    {
      final KeyStore.ProtectionParameter aProtection = new KeyStore.PasswordProtection (aKeyStoreKeyPassword);
      final KeyStore.Entry aEntry = aKeyStore.getEntry (sKeyStoreKeyAlias, aProtection);
      if (aEntry == null)
      {
        // No such entry
        return new LoadedKey<> (null, EKeyStoreLoadError.KEY_INVALID_ALIAS, sKeyStoreKeyAlias, sKeyStorePath);
      }
      if (!aTargetClass.isAssignableFrom (aEntry.getClass ()))
      {
        // Not a matching
        return new LoadedKey<> (null,
                                EKeyStoreLoadError.KEY_INVALID_TYPE,
                                sKeyStoreKeyAlias,
                                sKeyStorePath,
                                ClassHelper.getClassName (aEntry));
      }
      aKeyEntry = aTargetClass.cast (aEntry);
    }
    catch (final UnrecoverableKeyException ex)
    {
      return new LoadedKey<> (null,
                              EKeyStoreLoadError.KEY_INVALID_PASSWORD,
                              sKeyStoreKeyAlias,
                              sKeyStorePath,
                              ex.getMessage ());
    }
    catch (final GeneralSecurityException ex)
    {
      return new LoadedKey<> (null,
                              EKeyStoreLoadError.KEY_LOAD_ERROR,
                              sKeyStoreKeyAlias,
                              sKeyStorePath,
                              ex.getMessage ());
    }

    // Finally success
    return new LoadedKey<> (aKeyEntry, null);
  }

  /**
   * Load the specified private key entry from the provided keystore.
   *
   * @param aKeyStore
   *        The keystore to load the key from. May not be <code>null</code>.
   * @param sKeyStorePath
   *        Keystore path. For nice error messages only. May be
   *        <code>null</code>.
   * @param sKeyStoreKeyAlias
   *        The alias to be resolved in the keystore. Must be non-
   *        <code>null</code> to succeed.
   * @param aKeyStoreKeyPassword
   *        The key password for the keystore. Must be non-<code>null</code> to
   *        succeed.
   * @return The key loading result. Never <code>null</code>.
   */
  @Nonnull
  public static LoadedKey <KeyStore.PrivateKeyEntry> loadPrivateKey (@Nonnull final KeyStore aKeyStore,
                                                                     @Nonnull final String sKeyStorePath,
                                                                     @Nullable final String sKeyStoreKeyAlias,
                                                                     @Nullable final char [] aKeyStoreKeyPassword)
  {
    return _loadKey (aKeyStore, sKeyStorePath, sKeyStoreKeyAlias, aKeyStoreKeyPassword, KeyStore.PrivateKeyEntry.class);
  }

  /**
   * Load the specified secret key entry from the provided keystore.
   *
   * @param aKeyStore
   *        The keystore to load the key from. May not be <code>null</code>.
   * @param sKeyStorePath
   *        Keystore path. For nice error messages only. May be
   *        <code>null</code>.
   * @param sKeyStoreKeyAlias
   *        The alias to be resolved in the keystore. Must be non-
   *        <code>null</code> to succeed.
   * @param aKeyStoreKeyPassword
   *        The key password for the keystore. Must be non-<code>null</code> to
   *        succeed.
   * @return The key loading result. Never <code>null</code>.
   */
  @Nonnull
  public static LoadedKey <KeyStore.SecretKeyEntry> loadSecretKey (@Nonnull final KeyStore aKeyStore,
                                                                   @Nonnull final String sKeyStorePath,
                                                                   @Nullable final String sKeyStoreKeyAlias,
                                                                   @Nullable final char [] aKeyStoreKeyPassword)
  {
    return _loadKey (aKeyStore, sKeyStorePath, sKeyStoreKeyAlias, aKeyStoreKeyPassword, KeyStore.SecretKeyEntry.class);
  }

  /**
   * Load the specified private key entry from the provided keystore.
   *
   * @param aKeyStore
   *        The keystore to load the key from. May not be <code>null</code>.
   * @param sKeyStorePath
   *        Keystore path. For nice error messages only. May be
   *        <code>null</code>.
   * @param sKeyStoreKeyAlias
   *        The alias to be resolved in the keystore. Must be non-
   *        <code>null</code> to succeed.
   * @param aKeyStoreKeyPassword
   *        The key password for the keystore. Must be non-<code>null</code> to
   *        succeed.
   * @return The key loading result. Never <code>null</code>.
   */
  @Nonnull
  public static LoadedKey <KeyStore.TrustedCertificateEntry> loadTrustedCertificateKey (@Nonnull final KeyStore aKeyStore,
                                                                                        @Nonnull final String sKeyStorePath,
                                                                                        @Nullable final String sKeyStoreKeyAlias,
                                                                                        @Nullable final char [] aKeyStoreKeyPassword)
  {
    return _loadKey (aKeyStore,
                     sKeyStorePath,
                     sKeyStoreKeyAlias,
                     aKeyStoreKeyPassword,
                     KeyStore.TrustedCertificateEntry.class);
  }
}