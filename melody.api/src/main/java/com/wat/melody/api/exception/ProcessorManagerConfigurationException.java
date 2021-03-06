package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProcessorManagerConfigurationException extends MelodyException {

	private static final long serialVersionUID = 6549841630840684077L;

	public ProcessorManagerConfigurationException(String msg) {
		super(msg);
	}

	public ProcessorManagerConfigurationException(Throwable cause) {
		super(cause);
	}

	public ProcessorManagerConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}