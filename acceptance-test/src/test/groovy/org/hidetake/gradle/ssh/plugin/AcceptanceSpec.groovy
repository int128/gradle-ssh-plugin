package org.hidetake.gradle.ssh.plugin

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Timeout
import spock.lang.Unroll

class AcceptanceSpec extends Specification {

    static final List<String> tasks = []

    // find tasks which starts with `should`
    static {
        def writer = new StringWriter()
        GradleRunner.create()
            .withProjectDir(new File('fixture'))
            .withPluginClasspath()
            .withArguments('-s', ':spec:tasks', '--all')
            .forwardStdOutput(writer)
            .build()
        writer.toString().readLines().findAll {
            it =~ /^should /
        }.each {
            tasks << it
        }
    }

    @Timeout(10)
    @Unroll
    def "spec(#task)"() {
        given:
        def runner = GradleRunner.create()
            .withProjectDir(new File('fixture'))
            .withPluginClasspath()
            .withArguments('-s', ":spec:$task")
            .forwardOutput()

        when:
        runner.build()

        then:
        noExceptionThrown()

        where:
        task << tasks
    }

}
