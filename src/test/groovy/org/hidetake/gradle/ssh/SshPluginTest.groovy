package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SshPluginTest {
	@Test
	void applyPlugin() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		assertThat(project.ssh, instanceOf(SshPluginExtension))
		assertThat(project.ssh.remote, instanceOf(Remote))
		assertThat(project.ssh.config, instanceOf(Map))
		assertThat(project.ssh.config.isEmpty(), is(true))
	}

	@Test
	void remote_once() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				remote {
					host = 'hoge'
					user = 'fuga'
					identity = 'id_rsa'
				}
			}
		}
		assertThat(project.ssh.remote, instanceOf(Remote))
		assertThat(project.ssh.remote.host, is('hoge'))
		assertThat(project.ssh.remote.user, is('fuga'))
		assertThat(project.ssh.remote.identity, is('id_rsa'))
	}
	
	@Test
	void remote_override() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				remote {
					host = 'hoge'
					user = 'fuga'
					identity = 'id_rsa'
				}
				remote {
					host = 'localhost'
				}
			}
		}
		assertThat(project.ssh.remote, instanceOf(Remote))
		assertThat(project.ssh.remote.host, is('localhost'))
		assertThat(project.ssh.remote.user, is('fuga'))
		assertThat(project.ssh.remote.identity, is('id_rsa'))
	}

	@Test
	void config_once() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				config(hoge: true)
			}
		}
		assertThat(project.ssh.config, instanceOf(Map))
		assertThat(project.ssh.config.hoge, is(true))
	}

	@Test
	void config_override() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				config(hoge: true)
				config(someOption: 0)
			}
		}
		assertThat(project.ssh.config, instanceOf(Map))
		assertThat(project.ssh.config.hoge, is(true))
		assertThat(project.ssh.config.someOption, is(0))
	}
}
