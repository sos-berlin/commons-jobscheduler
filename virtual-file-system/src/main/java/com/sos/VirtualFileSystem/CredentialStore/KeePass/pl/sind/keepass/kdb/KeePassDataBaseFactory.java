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
package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.exceptions.KeePassDataBaseException;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.exceptions.UnsupportedDataBaseException;


public class KeePassDataBaseFactory {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());
	private static final List<DBType>						TYPES;
	static {
		// XKDB is xml based dont want to load class when unnecesary as its
		// dependencies may not be available if someone intends to use kdb only
		ArrayList<DBType> list = new ArrayList<DBType>();
		// TODO Keepass-Class as an Option
		list.add(new DBType(KeePassConst.KDB_SIG_1, KeePassConst.KDB_SIG_2, "com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1.KeePassDataBaseV1"));
		list.add(new DBType(KeePassConst.KDBX_SIG_1, KeePassConst.KDBX_SIG_2, "com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v2.KeePassDataBaseV2"));
		TYPES = Collections.unmodifiableList(list);
	}

	public static KeePassDataBase loadDataBase(final InputStream dbFile, final InputStream keyFile, final String password) throws IOException,
			UnsupportedDataBaseException, KeePassDataBaseException {
		// first load database
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];
		int read = dbFile.read(bytes);
		while (read > 0) {
			bos.write(bytes, 0, read);
			read = dbFile.read(bytes);
		}
		dbFile.close();
		bytes = bos.toByteArray();
		String c = identifyDataBase(bytes);
		return instantiate(c, bytes, keyFile, password);
	}

	private static KeePassDataBase instantiate(final String c, final byte[] data, final InputStream keyFile, final String password)
			throws UnsupportedDataBaseException, KeePassDataBaseException {
		try {
			Class<?> clazz = Class.forName(c);
			Constructor<?> constructor = clazz.getConstructor(new Class<?>[] { byte[].class, InputStream.class, String.class });
			return (KeePassDataBase) constructor.newInstance(new Object[] { data, keyFile, password });
		}
		catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				if (e.getCause() instanceof UnsupportedDataBaseException) {
					throw (UnsupportedDataBaseException) e.getCause();
				}
				else
					if (e.getCause() instanceof KeePassDataBaseException) {
						throw (KeePassDataBaseException) e.getCause();
					}
			}
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			// well... never should happen
			throw new RuntimeException(e);
		}
	}

	public static void saveDataBase(final KeePassDataBase dataBase, final OutputStream dbFile, final InputStream keyFile, final String password) {
	}

	private static String identifyDataBase(final byte[] data) throws UnsupportedDataBaseException {
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int dwSignature1 = bb.getInt();
		int dwSignature2 = bb.getInt();
		for (DBType type : TYPES) {
			if (type.getSig1() == dwSignature1 && type.getSig2() == dwSignature2) {
				return type.getClazz();
			}
		}
		throw new UnsupportedDataBaseException(String.format("Database with signature %x:%x is not supported", dwSignature1, dwSignature2));
	}
	private static class DBType {
		private final int		sig1;
		private final int		sig2;
		private final String	clazz;

		public DBType(final int kdbSig1, final int kdbSig2, final String clazz) {
			sig1 = kdbSig1;
			sig2 = kdbSig2;
			this.clazz = clazz;
		}

		public int getSig1() {
			return sig1;
		}

		public int getSig2() {
			return sig2;
		}

		public String getClazz() {
			return clazz;
		}
	}
}
