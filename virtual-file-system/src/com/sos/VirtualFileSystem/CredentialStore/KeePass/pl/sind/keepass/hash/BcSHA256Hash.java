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

import org.bouncycastle.crypto.digests.SHA256Digest;

public class BcSHA256Hash implements Hash {

	private SHA256Digest sha256;

	public BcSHA256Hash() {
		super();
		sha256 = new SHA256Digest();
	}

	public byte[] digest() {
		byte result[] = new byte[sha256.getDigestSize()];
		sha256.doFinal(result, 0);
		return result;
	}

	public byte[] hash(byte[] data) {
		sha256.reset();
		update(data);
		return digest();
	}

	public void reset() {
		sha256.reset();
	}

	public void update(byte[] data) {
		sha256.update(data,0,data.length);
	}

	public String getId() {
		return SHA_256;
	}

	public void update(byte[] data, int offset, int lenght) {
		sha256.update(data, offset, lenght);
	}

}
