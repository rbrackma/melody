package com.wat.melody.core.nativeplugin.call.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CallException extends TaskException {

	private static final long serialVersionUID = -5014891251658746878L;

	public CallException(String msg) {
		super(msg);
	}

	public CallException(Throwable cause) {
		super(cause);
	}

	public CallException(String msg, Throwable cause) {
		super(msg, cause);
	}

}