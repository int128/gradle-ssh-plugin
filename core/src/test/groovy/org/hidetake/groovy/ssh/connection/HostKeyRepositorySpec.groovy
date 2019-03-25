package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.HostKey
import spock.lang.Specification
import spock.lang.Unroll

class HostKeyRepositorySpec extends Specification {

    @Unroll
    def "comparing of JSch HostKey and raw host and port"() {
        given:
        def hostKey = new HostKey(jhost, HostKey.SSHRSA ,null)

        expect:
        HostKeyRepository.compare( hostKey, host, port ) == expected

        where:
        jhost                                           | host          | port || expected
        'simple'                                        | 'simple'      | 22   || true
        'notsimple'                                     | 'simple'      | 22   || false
        '[simple]:4321'                                 | 'simple'      | 4321 || true
        'simple,[notsimple]:4321'                       | 'simple'      | 22   || true
        'simple,[notsimple]:4321'                       | 'simple'      | 4321 || false
        'simple,[notsimple]:4321'                       | 'notsimple'   | 4321 || true
        'simple,[notsimple]:4321,more'                  | 'more'        | 22   || true
        'simple,[notsimple]:4321,more'                  | 'notsimple'   | 4321 || true

        '|1|c2FsdA==|Ip5vCJkMOpZGFriXFS4Jiw1khnY='      | 'hashme'      | 22   || true
        '|1|c2FsdA==|Ip5vCJkMOpZGFriXFS4Jiw1khnY='      | 'hashme'      | 4321 || false
        '|1|c2FsdHk=|4/+CSFSbcjz5uEJcO5B77hM70RM='      | 'hashmeport'  | 4321 || true
        '|1|c2FsdHk=|4/+CSFSbcjz5uEJcO5B77hM70RM='      | 'hashmeport'  | 22   || false
    }

}
