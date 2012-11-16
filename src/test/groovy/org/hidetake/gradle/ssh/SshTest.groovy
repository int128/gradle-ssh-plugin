package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SshTest {
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
			task(type: Ssh, 'testTask') {
				session(remotes.webServer) {
					execute 'ls'
				}
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh testTask = project.tasks.testTask
		SshSpec spec = testTask.computeSpec()
		assertThat(spec.config, instanceOf(Map))
		assertThat(spec.config.isEmpty(), is(true))
		assertThat(spec.sessionSpecs, instanceOf(Collection))
		assertThat(spec.sessionSpecs.size(), is(1))
		assertThat(spec.sessionSpecs[0], instanceOf(SessionSpec))
		assertThat(spec.sessionSpecs[0].remote, instanceOf(Remote))
		assertThat(spec.sessionSpecs[0].remote.name, is('webServer'))
		assertThat(spec.sessionSpecs[0].remote.host, is('web'))
		assertThat(spec.sessionSpecs[0].remote.user, is('webuser'))
		assertThat(spec.sessionSpecs[0].remote.identity, instanceOf(File))
		assertThat(spec.sessionSpecs[0].remote.identity.name, is('id_rsa'))
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
			task(type: Ssh, 'testTask') {
				session(remotes.appServer) {
					execute 'ls'
				}
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh testTask = project.tasks.testTask
		SshSpec spec = testTask.computeSpec()
		assertThat(spec.config, instanceOf(Map))
		assertThat(spec.config.isEmpty(), is(true))
		assertThat(spec.sessionSpecs, instanceOf(Collection))
		assertThat(spec.sessionSpecs.size(), is(1))
		assertThat(spec.sessionSpecs[0], instanceOf(SessionSpec))
		assertThat(spec.sessionSpecs[0].remote, instanceOf(Remote))
		assertThat(spec.sessionSpecs[0].remote.name, is('appServer'))
		assertThat(spec.sessionSpecs[0].remote.host, is('app'))
		assertThat(spec.sessionSpecs[0].remote.user, is('appuser'))
		assertThat(spec.sessionSpecs[0].remote.identity, instanceOf(File))
		assertThat(spec.sessionSpecs[0].remote.identity.name, is('id_rsa'))
	}

	@Test
	void global_config() {
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
		Ssh testTask = project.tasks.testTask
		SshSpec spec = testTask.computeSpec()
		assertThat(spec.config, instanceOf(Map))
		assertThat(spec.config.size(), is(1))
		assertThat(spec.config.StrictHostKeyChecking, is('no'))
		assertThat(spec.sessionSpecs[0], instanceOf(SessionSpec))
		assertThat(spec.sessionSpecs.size(), is(1))
	}
}
