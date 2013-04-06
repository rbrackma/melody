package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.firewall.FireWallRulesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateFireWall extends AbstractOperation {

	/**
	 * The 'UpdateFireWall' XML element
	 */
	public static final String UPDATE_FIREWALL = "UpdateFireWall";

	private FwRulesDecomposed maFwRules;

	public UpdateFireWall() {
		super();
		initFwRules();
	}

	private void initFwRules() {
		maFwRules = null;
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			setFwRules(new FireWallRulesLoader().load(getTargetNode())
					.decompose());
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceFireWallRulesAreUpToDate(getFwRules());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateFireWallEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
	}

	private FwRulesDecomposed getFwRules() {
		return maFwRules;
	}

	private FwRulesDecomposed setFwRules(FwRulesDecomposed fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid FwRules.");
		}
		FwRulesDecomposed previous = getFwRules();
		maFwRules = fwrs;
		return previous;
	}

}