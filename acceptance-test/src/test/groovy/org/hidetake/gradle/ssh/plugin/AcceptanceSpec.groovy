package org.hidetake.gradle.ssh.plugin

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

class AcceptanceSpec extends Specification {

    @Unroll
    def "acceptance test should be success on Gradle #version"() {
        given:
        def runner = GradleRunner.create()
                .withProjectDir(new File('fixture'))
                .withArguments('test')
                .withGradleVersion(version)

        and: 'show console on Gradle 2.x'
        if (version =~ /^2\./) {
            runner.forwardOutput()
        }

        when:
        runner.build()

        then:
        noExceptionThrown()

        where:
        version << System.getProperty('target.gradle.versions').split(/,/).toList()
    }

}
