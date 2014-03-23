package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.hidetake.gradle.ssh.api.SshSettings
import spock.lang.Specification
import spock.lang.Unroll

import static org.hidetake.gradle.ssh.test.TestDataHelper.createSpec

class SshSettingsSpec extends Specification {


    SshSettings settings

    private static final identityA = new File("identityA")
    private static final identityB = new File("identityB")


    def setup() {
        settings = new SshSettings()
    }


    def "compute merged adheres to priority order"() {
        given:
        def spec1 = createSpec()
        def spec2 = new SshSettings().with {
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
        def merged = SshSettings.DEFAULT + spec1 + spec2

        then:
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
        def merged = SshSettings.DEFAULT + spec1 + spec2

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

}
