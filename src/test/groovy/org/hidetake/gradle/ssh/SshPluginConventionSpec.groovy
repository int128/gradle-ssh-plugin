package org.hidetake.gradle.ssh

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.testfixtures.ProjectBuilder
import org.hidetake.gradle.ssh.api.SshService
import org.hidetake.gradle.ssh.api.SshSpec
import spock.lang.Specification

import static org.hidetake.gradle.ssh.test.TestDataHelper.createRemote


class SshPluginConventionSpec extends Specification {

    Project project
    SshPluginConvention convention


    def setup() {
        project = ProjectBuilder.builder().build()
        convention = new SshPluginConvention(project)
    }


    def "configure ssh"() {
        given:

        def configClosure = {
            dryRun = true
            retryCount = 1
            retryWaitSec = 1
            logger = [:] as Logger
            config(myConfig: 'myConfigValue')

        }

        when:
        convention.ssh(configClosure)

        then:
        convention.sshSpec.with {
            dryRun
            retryCount == 1
            retryWaitSec == 1
            config.myConfig == 'myConfigValue'
        }
    }

    def "configure ssh default values"() {
        given:
        def configClosure = {}

        when:
        convention.ssh(configClosure)

        then:
        convention.sshSpec.with {
            logger == project.logger
        }

    }

    def "configure ssh closure must be seg"() {
        when:
        convention.ssh(null)
        then:
        AssertionError err = thrown()
        err.message.contains("configure")
    }

    def "configure ssh does not allow specifying sessions"() {
        when:
        convention.ssh({
            session(createRemote()) { execute "ls -l" }
        })
        then:
        IllegalStateException ex = thrown()
        ex.message.contains("session")
    }

    def "configure ssh does not allow setting logger to null"() {
        when:
        convention.ssh({ logger = null })
        then:
        IllegalStateException ex = thrown()
        ex.message.contains("logger")
    }


    def "sshexec delegates to service"() {
        given:
        SshService serviceMock = Mock(SshService)
        convention.service = serviceMock

        SshSpec mergedSpecMock = Mock(SshSpec)
        GroovySpy(SshSpec, global: true)
        1 * SshSpec.computeMerged(_, _) >> mergedSpecMock

        when:
        convention.sshexec({})

        then:
        1 * serviceMock.execute(mergedSpecMock)
    }

    def "sshexec must specify closure"() {
        when:
        convention.sshexec(null)
        then:
        AssertionError err = thrown()
        err.message.contains("configure")
    }
}
