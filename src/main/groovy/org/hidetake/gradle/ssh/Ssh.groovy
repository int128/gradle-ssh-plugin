package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch

/**
 * Task for executing commands via SSH session.
 * 
 * @author hidetake.org
 *
 */
class Ssh extends DefaultTask {
	private Remote remote = project.extensions.getByType(SshPluginExtension).remote.clone()
	private Map config = [:]
	private List<Closure> channels = []

	/**
	 * Configure the remote host.
	 * This method overrides remote configuration of project convention.
	 * 
	 * @param configurationClosure
	 */
	void remote(Closure configurationClosure) {
		remote.with(configurationClosure)
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
	 * Add a SSH channel.
	 * 
	 * @param closure
	 */
	void channel(Closure closure) {
		channels += closure
	}

	@TaskAction
	def ssh() {
		def jsch = new JSch()
		jsch.config.putAll(project.extensions.getByType(SshPluginExtension).config)
		jsch.config.putAll(config)
		jsch.addIdentity(remote.identity)
		def session = jsch.getSession(remote.user, remote.host)
		try {
			session.connect()
			waitUntilClosed(channels.collect { closure ->
				ChannelExec channel = session.openChannel('exec')
				channel.command = null
				channel.inputStream = null
				channel.outputStream = System.out
				channel.errStream = System.err
				channel.with(closure)
				channel.connect()
				channel
			})
		} finally {
			session.disconnect()
		}
	}

	private def waitUntilClosed(List<Channel> channels) {
		while (channels.grep { !(it.closed) }.size() > 0) {
			Thread.sleep(100)
		}
		channels.grep { it.exitStatus > 0 }.each {
			logger.error "SSH exec returned status ${it.exitStatus} on channel #${it.id}"
		}.each {
			throw new RuntimeException('SSH exec returned error status')
		}
	}
}
