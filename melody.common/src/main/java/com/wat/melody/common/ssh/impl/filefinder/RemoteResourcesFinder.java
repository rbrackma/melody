package com.wat.melody.common.ssh.impl.filefinder;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.impl.SftpHelper;
import com.wat.melody.common.ssh.impl.downloader.DownloaderException;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesUpdater;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class RemoteResourcesFinder {

	public static List<RemoteResource> findResources(ChannelSftp chan,
			ResourcesSelector rs) throws DownloaderException {
		if (rs == null) {
			return new ArrayList<RemoteResource>();
		}
		if (chan == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		if (!chan.isConnected()) {
			throw new IllegalStateException("Channel must be a connected.");
		}

		List<RemoteResource> rrs = findMatchingResources(chan, rs);
		for (ResourcesUpdater ru : rs.getResourcesUpdaters()) {
			ru.update(rrs);
		}
		return rrs;
	}

	private static List<RemoteResource> findMatchingResources(ChannelSftp chan,
			ResourcesSelector rs) throws DownloaderException {
		if (rs == null) {
			return new ArrayList<RemoteResource>();
		}
		// Recurs lists remote files from the remote basedir
		String dir = SftpHelper.convertToUnixPath(rs.getRemoteBaseDir());
		List<RemoteResource> rrs;
		try {
			rrs = SftpHelper.listrecurs(chan, dir, rs);
		} catch (SshSessionException Ex) {
			throw new DownloaderException(Ex);
		}
		return findMatchingRemoteResources(rrs, rs);
	}

	private static List<RemoteResource> findMatchingRemoteResources(
			List<RemoteResource> rr, ResourcesSelector rs) {
		List<RemoteResource> rrs = new ArrayList<RemoteResource>();
		String path = Paths.get(rs.getRemoteBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + rs.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (RemoteResource r : rr) {
			if (matcher.matches(r.getPath())) {
				// include path parent til topdir is reacher
				// TODO : include path parent til topdir is reacher
				rrs.add(r);
			}
		}
		return rrs;
	}

}