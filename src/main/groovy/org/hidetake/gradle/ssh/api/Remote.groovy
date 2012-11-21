package org.hidetake.gradle.ssh.api

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
	 * Port.
	 */
	int port = 22

	/**
	 * Remote host.
	 */
	String host

	/**
	 * Password.
	 * Leave as null if public key authentication.
	 */
	String password

	/**
	 * Identity key file.
	 * Leave as null if password authentication.
	 */
	File identity

	Remote(String name) {
		this.name = name
	}
}
