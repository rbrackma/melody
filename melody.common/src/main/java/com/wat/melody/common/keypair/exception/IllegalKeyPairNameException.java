package com.wat.melody.common.keypair.exception;

import com.wat.melody.common.utils.exception.MelodyException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalKeyPairNameException extends MelodyException {

	private static final long serialVersionUID = -3854435676065529965L;

	public IllegalKeyPairNameException() {
		super();
	}

	public IllegalKeyPairNameException(String msg) {
		super(msg);
	}

	public IllegalKeyPairNameException(Throwable cause) {
		super(cause);
	}

	public IllegalKeyPairNameException(String msg, Throwable cause) {
		super(msg, cause);
	}

}