package org.hidetake.gradle.ssh.plugin

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.SshSettings
import org.hidetake.gradle.ssh.api.session.Sessions
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.test.RegistryHelper.factoryOf

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


    @ConfineMetaClassChanges(Sessions)
    def "sshexec delegates to executor"() {
        given:
        def sessions = Mock(Sessions)
        factoryOf(Sessions) << Mock(Sessions.Factory) {
            1 * create() >> sessions
        }

        def convention = new SshPluginConvention()

        def mergedMock = Mock(SshSettings)
        GroovySpy(SshSettings, global: true)
        1 * SshSettings.computeMerged(_, convention.ssh) >> mergedMock

        when:
        convention.sshexec({})

        then:
        1 * sessions.execute(mergedMock)
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
