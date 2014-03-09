package com.wat.melody.common.firewall;

import com.wat.melody.common.network.Address;
import com.wat.melody.common.network.PortRange;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SimpleTcpFireWallRule extends SimpleAbstractTcpUdpFireWallwRule {

	public SimpleTcpFireWallRule(Address fromAddress, PortRange fromPortRange,
			Address toAddress, PortRange toPortRange, Direction direction,
			Access access) {
		super(fromAddress, fromPortRange, toAddress, toPortRange, direction,
				access);
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.TCP;
	}

}