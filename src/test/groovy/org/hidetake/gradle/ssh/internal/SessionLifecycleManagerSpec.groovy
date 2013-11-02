package org.hidetake.gradle.ssh.internal

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
        def channel = Mock(DefaultCommandContext)

        when:
        mgr << channel

        then:
        mgr.contexts.size() == 1
    }

    def "disconnect disconnects all commands"() {
        given:
        def c1 = new DefaultCommandContext(Mock(ChannelExec))
        def c2 = new DefaultCommandContext(Mock(ChannelExec))

        mgr << c1
        mgr << c2

        when:
        mgr.disconnect()

        then:
        1 * c1.channel.disconnect()
        1 * c2.channel.disconnect()
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
        def context = new DefaultCommandContext(Mock(ChannelExec))
        mgr << context

        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then: 1 * context.channel.closed >> false
        then: 1 * sleepMock.call(_)
        then: 1 * context.channel.closed >> true
        then: 1 * closedChannelHandler.call(context)
        then: 1 * sleepMock.call(_)
    }

    def "wait for pending, one pending and one closed"() {
        given:
        def pending = new DefaultCommandContext(Mock(ChannelExec))
        def closed = new DefaultCommandContext(Mock(ChannelExec))
        mgr << pending
        mgr << closed

        def closedChannelHandler = Mock(Closure)

        when:
        mgr.waitForPending(closedChannelHandler)

        then:
        1 * pending.channel.closed >> false
        1 * closed.channel.closed >> true

        then: 1 * closedChannelHandler.call(closed)
        then: 1 * sleepMock.call(_)
        then: 1 * pending.channel.closed >> true
        then: 1 * closedChannelHandler.call(pending)
        then: 1 * sleepMock.call(_)
    }

}
