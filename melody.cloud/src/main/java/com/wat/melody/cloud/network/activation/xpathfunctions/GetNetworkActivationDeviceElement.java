package com.wat.melody.cloud.network.activation.xpathfunctions;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.xml.NetworkActivationHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.common.xpath.XPathFunctionHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public final class GetNetworkActivationDeviceElement implements XPathFunction {

	public static final String NAME = "getNetworkActivationDeviceElement";

	@SuppressWarnings("rawtypes")
	public Object evaluate(List list) throws XPathFunctionException {
		// will not fail: have been registered with arity 1
		Object arg0 = list.get(0);
		try {
			if (XPathFunctionHelper.isElement(arg0)) {
				return NetworkActivationHelper
						.findNetworkActivationDeviceElement((Element) arg0);
			}
			throw new XPathFunctionException(arg0.getClass().getCanonicalName()
					+ ": Not accepted. " + NAME + "() expects an "
					+ Element.class.getCanonicalName() + " as first "
					+ "argument.");
		} catch (NodeRelatedException Ex) {
			throw new XPathFunctionException(Ex);
		}
	}

}