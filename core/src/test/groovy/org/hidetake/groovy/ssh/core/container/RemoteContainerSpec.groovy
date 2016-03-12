package org.hidetake.groovy.ssh.core.container

import org.hidetake.groovy.ssh.core.Remote
import spock.lang.Specification
import spock.lang.Unroll

class RemoteContainerSpec extends Specification {

    RemoteContainer remotes

    def setup() {
        remotes = new RemoteContainer()
    }

    def "add() should add an item"() {
        when:
        remotes.add(createRemote('remote1', 'roleA'))

        then:
        remotes.size() == 1
        remotes.firstKey() == 'remote1'
    }

    def "addAll() should add items"() {
        when:
        remotes.addAll([
                createRemote('remote1', 'roleA'),
                createRemote('remote2', 'roleA'),
        ])

        then:
        remotes.size() == 2
        remotes.keySet() == ['remote1', 'remote2'].toSet()
    }

    def "create() should create and add an item"() {
        when:
        remotes.create('remote1') {
            host = 'host1'
        }

        then:
        remotes.size() == 1
        remotes.firstKey() == 'remote1'
        remotes.firstEntry().value.host == 'host1'
    }

    def "role() should return empty collection if empty one is given"() {
        when:
        def associated = remotes.role('something')

        then:
        associated instanceof Collection<Remote>
        associated.empty
    }

    @Unroll
    def "role() should filter remotes by #roles"() {
        given:
        remotes.addAll([
                createRemote('remote1', 'roleA'),
                createRemote('remote2', 'roleA', 'roleB'),
                createRemote('remote3', 'roleB'),
                createRemote('remote4', 'roleC'),
                createRemote('remote5'),
        ])

        when:
        def actualRemoteNames = remotes.role(*roles)*.name

        then:
        actualRemoteNames.toSet() == expectedRemoteNames.toSet()

        where:
        roles                                | expectedRemoteNames
        ['roleA']                            | ['remote1', 'remote2']
        ['roleB']                            | ['remote2', 'remote3']
        ['roleC']                            | ['remote4']
        ['roleD']                            | []
        ['roleA', 'roleB']                   | ['remote1', 'remote2', 'remote3']
        ['roleB', 'roleC']                   | ['remote2', 'remote3', 'remote4']
        ['roleA', 'roleC']                   | ['remote1', 'remote2', 'remote4']
        ['roleA', 'roleD']                   | ['remote1', 'remote2']
        ['roleA', 'roleB', 'roleC']          | ['remote1', 'remote2', 'remote3', 'remote4']
        ['roleA', 'roleB', 'roleD']          | ['remote1', 'remote2', 'remote3']
        ['roleA', 'roleB', 'roleC', 'roleD'] | ['remote1', 'remote2', 'remote3', 'remote4']
    }

    def "role() should throw error if null is given"() {
        when:
        remotes.role(null)

        then:
        AssertionError e = thrown()
        e.message.contains('role')
    }

    def "role() should throw error if no argument is given"() {
        when:
        remotes.role()

        then:
        AssertionError e = thrown()
        e.message.contains('role')
    }

    @Unroll
    def "allRoles() should filter remotes by #roles"() {
        given:
        remotes.add(createRemote('remote1', 'roleA'))
        remotes.add(createRemote('remote2', 'roleA', 'roleB'))
        remotes.add(createRemote('remote3', 'roleB'))
        remotes.add(createRemote('remote4', 'roleC', 'roleD'))
        remotes.add(createRemote('remote5', 'roleA', 'roleB', 'roleC'))
        remotes.add(createRemote('remote6'))

        when:
        def actualRemoteNames = remotes.allRoles(*roles)*.name

        then:
        actualRemoteNames.toSet() == expectedRemoteNames.toSet()

        where:
        roles                                | expectedRemoteNames
        ['roleA']                            | ['remote1', 'remote2', 'remote5']
        ['roleB']                            | ['remote2', 'remote3', 'remote5']
        ['roleC']                            | ['remote4', 'remote5']
        ['roleD']                            | ['remote4']
        ['roleA', 'roleB']                   | ['remote2', 'remote5']
        ['roleB', 'roleC']                   | ['remote5']
        ['roleA', 'roleC']                   | ['remote5']
        ['roleA', 'roleD']                   | []
        ['roleA', 'roleB', 'roleC']          | ['remote5']
        ['roleA', 'roleB', 'roleD']          | []
        ['roleA', 'roleB', 'roleC', 'roleD'] | []
    }

    def "allRoles() should throw error if null is given"() {
        when:
        remotes.allRoles(null)

        then:
        AssertionError e = thrown()
        e.message.contains('role')
    }

    def "allRoles() should throw error if no argument is given"() {
        when:
        remotes.allRoles()

        then:
        AssertionError e = thrown()
        e.message.contains('role')
    }

    private static createRemote(String name, String... roles) {
        def remote = new Remote(name)
        remote.roles.addAll(roles)
        remote
    }

}
