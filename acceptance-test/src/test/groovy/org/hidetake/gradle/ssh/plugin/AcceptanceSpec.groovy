package org.hidetake.gradle.ssh.plugin

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

class AcceptanceSpec extends Specification {

    static final matrix = []
    static {
        System.getProperty('target.gradle.versions').split(/,/).collect { gradleVersion ->
            System.getProperty('target.java.homes').split(/,/).collect { javaHome ->
                matrix << [gradleVersion, javaHome]
            }
        }
    }

    @Unroll
    def "specs should be pass on Gradle #gradleVersion, Java #javaHome"() {
        given:
        def runner = GradleRunner.create()
                .withProjectDir(new File('fixture'))
                .withArguments("-Dorg.gradle.java.home=$javaHome", 'test')
                .withGradleVersion(gradleVersion)

        and: 'show console on Gradle 2.x'
        if (gradleVersion =~ /^2\./) {
            runner.forwardOutput()
        }

        when:
        runner.build()

        then:
        noExceptionThrown()

        where:
        [gradleVersion, javaHome] << matrix
    }

}
