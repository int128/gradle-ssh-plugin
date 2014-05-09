package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.internal.DefaultSshTaskHandler;
import org.hidetake.gradle.ssh.internal.connection.ConnectionManager
import org.hidetake.gradle.ssh.internal.connection.ConnectionService
import org.hidetake.gradle.ssh.internal.session.SessionService
import org.hidetake.gradle.ssh.plugin.CompositeSettings
import org.hidetake.gradle.ssh.plugin.ConnectionSettings
import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.Proxy
import org.hidetake.gradle.ssh.plugin.Remote
import org.hidetake.gradle.ssh.plugin.session.SessionHandler

import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class DefaultSshTaskHandlerSpec extends Specification {

    DefaultSshTaskHandler sshTaskDelegate

    def setup() {
        sshTaskDelegate = new DefaultSshTaskHandler()
    }

    def "add a session"() {
        given:
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'
        def operationClosure = { assert false }

        when:
        sshTaskDelegate.session(remote, operationClosure)

        then:
        noExceptionThrown()
    }

    def "add a session with null remote throws assertion error"() {
        given:
        def operationClosure = { assert false }

        when:
        sshTaskDelegate.session(null as Remote, operationClosure)

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
        sshTaskDelegate.session(remote, theOperationClosure)

        then:
        AssertionError e = thrown()
        e.message.contains(errorContains)

        where:
        theHost          | theOperationClosure | errorContains
        null             | { assert false }    | "host"
        "www.myhost.com" | null                | "closure"
    }



    def "add session for multiple remotes"() {
        given:
        def remote1 = new Remote('myRemote1')
        remote1.user = 'myUser1'
        remote1.host = 'myHost1'
        def remote2 = new Remote('myRemote2')
        remote2.user = 'myUser2'
        remote2.host = 'myHost2'
        def closure = { assert false }

        when:
        sshTaskDelegate.session([remote1, remote2], closure)

        then:
        noExceptionThrown()
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
        sshTaskDelegate.session(remote, closure)

        then:
        noExceptionThrown()
    }


    
    def "add session for empty remotes throws assertion error"() {
        given:
        def closure = { assert false }

        when:
        sshTaskDelegate.session([], closure)

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
        sshTaskDelegate.session([remote], null)

        then:
        AssertionError ex2 = thrown()
        ex2.message.contains("closure")
    }


    def "add a session with remote properties"() {
        when:
        sshTaskDelegate.session(host: 'myHost', user: 'myUser') {
            assert false
        }

        then:
        noExceptionThrown()
    }

    def "add a session with remote properties and null closure throws an error"() {
        when:
        sshTaskDelegate.session(host: 'myHost', user: 'myUser', null)

        then:
        AssertionError ex = thrown()
        ex.message.contains("closure")
    }

    def "add a session with remote properties but without host throws an error"() {
        when:
        sshTaskDelegate.session(user: 'myUser', null)

        then:
        AssertionError ex = thrown()
        ex.message.contains("host")
    }


    @ConfineMetaClassChanges([ConnectionService, SessionService])
    def "execute sessions"() {
        given:
        def remote1 = new Remote('myRemote1')
        remote1.user = 'myUser1'
        remote1.host = 'myHost1'
        def remote2 = new Remote('myRemote2')
        remote2.user = 'myUser2'
        remote2.host = 'myHost2'
        def closure = {
            execute 'something'
        }

        def connectionService = Mock(ConnectionService) {
            1 * withManager(ConnectionSettings.DEFAULT, _) >> { ignore, Closure x ->
                x.call(Mock(ConnectionManager))
            }
        }
        ConnectionService.metaClass.static.getInstance = { -> connectionService }

        def sessionService = Mock(SessionService)
        SessionService.metaClass.static.getInstance = { -> sessionService }

        def sessionHandler1 = Mock(SessionHandler)
        def sessionHandler2 = Mock(SessionHandler)

        when:
        sshTaskDelegate.session([remote1, remote2], closure)
        def result = sshTaskDelegate.execute(new CompositeSettings())

        then: 1 * sessionService.createDelegate(remote1, OperationSettings.DEFAULT, _) >> sessionHandler1
        then: 1 * sessionService.createDelegate(remote2, OperationSettings.DEFAULT, _) >> sessionHandler2

        then: 1 * sessionHandler1.execute('something') >> 'result1'
        then: 1 * sessionHandler2.execute('something') >> 'result2'

        then: result == 'result2'
    }

}
