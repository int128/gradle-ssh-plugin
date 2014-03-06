package org.hidetake.gradle.ssh.internal.session

import com.jcraft.jsch.Channel
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(ChannelManager)
class ChannelManagerSpec extends Specification {

    ChannelManager mgr

    @Shared
    Closure sleepMock

    def setupSpec() {
        ChannelManager.metaClass.static.sleep = { long ms ->
            sleepMock.call(ms)
        }
    }

    def setup() {
        mgr = new ChannelManager()
        sleepMock = Mock(Closure)
    }


    def "executionStarted adds command"() {
        given:
        def c = Mock(Channel)

        when:
        mgr.add(c)

        then:
        mgr.channels.size() == 1
    }

    def "disconnect disconnects all commands"() {
        given:
        def c1 = Mock(Channel)
        def c2 = Mock(Channel)

        mgr.add(c1)
        mgr.add(c2)

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
        mgr.add(c)

        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then: 1 * c.closed >> false
        then: 1 * sleepMock.call(_)
        then: 1 * c.closed >> true
        then: 1 * closedChannelHandler.call(c)
        then: 1 * sleepMock.call(_)
    }

    def "wait for pending, one pending and one closed"() {
        given:
        def pending = Mock(Channel)
        def closed = Mock(Channel)
        mgr.add(pending)
        mgr.add(closed)

        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then:
        1 * pending.closed >> false
        1 * closed.closed >> true

        then: 1 * closedChannelHandler.call(closed)
        then: 1 * sleepMock.call(_)
        then: 1 * pending.closed >> true
        then: 1 * closedChannelHandler.call(pending)
        then: 1 * sleepMock.call(_)
    }

}
