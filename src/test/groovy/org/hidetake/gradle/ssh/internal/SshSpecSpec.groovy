package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec
import org.hidetake.gradle.ssh.api.SshSpec
import spock.lang.Specification
import spock.lang.Unroll

import static org.hidetake.gradle.ssh.test.TestDataHelper.createRemote
import static org.hidetake.gradle.ssh.test.TestDataHelper.createSpec

class SshSpecSpec extends Specification {


    SshSpec spec

    private static final identityA = new File("identityA")
    private static final identityB = new File("identityB")


    def setup() {
        spec = new SshSpec()
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
            knownHosts = new File("dummy")
            dryRun = true
            retryCount = 2
            retryWaitSec = 2
            outputLogLevel = LogLevel.DEBUG
            errorLogLevel = LogLevel.INFO
            encoding = 'US-ASCII'

            it
        }

        when:
        def merged = SshSpec.computeMerged(spec2, spec1)

        then:
        merged.sessionSpecs == spec1.sessionSpecs + spec2.sessionSpecs
        merged.knownHosts == spec2.knownHosts
        merged.dryRun == spec2.dryRun
        merged.retryCount == spec2.retryCount
        merged.retryWaitSec == spec2.retryWaitSec
        merged.outputLogLevel == spec2.outputLogLevel
        merged.errorLogLevel == spec2.errorLogLevel
        merged.encoding == spec2.encoding
    }


    @Unroll
    def "compute merged identity in order (#i1 #p1, #i2 #p2)"() {
        given:
        def spec1 = createSpec()
        spec1.with {
            identity = i1
            passphrase = p1
        }
        def spec2 = createSpec()
        spec2.with {
            identity = i2
            passphrase = p2
        }

        when:
        def merged = SshSpec.computeMerged(spec2, spec1)

        then:
        merged.identity == i0
        merged.passphrase == p0

        where:
        i1        | p1   | i2        | p2   || i0        | p0
        null      | null | null      | null || null      | null
        identityA | null | null      | null || identityA | null
        identityA | "pA" | null      | null || identityA | "pA"
        null      | null | identityA | null || identityA | null
        null      | null | identityA | "pA" || identityA | "pA"
        identityA | null | identityB | null || identityB | null
        identityA | "pA" | identityB | null || identityB | null
        identityA | null | identityB | "pB" || identityB | "pB"
        identityA | "pA" | identityB | "pB" || identityB | "pB"
    }



    private def assertEquals(SshSpec expected, SshSpec actual) {
        assert expected.knownHosts == actual.knownHosts
        assert expected.dryRun == actual.dryRun
        assert expected.retryCount == actual.retryCount
        assert expected.retryWaitSec == actual.retryWaitSec
        assert expected.sessionSpecs == actual.sessionSpecs
        assert expected.outputLogLevel == actual.outputLogLevel
        assert expected.errorLogLevel == actual.errorLogLevel

        true
    }

}
