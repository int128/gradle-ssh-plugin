package org.hidetake.gradle.ssh


import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SshTest {
	@Test
	public void configuration_minimum() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Ssh, 'testTask') {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				channel { command = 'ls' }
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.channels.size(), is(1))
		assertThat(project.tasks.testTask.channels[0], instanceOf(Closure))
		def channel = [command: null]
		channel.with(project.tasks.testTask.channels[0])
		assertThat(channel.command, is('ls'))
	}

	@Test
	public void configuration_remote_convention() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
			}
			task(type: Ssh, 'testTask') {
				channel { command = 'ls' }
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.channels.size(), is(1))
		assertThat(project.tasks.testTask.channels[0], instanceOf(Closure))
		def channel = [command: null]
		channel.with(project.tasks.testTask.channels[0])
		assertThat(channel.command, is('ls'))
	}

	@Test
	public void configuration_remote_override() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
			}
			task(type: Ssh, 'testTask') {
				remote {
					host = 'somehost'
					user = 'fuga'
				}
				channel { command = 'ls' }
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		assertThat(project.tasks.testTask.remote.host, is('somehost'))
		assertThat(project.tasks.testTask.remote.user, is('fuga'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.channels.size(), is(1))
		assertThat(project.tasks.testTask.channels[0], instanceOf(Closure))
		def channel = [command: null]
		channel.with(project.tasks.testTask.channels[0])
		assertThat(channel.command, is('ls'))
	}

	@Test
	public void configuration_jschConfig() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Ssh, 'testTask') {
				config(StrictHostKeyChecking: 'no')
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				channel { command = 'ls' }
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.size(), is(1))
		assertThat(project.tasks.testTask.config.StrictHostKeyChecking, is('no'))
		assertThat(project.tasks.testTask.channels.size(), is(1))
		assertThat(project.tasks.testTask.channels[0], instanceOf(Closure))
		def channel = [command: null]
		channel.with(project.tasks.testTask.channels[0])
		assertThat(channel.command, is('ls'))
	}

	@Test
	public void configuration_multiChannels() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Ssh, 'testTask') {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				channel { command = 'ls' }
				channel { command = 'uname' }
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.channels.size(), is(2))
		assertThat(project.tasks.testTask.channels[0], instanceOf(Closure))
		def channel = [command: null]
		channel.with(project.tasks.testTask.channels[0])
		assertThat(channel.command, is('ls'))
	}
}
