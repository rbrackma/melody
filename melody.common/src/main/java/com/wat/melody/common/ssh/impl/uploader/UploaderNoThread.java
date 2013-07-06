package com.wat.melody.common.ssh.impl.uploader;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.filesfinder.EnhancedFileAttributes;
import com.wat.melody.common.ssh.filesfinder.Resource;
import com.wat.melody.common.ssh.impl.SftpHelper;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class UploaderNoThread {

	private static Logger log = LoggerFactory.getLogger(UploaderNoThread.class);

	private ChannelSftp _channel;
	private Resource _resource;
	private TemplatingHandler _templatingHandler;

	protected UploaderNoThread(ChannelSftp channel, Resource r,
			TemplatingHandler th) {
		setChannel(channel);
		setResource(r);
		setTemplatingHandler(th);
	}

	protected void upload() throws UploaderException {
		Resource r = getResource();
		log.debug(Msg.bind(Messages.UploadMsg_BEGIN, r));
		try {
			// ensure parent directory exists
			Path dest = r.getDestination().normalize();
			if (dest.getNameCount() > 1) {
				SftpHelper.scp_mkdirs(getChannel(), dest.getParent());
				// neither chmod nor chgrp.
			}

			// deal with resource, regarding its type
			if (r.isSymbolicLink()) {
				ln(r);
			} else if (r.isDirectory()) {
				mkdir(r.getDestination());
				chmod(r.getDestination(), r.getDirModifiers());
				chgrp(r.getDestination(), r.getGroup());
			} else if (r.isRegularFile()) {
				template(r);
				chmod(r.getDestination(), r.getFileModifiers());
				chgrp(r.getDestination(), r.getGroup());
			} else {
				log.warn(Msg.bind(Messages.UploadMsg_NOTFOUND, r));
				return;
			}
		} catch (SshSessionException Ex) {
			throw new UploaderException(Ex);
		}
		log.info(Msg.bind(Messages.UploadMsg_END, r));
	}

	protected void ln(Resource r) throws SshSessionException {
		switch (r.getLinkOption()) {
		case KEEP_LINKS:
			ln_keep(r);
			break;
		case COPY_LINKS:
			ln_copy(r);
			break;
		case COPY_UNSAFE_LINKS:
			ln_copy_unsafe(r);
			break;
		}
	}

	protected void ln_copy_unsafe(Resource r) throws SshSessionException {
		if (r.isSafeLink()) {
			ln_keep(r);
		} else {
			ln_copy(r);
		}
	}

	protected void ln_keep(Resource r) throws SshSessionException {
		String unixLink = SftpHelper.convertToUnixPath(r.getDestination());
		String unixTarget = SftpHelper.convertToUnixPath(r
				.getSymbolicLinkTarget());
		if (SftpHelper.scp_ensureLink(getChannel(), unixTarget, unixLink)) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_LINK_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_symlink(getChannel(), unixTarget, unixLink);
	}

	protected void ln_copy(Resource r) throws SshSessionException {
		if (!r.exists()) {
			String unixPath = SftpHelper.convertToUnixPath(r.getDestination());
			SftpATTRS attrs = SftpHelper.scp_lstat(getChannel(), unixPath);
			if (attrs != null) {
				if (attrs.isDir()) {
					SftpHelper.scp_rmdirs(getChannel(), unixPath);
				} else {
					SftpHelper.scp_rm(getChannel(), unixPath);
				}
			}
			log.warn(Messages
					.bind(Messages.UploadMsg_COPY_UNSAFE_IMPOSSIBLE, r));
		} else if (r.isRegularFile()) {
			template(r);
			chmod(r.getDestination(), r.getFileModifiers());
			chgrp(r.getDestination(), r.getGroup());
		} else {
			mkdir(r.getDestination());
			chmod(r.getDestination(), r.getDirModifiers());
			chgrp(r.getDestination(), r.getGroup());
		}
	}

	protected void template(Resource r) throws SshSessionException {
		if (r.getTemplate() == true) {
			if (getTemplatingHandler() == null) {
				throw new SshSessionException(
						Messages.UploadEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(r.getPath());
			} catch (TemplatingException Ex) {
				throw new SshSessionException(Ex);
			}
			put(template, r.getAttributes(), r.getDestination(),
					r.getTransferBehavior());
		} else {
			put(r.getPath(), r.getAttributes(), r.getDestination(),
					r.getTransferBehavior());
		}
	}

	protected void mkdir(Path dir) throws SshSessionException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a Directory Path, relative or absolute).");
		}
		String unixDir = SftpHelper.convertToUnixPath(dir);
		if (SftpHelper.scp_ensureDir(getChannel(), unixDir)) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_DIR_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_mkdir(getChannel(), unixDir);
	}

	protected void put(Path source, EnhancedFileAttributes localFileAttrs,
			Path dest, TransferBehavior tb) throws SshSessionException {
		if (source == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Path.class.getCanonicalName()
					+ " (the source file Path).");
		}
		if (dest == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Path.class.getCanonicalName()
					+ " (the destination file Path).");
		}
		String unixFile = SftpHelper.convertToUnixPath(dest);
		if (SftpHelper.scp_ensureFile(localFileAttrs, getChannel(), unixFile,
				tb)) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_FILE_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_put(getChannel(), source.toString(), unixFile);
	}

	protected void chmod(Path path, Modifiers modifiers)
			throws SshSessionException {
		if (modifiers == null) {
			return;
		}
		if (path == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory or file Path, relative or absolute).");
		}
		String unixPath = SftpHelper.convertToUnixPath(path);
		SftpHelper.scp_chmod(getChannel(), modifiers, unixPath);
	}

	protected void chgrp(Path path, GroupID group) throws SshSessionException {
		if (group == null) {
			return;
		}
		if (path == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory or file Path, relative or absolute).");
		}
		String unixPath = SftpHelper.convertToUnixPath(path);
		SftpHelper.scp_chgrp(getChannel(), group, unixPath);
	}

	protected ChannelSftp getChannel() {
		return _channel;
	}

	private ChannelSftp setChannel(ChannelSftp channel) {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		ChannelSftp previous = getChannel();
		_channel = channel;
		return previous;
	}

	protected Resource getResource() {
		return _resource;
	}

	private Resource setResource(Resource lr) {
		if (lr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Resource.class.getCanonicalName()
					+ ".");
		}
		Resource previous = getResource();
		_resource = lr;
		return previous;
	}

	private TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	private TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

}