package org.hidetake.gradle.ssh

import com.jcraft.jsch.ChannelSftp;

/**
 * Represents transfer unit.
 * 
 * @author hidetake.org
 *
 */
interface Transfer {
	/**
	 * Perform this transfer.
	 * 
	 * @param channel SFTP channel
	 */
	void perform(ChannelSftp channel)
}
