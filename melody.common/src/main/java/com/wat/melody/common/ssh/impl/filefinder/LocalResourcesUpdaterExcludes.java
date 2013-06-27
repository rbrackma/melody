package com.wat.melody.common.ssh.impl.filefinder;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.common.ssh.types.filesfinder.Resource;
import com.wat.melody.common.ssh.types.filesfinder.ResourceSpecification;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesUpdater;
import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * Exclude all {@link Resource}s which have a path (see
 * {@link Resource#getPath()}) matching this object's local path.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LocalResourcesUpdaterExcludes extends ResourceSpecification
		implements ResourcesUpdater {

	private ResourcesSelector _r;

	public LocalResourcesUpdaterExcludes(ResourcesSelector r) {
		super(r);
		_r = r;
	}

	@Override
	public void update(List<? extends Resource> list) {
		List<Resource> matching = new ArrayList<Resource>();
		String path = Paths.get(_r.getLocalBaseDir().getAbsolutePath())
				.normalize() + SysTool.FILE_SEPARATOR + getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (Resource r : list) {
			if (matcher.matches(r.getPath())) {
				matching.add(r);
			}
		}
		list.removeAll(matching);
	}

}