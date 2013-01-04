package com.wat.melody.cloud.network;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.IllegalManagementMethodNetworkException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class NetworkManagementHelper {

	/**
	 * XML attribute in the SD, which indicate if the Network Device Management
	 * feature should be used or not.
	 */
	public static final String ENABLE_NETWORK_MGNT_ATTR = "enableNetworkManagement";

	/**
	 * XML attribute in the SD, which indicate the timeout of the Network Device
	 * Management feature operations.
	 */
	public static final String ENABLE_NETWORK_MGNT_TIMEOUT_ATTR = "enableNetworkManagementTimeout";

	/**
	 * XML attribute in the SD, which contains the XPath Expression to select
	 * Network Devices
	 */
	public static final String NETWORK_DEVICE_NODES_SELECTOR_ATTR = "networkDevicesSelector";

	/**
	 * XML Element in the RD, which contains Network Device Management datas of
	 * the related Instance Node (more formally called the
	 * "Network Device Management Node")
	 */
	public static final String NETWORK_MGMT_NE = "network-management";

	/**
	 * XPath Expression which select the Network Device Management Node of the
	 * related Instance Node.
	 */
	public static final String NETWORK_MGMT_NODE_SELECTOR = "//"
			+ NETWORK_MGMT_NE;

	/**
	 * XML attribute of the Network Device Management Node, which indicate if
	 * the Network Device Management feature should be used or not for the
	 * related Instance.
	 */
	public static final String NETWORK_MGMT_ENABLE_ATTR = "enable";

	/**
	 * XML attribute of the Network Device Management Node, which indicate the
	 * Network Device Management feature's Management Method to use for the
	 * related Instance.
	 */
	public static final String NETWORK_MGMT_METHOD_ATTR = "method";

	/**
	 * XML attribute of the Network Device Management Node, which indicate the
	 * Network Device Management feature's Port to use for the related Instance.
	 */
	public static final String NETWORK_MGMT_PORT_ATTR = "port";

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * XPath Expression to select Network Devices.
	 */
	public static final String NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE = "networkDevicesSelector";

	/**
	 * Default XPath Expression to select Network Devices.
	 */
	public static final String DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR = "//interface";

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * criteria of XPath Expression to select Network Device Management Node.
	 */
	public static final String NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR = "mgmtNetworkDeviceCriteria";

	/**
	 * Default XPath Expression to select Network Device Management Node
	 */
	public static final String DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA = "@device='eth0'";

	/**
	 * XML attribute of the Network Device Management Node, which contains the
	 * XML attribute of the Network Device Management Node which select the Host
	 * to manage.
	 */
	public static final String NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR = "mgmtDetworkDeviceAttribute";

	/**
	 * Default XML attribute of the Network Device Management Node which select
	 * the Host to manage.
	 */
	public static final String DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR = "ip";

	/**
	 * <p>
	 * Return the Network Device Management {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return <ul>
	 *         <li>The Network Device Management {@link Node} related to the
	 *         given Instance {@link Node}, if one Network Device Management
	 *         {@link Node} is found ;</li>
	 *         <li>The last Network Device Management {@link Node} related to
	 *         the given Instance {@link Node}, if multiple Network Device
	 *         Management {@link Node} were found ;</li>
	 *         </ul>
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Network Device Management {@link Node} can be found.
	 */
	public static Node findNetworkManagementNode(Node instanceNode)
			throws ResourcesDescriptorException {
		NodeList nl = null;
		try {
			nl = XPathHelper.getHeritedContent(instanceNode,
					NETWORK_MGMT_NODE_SELECTOR);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the herited content of '" + NETWORK_MGMT_NODE_SELECTOR
					+ "'. " + "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		if (nl.getLength() == 0) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_NO_MGMT_NODE, NETWORK_MGMT_NE));
		} else if (nl.getLength() > 1) {
			return nl.item(nl.getLength() - 1);
		}
		return nl.item(0);
	}

	/**
	 * <p>
	 * Return the Management Network Device Selector of the given Network Device
	 * Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}, or
	 *            <code>null</code>.
	 * 
	 * @return the concatenation of :
	 *         <ul>
	 *         <li>The Network Devices Selector of the given Network Device
	 *         Management {@link Node} (see
	 *         {@link #getNetworkDevicesSelector(Node)} ;</li>
	 *         <li>The character '[' ;</li>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR} XML Attribute of
	 *         the given Network Device Management {@link Node} or
	 *         {@link #DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA} ;</li>
	 *         <li>The character ']' ;</li>
	 *         </ul>
	 */
	public static String getManagementNetworkDeviceSelector(Node mgmtNode) {
		String sCriteria = null;
		try {
			sCriteria = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_DEVICE_NODE_CRITERIA_ATTR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			sCriteria = DEFAULT_NETOWRK_MGMT_DEVICE_NODE_CRITERIA;
		}
		return getNetworkDevicesSelector(mgmtNode) + "[" + sCriteria + "]";
	}

	/**
	 * <p>
	 * Return the Management Network Device Attribute Selector of the given
	 * Network Device Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}, or
	 *            <code>null</code>.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR} XML
	 *         Attribute of the given Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR} if the
	 *         given Network Device Management {@link Node} was
	 *         <code>null</code> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR} if no
	 *         {@link #NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR} XML
	 *         Attribute can be found ;</li>
	 *         </ul>
	 */
	public static String getManagementNetworkDeviceAttribute(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR_ATTR)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETWORK_MGMT_DEVICE_ATTRIBUTE_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Management Network Device {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Management Network Device {@link Node} related to the given
	 *         Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector is not a valid
	 *             XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node findManagementNetworkDevice(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkDevice(instanceNode, mgmtNode);
	}

	/**
	 * <p>
	 * Return the Management Network Device {@link Node} related to the given
	 * Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Management Network Device {@link Node} related to the given
	 *         Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector is not a valid
	 *             XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 */
	public static Node getManagementNetworkDevice(Node instanceNode,
			Node mgmtNode) throws ResourcesDescriptorException {
		NodeList nl = null;
		String sMgmtInterfaceSelector = getManagementNetworkDeviceSelector(mgmtNode);
		try {
			nl = Doc.evaluateAsNodeList("." + sMgmtInterfaceSelector,
					instanceNode);
			if (nl != null && nl.getLength() > 1) {
				throw new ResourcesDescriptorException(instanceNode,
						Messages.NetMgmtEx_TOO_MANY_MGMT_NETWORK_DEVICE);
			} else if (nl == null || nl.getLength() == 0) {
				sMgmtInterfaceSelector = getNetworkDevicesSelector(mgmtNode);
				nl = Doc.evaluateAsNodeList("." + sMgmtInterfaceSelector,
						instanceNode);
			}
		} catch (XPathExpressionException Ex) {
			throw new ResourcesDescriptorException(instanceNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_SELECTOR,
					sMgmtInterfaceSelector), Ex);
		}
		if (nl == null || nl.getLength() == 0) {
			throw new ResourcesDescriptorException(instanceNode,
					Messages.NetMgmtEx_NO_MGMT_NETWORK_DEVICE);
		}
		return nl.item(0);
	}

	/**
	 * 
	 * @param instanceNodes
	 *            is a {@link List} of {@link Node} which describes multiple
	 *            Instances.
	 * 
	 * @return a {@link list} of Management Network Device {@link Host} related
	 *         to the given {@link List} of Instance {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device Selector of one Instance is
	 *             not a valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found in
	 *             at least one Instance.
	 * @throws ResourcesDescriptorException
	 *             if the Management Network Device {@link Node} of one Instance
	 *             doesn't have a attribute equal to the Network Device
	 *             Management Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the value of one attribute is not a valid {@link Host}.
	 */
	public static List<Host> findManagementNetworkHost(List<Node> instanceNodes)
			throws ResourcesDescriptorException {
		List<Host> hl = new ArrayList<Host>();
		Node mgmtNode = null;
		for (Node instanceNode : instanceNodes) {
			try {
				mgmtNode = findNetworkManagementNode(instanceNode);
			} catch (ResourcesDescriptorException Ex) {
				// raised when Network Device Management datas are invalid.
				// in this situation, we will use default values
			}
			hl.add(getManagementNetworkHost(instanceNode, mgmtNode));
		}
		return hl;
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * 
	 * @return the Instance's Management Network Device's {@link Host}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Management
	 *             Network Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host findManagementNetworkHost(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		return getManagementNetworkHost(instanceNode, mgmtNode);
	}

	/**
	 * 
	 * @param instanceNode
	 *            is a {@link Node} which describes an Instance.
	 * @param mgmtNode
	 *            is the Network Device Management {@link Node} related to the
	 *            given Instance {@link Node}, or <code>null</code>, if the
	 *            given Instance {@link Node} has no Network Device Management
	 *            {@link Node}.
	 * 
	 * @return the Instance's Management Network Device {@link Host}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device Selector is not a
	 *             valid XPath expression.
	 * @throws ResourcesDescriptorException
	 *             if no Management Network Device {@link Node} can be found.
	 * @throws ResourcesDescriptorException
	 *             if the Instance's Management Network Device {@link Node}
	 *             doesn't have a attribute equal to the Instance's Management
	 *             Network Device Attribute Selector.
	 * @throws ResourcesDescriptorException
	 *             if the found value is not a valid {@link Host}.
	 */
	public static Host getManagementNetworkHost(Node instanceNode, Node mgmtNode)
			throws ResourcesDescriptorException {
		Node netNode = getManagementNetworkDevice(instanceNode, mgmtNode);
		String attr = getManagementNetworkDeviceAttribute(mgmtNode);
		String sHost = null;
		try {
			sHost = netNode.getAttributes().getNamedItem(attr).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_MGMT_NETWORK_DEVICE_ATTRIBUTE,
					attr));
		}
		try {
			return Host.parseString(sHost);
		} catch (IllegalHostException Ex) {
			throw new ResourcesDescriptorException(netNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, attr), Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_PORT_ATTR} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_PORT_ATTR} can be found in the
	 *             given Instance's Network Device Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_PORT_ATTR} found in
	 *             the given Instance's Network Device Management {@link Node}
	 *             is not a valid {@link Port}.
	 */
	public static Port findManagementNetworkPort(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkPort(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_PORT_ATTR} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Network Device Management {@link Node} is
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_PORT_ATTR} can be found in the
	 *             given Network Device Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_PORT_ATTR} found in
	 *             the given Network Device Management {@link Node} is not a
	 *             valid {@link Port}.
	 */
	public static Port getManagementNetworkPort(Node mgmtNode)
			throws ResourcesDescriptorException {
		String sPort = null;
		try {
			sPort = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_PORT_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, NETWORK_MGMT_PORT_ATTR));
		}
		try {
			return Port.parseString(sPort);
		} catch (IllegalPortException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, NETWORK_MGMT_PORT_ATTR),
					Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return the Instance's Network Device Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_METHOD_ATTR} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Instance {@link Node} is not valid (ex :
	 *             contains invalid HERIT_ATTR).
	 * @throws ResourcesDescriptorException
	 *             if no Instance's Network Device Management {@link Node} can
	 *             be found.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_METHOD_ATTR} can be found in the
	 *             given Instance's Network Device Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_METHOD_ATTR} found
	 *             in the given Instance's Network Device Management
	 *             {@link Node} is not a valid {@link ManagementNetworkMethod}.
	 */
	public static ManagementNetworkMethod findManagementNetworkMethod(
			Node instanceNode) throws ResourcesDescriptorException {
		Node mgmtNode = findNetworkManagementNode(instanceNode);
		return getManagementNetworkMethod(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return the Network Device Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_METHOD_ATTR} XML Attribute.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the given Network Device Management {@link Node} is
	 *             <code>null</code>.
	 * @throws ResourcesDescriptorException
	 *             if no {@link #NETWORK_MGMT_METHOD_ATTR} can be found in the
	 *             given Network Device Management {@link Node}.
	 * @throws ResourcesDescriptorException
	 *             if the value of the {@link #NETWORK_MGMT_METHOD_ATTR} found
	 *             in the given Network Device Management {@link Node} is not a
	 *             valid {@link ManagementNetworkMethod}.
	 */
	public static ManagementNetworkMethod getManagementNetworkMethod(
			Node mgmtNode) throws ResourcesDescriptorException {
		String sMethod = null;
		try {
			sMethod = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_METHOD_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_MISSING_ATTR, NETWORK_MGMT_METHOD_ATTR));
		}
		try {
			return ManagementNetworkMethod.parseString(sMethod);
		} catch (IllegalManagementMethodNetworkException Ex) {
			throw new ResourcesDescriptorException(mgmtNode, Messages.bind(
					Messages.NetMgmtEx_INVALID_ATTR, NETWORK_MGMT_METHOD_ATTR),
					Ex);
		}
	}

	/**
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Instance's Network Device Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_ENABLE_ATTR} XML Attribute ;</li>
	 *         <li><code>false</code> if the given Instance has no Network
	 *         Management {@link Node} ;</li>
	 *         <li><code>true</code> if the given Instance's Network Device
	 *         Management {@link Node} has no {@link #NETWORK_MGMT_ENABLE_ATTR}
	 *         XML Attribute ;</li>
	 *         </ul>
	 */
	public static boolean isManagementNetworkEnable(Node instanceNode)
			throws ResourcesDescriptorException {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			return false;
		}
		return getManagementNetworkEnable(mgmtNode);
	}

	/**
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The Network Device Management {@link Node}'s
	 *         {@link #NETWORK_MGMT_ENABLE_ATTR} XML Attribute ;</li>
	 *         <li><code>true</code> if the given Network Device Management
	 *         {@link Node} is <code>null</code> ;</li>
	 *         <li><code>true</code> if the given Network Device Management
	 *         {@link Node} has no {@link #NETWORK_MGMT_ENABLE_ATTR} XML
	 *         Attribute ;</li>
	 *         </ul>
	 */
	public static boolean getManagementNetworkEnable(Node mgmtNode) {
		String sEnable = null;
		try {
			sEnable = mgmtNode.getAttributes()
					.getNamedItem(NETWORK_MGMT_ENABLE_ATTR).getNodeValue();
		} catch (NullPointerException Ex) {
			return true;
		}
		return Boolean.parseBoolean(sEnable);
	}

	/**
	 * <p>
	 * Return the Network Devices Selector of the given Instance {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute of
	 *         the given Instance's Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Instance has no Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Instance's Network Device Management {@link Node} has no
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute ;</li>
	 *         </ul>
	 */
	public static String findNetworkDevicesSelector(Node instanceNode) {
		Node mgmtNode = null;
		try {
			mgmtNode = findNetworkManagementNode(instanceNode);
		} catch (ResourcesDescriptorException Ex) {
			// raised when Network Device Management datas are invalid.
			// in this situation, we will use default values
		}
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Network Devices Selector of the given Network Device
	 * Management {@link Node}.
	 * </p>
	 * 
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * 
	 * @return <ul>
	 *         <li>The content of the
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute of
	 *         the given Network Device Management {@link Node} ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Network Device Management {@link Node} is <code>null</code> ;</li>
	 *         <li>
	 *         {@link #DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR} if the given
	 *         Network Device Management {@link Node} has no
	 *         {@link #NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE} XML Attribute ;</li>
	 *         </ul>
	 */
	public static String getNetworkDevicesSelector(Node mgmtNode) {
		try {
			return mgmtNode.getAttributes()
					.getNamedItem(NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE)
					.getNodeValue();
		} catch (NullPointerException Ex) {
			return DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR;
		}
	}

	/**
	 * <p>
	 * Return the Network Device {@link Node} of the given Instance {@link Node}
	 * whose Device Name match the given name.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * @param netDevName
	 *            is the requested network device name.
	 * 
	 * @return The Network Device {@link Node} of the given Instance
	 *         {@link Node}, whose "device" XML Attribute's content is equal to
	 *         the given name.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static Node findNetworkDeviceByName(Node instanceNode,
			String netDevName) throws ResourcesDescriptorException {
		String sAllNetDevSelector = findNetworkDevicesSelector(instanceNode);
		String sNetDevSelector = "." + sAllNetDevSelector + "[@device='"
				+ netDevName + "']";
		try {
			return Doc.evaluateAsNode(sNetDevSelector, instanceNode);
		} catch (XPathExpressionException Ex) {
			Node mgmtNode = findNetworkManagementNode(instanceNode);
			Node attr = mgmtNode.getAttributes().getNamedItem(
					NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new ResourcesDescriptorException(attr, Messages.bind(
					Messages.NetMgmtEx_INVALID_NETWORK_DEVICES_SELECTOR,
					sAllNetDevSelector), Ex);
		}
	}

	/**
	 * <p>
	 * Return the Network Device {@link Node} of the given Instance {@link Node}
	 * whose Device Name match the given name.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * @param mgmtNode
	 *            is a Network Device Management {@link Node}.
	 * @param netDevName
	 *            is the requested network device name.
	 * 
	 * @return The Network Device {@link Node} of the given Instance
	 *         {@link Node}, whose "device" XML Attribute's content is equal to
	 *         the given name.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static Node getNetworkDeviceByName(Node instanceNode, Node mgmtNode,
			String netDevName) throws ResourcesDescriptorException {
		String sAllNetDevSelector = getNetworkDevicesSelector(mgmtNode);
		String sNetDevSelector = "." + sAllNetDevSelector + "[@device='"
				+ netDevName + "']";
		try {
			return Doc.evaluateAsNode(sNetDevSelector, instanceNode);
		} catch (XPathExpressionException Ex) {
			Node attr = mgmtNode.getAttributes().getNamedItem(
					NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new ResourcesDescriptorException(attr, Messages.bind(
					Messages.NetMgmtEx_INVALID_NETWORK_DEVICES_SELECTOR,
					sAllNetDevSelector), Ex);
		}
	}

	/**
	 * <p>
	 * Return the Network Device {@link Node}s of the given Instance
	 * {@link Node}.
	 * </p>
	 * 
	 * @param instanceNode
	 *            is an Instance {@link Node}.
	 * 
	 * @return The Network Device {@link Node}s of the given Instance
	 *         {@link Node}.
	 * 
	 * @throws ResourcesDescriptorException
	 *             if the Network Devices Selector (found in the Network Device
	 *             Management {@link Node}) is not a valid XPath Expression.
	 */
	public static NodeList findNetworkDevices(Node instanceNode)
			throws ResourcesDescriptorException {
		String sAllNetDevSelector = "."
				+ findNetworkDevicesSelector(instanceNode);
		try {
			return Doc.evaluateAsNodeList(sAllNetDevSelector, instanceNode);
		} catch (XPathExpressionException Ex) {
			Node mgmtNode = findNetworkManagementNode(instanceNode);
			Node attr = mgmtNode.getAttributes().getNamedItem(
					NETWORK_DEVICE_NODES_SELECTOR_ATTRIBUTE);
			throw new ResourcesDescriptorException(attr, Messages.bind(
					Messages.NetMgmtEx_INVALID_NETWORK_DEVICES_SELECTOR,
					sAllNetDevSelector), Ex);
		}
	}

}