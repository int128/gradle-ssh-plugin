package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import spock.lang.Specification

class SshTaskSpec extends Specification {

    def SshSpec mergedSpecMock


    def setup() {
        mergedSpecMock = Mock(SshSpec)
        GroovySpy(SshSpec, global: true)
    }

    def "task action delegates to service"() {
        given:
        Project project = setupSampleProject()
        def task = project.tasks.testTask
        def mockService = Mock(SshService)
        task.service = mockService
        def projectSshSpec = project.convention.getPlugin(SshPluginConvention).sshSpec


        1 * SshSpec.computeMerged(task.sshSpec, projectSshSpec) >> mergedSpecMock


        when:
        task.perform()

        then:
        1 * mockService.execute(mergedSpecMock)
    }

    def "task delegates to SshSpec"() {
        when:
        Project project = setupSampleProject()
        SshTask task = project.tasks.testTask


        then:
        task.config == task.sshSpec.config
        task.config.myConf == 'MyConfValue'

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
                config(myConf : 'MyConfValue')
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            it
        }
    }

}
