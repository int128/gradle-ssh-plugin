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
		assertThat(testTask.config.isEmpty(), is(true))
		assertThat(testTask.sessionSpecs.size(), is(1))
		assertThat(testTask.sessionSpecs[0], instanceOf(SshSpec.SessionSpec))
		assertThat(testTask.sessionSpecs[0].remote, instanceOf(Remote))
		assertThat(testTask.sessionSpecs[0].remote.name, is('webServer'))
		assertThat(testTask.sessionSpecs[0].remote.host, is('web'))
		assertThat(testTask.sessionSpecs[0].remote.user, is('webuser'))
		assertThat(testTask.sessionSpecs[0].remote.identity, instanceOf(File))
		assertThat(testTask.sessionSpecs[0].remote.identity.name, is('id_rsa'))
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
		assertThat(testTask.config.isEmpty(), is(true))
		assertThat(testTask.sessionSpecs.size(), is(1))
		assertThat(testTask.sessionSpecs[0], instanceOf(SshSpec.SessionSpec))
		assertThat(testTask.sessionSpecs[0].remote, instanceOf(Remote))
		assertThat(testTask.sessionSpecs[0].remote.name, is('appServer'))
		assertThat(testTask.sessionSpecs[0].remote.host, is('app'))
		assertThat(testTask.sessionSpecs[0].remote.user, is('appuser'))
		assertThat(testTask.sessionSpecs[0].remote.identity, instanceOf(File))
		assertThat(testTask.sessionSpecs[0].remote.identity.name, is('id_rsa'))
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
		assertThat(testTask.config.size(), is(1))
		assertThat(testTask.config.StrictHostKeyChecking, is('no'))
		assertThat(testTask.sessionSpecs.size(), is(1))
	}
}
