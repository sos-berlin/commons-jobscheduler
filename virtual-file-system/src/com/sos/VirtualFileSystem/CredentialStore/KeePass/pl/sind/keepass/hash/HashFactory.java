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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HashFactory {
	private static final Map<String, Hash> hashes;
	//TODO hardcoded for now...
	static{
		HashMap<String, Hash> hash = new HashMap<String, Hash>();
		hash.put(Hash.SHA_256, new BcSHA256Hash());
		hashes = Collections.unmodifiableMap(hash);
	}
	
	public static Hash getHash(String name){
		return hashes.get(name);
	}
}
