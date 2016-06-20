package org.hidetake.gradle.ssh.plugin

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

class AcceptanceSpec extends Specification {

    @Unroll
    def "acceptance test should be success on Gradle #version"() {
        when:
        GradleRunner.create()
                .withProjectDir(new File('fixture'))
                .withArguments('test')
                .withGradleVersion(version)
                .build()

        then:
        noExceptionThrown()

        where:
        version << ['2.13', '1.12']
    }

}
