package org.hidetake.gradle.ssh

/**
 * Represents a remote host.
 * 
 * @author hidetake.org
 *
 */
class Remote {
	/**
	 * Name of this instance.
	 */
	final String name

	/**
	 * Remote user.
	 */
	String user

	/**
	 * Remote host.
	 */
	String host

	/**
	 * Identity key file.
	 */
	File identity

	Remote(String name) {
		this.name = name
	}
}
