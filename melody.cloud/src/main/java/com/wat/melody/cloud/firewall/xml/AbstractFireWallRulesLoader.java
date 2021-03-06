package com.wat.melody.cloud.firewall.xml;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Directions;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceNameRefs;
import com.wat.melody.common.firewall.exception.IllegalAccessException;
import com.wat.melody.common.firewall.exception.IllegalDirectionsException;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameRefsException;
import com.wat.melody.common.network.Addresses;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractFireWallRulesLoader {

	/**
	 * XML attribute of a FireWall Rule Element, which define the device-names
	 * the FireWall Rule apply to.
	 */
	public static final String DEVICES_NAME_ATTR = "devices-name";

	/**
	 * XML attribute of a FireWall Rule Element, which define its source ips.
	 */
	public static final String FROM_IPS_ATTR = "from-ips";

	/**
	 * XML attribute of a FireWall Rule Element, which define its destination
	 * ips.
	 */
	public static final String TO_IPS_ATTR = "to-ips";

	/**
	 * XML attribute of a FireWall Rule Element, which define its directions.
	 */
	public static final String DIRECTIONS_ATTR = "directions";

	/**
	 * XML attribute of a FireWall Rule Element, which define its access.
	 */
	public static final String ACCESS_ATTR = "access";

	protected NetworkDeviceNameRefs loadNetworkDeviceNameRefs(Element e)
			throws NodeRelatedException {
		try {
			String v = XPathHelper.getHeritedAttributeValue(e, "/@"
					+ DEVICES_NAME_ATTR, null);
			if (v == null || v.length() == 0) {
				return NetworkDeviceNameRefs.ALL;
			}
			try {
				return NetworkDeviceNameRefs.parseString(v);
			} catch (IllegalNetworkDeviceNameRefsException Ex) {
				Attr attr = FilteredDocHelper.getHeritedAttribute(e, "/@"
						+ DEVICES_NAME_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	protected Addresses loadFromIps(Element e, Element instanceElmt)
			throws NodeRelatedException {
		try {
			String v = XPathHelper.getHeritedAttributeValue(e, "/@"
					+ FROM_IPS_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return FireWallRulesHelper.parseAddresses(
					instanceElmt,
					FilteredDocHelper.getHeritedAttribute(e, "/@"
							+ FROM_IPS_ATTR, null), v);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	protected Addresses loadToIps(Element e, Element instanceElmt)
			throws NodeRelatedException {
		try {
			String v = XPathHelper.getHeritedAttributeValue(e, "/@"
					+ TO_IPS_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			return FireWallRulesHelper.parseAddresses(instanceElmt,
					FilteredDocHelper.getHeritedAttribute(e,
							"/@" + TO_IPS_ATTR, null), v);
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	protected Directions loadDirection(Element e) throws NodeRelatedException {
		try {
			String v = XPathHelper.getHeritedAttributeValue(e, "/@"
					+ DIRECTIONS_ATTR, null);
			if (v == null || v.length() == 0) {
				return Directions.ALL;
			}
			try {
				return Directions.parseString(v);
			} catch (IllegalDirectionsException Ex) {
				Attr attr = FilteredDocHelper.getHeritedAttribute(e, "/@"
						+ DIRECTIONS_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	protected Access loadAccess(Element e) throws NodeRelatedException {
		try {
			String v = XPathHelper.getHeritedAttributeValue(e, "/@"
					+ ACCESS_ATTR, null);
			if (v == null || v.length() == 0) {
				return null;
			}
			try {
				return Access.parseString(v);
			} catch (IllegalAccessException Ex) {
				Attr attr = FilteredDocHelper.getHeritedAttribute(e, "/@"
						+ ACCESS_ATTR, null);
				throw new NodeRelatedException(attr, Ex);
			}
		} catch (XPathExpressionException bug) {
			throw new RuntimeException("Because the XPath Expression "
					+ "is hard-coded, such error cannot happened. "
					+ "There must be a bug somewhere.", bug);
		}
	}

	public abstract FireWallRulesPerDevice load(Element instanceElmt)
			throws NodeRelatedException;

}