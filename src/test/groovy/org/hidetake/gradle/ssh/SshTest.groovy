package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.Remote;
import org.hidetake.gradle.ssh.api.SessionSpec;
import org.hidetake.gradle.ssh.api.SshService;
import org.hidetake.gradle.ssh.api.SshSpec;
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
		target.service = [execute: { actual = it }] as SshService
		target.execute()
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
		target.service = [execute: { actual = it }] as SshService
		target.execute()
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
	void conventionTest_conventionConfig() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				config(StrictHostKeyChecking: 'no')
			}
			task(type: Ssh, 'testTask') {
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.config, instanceOf(Map))
		assertThat(actual.config.size(), is(1))
		assertThat(actual.config.StrictHostKeyChecking, is('no'))
	}

	@Test
	void conventionTest_taskSpecificConfig() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			task(type: Ssh, 'testTask') {
				config(StrictHostKeyChecking: 'no')
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.config, instanceOf(Map))
		assertThat(actual.config.size(), is(1))
		assertThat(actual.config.StrictHostKeyChecking, is('no'))
	}

	@Test
	void conventionTest_globalAndTaskSpecificConfig() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				config(StrictHostKeyChecking: 'no')
			}
			task(type: Ssh, 'testTask') {
				config(StrictHostKeyChecking: 'yes')
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.config, instanceOf(Map))
		assertThat(actual.config.size(), is(1))
		assertThat(actual.config.StrictHostKeyChecking, is('yes'))
	}

	@Test
	void conventionTest_conventionDryRunOmitted() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			task(type: Ssh, 'testTask') {
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.dryRun, is(false))
	}

	@Test
	void conventionTest_conventionDryRunIsFalse() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				dryRun = false
			}
			task(type: Ssh, 'testTask') {
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.dryRun, is(false))
	}

	@Test
	void conventionTest_conventionDryRunIsTrue() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				dryRun = true
			}
			task(type: Ssh, 'testTask') {
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.dryRun, is(true))
	}

	@Test
	void conventionTest_taskSpecificDryRunIsFalse() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			task(type: Ssh, 'testTask') {
				dryRun = false
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.dryRun, is(false))
	}
	
	@Test
	void conventionTest_taskSpecificDryRunIsTrue() {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			task(type: Ssh, 'testTask') {
				dryRun = true
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.dryRun, is(true))
	}

	@Test
	void conventionTest_globalAndTaskSpecificDryRun() {
		conventionTestHelper_globalAndTaskSpecificDryRun(null, null, false)
		conventionTestHelper_globalAndTaskSpecificDryRun(null, false, false)
		conventionTestHelper_globalAndTaskSpecificDryRun(null, true, true)
		conventionTestHelper_globalAndTaskSpecificDryRun(false, null, false)
		conventionTestHelper_globalAndTaskSpecificDryRun(false, false, false)
		conventionTestHelper_globalAndTaskSpecificDryRun(false, true, true)
		conventionTestHelper_globalAndTaskSpecificDryRun(true, null, true)
		conventionTestHelper_globalAndTaskSpecificDryRun(true, false, false)
		conventionTestHelper_globalAndTaskSpecificDryRun(true, true, true)
	}

	protected void conventionTestHelper_globalAndTaskSpecificDryRun(
		Boolean globalDryRun, Boolean taskSpecificDryRun, boolean expected) {
		Project project = ProjectBuilder.builder().build()
		project.with {
			apply plugin: 'ssh'
			ssh {
				dryRun = globalDryRun
			}
			task(type: Ssh, 'testTask') {
				dryRun = taskSpecificDryRun
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
		assertThat(actual.dryRun, is(expected))
	}

	@Test(expected = TaskExecutionException)
	void conventionTest_conventionSessions() {
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
			ssh {
				// bad
				session(remotes.webServer) {
					execute 'ls'
				}
			}
			task(type: Ssh, 'testTask') {
			}
		}
		assertThat(project.tasks.testTask, instanceOf(Ssh))
		Ssh target = project.tasks.testTask
		SshSpec actual
		target.service = [execute: { actual = it }] as SshService
		target.execute()
	}
}
