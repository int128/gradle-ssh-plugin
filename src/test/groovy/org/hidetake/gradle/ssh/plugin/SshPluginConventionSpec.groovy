package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.operation.OperationSettings
import org.hidetake.gradle.ssh.api.session.Sessions
import org.hidetake.gradle.ssh.api.ssh.ConnectionSettings
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.test.RegistryHelper.factoryOf

class SshPluginConventionSpec extends Specification {

    def "apply global settings"() {
        given:
        def convention = new SshPluginConvention()

        when:
        convention.ssh {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
        }

        then:
        convention.globalSettings.dryRun
        convention.globalSettings.retryCount == 1
        convention.globalSettings.retryWaitSec == 1
        convention.globalSettings.outputLogLevel == LogLevel.DEBUG
        convention.globalSettings.errorLogLevel == LogLevel.INFO
    }

    def "apply global settings but null"() {
        when:
        def convention = new SshPluginConvention()
        convention.ssh(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }


    @ConfineMetaClassChanges(Sessions)
    def "apply task specific settings"() {
        given:
        def sessionsFactory = Mock(Sessions.Factory)
        factoryOf(Sessions) << sessionsFactory
        def sessions1 = Mock(Sessions)
        def sessions2 = Mock(Sessions)
        def convention = new SshPluginConvention()

        def remoteMock = Mock(Remote)
        remoteMock.name >> 'name'
        remoteMock.user >> 'user'
        remoteMock.host >> 'host'

        when:
        convention.sshexec {
            ssh {
                knownHosts = allowAnyHosts
                dryRun = true
                outputLogLevel = LogLevel.ERROR
            }
            session(remoteMock) {
                execute 'ls'
            }
        }

        then: 1 * sessionsFactory.create() >> sessions1
        then: 1 * sessions1.add(_, _)
        then:
        1 * sessions1.execute(
                ConnectionSettings.DEFAULT + new ConnectionSettings(knownHosts: ConnectionSettings.allowAnyHosts),
                OperationSettings.DEFAULT + new OperationSettings(dryRun: true, outputLogLevel: LogLevel.ERROR)
        )

        when:
        convention.sshexec {
            session(remoteMock) {
                execute 'ls'
            }
        }

        then: 1 * sessionsFactory.create() >> sessions2
        then: 1 * sessions2.add(_, _)
        then:
        1 * sessions2.execute(ConnectionSettings.DEFAULT, OperationSettings.DEFAULT)
    }

    def "apply task specific settings but null"() {
        when:
        def convention = new SshPluginConvention()
        convention.sshexec(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }
}
