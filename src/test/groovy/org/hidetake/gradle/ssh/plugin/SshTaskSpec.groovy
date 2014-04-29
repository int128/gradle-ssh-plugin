package org.hidetake.gradle.ssh.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.internal.SshTaskService
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

class SshTaskSpec extends Specification {

    static project() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'ssh'
            remotes {
                webServer {
                    host = 'web'
                    user = 'webuser'
                    identity = file('id_rsa')
                }
            }
            ssh {
                dryRun = false
            }
            task(type: SshTask, 'testTask1') {
                ssh {
                    dryRun = true
                }
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            task(type: SshTask, 'testTask2') {
                session(remotes.webServer) {
                    execute 'ls'
                }
            }
            it
        }
    }


    @ConfineMetaClassChanges(SshTaskService)
    def "task action delegates to executor"() {
        given:
        def service = Mock(SshTaskService)
        SshTaskService.metaClass.static.getInstance = { -> service }

        def taskHandler1 = Mock(SshTaskHandler)
        def taskHandler2 = Mock(SshTaskHandler)

        when:
        def project = project()

        then: 1 * service.createDelegate() >> taskHandler1
        then: 1 * taskHandler1.ssh(_)
        then: 1 * taskHandler1.session(_ as Remote, _ as Closure)

        then: 1 * service.createDelegate() >> taskHandler2
        then: 1 * taskHandler2.session(_ as Remote, _ as Closure)

        when: project.tasks.testTask1.execute()
        then: 1 * taskHandler1.execute(new GlobalSettings(
                operationSettings: new OperationSettings(dryRun: false)
        ))

        when: project.tasks.testTask2.execute()
        then: 1 * taskHandler2.execute(new GlobalSettings(
                operationSettings: new OperationSettings(dryRun: false)
        ))
    }

}
