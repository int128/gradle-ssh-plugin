package org.hidetake.gradle.ssh.api

/**
 * Represents a group of {@link Remote}s.
 * 
 * @author hidetake.org
 *
 */
class RemoteGroup extends ArrayList<Remote> {
	/**
	 * Name of this instance.
	 */
	final String name

	RemoteGroup(String name) {
		super()
		this.name = name
	}
}
