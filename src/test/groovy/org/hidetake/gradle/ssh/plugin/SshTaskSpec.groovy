package org.hidetake.gradle.ssh.plugin

import com.jcraft.jsch.JSchException
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class SshTaskSpec extends Specification {

    static project() {
        ProjectBuilder.builder().build().with {
            apply plugin: 'org.hidetake.ssh'
            proxies {
                globalProxy { host = 'globalPrx' }
                overrideProxy { host = 'overridePrx' }
            }
            remotes {
                webServer {
                    host = 'web'
                    user = 'webuser'
                    identity = file('id_rsa')
                }
            }
            ssh.settings {
                dryRun = false
                proxy = proxies.globalProxy
            }
            task(type: SshTask, 'testTask1') {
                ssh {
                    dryRun = true
                    proxy = proxies.overrideProxy
                }
                session(remotes.webServer) {
                    execute 'ls'
                    project.ext.checkpoint1 = true
                }
            }
            task(type: SshTask, 'testTask2') {
                session(remotes.webServer) {
                    execute 'ls'
                    project.ext.checkpoint2 = true
                }
            }
            it
        }
    }


    def "task action delegates to executor"() {
        given:
        def project = project()

        when: project.tasks.testTask1.execute()
        then: project.ext.checkpoint1

        when: project.tasks.testTask2.execute()
        then:
        TaskExecutionException e = thrown()
        e.cause.cause instanceof JSchException
        e.cause.cause.cause instanceof FileNotFoundException
    }

}
