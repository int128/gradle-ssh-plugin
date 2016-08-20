package org.hidetake.gradle.ssh.plugin

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

class AcceptanceSpec extends Specification {

    static matrix = []
    static {
        System.getProperty('target.product.versions').split(/,/).each { productVersion ->
            System.getProperty('target.gradle.versions').split(/,/).each { gradleVersion ->
                matrix << [productVersion, gradleVersion]
            }
        }
    }

    @Unroll
    def "specs should be pass on Plugin #productVersion, Gradle #gradleVersion"() {
        given:
        def runner = GradleRunner.create()
                .withProjectDir(new File('fixture'))
                .withArguments("-Ptarget.product.version=$productVersion", '-s', 'test')
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
        [productVersion, gradleVersion] << matrix
    }

}
