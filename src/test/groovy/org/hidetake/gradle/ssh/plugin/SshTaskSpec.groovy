package org.hidetake.gradle.ssh.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.groovy.ssh.api.CompositeSettings
import org.hidetake.groovy.ssh.api.ConnectionSettings
import org.hidetake.groovy.ssh.api.OperationSettings
import org.hidetake.groovy.ssh.internal.DefaultRunHandler
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

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
            ssh {
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


    @ConfineMetaClassChanges(DefaultRunHandler)
    def "task action delegates to executor"() {
        given:
        def called = Mock(Closure)
        DefaultRunHandler.metaClass.run = { CompositeSettings s -> called(s) }

        def project = project()

        when: project.tasks.testTask1.execute()

        then: 1 * called(new CompositeSettings(
            operationSettings: new OperationSettings(dryRun: false),
            connectionSettings: new ConnectionSettings(proxy: project.proxies.globalProxy)
        ))

        when: project.tasks.testTask2.execute()
        then: 1 * called(new CompositeSettings(
            operationSettings: new OperationSettings(dryRun: false),
            connectionSettings: new ConnectionSettings(proxy: project.proxies.globalProxy)
        ))
    }

}
