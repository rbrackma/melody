package com.wat.melody.common.keypair;

import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairName {

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link KeyPairName} object.
	 * </p>
	 * 
	 * @param sKeyPairName
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link KeyPairName} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalKeyPairNameException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link KeyPairName}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static KeyPairName parseString(String sKeyPairName)
			throws IllegalKeyPairNameException {
		return new KeyPairName(sKeyPairName);
	}

	/**
	 * The pattern a KeyPairName must satisfy.
	 */
	public static final String PATTERN = "[.\\d\\w-_\\[\\]\\{\\}\\(\\)\\\\ \"']+";

	private String msValue;

	public KeyPairName(String sKeyPairName) throws IllegalKeyPairNameException {
		setValue(sKeyPairName);
	}

	@Override
	public String toString() {
		return msValue;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof KeyPairName) {
			KeyPairName on = (KeyPairName) anObject;
			return getValue().equals(on.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	public String setValue(String sKeyPairName)
			throws IllegalKeyPairNameException {
		if (sKeyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ KeyPairName.class.getCanonicalName() + ").");
		}
		if (sKeyPairName.trim().length() == 0) {
			throw new IllegalKeyPairNameException(Messages.bind(
					Messages.KeyPairNameEx_EMPTY, sKeyPairName));
		} else if (!sKeyPairName.matches("^" + PATTERN + "$")) {
			throw new IllegalKeyPairNameException(Messages.bind(
					Messages.KeyPairNameEx_INVALID, sKeyPairName, PATTERN));
		}
		String previous = toString();
		msValue = sKeyPairName;
		return previous;
	}

}