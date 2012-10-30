package org.hidetake.gradle.ssh

/**
 * Represents a remote connection.
 * 
 * @author hidetake.org
 *
 */
class Remote implements Cloneable {
	/**
	 * Remote user.
	 */
	String user

	/**
	 * Remote host.
	 */
	String host

	/**
	 * File path of the identity key file.
	 */
	String identity

	@Override
	Remote clone() {
		new Remote(user: user, host: host, identity: identity)
	}
}
