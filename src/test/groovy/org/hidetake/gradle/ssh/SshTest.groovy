package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SshTest {
	@Test
	void conventionTest_1remote() {
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
			task(type: Ssh, 'testTask') {
				session(remotes.webServer) {
					execute 'ls'
				}
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as Executor
		target.execute()
		assertThat(actual.config, instanceOf(Map))
		assertThat(actual.config.isEmpty(), is(true))
		assertThat(actual.sessionSpecs, instanceOf(Collection))
		assertThat(actual.sessionSpecs.size(), is(1))
		assertThat(actual.sessionSpecs[0], instanceOf(SessionSpec))
		assertThat(actual.sessionSpecs[0].remote, instanceOf(Remote))
		assertThat(actual.sessionSpecs[0].remote.name, is('webServer'))
		assertThat(actual.sessionSpecs[0].remote.host, is('web'))
		assertThat(actual.sessionSpecs[0].remote.user, is('webuser'))
		assertThat(actual.sessionSpecs[0].remote.identity, instanceOf(File))
		assertThat(actual.sessionSpecs[0].remote.identity.name, is('id_rsa'))
	}

	@Test
	void conventionTest_2remotes() {
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
			task(type: Ssh, 'testTask') {
				session(remotes.appServer) {
					execute 'ls'
				}
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as Executor
		target.execute()
		assertThat(actual.config, instanceOf(Map))
		assertThat(actual.config.isEmpty(), is(true))
		assertThat(actual.sessionSpecs, instanceOf(Collection))
		assertThat(actual.sessionSpecs.size(), is(1))
		assertThat(actual.sessionSpecs[0], instanceOf(SessionSpec))
		assertThat(actual.sessionSpecs[0].remote, instanceOf(Remote))
		assertThat(actual.sessionSpecs[0].remote.name, is('appServer'))
		assertThat(actual.sessionSpecs[0].remote.host, is('app'))
		assertThat(actual.sessionSpecs[0].remote.user, is('appuser'))
		assertThat(actual.sessionSpecs[0].remote.identity, instanceOf(File))
		assertThat(actual.sessionSpecs[0].remote.identity.name, is('id_rsa'))
	}

	@Test
	void conventionTest_config() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				config(StrictHostKeyChecking: 'no')
			}
			remotes {
				webServer {
					host = 'web'
					user = 'webuser'
					identity = file('id_rsa')
				}
			}
			task(type: Ssh, 'testTask') {
				session(remotes.webServer) {
					execute 'ls'
				}
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as Executor
		target.execute()
		assertThat(actual.config, instanceOf(Map))
		assertThat(actual.config.size(), is(1))
		assertThat(actual.config.StrictHostKeyChecking, is('no'))
		assertThat(actual.sessionSpecs[0], instanceOf(SessionSpec))
		assertThat(actual.sessionSpecs.size(), is(1))
	}
}
