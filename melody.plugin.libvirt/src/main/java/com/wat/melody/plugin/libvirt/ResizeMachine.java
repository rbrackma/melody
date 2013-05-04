package com.wat.melody.plugin.libvirt;

import java.util.Arrays;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Common;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResizeMachine extends AbstractOperation {

	// private static Log log = LogFactory.getLog(ResizeMachine.class);

	/**
	 * The 'ResizeMachine' XML element
	 */
	public static final String RESIZE_MACHINE = "ResizeMachine";

	/**
	 * The 'instanceType' XML attribute
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	private InstanceType msInstanceType;

	public ResizeMachine() {
		super();
		initInstanceType();
	}

	private void initInstanceType() {
		msInstanceType = null;
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		try {
			String v = null;
			v = XPathHelper.getHeritedAttributeValue(getTargetNode(),
					Common.INSTANCETYPE_ATTR);
			try {
				try {
					if (v != null) {
						setInstanceType(InstanceType.parseString(v));
					}
				} catch (IllegalInstanceTypeException Ex) {
					throw new LibVirtException(Messages.bind(
							Messages.ResizeEx_INVALID_INSTANCETYPE_ATTR, v));
				}
			} catch (LibVirtException Ex) {
				throw new LibVirtException(Messages.bind(
						Messages.ResizeEx_INSTANCETYPE_ERROR,
						Common.INSTANCETYPE_ATTR, getTargetNodeLocation()), Ex);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}

		// Initialize optional task's attributes with their default value
		if (getInstanceType() == null) {
			throw new LibVirtException(Messages.bind(
					Messages.ResizeEx_MISSING_INSTANCETYPE_ATTR,
					ResizeMachine.INSTANCETYPE_ATTR,
					ResizeMachine.RESIZE_MACHINE, Common.INSTANCETYPE_ATTR,
					getTargetNodeLocation()));
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceSizing(getInstanceType());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.ResizeEx_GENERIC_FAIL, getTargetNodeLocation(),
					getInstanceType()), Ex);
		}
	}

	public InstanceType getInstanceType() {
		return msInstanceType;
	}

	@Attribute(name = INSTANCETYPE_ATTR)
	public InstanceType setInstanceType(InstanceType instanceType)
			throws LibVirtException {
		if (instanceType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid InstanceType (Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		InstanceType previous = getInstanceType();
		msInstanceType = instanceType;
		return previous;
	}

}