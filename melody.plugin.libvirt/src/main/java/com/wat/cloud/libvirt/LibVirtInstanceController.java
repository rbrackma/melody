package com.wat.cloud.libvirt;

import org.libvirt.Connect;
import org.libvirt.Domain;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.DefaultInstanceController;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.keypair.KeyPairName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtInstanceController extends DefaultInstanceController {

	private Connect _cnx;
	private Domain _domain;

	public LibVirtInstanceController(Connect connection, String instanceId) {
		setConnection(connection);
		setInstanceId(instanceId);
	}

	@Override
	public boolean instanceExists() {
		return LibVirtCloud.instanceExists(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceLives() {
		return LibVirtCloud.instanceLives(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceRuns() {
		return LibVirtCloud.instanceRuns(getConnection(), getInstanceId());
	}

	@Override
	public InstanceState getInstanceState() {
		return LibVirtCloud.getInstanceState(getConnection(), getInstanceId());
	}

	@Override
	public InstanceType getInstanceType() {
		return LibVirtCloud.getDomainType(getInstance());
	}

	@Override
	public boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException {
		return LibVirtCloud.waitUntilInstanceStatusBecomes(getConnection(),
				getInstanceId(), state, timeout, sleepfirst);
	}

	@Override
	public String createInstance(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException {
		return LibVirtCloud.newInstance(getConnection(), type, imageId,
				keyPairName);
	}

	@Override
	public void destroyInstance(long destroyTimeout) throws OperationException,
			InterruptedException {
		LibVirtCloud.deleteInstance(this.getInstance());
	}

	@Override
	public void startInstance(long startTimeout) throws OperationException,
			InterruptedException {
		if (!LibVirtCloud.startInstance(this.getInstance(), startTimeout)) {
			throw new OperationException(Messages.bind(
					Messages.StartEx_TIMEOUT, getInstanceId(), startTimeout));
		}
	}

	@Override
	public void stopInstance(long stopTimeout) throws OperationException,
			InterruptedException {
		if (!LibVirtCloud.stopInstance(this.getInstance(), stopTimeout)) {
			throw new OperationException(Messages.bind(Messages.StopEx_TIMEOUT,
					getInstanceId(), stopTimeout));
		}
	}

	@Override
	public void resizeInstance(InstanceType targetType)
			throws OperationException, InterruptedException {
		if (!LibVirtCloud.resizeInstance(this.getInstance(), targetType)) {
			throw new OperationException(Messages.bind(
					Messages.ResizeEx_FAILED, getInstanceId(),
					getInstanceType(), targetType));
		}
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return LibVirtCloud.getDiskDevices(this.getInstance());
	}

	@Override
	public void detachAndDeleteInstanceDiskDevices(
			DiskDeviceList disksToRemove, long detachTimeout)
			throws OperationException, InterruptedException {
		LibVirtCloud.detachAndDeleteDiskDevices(this.getInstance(),
				disksToRemove);
	}

	@Override
	public void createAndAttachDiskInstanceDevices(DiskDeviceList disksToAdd,
			long createTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		LibVirtCloud.createAndAttachDiskDevices(this.getInstance(), disksToAdd);
	}

	@Override
	public void updateInstanceDiskDevicesDeleteOnTerminationFlag(
			DiskDeviceList diskList) {
		/*
		 * Not supported by LibVirt. Disk are always deleted on instance
		 * termination.
		 */
	}

	@Override
	public NetworkDeviceNameList getInstanceNetworkDevices() {
		return LibVirtCloud.getNetworkDevices(this.getInstance());
	}

	@Override
	public void detachInstanceNetworkDevices(
			NetworkDeviceNameList netDevivesToRemove, long detachTimeout)
			throws OperationException, InterruptedException {
		for (NetworkDeviceName netDev : netDevivesToRemove) {
			LibVirtCloud.detachNetworkDevice(this.getInstance(), netDev);
		}
	}

	@Override
	public void attachInstanceNetworkDevices(
			NetworkDeviceNameList netDevivesToAdd, long attachTimeout)
			throws OperationException, InterruptedException {
		for (NetworkDeviceName netDev : netDevivesToAdd) {
			LibVirtCloud.attachNetworkDevice(this.getInstance(), netDev);
		}
	}

	@Override
	public NetworkDeviceDatas getInstanceNetworkDeviceDatas(
			NetworkDeviceName netdev) {
		return LibVirtCloud.getNetworkDeviceDatas(this.getInstance(), netdev);
	}

	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netDev) {
		return LibVirtCloudFireWall
				.getFireWallRules(this.getInstance(), netDev);
	}

	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toRevoke) throws OperationException {
		LibVirtCloudFireWall.revokeFireWallRules(this.getInstance(), netDev,
				toRevoke);
	}

	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toAutorize) throws OperationException {
		LibVirtCloudFireWall.authorizeFireWallRules(this.getInstance(), netDev,
				toAutorize);
	}

	private void refreshInternalDatas() {
		setInstance(LibVirtCloud.getDomain(getConnection(), getInstanceId()));
	}

	public Connect getConnection() {
		return _cnx;
	}

	private Connect setConnection(Connect connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getConnection();
		_cnx = connection;
		return previous;
	}

	private Domain getInstance() {
		return _domain;
	}

	/**
	 * Can be <tt>null</tt>, when the current instance has no underlying LibVirt
	 * Domain.
	 * 
	 * @param instance
	 * @return
	 */
	private Domain setInstance(Domain instance) {
		Domain previous = getInstance();
		_domain = instance;
		return previous;
	}

	@Override
	public String setInstanceId(String instanceId) {
		String previous = super.setInstanceId(instanceId);
		refreshInternalDatas();
		return previous;
	}

}