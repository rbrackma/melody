package com.wat.melody.common.ssh.types.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProxyTypeException extends MelodyException {

	private static final long serialVersionUID = -2133590963741240905L;

	public IllegalProxyTypeException(String msg) {
		super(msg);
	}

	public IllegalProxyTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalProxyTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}