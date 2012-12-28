package com.wat.melody.cloud.network;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.network.messages";

	public static String NetMgmtMsg_INTRO;
	public static String NetMgmtMsg_RESUME;
	public static String NetMgmtMsg_FAILED;

	public static String MgmtEx_SSH_MGMT_ENABLE_TIMEOUT;

	public static String MgmtEx_WINRM_MGMT_NOT_SUPPORTED;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
