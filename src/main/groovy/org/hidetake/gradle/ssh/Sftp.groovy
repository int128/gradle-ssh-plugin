package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch

/**
 * Task to transfer files from or into remote host via SFTP.
 * 
 * @author hidetake.org
 *
 */
class Sftp extends DefaultTask {
	Remote remote = project.extensions.getByType(SshPluginExtension).remote.clone()
	Map config = [:]
	List<Transfer> transfers = []

	/**
	 * Configure the remote host.
	 * This method overrides remote configuration of project convention.
	 * 
	 * @param remoteConfiguration
	 */
	void remote(Closure remoteConfiguration) {
		remote.with(remoteConfiguration)
	}

	/**
	 * Add configuration for JSch. For example:
	 * <pre>
	 * config(StrictHostKeyChecking: 'no')
	 * </pre>
	 * 
	 * @param pairs key value pairs of configuration
	 */
	void config(Map pairs) {
		config.putAll(pairs)
	}

	/**
	 * Add a transfer unit.
	 * 
	 * @param transfer transfer such as GET or PUT
	 */
	void transfer(Transfer transfer) {
		transfers += transfer
	}

	@TaskAction
	def put() {
		def jsch = new JSch()
		jsch.config.putAll(project.extensions.getByType(SshPluginExtension).config)
		jsch.config.putAll(config)
		jsch.addIdentity(remote.identity)
		def session = jsch.getSession(remote.user, remote.host)
		try {
			session.connect()
			ChannelSftp channel = session.openChannel('sftp')
			try {
				channel.connect()
				transfers.each { it.perform(channel) }
			} finally {
				channel.disconnect()
			}
		} finally {
			session.disconnect()
		}
	}
}
