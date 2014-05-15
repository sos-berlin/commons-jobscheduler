/*
 * Copyright 2009 Lukasz Wozniak
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.crypto.CipherException;


public class JdkSHA256Hash implements Hash {
	
	private MessageDigest sha256;
	
	public JdkSHA256Hash() throws CipherException{
		super();
		try {
			sha256 = MessageDigest.getInstance(Hash.SHA_256);
		} catch (NoSuchAlgorithmException e) {
			throw new CipherException("SHA-256 is not supported on this system.", e);
		}
	}

	@Override public byte[] digest() {
		return sha256.digest();
	}

	@Override public byte[] hash(final byte[] data) {
		reset();
		update(data);
		return digest();
	}

	@Override public void reset() {
		sha256.reset();
	}

	@Override public void update(final byte[] data) {
		sha256.update(data);
	}

	@Override public String getId() {
		return Hash.SHA_256;
	}

	@Override public void update(final byte[] data, final int offset, final int lenght) {
		update(data, offset, lenght);
	}

}
