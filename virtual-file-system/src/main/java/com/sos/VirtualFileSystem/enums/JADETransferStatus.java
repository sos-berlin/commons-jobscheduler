/**
 * 
 */
package com.sos.VirtualFileSystem.enums;

import com.sos.VirtualFileSystem.common.SOSMsgVfs;

/**
 * @author KB
 *
 */
public enum JADETransferStatus {
	
		// Java -> Code Style -> Formatter -> Edit -> Off/On Tags
		// @formatter:off
		transferUndefined			("SOSVfs_T_0310"), 
		waiting4transfer			("SOSVfs_T_0311"), 
		transferring				("SOSVfs_T_0312"), 
		transferInProgress			("SOSVfs_T_0313"), 
		transferred("SOSVfs_T_0314"), 
		transfer_skipped("SOSVfs_T_0315"), 
		transfer_has_errors("SOSVfs_T_0316"), 
		transfer_aborted("SOSVfs_T_0317"), 
		compressed("SOSVfs_T_0318"), 
		notOverwritten("SOSVfs_T_0319"), 
		deleted("SOSVfs_T_0320"), 
		renamed("SOSVfs_T_0321"), 
		IgnoredDueToZerobyteConstraint("SOSVfs_T_0322"), 
		setBack("SOSVfs_T_0323"), 
		polling("SOSVfs_T_0324"), 
		FileNotFound("SOSVfs_T_0325");
		// @formatter:on
		public String	description;  // returns the i18n text
		public String	MsgCode;	// returns the property key of the i18n file

		public String Text() {
			return description;
		}

		/**
		 * constructor for enum
		 * @param name
		 */
		private JADETransferStatus(final String name) {
			String k;
			if (name == null) {
				k = this.name();
			}
			else {
				k = new SOSMsgVfs(name).get();
				MsgCode = name;
			}
			description = k;
		}

		public static String[] getArray() {
			String[] strA = new String[JADETransferStatus.values().length];
			int i = 0;
			for (JADETransferStatus enuType : JADETransferStatus.values()) {
				strA[i++] = enuType.description;
			}
			return strA;
		}
	}
