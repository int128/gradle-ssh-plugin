package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SshPluginTest {
	@Test
	void applyPlugin() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		SshPluginConvention actual = project.convention.getPlugin(SshPluginConvention)
		assertThat(actual.sshSpec.config, instanceOf(Map))
		assertThat(actual.sshSpec.config.isEmpty(), is(true))
	}

	@Test
	void global_config() {
		def project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				config(someOption: true)
				config(path: 'hoge')
			}
		}

		SshPluginConvention actual = project.convention.getPlugin(SshPluginConvention)
		assertThat(actual.sshSpec.config, instanceOf(Map))
		assertThat(actual.sshSpec.config.size(), is(2))
		assertThat(actual.sshSpec.config.someOption, is(true))
		assertThat(actual.sshSpec.config.path, is('hoge'))
	}

	@Test
	void global_remotes_1() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer {
					host = 'web'
					user = 'webuser'
					identity = file('id_rsa')
				}
			}
		}

		assertThat(project.remotes, instanceOf(NamedDomainObjectCollection))
		assertThat(project.remotes.size(), is(1))
		assertThat(project.remotes.webServer, instanceOf(Remote))
		Remote webServer = project.remotes.webServer
		assertThat(webServer.name, is('webServer'))
		assertThat(webServer.host, is('web'))
		assertThat(webServer.user, is('webuser'))
		assertThat(webServer.identity, instanceOf(File))
		assertThat(webServer.identity.name, is('id_rsa'))
	}

	@Test
	void global_remotes_2() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer {
					host = 'web'
					user = 'webuser'
					identity = file('id_rsa')
				}
				appServer {
					host = 'app'
					user = 'appuser'
					identity = file('id_rsa')
				}
			}
		}

		assertThat(project.remotes, instanceOf(NamedDomainObjectCollection))
		assertThat(project.remotes.size(), is(2))
		assertThat(project.remotes.webServer, instanceOf(Remote))
		Remote webServer = project.remotes.webServer
		assertThat(webServer.name, is('webServer'))
		assertThat(webServer.host, is('web'))
		assertThat(webServer.user, is('webuser'))
		assertThat(webServer.identity, instanceOf(File))
		assertThat(webServer.identity.name, is('id_rsa'))
		assertThat(project.remotes.appServer, instanceOf(Remote))
		Remote appServer = project.remotes.appServer
		assertThat(appServer.name, is('appServer'))
		assertThat(appServer.host, is('app'))
		assertThat(appServer.user, is('appuser'))
		assertThat(appServer.identity, instanceOf(File))
		assertThat(appServer.identity.name, is('id_rsa'))
	}
}
