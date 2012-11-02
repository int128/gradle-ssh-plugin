package org.hidetake.gradle.ssh

import com.jcraft.jsch.ChannelSftp

/**
 * Represents a PUT transfer.
 * 
 * @author hidetake.org
 *
 */
class PutTransfer implements Transfer {
	/**
	 * Path to file or directory.
	 */
	String from

	/**
	 * Path to file or directory.
	 */
	String to

	@Override
	void perform(ChannelSftp channel) {
		channel.put(from, to)
	}
}
