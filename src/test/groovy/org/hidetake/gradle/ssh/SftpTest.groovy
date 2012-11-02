package org.hidetake.gradle.ssh

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SftpTest {
	@Test
	void put_configuration_minimum() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Sftp, 'testTask') {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				transfer(new PutTransfer(from: 'sample.txt', to: '.'))
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Sftp))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.transfers.size(), is(1))
		assertThat(project.tasks.testTask.transfers[0], instanceOf(PutTransfer))
		assertThat(project.tasks.testTask.transfers[0].from, is('sample.txt'))
		assertThat(project.tasks.testTask.transfers[0].to, is('.'))
	}

	@Test
	void put_configuration_remote_convention() {
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
			task(type: Sftp, 'testTask') {
				transfer(new PutTransfer(from: 'sample.txt', to: '.'))
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Sftp))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.transfers.size(), is(1))
		assertThat(project.tasks.testTask.transfers[0], instanceOf(PutTransfer))
		assertThat(project.tasks.testTask.transfers[0].from, is('sample.txt'))
		assertThat(project.tasks.testTask.transfers[0].to, is('.'))
	}

	@Test
	void put_configuration_remote_override() {
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
			task(type: Sftp, 'testTask') {
				remote {
					host = 'somehost'
					user = 'fuga'
				}
				transfer(new PutTransfer(from: 'sample.txt', to: '.'))
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Sftp))
		assertThat(project.tasks.testTask.remote.host, is('somehost'))
		assertThat(project.tasks.testTask.remote.user, is('fuga'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.transfers.size(), is(1))
		assertThat(project.tasks.testTask.transfers[0], instanceOf(PutTransfer))
		assertThat(project.tasks.testTask.transfers[0].from, is('sample.txt'))
		assertThat(project.tasks.testTask.transfers[0].to, is('.'))
	}

	@Test
	void put_configuration_jschConfig() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Sftp, 'testTask') {
				config(StrictHostKeyChecking: 'no')
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				transfer(new PutTransfer(from: 'sample.txt', to: '.'))
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Sftp))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.size(), is(1))
		assertThat(project.tasks.testTask.config.StrictHostKeyChecking, is('no'))
		assertThat(project.tasks.testTask.transfers.size(), is(1))
		assertThat(project.tasks.testTask.transfers[0], instanceOf(PutTransfer))
		assertThat(project.tasks.testTask.transfers[0].from, is('sample.txt'))
		assertThat(project.tasks.testTask.transfers[0].to, is('.'))
	}

	@Test
	void put_configuration_multiTransfers() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Sftp, 'testTask') {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				transfer(new PutTransfer(from: 'sample.txt', to: '.'))
				transfer(new PutTransfer(from: 'example.md', to: 'data'))
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Sftp))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.transfers.size(), is(2))
		assertThat(project.tasks.testTask.transfers[0], instanceOf(PutTransfer))
		assertThat(project.tasks.testTask.transfers[0].from, is('sample.txt'))
		assertThat(project.tasks.testTask.transfers[0].to, is('.'))
		assertThat(project.tasks.testTask.transfers[1], instanceOf(PutTransfer))
		assertThat(project.tasks.testTask.transfers[1].from, is('example.md'))
		assertThat(project.tasks.testTask.transfers[1].to, is('data'))
	}

	@Test
	void get_configuration_minimum() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			task(type: Sftp, 'testTask') {
				remote {
					host = 'localhost'
					user = 'hoge'
					identity = 'id_rsa'
				}
				transfer(new GetTransfer(from: 'sample.txt', to: '.'))
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Sftp))
		assertThat(project.tasks.testTask.remote.host, is('localhost'))
		assertThat(project.tasks.testTask.remote.user, is('hoge'))
		assertThat(project.tasks.testTask.remote.identity, is('id_rsa'))
		assertThat(project.tasks.testTask.config.isEmpty(), is(true))
		assertThat(project.tasks.testTask.transfers.size(), is(1))
		assertThat(project.tasks.testTask.transfers[0], instanceOf(GetTransfer))
		assertThat(project.tasks.testTask.transfers[0].from, is('sample.txt'))
		assertThat(project.tasks.testTask.transfers[0].to, is('.'))
	}
}
