package org.hidetake.gradle.ssh

import com.jcraft.jsch.ChannelSftp

/**
 * Represents a GET transfer.
 * 
 * @author hidetake.org
 *
 */
class GetTransfer implements Transfer {
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
		channel.get(from, to)
	}
}
