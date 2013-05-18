package com.wat.cloud.libvirt;

import java.util.Arrays;

import org.libvirt.DomainInfo;

import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.Messages;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class InstanceStateConverter {

	/**
	 * <p>
	 * Convert the given {@link DomainInfo.DomainState} to an
	 * {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given {@link DomainInfo.DomainState} to convert.
	 * 
	 * @return an {@link InstanceState} object, whose equal to the given input
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input {@link DomainInfo.DomainState} is not a
	 *             valid {@link InstanceState} Enumeration Constant.
	 */
	public static InstanceState parse(DomainInfo.DomainState iState)
			throws IllegalInstanceStateException {
		if (iState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DomainInfo.DomainState.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(DomainInfo.DomainState.values()) + ").");
		}
		switch (iState) {
		case VIR_DOMAIN_RUNNING:
			return InstanceState.RUNNING;
		case VIR_DOMAIN_SHUTDOWN:
			return InstanceState.STOPPING;
		case VIR_DOMAIN_SHUTOFF:
			return InstanceState.STOPPED;
		default:
		}
		/*
		 * TODO : créer un state PENDING, qui correspond au moment ou
		 * libVitCloud provisionne le Domain
		 */
		/*
		 * TODO : créer un state SHUTTING_DOWN, qui correspond au moment ou
		 * libVitCloud dé-provisionne le Domain
		 */
		throw new IllegalInstanceStateException(Messages.bind(
				Messages.InstanceStateEx_INVALID, iState,
				Arrays.asList(DomainInfo.DomainState.values())));
	}

}