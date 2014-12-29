package org.hidetake.groovy.ssh.core

import org.hidetake.groovy.ssh.api.Proxy
import org.hidetake.groovy.ssh.api.Remote
import org.hidetake.groovy.ssh.internal.session.Plan
import spock.lang.Specification
import spock.lang.Unroll

class RunHandlerSpec extends Specification {

    RunHandler runHandler

    def setup() {
        runHandler = new RunHandler()
    }

    def "add a session"() {
        given:
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'
        def closure = { assert false }

        when:
        runHandler.session(remote, closure)

        then:
        runHandler.sessions == [new Plan(remote, closure)]
    }

    def "add a session with null remote throws assertion error"() {
        given:
        def closure = { assert false }

        when:
        runHandler.session(null as Remote, closure)

        then:
        AssertionError e = thrown()
        e.message.contains('remote')
    }

    @Unroll
    def "add session with invalid params throws assertion error"() {
        given:
        def remote = new Remote('myRemote')
        remote.host = theHost

        when:
        runHandler.session(remote, theClosure)

        then:
        AssertionError e = thrown()
        e.message.contains(errorContains)

        where:
        theHost          | theClosure          | errorContains
        null             | { assert false }    | "host"
        "www.myhost.com" | null                | "closure"
    }



    def "add session for multiple remotes by list"() {
        given:
        def remote1 = new Remote('myRemote1')
        remote1.user = 'myUser1'
        remote1.host = 'myHost1'
        def remote2 = new Remote('myRemote2')
        remote2.user = 'myUser2'
        remote2.host = 'myHost2'
        def closure = { assert false }

        when:
        runHandler.session([remote1, remote2], closure)

        then:
        runHandler.sessions == [new Plan(remote1, closure), new Plan(remote2, closure)]
    }

    def "add session for multiple remotes by var args"() {
        given:
        def remote1 = new Remote('myRemote1')
        remote1.user = 'myUser1'
        remote1.host = 'myHost1'
        def remote2 = new Remote('myRemote2')
        remote2.user = 'myUser2'
        remote2.host = 'myHost2'
        def closure = { assert false }

        when:
        runHandler.session(remote1, remote2, closure)

        then:
        runHandler.sessions == [new Plan(remote1, closure), new Plan(remote2, closure)]
    }

    def "add session for remote with proxy"() {
        given:
        def proxy = new Proxy('myProxy')
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'
        remote.proxy = proxy
        def closure = { assert false }

        when:
        runHandler.session(remote, closure)

        then:
        runHandler.sessions == [new Plan(remote, closure)]
    }

    def "add session for empty remotes throws assertion error"() {
        given:
        def closure = { assert false }

        when:
        runHandler.session([], closure)

        then:
        AssertionError ex = thrown()
        ex.message.contains("remotes")
    }

    def "add session for multiple remotes with null closure throws assertion error"() {
        given:
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'

        when:
        runHandler.session([remote], null)

        then:
        AssertionError ex2 = thrown()
        ex2.message.contains("closure")
    }


    def "add a session with remote properties"() {
        given:
        def closure = { assert false }

        when:
        runHandler.session(host: 'myHost', user: 'myUser', closure)

        then:
        runHandler.sessions.size() == 1
        runHandler.sessions.first().remote.host == 'myHost'
        runHandler.sessions.first().remote.user == 'myUser'
        runHandler.sessions.first().closure == closure
    }

    def "add a session with remote properties and null closure throws an error"() {
        when:
        runHandler.session(host: 'myHost', user: 'myUser', null)

        then:
        AssertionError ex = thrown()
        ex.message.contains("closure")
    }

    def "add a session with remote properties but without host throws an error"() {
        when:
        runHandler.session(user: 'myUser', null)

        then:
        AssertionError ex = thrown()
        ex.message.contains("host")
    }


    def "session() with wrong arguments causes an error"() {
        given:
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'

        when:
        runHandler.session(remote)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('arguments')
    }

}
