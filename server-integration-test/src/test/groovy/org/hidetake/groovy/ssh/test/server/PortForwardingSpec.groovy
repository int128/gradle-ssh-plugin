package org.hidetake.groovy.ssh.test.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.sshd.SshServer
import org.apache.sshd.common.ForwardingFilter
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Slf4j
class PortForwardingSpec extends Specification {

    Service ssh

    @Shared HttpServer httpServer
    @Shared int httpServerPort

    @Shared SshServer sshServer

    def setupSpec() {
        httpServerPort = SshServerMock.pickUpFreePort()
        httpServer = HttpServer.create(new InetSocketAddress(httpServerPort), 0)
        httpServer.createContext '/', { HttpExchange httpExchange ->
            httpExchange.sendResponseHeaders(200, 0)
            httpExchange.responseBody.close()
        }
        httpServer.start()

        sshServer = SshServerMock.setUpLocalhostServer()
        sshServer.passwordAuthenticator = Mock(PasswordAuthenticator) {
            authenticate("someUser", "somePassword", _) >> true
        }
        sshServer.tcpipForwardingFilter = Mock(ForwardingFilter) {
            canConnect(_, _) >> true
            canListen(_, _) >> true
        }
        sshServer.start()
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert sshServer.activeSessions.empty
        }
        sshServer.stop()
        httpServer.stop(0)
    }

    def setup() {
        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
        ssh.remotes {
            some {
                host = sshServer.host
                port = sshServer.port
                user = 'someUser'
                password = 'somePassword'
            }
        }
    }


    def "HTTP server should response 200"() {
        when:
        def response = new RESTClient("http://localhost:$httpServerPort").get(path: '/') as HttpResponseDecorator

        then:
        response.status == 200
    }


    def "auto allocated local port should be forwarded to the HTTP server"() {
        when:
        def response = ssh.run {
            session(ssh.remotes.some) {
                int port = forwardLocalPort(hostPort: httpServerPort)
                new RESTClient("http://localhost:$port").get(path: '/')
            }
        }

        then:
        response.status == 200
    }

    def "specified local port should be forwarded to the HTTP server"() {
        when:
        def response = ssh.run {
            session(ssh.remotes.some) {
                int port = SshServerMock.pickUpFreePort()
                forwardLocalPort(port: port, hostPort: httpServerPort)
                new RESTClient("http://localhost:$port").get(path: '/')
            }
        }

        then:
        response.status == 200
    }

    def "specified local port should be forwarded to the HTTP server with addresses"() {
        when:
        def response = ssh.run {
            session(ssh.remotes.some) {
                int port = SshServerMock.pickUpFreePort()
                forwardLocalPort(bind: '0.0.0.0', port: port, host: 'localhost', hostPort: httpServerPort)
                new RESTClient("http://localhost:$port").get(path: '/')
            }
        }

        then:
        response.status == 200
    }


    def "specified remote port should be forwarded to the HTTP server"() {
        when:
        def response = ssh.run {
            session(ssh.remotes.some) {
                int port = SshServerMock.pickUpFreePort()
                forwardRemotePort(port: port, hostPort: httpServerPort)
                new RESTClient("http://localhost:$port").get(path: '/')
            }
        }

        then:
        response.status == 200
    }

    def "specified remote port should be forwarded to the HTTP server with addresses"() {
        when:
        def response = ssh.run {
            session(ssh.remotes.some) {
                int port = SshServerMock.pickUpFreePort()
                forwardRemotePort(bind: '0.0.0.0', port: port, host: '0.0.0.0', hostPort: httpServerPort)
                new RESTClient("http://localhost:$port").get(path: '/')
            }
        }

        then:
        response.status == 200
    }


}
