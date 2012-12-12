package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.Remote
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
		assertThat(webServer.port, is(22))
		assertThat(webServer.user, is('webuser'))
		assertThat(webServer.password, is(nullValue()))
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
		assertThat(webServer.port, is(22))
		assertThat(webServer.user, is('webuser'))
		assertThat(webServer.password, is(nullValue()))
		assertThat(webServer.identity, instanceOf(File))
		assertThat(webServer.identity.name, is('id_rsa'))
		assertThat(project.remotes.appServer, instanceOf(Remote))
		Remote appServer = project.remotes.appServer
		assertThat(appServer.name, is('appServer'))
		assertThat(appServer.host, is('app'))
		assertThat(appServer.port, is(22))
		assertThat(appServer.user, is('appuser'))
		assertThat(appServer.password, is(nullValue()))
		assertThat(appServer.identity, instanceOf(File))
		assertThat(appServer.identity.name, is('id_rsa'))
	}

	@Test
	void global_remotes_password() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer {
					host = 'web'
					user = 'webuser'
					password = 'hogehoge'
				}
			}
		}

		assertThat(project.remotes, instanceOf(NamedDomainObjectCollection))
		assertThat(project.remotes.size(), is(1))
		assertThat(project.remotes.webServer, instanceOf(Remote))
		Remote webServer = project.remotes.webServer
		assertThat(webServer.name, is('webServer'))
		assertThat(webServer.host, is('web'))
		assertThat(webServer.port, is(22))
		assertThat(webServer.user, is('webuser'))
		assertThat(webServer.password, is('hogehoge'))
		assertThat(webServer.identity, is(nullValue()))
	}

	@Test
	void global_remotes_port() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer {
					host = 'web'
					port = 10022
					user = 'webuser'
					password = 'hogehoge'
				}
			}
		}

		assertThat(project.remotes, instanceOf(NamedDomainObjectCollection))
		assertThat(project.remotes.size(), is(1))
		assertThat(project.remotes.webServer, instanceOf(Remote))
		Remote webServer = project.remotes.webServer
		assertThat(webServer.name, is('webServer'))
		assertThat(webServer.host, is('web'))
		assertThat(webServer.port, is(10022))
		assertThat(webServer.user, is('webuser'))
		assertThat(webServer.password, is('hogehoge'))
		assertThat(webServer.identity, is(nullValue()))
	}

	@Test
	void global_remotes_role_empty() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
			}
		}

		Collection<Remote> servers = project.remotes.role('servers')
		assertThat(servers, instanceOf(Collection))
		assertThat(servers.size(), is(0))
	}

	@Test
	void global_remotes_role_noMatch() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer1 {
					role 'primaryServers'
					host = 'web1'
					user = 'webuser'
				}
				webServer2 {
					host = 'web2'
					user = 'webuser'
				}
				managementServer {
					role 'primaryServers'
					host = 'mng'
					user = 'mnguser'
				}
			}
		}

		Collection<Remote> servers = project.remotes.role('servers')
		assertThat(servers, instanceOf(Collection))
		assertThat(servers.size(), is(0))
	}

	@Test
	void global_remotes_role_someMatch() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer1 {
					role 'primaryServers'
					host = 'web1'
					user = 'webuser'
				}
				webServer2 {
					host = 'web2'
					user = 'webuser'
				}
				managementServer {
					role 'primaryServers'
					host = 'mng'
					user = 'mnguser'
				}
			}
		}

		Collection<Remote> servers = project.remotes.role('primaryServers')
		assertThat(servers, instanceOf(Collection))
		assertThat(servers.size(), is(2))
		def servers_webServer = servers.find { it.name == 'webServer1' }
		assertThat(servers_webServer, instanceOf(Remote))
		assertThat(servers_webServer.name, is('webServer1'))
		assertThat(servers_webServer.host, is('web1'))
		assertThat(servers_webServer.user, is('webuser'))
		def servers_managementServer = servers.find { it.name == 'managementServer' }
		assertThat(servers_managementServer, instanceOf(Remote))
		assertThat(servers_managementServer.name, is('managementServer'))
		assertThat(servers_managementServer.host, is('mng'))
		assertThat(servers_managementServer.user, is('mnguser'))
	}

	@Test
	void global_remotes_role_exactlyMatch() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer1 {
					role 'primaryServers'
					host = 'web1'
					user = 'webuser'
				}
				managementServer {
					role 'primaryServers'
					host = 'mng'
					user = 'mnguser'
				}
			}
		}

		Collection<Remote> servers = project.remotes.role('primaryServers')
		assertThat(servers, instanceOf(Collection))
		assertThat(servers.size(), is(2))
		def servers_webServer = servers.find { it.name == 'webServer1' }
		assertThat(servers_webServer, instanceOf(Remote))
		assertThat(servers_webServer.name, is('webServer1'))
		assertThat(servers_webServer.host, is('web1'))
		assertThat(servers_webServer.user, is('webuser'))
		def servers_managementServer = servers.find { it.name == 'managementServer' }
		assertThat(servers_managementServer, instanceOf(Remote))
		assertThat(servers_managementServer.name, is('managementServer'))
		assertThat(servers_managementServer.host, is('mng'))
		assertThat(servers_managementServer.user, is('mnguser'))
	}

	@Test
	void global_remotes_role_complex() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			remotes {
				webServer {
					role 'serversA'
					host = 'web'
					user = 'webuser'
				}
				appServer {
					role 'serversB'
					host = 'app'
					user = 'appuser'
				}
				dbServer {
					host = 'db'
					user = 'dbuser'
				}
				managementServer {
					role 'serversA'
					role 'serversB'
					host = 'mng'
					user = 'mnguser'
				}
			}
		}

		Collection<Remote> servers = project.remotes.role('serversA', 'serversB')
		assertThat(servers, instanceOf(Collection))
		assertThat(servers.size(), is(3))
		def servers_webServer = servers.find { it.name == 'webServer' }
		assertThat(servers_webServer, instanceOf(Remote))
		assertThat(servers_webServer.name, is('webServer'))
		assertThat(servers_webServer.host, is('web'))
		assertThat(servers_webServer.user, is('webuser'))
		def servers_appServer = servers.find { it.name == 'appServer' }
		assertThat(servers_appServer, instanceOf(Remote))
		assertThat(servers_appServer.name, is('appServer'))
		assertThat(servers_appServer.host, is('app'))
		assertThat(servers_appServer.user, is('appuser'))
		def servers_managementServer = servers.find { it.name == 'managementServer' }
		assertThat(servers_managementServer, instanceOf(Remote))
		assertThat(servers_managementServer.name, is('managementServer'))
		assertThat(servers_managementServer.host, is('mng'))
		assertThat(servers_managementServer.user, is('mnguser'))
	}
}
