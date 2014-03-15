package org.hidetake.gradle.ssh.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSettings
import spock.lang.Specification

class SshTaskSpec extends Specification {

    def SshSettings mergedSpecMock


    def setup() {
        mergedSpecMock = Mock(SshSettings)
        GroovySpy(SshSettings, global: true)
    }

    def "task action delegates to service"() {
        given:
        Project project = setupSampleProject()
        def task = project.tasks.testTask
        def mockService = Mock(SshService)
        task.service = mockService
        def projectSshSettings = project.convention.getPlugin(SshPluginConvention).sshSettings


        1 * SshSettings.computeMerged(task.sshSettings, projectSshSettings) >> mergedSpecMock


        when:
        task.perform()

        then:
        1 * mockService.execute(mergedSpecMock)
    }



    private Project setupSampleProject() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'ssh'
            remotes {
                webServer {
                    host = 'web'
                    user = 'webuser'
                    identity = file('id_rsa')
                }
            }
            task(type: SshTask, 'testTask') {
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            it
        }
    }

}
