package com.wat.melody.plugin.libvirt.protectedarea;

import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.LibVirtProtectedAreaController;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.cloud.protectedarea.ProtectedAreaController;
import com.wat.melody.cloud.protectedarea.ProtectedAreaControllerRelatedToAProtectedAreaElement;
import com.wat.melody.cloud.protectedarea.ProtectedAreaDatas;
import com.wat.melody.cloud.protectedarea.ProtectedAreaDatasValidator;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaDatasException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.cloud.protectedarea.xml.ProtectedAreaDatasLoader;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.LibVirtPlugInConfiguration;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract public class AbstractProtectedAreaOperation implements ITask,
		ProtectedAreaDatasValidator {

	/**
	 * Task's attribute, which specifies the {@link Element} which contains the
	 * protected area description.
	 */
	public static final String TARGET_ATTR = "target";

	private static String DEFAULT_PROTECTED_AREA_DESCRIPTION = "melody-default-description";

	private String _target = null;
	private Element _targetElmt = null;
	private ProtectedAreaId _protectedAreaId = null;
	private Connect _cloudConnection = null;
	private ProtectedAreaController _protectedAreaController = null;
	private ProtectedAreaDatas _protectedAreaDatas = null;

	public AbstractProtectedAreaOperation() {
	}

	@Override
	public void validate() throws LibVirtException {
		// Build a ProtectedAreaDatas with target Element's datas
		try {
			setProtectedAreaDatas(new ProtectedAreaDatasLoader().load(
					getTargetElement(), this));
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}

		setProtectedAreaController(createProtectedAreaController());
	}

	protected ProtectedAreaController createProtectedAreaController() {
		ProtectedAreaController protectedAreaCtrl = new LibVirtProtectedAreaController(
				getCloudConnection(), getProtectedAreaId());
		protectedAreaCtrl = new ProtectedAreaControllerRelatedToAProtectedAreaElement(
				protectedAreaCtrl, getTargetElement());
		return protectedAreaCtrl;
	}

	protected IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	protected LibVirtPlugInConfiguration getLibVirtPlugInConfiguration()
			throws LibVirtException {
		try {
			return LibVirtPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void validateAndTransform(ProtectedAreaDatas datas)
			throws IllegalProtectedAreaDatasException {
		try {
			validateRegion(datas);
			validateName(datas);
			validateDescription(datas);
		} catch (LibVirtException Ex) {
			throw new IllegalProtectedAreaDatasException(Ex);
		}
	}

	protected void validateRegion(ProtectedAreaDatas datas)
			throws IllegalProtectedAreaDatasException, LibVirtException {
		if (datas.getRegion() == null) {
			throw new IllegalProtectedAreaDatasException(Msg.bind(
					Messages.MachineEx_MISSING_REGION_ATTR,
					ProtectedAreaDatasLoader.REGION_ATTR));
		}
		Connect connect = getLibVirtPlugInConfiguration().getCloudConnection(
				datas.getRegion());
		if (connect == null) {
			throw new IllegalProtectedAreaDatasException(Msg.bind(
					Messages.MachineEx_INVALID_REGION_ATTR, datas.getRegion()));
		}
		// Initialize Connection to LibVirt CLoud
		setCloudConnection(connect);
	}

	protected void validateName(ProtectedAreaDatas datas)
			throws IllegalProtectedAreaDatasException {
		if (datas.getName() == null) {
			throw new IllegalProtectedAreaDatasException(Msg.bind(
					Messages.MachineEx_MISSING_NAME_ATTR,
					ProtectedAreaDatasLoader.NAME_ATTR));
		}
		// Generate a unique Protected Area Name from the given
		// Protected Area Name
		try {
			datas.setName(ProtectedAreaName
					.parseString("melody-protected-area:" + datas.getName()
							+ ":" + UUID.randomUUID()));
		} catch (IllegalProtectedAreaNameException Ex) {
			throw new RuntimeException("Unexecpted error while transforming "
					+ "the User Protected Area name to more readable "
					+ "Protected Area Name. "
					+ "Since the validity of this 0-arg constructor have been "
					+ "previously validated, such error cannot happened. "
					+ "Source code has certainly been modified "
					+ "and a bug have been introduced.", Ex);
		}
	}

	protected void validateDescription(ProtectedAreaDatas datas)
			throws IllegalProtectedAreaDatasException {
		// Cannot create a Protected Area with a null/empty description :
		// so, assign a default description
		if (datas.getDescription() == null) {
			datas.setDescription(DEFAULT_PROTECTED_AREA_DESCRIPTION);
		}
	}

	protected ProtectedAreaDatas getProtectedAreaDatas() {
		return _protectedAreaDatas;
	}

	protected ProtectedAreaDatas setProtectedAreaDatas(
			ProtectedAreaDatas protectedAreaDatas) {
		if (protectedAreaDatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaDatas.class.getCanonicalName() + ".");
		}
		ProtectedAreaDatas previous = getProtectedAreaDatas();
		_protectedAreaDatas = protectedAreaDatas;
		return previous;
	}

	protected Connect getCloudConnection() {
		return _cloudConnection;
	}

	protected Connect setCloudConnection(Connect cnx) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getCloudConnection();
		_cloudConnection = cnx;
		return previous;
	}

	protected ProtectedAreaController getProtectedAreaController() {
		return _protectedAreaController;
	}

	protected ProtectedAreaController setProtectedAreaController(
			ProtectedAreaController protectedAreaController) {
		if (protectedAreaController == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaController.class.getCanonicalName() + ".");
		}
		ProtectedAreaController previous = getProtectedAreaController();
		_protectedAreaController = protectedAreaController;
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
					+ " (the targeted LibVirt Protected Area Element Node).");
		}
		Element previous = getTargetElement();
		_targetElmt = n;
		return previous;
	}

	/**
	 * @return the Protected Area Id which is registered in the targeted Element
	 *         Node (can be <tt>null</tt>).
	 */
	protected ProtectedAreaId getProtectedAreaId() {
		return _protectedAreaId;
	}

	protected ProtectedAreaId setProtectedAreaId(ProtectedAreaId protectedAreaId) {
		// can be null, if no Protected Area have been created yet
		ProtectedAreaId previous = getProtectedAreaId();
		_protectedAreaId = protectedAreaId;
		return previous;
	}

	/**
	 * @return the XPath expression which selects the targeted Node.
	 */
	public String getTarget() {
		return _target;
	}

	@Attribute(name = TARGET_ATTR, mandatory = true)
	public String setTarget(String target) throws LibVirtException {
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
			throw new LibVirtException(Msg.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new LibVirtException(Msg.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH,
					target));
		} else if (nl.getLength() > 1) {
			throw new LibVirtException(Msg.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH,
					target, nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new LibVirtException(Msg.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH,
					target, DocHelper.parseNodeType(n)));
		}
		setTargetElement((Element) n);
		String paId = null;
		try {
			paId = getTargetElement().getAttributeNode(
					ProtectedAreaDatasLoader.ID_ATTR).getNodeValue();
			try {
				setProtectedAreaId(ProtectedAreaId.parseString(paId));
			} catch (IllegalProtectedAreaIdException Ex) {
				throw new LibVirtException(Ex);
			}
		} catch (NullPointerException ignored) {
		}
		String previous = getTarget();
		_target = target;
		return previous;
	}

}