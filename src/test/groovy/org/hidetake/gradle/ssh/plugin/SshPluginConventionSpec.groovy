package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.task.Executor
import org.hidetake.gradle.ssh.registry.Registry
import org.hidetake.gradle.ssh.test.ConfineRegistryChanges
import spock.lang.Specification

@ConfineRegistryChanges
class SshPluginConventionSpec extends Specification {

    def "configure ssh"() {
        given:
        def convention = new SshPluginConvention()
        def configClosure = {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
        }

        when:
        convention.ssh(configClosure)

        then:
        convention.ssh.with {
            dryRun
            retryCount == 1
            retryWaitSec == 1
            outputLogLevel == LogLevel.DEBUG
            errorLogLevel == LogLevel.INFO
        }
    }

    def "configure ssh closure must be seg"() {
        when:
        def convention = new SshPluginConvention()
        convention.ssh(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }


    def "sshexec delegates to executor"() {
        given:
        def executor = Registry.instance[Executor] = Mock(Executor)

        def convention = new SshPluginConvention()

        def mergedMock = Mock(SshSettings)
        GroovySpy(SshSettings, global: true)
        1 * SshSettings.computeMerged(_, convention.ssh) >> mergedMock

        when:
        convention.sshexec({})

        then:
        1 * executor.execute(mergedMock, [])
    }

    def "sshexec must specify closure"() {
        when:
        def convention = new SshPluginConvention()
        convention.sshexec(null)

        then:
        AssertionError err = thrown()
        err.message.contains("closure")
    }
}
