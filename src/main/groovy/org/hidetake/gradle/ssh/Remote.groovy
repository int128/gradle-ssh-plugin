package org.hidetake.gradle.ssh

/**
 * Represents a remote host.
 * 
 * @author hidetake.org
 *
 */
class Remote implements Cloneable {
	def String user
	def String host
	def String identity

	@Override
	Remote clone() {
		new Remote(user: user, host: host, identity: identity)
	}
}
