package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.JSchException
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(DefaultSshService)
class DefaultSshServiceSpec extends Specification {

    @Shared
    Closure sleepMock

    def setupSpec() {
        DefaultSshService.instance.metaClass.static.sleep = { long ms ->
            sleepMock.call(ms)
        }
    }

    def setup() {
        sleepMock = Mock(Closure)
    }


    def "no retrying"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(0, 10, closure)

        then:
        1 * closure.call()
        0 * sleepMock.call(_)
    }

    def "no retrying, exception"() {
        given:
        def closure = Mock(Closure) {
            1 * call() >> { throw new JSchException() }
        }

        when:
        DefaultSshService.instance.retry(0, 10, closure)

        then:
        0 * sleepMock.call(_)
        thrown(JSchException)
    }

    def "retrying once"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(1, 10, closure)

        then:
        1 * closure.call()
        0 * sleepMock.call(_)
    }

    def "retrying once, exception once"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(1, 10, closure)

        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call()
    }

    def "retrying once, exception twice"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(1, 10, closure)

        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call() >> { throw new JSchException() }
        then: thrown(JSchException)
    }

    def "retrying twice"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(2, 10, closure)

        then:
        1 * closure.call()
        0 * sleepMock.call(_)
    }

    def "retrying twice, exception once"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(2, 10, closure)

        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call()
    }

    def "retrying twice, exception twice"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(2, 10, closure)

        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call()
    }

    def "retrying twice, exception 3 times"() {
        given:
        def closure = Mock(Closure)

        when:
        DefaultSshService.instance.retry(2, 10, closure)

        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call() >> { throw new JSchException() }
        then: 1 * sleepMock.call(10000)
        then: 1 * closure.call() >> { throw new JSchException() }
        then: thrown(JSchException)
    }

}
