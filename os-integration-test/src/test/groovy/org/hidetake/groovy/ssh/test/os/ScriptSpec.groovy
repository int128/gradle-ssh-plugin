package org.hidetake.groovy.ssh.test.os

import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Specification

import static org.hidetake.groovy.ssh.test.os.Fixture.createRemotes

class ScriptSpec extends Specification {

    Service ssh

    def setup() {
        ssh = Ssh.newService()
        createRemotes(ssh)
    }

    def 'should execute a script'() {
        when:
        def actual = ssh.run {
            session(ssh.remotes.Default) {
                executeScript '''#!/bin/sh -xe
echo 1
echo 2
'''
            }
        }

        then:
        actual == "1${Utilities.eol()}2"
    }

}
