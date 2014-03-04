package com.wat.melody.plugin.aws.ec2.common;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
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
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceControllerRelatedToAnInstanceElement;
import com.wat.melody.cloud.instance.InstanceControllerWithNetworkActivation;
import com.wat.melody.cloud.instance.InstanceDatas;
import com.wat.melody.cloud.instance.InstanceDatasValidator;
import com.wat.melody.cloud.instance.exception.IllegalInstanceDatasException;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.cloud.instance.xml.NetworkActivatorRelatedToAnInstanceElement;
import com.wat.melody.cloud.network.activation.NetworkActivatorConfigurationCallback;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationHelper;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.common.AwsPlugInConfiguration;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsPlugInEc2Exception;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract public class AbstractOperation implements ITask,
		InstanceDatasValidator, NetworkActivatorConfigurationCallback {

	private static GenericTimeout createTimeout(int timeout) {
		try {
			return GenericTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenricTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * Task's attribute, which specifies the {@link Element} which contains the
	 * instance description.
	 */
	public static final String TARGET_ATTR = "target";

	private String _target = null;
	private Element _targetElmt = null;
	private String _instanceId = null;
	private AmazonEC2 _ec2Connection = null;
	private InstanceController _instanceController = null;
	private InstanceDatas _instanceDatas = null;
	private GenericTimeout _defaultTimeout = createTimeout(90000);

	public AbstractOperation() {
	}

	@Override
	public void validate() throws AwsPlugInEc2Exception {
		// Build an InstanceDatas with target Element's datas found in the RD
		try {
			setInstanceDatas(new InstanceDatasLoader().load(getTargetElement(),
					this));
		} catch (NodeRelatedException Ex) {
			throw new AwsPlugInEc2Exception(Ex);
		}

		setInstanceController(createInstanceController());
	}

	protected InstanceController createInstanceController()
			throws AwsPlugInEc2Exception {
		InstanceController instanceCtrl = newAwsInstanceController();
		instanceCtrl = new InstanceControllerRelatedToAnInstanceElement(
				instanceCtrl, getTargetElement());
		if (NetworkActivationHelper
				.isNetworkActivationEnabled(getTargetElement())) {
			instanceCtrl = new InstanceControllerWithNetworkActivation(
					instanceCtrl,
					new NetworkActivatorRelatedToAnInstanceElement(this,
							getTargetElement()));
		}
		return instanceCtrl;
	}

	/**
	 * Can be override by subclasses to provide enhanced behavior of the
	 * {@link AwsInstanceController}.
	 */
	protected InstanceController newAwsInstanceController() {
		return new AwsInstanceController(getEc2Connection(), getInstanceId());
	}

	protected IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	protected AwsPlugInConfiguration getAwsPlugInConfiguration()
			throws AwsPlugInEc2Exception {
		try {
			return AwsPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new AwsPlugInEc2Exception(Ex);
		}
	}

	protected SshPlugInConfiguration getSshPlugInConfiguration()
			throws AwsPlugInEc2Exception {
		try {
			return SshPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new AwsPlugInEc2Exception(Ex);
		}
	}

	@Override
	public SshPlugInConfiguration getSshConfiguration() {
		try {
			return getSshPlugInConfiguration();
		} catch (AwsPlugInEc2Exception Ex) {
			throw new RuntimeException("Unexpected error when retrieving Ssh "
					+ " Plug-In configuration. "
					+ "Because such configuration registration have been "
					+ "previously prouved, such error cannot happened.");
		}
	}

	@Override
	public void validateAndTransform(InstanceDatas datas)
			throws IllegalInstanceDatasException {
		try {
			validateRegion(datas);
			validateSite(datas);
			validateImageId(datas);
			validateInstanceType(datas);
			validateKeyPairRepositoryPath(datas);
			validateKeyPairName(datas);
			validatePassphrase(datas);
			validateKeyPairSize(datas);
			validateProtectedAreaNames(datas);
			validateCreateTimeout(datas);
			validateDeleteTimeout(datas);
			validateStartTimeout(datas);
			validateStopTimeout(datas);
		} catch (AwsPlugInEc2Exception Ex) {
			throw new IllegalInstanceDatasException(Ex);
		}
	}

	protected void validateRegion(InstanceDatas datas)
			throws IllegalInstanceDatasException, AwsPlugInEc2Exception {
		if (datas.getRegion() == null) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_MISSING_REGION_ATTR,
					InstanceDatasLoader.REGION_ATTR));
		}
		AmazonEC2 connect = getAwsPlugInConfiguration().getAwsEc2Connection(
				datas.getRegion());
		if (connect == null) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_INVALID_REGION_ATTR, datas.getRegion()));
		}
		// Initialize Connection to AWS EC2
		setEc2Connection(connect);
	}

	protected void validateSite(InstanceDatas datas)
			throws IllegalInstanceDatasException {
		if (datas.getSite() == null) {
			// can be null
			return;
		}
		String az = datas.getRegion() + datas.getSite();
		if (!AwsEc2Cloud.availabilityZoneExists(getEc2Connection(), az)) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_INVALID_SITE_ATTR, az));
		}
		datas.setSite(az);
	}

	protected void validateImageId(InstanceDatas datas)
			throws IllegalInstanceDatasException {
		if (datas.getImageId() == null) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_MISSING_IMAGEID_ATTR,
					InstanceDatasLoader.IMAGEID_ATTR));
		}
		if (!AwsEc2Cloud.imageIdExists(getEc2Connection(), datas.getImageId())) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_INVALID_IMAGEID_ATTR, datas.getImageId(),
					datas.getRegion()));
		}
	}

	protected void validateInstanceType(InstanceDatas datas)
			throws IllegalInstanceDatasException {
		if (datas.getInstanceType() == null) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_MISSING_INSTANCETYPE_ATTR,
					InstanceDatasLoader.INSTANCETYPE_ATTR));
		}
	}

	protected void validateKeyPairRepositoryPath(InstanceDatas datas)
			throws AwsPlugInEc2Exception {
		if (datas.getKeyPairRepositoryPath() == null) {
			datas.setKeyPairRepositoryPath(getSshPlugInConfiguration()
					.getKeyPairRepositoryPath());
		}
	}

	protected void validateKeyPairName(InstanceDatas datas)
			throws IllegalInstanceDatasException {
		if (datas.getKeyPairName() == null) {
			throw new IllegalInstanceDatasException(Msg.bind(
					Messages.Ec2Ex_MISSING_KEYPAIR_NAME_ATTR,
					InstanceDatasLoader.KEYPAIR_NAME_ATTR));
		}
	}

	protected void validatePassphrase(InstanceDatas datas) {
		// can be null
	}

	protected void validateKeyPairSize(InstanceDatas datas)
			throws AwsPlugInEc2Exception {
		if (datas.getKeyPairSize() == null) {
			datas.setKeyPairSize(getSshPlugInConfiguration().getKeyPairSize());
		}
	}

	protected void validateProtectedAreaNames(InstanceDatas datas) {
		if (datas.getProtectedAreaIds() == null) {
			// if null, assign an empty list
			datas.setProtectedAreaIds(new ProtectedAreaIds());
		}
	}

	protected void validateCreateTimeout(InstanceDatas datas) {
		if (datas.getCreateTimeout() == null) {
			datas.setCreateTimeout(getDefaultTimeout());
		}
	}

	protected void validateDeleteTimeout(InstanceDatas datas) {
		if (datas.getDeleteTimeout() == null) {
			datas.setDeleteTimeout(getDefaultTimeout().factor(2));
		}
	}

	protected void validateStartTimeout(InstanceDatas datas) {
		if (datas.getStartTimeout() == null) {
			datas.setStartTimeout(getDefaultTimeout());
		}
	}

	protected void validateStopTimeout(InstanceDatas datas) {
		if (datas.getStopTimeout() == null) {
			datas.setStopTimeout(getDefaultTimeout());
		}
	}

	protected InstanceDatas getInstanceDatas() {
		return _instanceDatas;
	}

	protected InstanceDatas setInstanceDatas(InstanceDatas instanceDatas) {
		if (instanceDatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceDatas.class.getCanonicalName() + ".");
		}
		InstanceDatas previous = getInstanceDatas();
		_instanceDatas = instanceDatas;
		return previous;
	}

	protected GenericTimeout getDefaultTimeout() {
		return _defaultTimeout;
	}

	protected GenericTimeout setDefaultTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GenericTimeout.class.getCanonicalName() + ".");
		}
		GenericTimeout previous = getDefaultTimeout();
		_defaultTimeout = timeout;
		return previous;
	}

	protected AmazonEC2 getEc2Connection() {
		return _ec2Connection;
	}

	protected AmazonEC2 setEc2Connection(AmazonEC2 ec2) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		AmazonEC2 previous = getEc2Connection();
		_ec2Connection = ec2;
		return previous;
	}

	protected InstanceController getInstanceController() {
		return _instanceController;
	}

	protected InstanceController setInstanceController(
			InstanceController instanceCtrl) {
		if (instanceCtrl == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstanceController();
		_instanceController = instanceCtrl;
		return previous;
	}

	/**
	 * @return the targeted {@link Element}.
	 */
	protected Element getTargetElement() {
		return _targetElmt;
	}

	protected Element setTargetElement(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ " (the targeted Aws Instance Element Node).");
		}
		Element previous = getTargetElement();
		_targetElmt = n;
		return previous;
	}

	/**
	 * @return the Instance Id which is registered in the targeted Element Node
	 *         (can be <tt>null</tt>).
	 */
	protected String getInstanceId() {
		return _instanceId;
	}

	protected String setInstanceId(String instanceId) {
		// can be null, if no Instance have been created yet
		String previous = getInstanceId();
		_instanceId = instanceId;
		return previous;
	}

	/**
	 * @return the XPath expression which selects the targeted Node.
	 */
	public String getTarget() {
		return _target;
	}

	@Attribute(name = TARGET_ATTR, mandatory = true)
	public String setTarget(String target) throws AwsPlugInEc2Exception {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XPath Expression, which "
					+ "selects a unique XML Element node in the Resources "
					+ "Descriptor).");
		}

		NodeList nl = null;
		try {
			nl = getRD().evaluateAsNodeList(target);
		} catch (XPathExpressionException Ex) {
			throw new AwsPlugInEc2Exception(Msg.bind(
					Messages.Ec2Ex_INVALID_TARGET_ATTR_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new AwsPlugInEc2Exception(Msg.bind(
					Messages.Ec2Ex_INVALID_TARGET_ATTR_NO_NODE_MATCH, target));
		} else if (nl.getLength() > 1) {
			throw new AwsPlugInEc2Exception(Msg.bind(
					Messages.Ec2Ex_INVALID_TARGET_ATTR_MANY_NODES_MATCH,
					target, nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new AwsPlugInEc2Exception(Msg.bind(
					Messages.Ec2Ex_INVALID_TARGET_ATTR_NOT_ELMT_MATCH, target,
					DocHelper.parseNodeType(n)));
		}
		setTargetElement((Element) n);
		try {
			setInstanceId(getTargetElement().getAttributeNode(
					InstanceDatasLoader.INSTANCE_ID_ATTR).getNodeValue());
		} catch (NullPointerException ignored) {
		}
		String previous = getTarget();
		_target = target;
		return previous;
	}

}