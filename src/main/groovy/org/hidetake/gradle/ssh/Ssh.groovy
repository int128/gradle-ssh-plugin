package org.hidetake.gradle.ssh

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException;
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
	Remote remote = project.extensions.getByType(SshPluginExtension).remote.clone()
	Map config = [:]
	List<Closure> channels = []

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
	 * Add a SSH channel.
	 * 
	 * @param channelConfiguration configuration closure for {@link ChannelExec}
	 */
	void channel(Closure channelConfiguration) {
		channels += channelConfiguration
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
			def runningChannels = channels.collect { channelConfiguration ->
				ChannelExec channel = session.openChannel('exec')
				channel.command = null
				channel.inputStream = null
				channel.setOutputStream(System.out, true)
				channel.setErrStream(System.err, true)
				channel.with(channelConfiguration)
				channel.connect()
				channel
			}
			waitForChannels(runningChannels)
		} finally {
			session.disconnect()
		}
	}

	private def waitForChannels(List<Channel> channels) {
		while (channels.find { !(it.closed) }) {
			Thread.sleep(500L)
		}
		channels.findAll { it.exitStatus != 0 }.each {
			logger.error "SSH exec returned status ${it.exitStatus} on channel #${it.id}"
		}.each {
			throw new GradleException('SSH exec returned error status')
		}
	}
}
