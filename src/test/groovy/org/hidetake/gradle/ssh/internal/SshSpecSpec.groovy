package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSpec
import spock.lang.Specification
import spock.lang.Unroll

import static org.hidetake.gradle.ssh.test.TestDataHelper.createRemote
import static org.hidetake.gradle.ssh.test.TestDataHelper.createSpec


class SshSpecSpec extends Specification {


    SshSpec spec

    def setup() {
        spec = new SshSpec()
    }

    def "add configuration params"() {
        when:
        spec.config conf1: '1', conf2: '2'

        then:
        spec.config.size() == 2
        spec.config == [conf1: '1', conf2: '2']
    }

    def "add config with null param throws assertion error"() {
        when:
        spec.config null

        then:
        thrown AssertionError
    }


    def "add session"() {
        given:
        def remote = createRemote()
        def operationClosure = {-> println "whatever" }

        when:
        spec.session(remote, operationClosure)


        then:
        spec.sessionSpecs.size() == 1
        spec.sessionSpecs[0].remote == remote
        spec.sessionSpecs[0].operationClosure == operationClosure
    }

    @Unroll("add session with remote: #theRemote, user: #theUser, host: #theHost")
    def "add session with invalid params throws assertion error"() {
        given:
        def remote

        when:
        remote = theRemote ? createRemote([user: theUser, host: theHost]) : null
        spec.session(remote, theOperationClosure)

        then:
        AssertionError e = thrown()
        e.message.contains(errorContains)


        where:
        theRemote  | theUser  | theHost          | theOperationClosure | errorContains
        null       | "myUser" | "www.myhost.com" | {-> println it }    | "remote"
        "myRemote" | null     | "www.myhost.com" | {-> println it }    | "user"
        "myRemote" | "myUser" | null             | {-> println it }    | "host"
        "myRemote" | "myUser" | "www.myhost.com" | null                | "operation"
    }



    def "add session for multiple remotes"() {
        given:
        def remote1 = createRemote([name: "remote1"])
        def remote2 = createRemote([name: "remote2"])
        def closure = {-> println "whatever" }

        when:
        spec.session([remote1, remote2], closure)

        then:
        spec.sessionSpecs.size() == 2
    }


    def "add session for multiple remotes with illegal args throws assertion error"() {
        given:
        def remote = createRemote()

        when:
        spec.session([], {-> println "whatever" })

        then:
        AssertionError ex = thrown()
        ex.message.contains("remotes")

        when:
        spec.session([remote], null)

        then:
        AssertionError ex2 = thrown()
        ex2.message.contains("operation")

    }



    def "compute merged on one spec is a clone"() {
        given:
        def spec = createSpec()

        when:
        def merged = SshSpec.computeMerged(spec)

        then:
        assertEquals spec, merged
    }

    def "compute merged adheres to priority order"() {
        given:
        def spec1 = createSpec()
        def spec2 = new SshSpec().with {
            sessionSpecs.addAll([new SessionSpec(Mock(Remote), {-> println "whatever" })])
            dryRun = true
            retryCount = 2
            retryWaitSec = 2
            logger = Mock(Logger)
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
            config([myConf: 'myConf2'])

            it
        }

        when:
        def merged = SshSpec.computeMerged(spec2, spec1)

        then:
        merged.config == spec1.config + spec2.config
        merged.sessionSpecs == spec1.sessionSpecs + spec2.sessionSpecs
        merged.dryRun == spec2.dryRun
        merged.retryCount == spec2.retryCount
        merged.retryWaitSec == spec2.retryWaitSec
        merged.logger == spec2.logger
        merged.outputLogLevel == spec2.outputLogLevel
        merged.errorLogLevel == spec2.errorLogLevel
    }

    private def assertEquals(SshSpec expected, SshSpec actual) {
        assert expected.config == actual.config
        assert expected.dryRun == actual.dryRun
        assert expected.retryCount == actual.retryCount
        assert expected.retryWaitSec == actual.retryWaitSec
        assert expected.sessionSpecs == actual.sessionSpecs
        assert expected.logger == actual.logger
        assert expected.outputLogLevel == actual.outputLogLevel
        assert expected.errorLogLevel == actual.errorLogLevel

        true
    }

}
