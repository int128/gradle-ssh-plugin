package org.hidetake.groovy.ssh.core.container

import spock.lang.Specification

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

class ContainerBuilderSpec extends Specification {

    RemoteContainer remotes
    ContainerBuilder builder

    def setup() {
        remotes = new RemoteContainer()
        builder = new ContainerBuilder(remotes)
    }

    def "empty closure should do nothing"() {
        when:
        callWithDelegate({}, builder)

        then:
        remotes.isEmpty()
    }

    def "missing property should cause an error"() {
        given:
        def closure = {
            unknownSymbol
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingPropertyException e = thrown()
        e.property == 'unknownSymbol'
    }

    def "missing method with a closure should create an new object"() {
        given:
        def closure = {
            testServer {
                host = 'someHost'
                user = 'someUser'
            }
        }

        when:
        callWithDelegate(closure, builder)

        then:
        remotes.size() == 1
        remotes.testServer.name == 'testServer'
        remotes.testServer.host == 'someHost'
        remotes.testServer.user == 'someUser'
    }

    def "missing method with a closure should work even in another closure"() {
        given:
        def closure = {
            (1..100).each { id ->
                "testServer$id" {
                    host = 'someHost'
                    user = "user$id"
                }
            }
        }

        when:
        callWithDelegate(closure, builder)

        then:
        remotes.size() == 100
        remotes.testServer50.user == 'user50'
    }

    def "missing method with no arg should cause an error"() {
        given:
        def closure = {
            unknownSymbol()
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingMethodException e = thrown()
        e.method == 'unknownSymbol'
    }

    def "missing method with a wrong arg should cause an error"() {
        given:
        def closure = {
            unknownSymbol(100)
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingMethodException e = thrown()
        e.method == 'unknownSymbol'
    }

    def "missing property in the child closure should cause an error"() {
        given:
        def closure = {
            testServer {
                host = 'someHost'
                user = unknownSymbol
            }
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingPropertyException e = thrown()
        e.property == 'unknownSymbol'
    }

    def "missing method with a closure in the child closure should cause an error"() {
        given:
        def closure = {
            testServer {
                host = 'someHost'
                user = unknownSymbol {
                }
            }
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingMethodException e = thrown()
        e.method == 'unknownSymbol'
    }

    def "missing method with no arg in the child closure should cause an error"() {
        given:
        def closure = {
            testServer {
                host = 'someHost'
                user = unknownSymbol()
            }
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingMethodException e = thrown()
        e.method == 'unknownSymbol'
    }

    def "missing method with a wrong in the child closure arg should cause an error"() {
        given:
        def closure = {
            testServer {
                host = 'someHost'
                user = unknownSymbol(100)
            }
        }

        when:
        callWithDelegate(closure, builder)

        then:
        MissingMethodException e = thrown()
        e.method == 'unknownSymbol'
    }

}
