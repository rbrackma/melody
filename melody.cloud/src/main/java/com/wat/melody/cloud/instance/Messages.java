package com.wat.melody.cloud.instance;

import org.eclipse.osgi.util.NLS;

import com.wat.melody.common.ex.Util;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.wat.melody.cloud.instance.messages";

	public static String InstanceTypeEx_EMPTY;
	public static String InstanceTypeEx_INVALID;

	public static String InstanceStateEx_EMPTY;
	public static String InstanceStateEx_INVALID;

	public static String CreateMsg_LIVES;

	public static String DestroyMsg_NO_INSTANCE;
	public static String DestroyMsg_TERMINATED;

	public static String StartMsg_PENDING;
	public static String StartMsg_RUNNING;
	public static String StartMsg_STOPPING;
	public static String StartEx_NO_INSTANCE;
	public static String StartEx_WAIT_TO_START_TIMEOUT;
	public static String StartEx_WAIT_TO_RESTART_TIMEOUT;
	public static String StartEx_SHUTTING_DOWN;
	public static String StartEx_TERMINATED;

	public static String StopMsg_ALREADY_STOPPED;
	public static String StopEx_NO_INSTANCE;

	public static String ResizeMsg_NO_INSTANCE;
	public static String ResizeEx_INVALID_INSTANCE_ID;
	public static String ResizeMsg_NO_NEED;
	public static String ResizeEx_NOT_STOPPED;

	public static String UpdateDiskDevMsg_NO_INSTANCE;
	public static String UpdateDiskDevEx_INVALID_INSTANCE_ID;
	public static String UpdateDiskDevMsg_DISK_DEVICES_RESUME;

	public static String UpdateNetDevMsg_NO_INSTANCE;
	public static String UpdateNetDevEx_INVALID_INSTANCE_ID;
	public static String UpdateNetDevMsg_NETWORK_DEVICES_RESUME;

	public static String UpdateFireWallMsg_NO_INSTANCE;
	public static String UpdateFireWallEx_INVALID_INSTANCE_ID;
	public static String UpdateFireWallMsg_FWRULES_RESUME;

	public static String InstanceEx_MANAGEMENT_ENABLE_FAILED;
	public static String InstanceEx_MANAGEMENT_DISABLE_FAILED;
	public static String InstanceMsg_MANAGEMENT_ENABLE_BEGIN;
	public static String InstanceMsg_MANAGEMENT_ENABLE_SUCCESS;
	public static String InstanceMsg_MANAGEMENT_DISABLE_BEGIN;
	public static String InstanceMsg_MANAGEMENT_DISABLE_SUCCESS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String bind(String message, Object... bindings) {
		for (int i = 0; i < bindings.length; i++) {
			bindings[i] = bindings[i].toString().replaceAll(Util.NEW_LINE,
					Util.NEW_LINE + "  ");
		}
		return NLS.bind(message, bindings);
	}

	private Messages() {
	}

}
