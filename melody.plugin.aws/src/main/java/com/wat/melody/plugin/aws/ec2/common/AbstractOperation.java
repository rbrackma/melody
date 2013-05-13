package com.wat.melody.plugin.aws.ec2.common;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.AmazonEC2;
import com.wat.cloud.aws.ec2.AwsEc2Cloud;
import com.wat.cloud.aws.ec2.AwsInstanceController;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceControllerWithNetworkManagement;
import com.wat.melody.cloud.instance.InstanceControllerWithRelatedNode;
import com.wat.melody.cloud.instance.InstanceDatasLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.cloud.network.NetworkManagerFactoryConfigurationCallback;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract public class AbstractOperation implements ITask,
		NetworkManagerFactoryConfigurationCallback {

	/**
	 * The 'region' XML attribute
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * The 'target' XML attribute
	 */
	public static final String TARGET_ATTR = "target";

	/**
	 * The 'timeout' XML attribute
	 */
	public static final String TIMEOUT_ATTR = "timeout";

	private AmazonEC2 _ec2 = null;
	private InstanceController _instance = null;
	private String _instanceId = null;
	private Node _targetNode = null;
	private String _region = null;
	private String _target = null;
	private long _timeout = 90000;

	public AbstractOperation() {
	}

	@Override
	public void validate() throws AwsException {
		// Initialize task parameters with their default value
		String v = null;
		try {
			v = XPathHelper.getHeritedAttributeValue(getTargetNode(),
					AwsEc2Cloud.REGION_ATTR);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
		try {
			if (v != null) {
				setRegion(v);
			}
		} catch (AwsException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_REGION_ERROR, AwsEc2Cloud.REGION_ATTR,
					getTargetNodeLocation()), Ex);
		}

		// Is everything correctly loaded ?
		if (getRegion() == null) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_MISSING_REGION_ATTR, new Object[] {
							REGION_ATTR,
							getClass().getSimpleName().toLowerCase(),
							AwsEc2Cloud.REGION_ATTR, getTargetNodeLocation() }));
		}

		// Initialize AmazonEC2 for the current region
		setEc2(getAwsPlugInConfiguration().getAmazonEC2(getRegion()));

		setInstance(createInstance());
	}

	public InstanceController createInstance() throws AwsException {
		InstanceController instance = newAwsInstanceController();
		instance = new InstanceControllerWithRelatedNode(instance, getRD(),
				getTargetNode());
		if (NetworkManagementHelper.isManagementNetworkEnable(getTargetNode())) {
			instance = new InstanceControllerWithNetworkManagement(instance,
					this, getTargetNode());
		}
		return instance;
	}

	/**
	 * Can be override by subclasses to provide enhanced behavior of the
	 * {@link AwsInstanceController}.
	 */
	public InstanceController newAwsInstanceController() {
		return new AwsInstanceController(getEc2(), getInstanceId());
	}

	public IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	public String getTargetNodeLocation() {
		return Doc.getNodeLocation(getTargetNode()).toFullString();
	}

	public AwsPlugInConfiguration getAwsPlugInConfiguration()
			throws AwsException {
		try {
			return AwsPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new AwsException(Ex);
		}
	}

	public SshPlugInConfiguration getSshPlugInConfiguration()
			throws AwsException {
		try {
			return SshPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public SshPlugInConfiguration getSshConfiguration() {
		try {
			return getSshPlugInConfiguration();
		} catch (AwsException Ex) {
			throw new RuntimeException("Unexpected error when retrieving Ssh "
					+ " Plug-In configuration. "
					+ "Because such configuration registration have been "
					+ "previously prouved, such error cannot happened.");
		}
	}

	protected AmazonEC2 getEc2() {
		return _ec2;
	}

	private AmazonEC2 setEc2(AmazonEC2 ec2) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		AmazonEC2 previous = getEc2();
		_ec2 = ec2;
		return previous;
	}

	public InstanceController getInstance() {
		return _instance;
	}

	public InstanceController setInstance(InstanceController instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstance();
		_instance = instance;
		return previous;
	}

	/**
	 * @return the targeted {@link Node}.
	 */
	public Node getTargetNode() {
		return _targetNode;
	}

	public Node setTargetNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node (the targeted AWS Instance Node).");
		}
		Node previous = getTargetNode();
		_targetNode = n;
		return previous;
	}

	/**
	 * @return the Aws Instance Id which is registered in the targeted Node (can
	 *         be <code>null</code>).
	 */
	protected String getInstanceId() {
		return _instanceId;
	}

	protected String setInstanceId(String instanceID) {
		// can be null, if no AWS instance have been created yet
		String previous = getInstanceId();
		_instanceId = instanceID;
		return previous;
	}

	public String getRegion() {
		return _region;
	}

	@Attribute(name = REGION_ATTR)
	public String setRegion(String region) throws AwsException {
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS Region Name).");
		}
		if (getAwsPlugInConfiguration().getAmazonEC2(region) == null) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_REGION_ATTR, region));
		}
		String previous = getRegion();
		this._region = region;
		return previous;
	}

	/**
	 * @return the XPath expression which selects the targeted Node.
	 */
	public String getTarget() {
		return _target;
	}

	@Attribute(name = TARGET_ATTR, mandatory = true)
	public String setTarget(String target) throws AwsException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XPath Expression, which "
					+ "selects a sole XML Element node in the Resources "
					+ "Descriptor.");
		}

		NodeList nl = null;
		try {
			nl = getRD().evaluateAsNodeList(target);
		} catch (XPathExpressionException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH,
					target));
		} else if (nl.getLength() > 1) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH,
					target, nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH,
					target, Doc.parseNodeType(n.getNodeType())));
		}
		setTargetNode(n);
		try {
			setInstanceId(n.getAttributes()
					.getNamedItem(InstanceDatasLoader.INSTANCE_ID_ATTR)
					.getNodeValue());
		} catch (NullPointerException ignored) {
		}
		String previous = getTarget();
		_target = target;
		return previous;
	}

	public long getTimeout() {
		return _timeout;
	}

	@Attribute(name = TIMEOUT_ATTR)
	public long setTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getTimeout();
		_timeout = timeout;
		return previous;
	}

}