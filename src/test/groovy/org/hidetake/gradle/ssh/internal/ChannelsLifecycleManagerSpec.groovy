package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.Channel
import spock.lang.Specification

class ChannelsLifecycleManagerSpec extends Specification {

    ChannelsLifecycleManager mgr
    ExitStatusValidator validatorMock


    def setupSpec() {
        ChannelsLifecycleManager.metaClass.static.sleep = { long ms ->
            println "sleep mock"
        }
    }

    def teardownSpec() {
        ChannelsLifecycleManager.metaClass.static.sleep = null
    }


    def setup() {
        mgr = new ChannelsLifecycleManager()
        validatorMock = Mock(ExitStatusValidator)
    }


    def "unmanagedChannelConnected adds channel"() {
        given:
        def channel = Mock(Channel)

        when:
        mgr.unmanagedChannelConnected(channel, null)

        then:
        mgr.channels.size() == 1
    }

    def "disconnect disconnects all managed channels"() {
        given:
        def ch1 = Mock(Channel)
        def ch2 = Mock(Channel)
        mgr.channels.addAll([ch1, ch2])

        when:
        mgr.disconnect()

        then:
        1 * ch1.disconnect()
        1 * ch2.disconnect()
    }



    def "wait for pending with no channels does nothing"() {
        when:
        mgr.waitForPending(validatorMock)

        then:
        0 * validatorMock.channelClosed(_)
    }

    def "wait for pending, one pending channel that closes"() {
        given:
        def channel = Mock(Channel)
        mgr.unmanagedChannelConnected(channel, null)

        2 * channel.closed >> false
        1 * channel.closed >> true

        when:
        mgr.waitForPending(validatorMock)

        then:
        0 * validatorMock.channelClosed(channel)
        0 * channel.disconnect()
    }

    def "wait for pending, one pending and one closed"() {
        given:
        def pending = Mock(Channel)
        def closed = Mock(Channel)
        mgr.unmanagedChannelConnected(pending, null)
        mgr.unmanagedChannelConnected(closed, null)

        2 * pending.closed >> false
        1 * pending.closed >> true
        2 * closed.closed >> true
        1 * closed.exitStatus >> 1


        when:
        mgr.waitForPending(validatorMock)

        then:
        1 * validatorMock.channelClosed(closed)
        1 * closed.disconnect()
    }

}
