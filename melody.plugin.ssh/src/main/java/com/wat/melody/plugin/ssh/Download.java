package com.wat.melody.plugin.ssh;

import java.io.File;

import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.RemoteResourcesSpecification;
import com.wat.melody.common.ssh.filesfinder.ResourcesSpecification;
import com.wat.melody.plugin.ssh.common.Transfer;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Download extends Transfer {

	/**
	 * Task's name
	 */
	public static final String DOWNLOAD = "download";

	public Download() {
		super();
	}

	public void doTransfer(ISshSession session) throws SshSessionException,
			InterruptedException {
		session.download(getResourcesSpecifications(), getMaxPar(), this);
	}

	public ResourcesSpecification newResourcesSpecification(File basedir) {
		return new RemoteResourcesSpecification(basedir);
	}

}