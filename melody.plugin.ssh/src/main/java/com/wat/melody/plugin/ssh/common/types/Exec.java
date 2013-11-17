package com.wat.melody.plugin.ssh.common.types;

import java.io.File;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.WrapperFile;
import com.wat.melody.common.files.exception.IllegalFileException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Exec {

	/**
	 * The 'exec' XML element used in the Sequence Descriptor
	 */
	public static final String EXEC = "exec";

	/**
	 * Exec's attribute, which specifies the path of a script.
	 */
	public static final String FILE_ATTR = "file";

	/**
	 * Exec's attribute, which indicates if the script contains Melody
	 * Expression to be resolved.
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * Exec's attribute, which specifies an in-line command.
	 */
	public static final String COMMAND_ATTR = "command";

	private String _command;
	private File _file;
	private boolean _template;

	public String getCommand() {
		return _command;
	}

	@Attribute(name = COMMAND_ATTR)
	public String setCommand(String c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a shell command).");
		}
		String previous = getCommand();
		_command = c;
		return previous;
	}

	public File getFile() {
		return _file;
	}

	@Attribute(name = FILE_ATTR)
	public File setFile(WrapperFile f) throws IllegalFileException {
		if (f == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + File.class.getCanonicalName()
					+ " (which list shell commands).");
		}
		FS.validateFileExists(f.toString());
		File previous = getFile();
		_file = f;
		return previous;
	}

	public boolean getTemplate() {
		return _template;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean c) {
		boolean previous = getTemplate();
		_template = c;
		return previous;
	}

}