package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges(CommandLifecycleManager)
class CommandLifecycleManagerSpec extends Specification {

    CommandLifecycleManager mgr

    @Shared
    Closure sleepMock

    def setupSpec() {
        CommandLifecycleManager.metaClass.static.sleep = { long ms ->
            sleepMock.call(ms)
        }
    }

    def setup() {
        mgr = new CommandLifecycleManager()
        sleepMock = Mock(Closure)
    }


    def "executionStarted adds command"() {
        given:
        def channel = Mock(CommandContext)

        when:
        mgr << channel

        then:
        mgr.contexts.size() == 1
    }

    def "disconnect disconnects all commands"() {
        given:
        def c1 = new CommandContext(Mock(Channel))
        def c2 = new CommandContext(Mock(Channel))

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
        def context = new CommandContext(Mock(Channel))
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
        def pending = new CommandContext(Mock(Channel))
        def closed = new CommandContext(Mock(Channel))
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
