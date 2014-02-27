package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.api.OperationHandler
import org.hidetake.gradle.ssh.api.Remote
import org.hidetake.gradle.ssh.api.SessionSpec
import spock.lang.Specification

class GatewaySessionTransformationSpec extends Specification {

    def "empty list"() {
        given:
        def specs = []

        when:
        def transformed = GatewaySessionTransformation.transform(specs)

        then:
        transformed == specs
    }

    def "sessions without any gateway"() {
        given:
        def sessionA = new SessionSpec(new Remote('A', 'userA', 1, 'localhost'), {})
        def sessionB = new SessionSpec(new Remote('B', 'userB', 2, 'localhost'), {})
        def specs = [sessionA, sessionB]

        when:
        def transformed = GatewaySessionTransformation.transform(specs)

        then:
        transformed == specs
    }

    def "one hop gateway (client -> G -> T)"() {
        given:
        def remoteG = new Remote('G', 'userG', 1, 'gateway')
        def remoteT = new Remote('T', 'userT', 2, 'target', remoteG)
        def operationsT = { /* operations on the target host */ }

        def anotherSession = new SessionSpec(remoteG, {})
        def targetSession = new SessionSpec(remoteT, operationsT)
        def specs = [anotherSession, targetSession]

        def operationClosureDelegate = Mock(OperationHandler)

        when:
        def transformed = GatewaySessionTransformation.transform(specs)

        then: 'another session should be kept'
        transformed[0] == anotherSession

        and: 'gateway session (client -> G) should be inserted'
        transformed[1].remote == remoteG

        and: 'original session should be kept except host and port'
        transformed[2].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == GatewaySessionTransformation.PORT_TBD
            remote.name == remoteT.name
            remote.user == remoteT.user
            operationClosure == operationsT
        }

        when: 'invokes gateway session (client -> G)'
        transformed[1].with {
            operationClosure.delegate = operationClosureDelegate
            operationClosure.call()
        }

        then:
        1 * operationClosureDelegate.forwardLocalPortTo(remoteT.host, remoteT.port) >> 777

        then:
        transformed[2].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == 777
            remote.name == remoteT.name
            remote.user == remoteT.user
            operationClosure == operationsT
        }
    }

    /**
     * 2 hop gateways.
     *
     * Situation:
     * <ul>
     *     <li>Client can reach directly to F but not to G and T.
     *     <li>F can reach directly to G but not to T.
     *     <li>G can reach directly to T.
     * </ul>
     * Solution:
     * <ul>
     *     <li>Client connects to F and invokes port forwarder #1.
     *     <li>Client connects to G with port forwarder #1, and invokes port forwarder #2.
     *     <li>Client connects to T with port forwarder #2.
     * </ul>
     */
    def "2 hop gateways (client -> F -> G -> T)"() {
        given:
        def remoteF = new Remote('F', 'userF', 1, 'front')
        def remoteG = new Remote('G', 'userG', 2, 'gateway', remoteF)
        def remoteT = new Remote('T', 'userT', 3, 'target', remoteG)
        def operationsT = { /* operations on the target host */ }

        def anotherSession = new SessionSpec(remoteF, {})
        def targetSession = new SessionSpec(remoteT, operationsT)
        def specs = [anotherSession, targetSession]

        def operationClosureDelegate = Mock(OperationHandler)

        when:
        def transformed = GatewaySessionTransformation.transform(specs)

        then: 'another session should be kept'
        transformed[0] == anotherSession

        and: 'gateway session (client -> F) should be inserted'
        transformed[1].remote == remoteF

        and: 'gateway session (client -> F -> G) should be inserted'
        transformed[2].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == GatewaySessionTransformation.PORT_TBD
            remote.name == remoteG.name
            remote.user == remoteG.user
        }

        and: 'original session should be kept except host and port'
        transformed[3].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == GatewaySessionTransformation.PORT_TBD
            remote.name == remoteT.name
            remote.user == remoteT.user
            operationClosure == operationsT
        }

        when: 'invokes gateway session (client -> F -> G)'
        transformed[1].with {
            operationClosure.delegate = operationClosureDelegate
            operationClosure.call()
        }

        then:
        1 * operationClosureDelegate.forwardLocalPortTo(remoteG.host, remoteG.port) >> 777

        then:
        transformed[2].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == 777
            remote.name == remoteG.name
            remote.user == remoteG.user
        }

        when: 'invokes gateway session (client -> F -> G -> T)'
        transformed[2].with {
            operationClosure.delegate = operationClosureDelegate
            operationClosure.call()
        }

        then:
        1 * operationClosureDelegate.forwardLocalPortTo(remoteT.host, remoteT.port) >> 888

        then:
        transformed[3].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == 888
            remote.name == remoteT.name
            remote.user == remoteT.user
            operationClosure == operationsT
        }
    }

}
