package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.firewall.FireWallRulesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateFireWall extends AbstractOperation implements ITask {

	/**
	 * The 'UpdateFireWall' XML element
	 */
	public static final String UPDATE_FIREWALL = "UpdateFireWall";

	private FireWallRulesPerDevice _rulesPerDevice;

	public UpdateFireWall() {
		initFwRules();
	}

	private void initFwRules() {
		_rulesPerDevice = null;
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			setFwRules(new FireWallRulesLoader().load(getTargetNode()));
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceFireWallRulesAreUpToDate(getFwRules());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateFireWallEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
	}

	private FireWallRulesPerDevice getFwRules() {
		return _rulesPerDevice;
	}

	private FireWallRulesPerDevice setFwRules(FireWallRulesPerDevice fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ FireWallRulesPerDevice.class.getCanonicalName() + ".");
		}
		FireWallRulesPerDevice previous = getFwRules();
		_rulesPerDevice = fwrs;
		return previous;
	}

}
