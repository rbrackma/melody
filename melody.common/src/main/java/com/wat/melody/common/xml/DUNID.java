package com.wat.melody.common.xml;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xml.exception.IllegalDUNIDException;

/**
 * <p>
 * A DUNID is a Document Unique Node IDentifier.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class DUNID {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link DUNID} object.
	 * </p>
	 * 
	 * @param sDunid
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link DUNID}, which is equal to the given <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDUNIDException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static DUNID parseString(String sDunid) throws IllegalDUNIDException {
		return new DUNID(sDunid);
	}

	/**
	 * The pattern this object must satisfied.
	 */
	public static final String PATTERN = "[0-9a-zA-Z]{8}-" + "[0-9a-zA-Z]{4}-"
			+ "[0-9a-zA-Z]{4}-" + "[0-9a-zA-Z]{4}-" + "[0-9a-zA-Z]{12}";

	private String _value;

	public DUNID() {
		try {
			setValue(SysTool.newUUID().toString());
		} catch (IllegalDUNIDException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a new DUNID. "
					+ "Since this DUNID have been automaticaly generated by "
					+ "java.util.UUID, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public DUNID(String sDunid) throws IllegalDUNIDException {
		setValue(sDunid);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
	}

	@Override
	public String toString() {
		return _value;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof DUNID) {
			DUNID dunid = (DUNID) anObject;
			return getValue().equals(dunid.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String sDunid) throws IllegalDUNIDException {
		if (sDunid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a DUNID).");
		}
		if (sDunid.trim().length() == 0) {
			throw new IllegalDUNIDException(Msg.bind(Messages.DUNIDEx_EMPTY,
					sDunid));
		} else if (!sDunid.matches("^" + PATTERN + "$")) {
			throw new IllegalDUNIDException(Msg.bind(Messages.DUNIDEx_INVALID,
					sDunid, PATTERN));
		}
		String previous = getValue();
		_value = sDunid;
		return previous;
	}

}