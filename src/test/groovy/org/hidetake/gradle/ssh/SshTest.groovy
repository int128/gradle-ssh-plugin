package org.hidetake.gradle.ssh


import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SshTest {
	@Test
	public void singleChannel() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'ssh'
		project.with {
			ssh {
				remote {
					host = 'localhost'
					user = System.properties['user.name']
					identity = "${System.properties['user.home']}/.ssh/id_rsa"
				}
			}

			task(type: Ssh, 'testTask') {
				config(StrictHostKeyChecking: 'no')
				channel { command = 'uname -a' }
			}
		}

		assertThat(project.tasks.testTask, instanceOf(Ssh))
		// TODO
	}
}
