package com.wat.melody.common.order.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalOrderNameException extends MelodyException {

	private static final long serialVersionUID = -1223213213225787878L;

	public IllegalOrderNameException(String msg) {
		super(msg);
	}

	public IllegalOrderNameException(Throwable cause) {
		super(cause);
	}

	public IllegalOrderNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}