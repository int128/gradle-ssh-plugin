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

        when:
        convention.ssh {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
        }

        then:
        convention.ssh.dryRun
        convention.ssh.retryCount == 1
        convention.ssh.retryWaitSec == 1
        convention.ssh.outputLogLevel == LogLevel.DEBUG
        convention.ssh.errorLogLevel == LogLevel.INFO
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

        when:
        convention.sshexec {
            dryRun = true
            outputLogLevel = LogLevel.ERROR
        }

        then:
        1 * sessions.execute(_) >> { SshSettings settings ->
            assert settings.dryRun
            assert settings.outputLogLevel == LogLevel.ERROR
        }
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
