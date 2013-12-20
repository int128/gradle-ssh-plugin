package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(SessionLifecycleManager)
class SessionLifecycleManagerSpec extends Specification {

    SessionLifecycleManager mgr

    @Shared
    Closure sleepMock

    def setupSpec() {
        SessionLifecycleManager.metaClass.static.sleep = { long ms ->
            sleepMock.call(ms)
        }
    }

    def setup() {
        mgr = new SessionLifecycleManager()
        sleepMock = Mock(Closure)
    }


    def "executionStarted adds command"() {
        given:
        def o = Mock(ChannelObservable)

        when:
        mgr << o

        then:
        mgr.contexts.size() == 1
    }

    def "disconnect disconnects all commands"() {
        given:
        def c1 = Mock(Channel)
        def c2 = Mock(Channel)
        def o1 = Mock(ChannelObservable) { getChannel() >> c1 }
        def o2 = Mock(ChannelObservable) { getChannel() >> c2 }

        mgr << o1
        mgr << o2

        when:
        mgr.disconnect()

        then:
        1 * c1.disconnect()
        1 * c2.disconnect()
    }



    def "wait for pending with no channels does nothing"() {
        given:
        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then:
        0 * closedChannelHandler.call(_)
        0 * sleepMock.call(_)
    }

    def "wait for pending, one pending channel that closes"() {
        given:
        def c = Mock(Channel)
        def o = Mock(ChannelObservable) { getChannel() >> c }
        mgr << o

        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then: 1 * c.closed >> false
        then: 1 * sleepMock.call(_)
        then: 1 * c.closed >> true
        then: 1 * closedChannelHandler.call(o)
        then: 1 * sleepMock.call(_)
    }

    def "wait for pending, one pending and one closed"() {
        given:
        def pending = Mock(Channel)
        def closed = Mock(Channel)
        def pendingObservable = Mock(ChannelObservable) { getChannel() >> pending }
        def closedObservable = Mock(ChannelObservable) { getChannel() >> closed }
        mgr << pendingObservable
        mgr << closedObservable

        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then:
        1 * pending.closed >> false
        1 * closed.closed >> true

        then: 1 * closedChannelHandler.call(closedObservable)
        then: 1 * sleepMock.call(_)
        then: 1 * pending.closed >> true
        then: 1 * closedChannelHandler.call(pendingObservable)
        then: 1 * sleepMock.call(_)
    }

}
