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
            remote.port == 0
            remote.name == remoteT.name
            remote.user == remoteT.user
            operationClosure == operationsT
        }

        when:
        transformed[1].with {
            operationClosure.delegate = operationClosureDelegate
            operationClosure.call()
        }

        then:
        1 * operationClosureDelegate.forwardLocalPortTo(remoteT.host, remoteT.port) >> 777

        then: 'gateway session should rewrite port to assigned one'
        transformed[2].with {
            remote.host == GatewaySessionTransformation.LOCALHOST
            remote.port == 777
            remote.name == remoteT.name
            remote.user == remoteT.user
            operationClosure == operationsT
        }
    }

}
