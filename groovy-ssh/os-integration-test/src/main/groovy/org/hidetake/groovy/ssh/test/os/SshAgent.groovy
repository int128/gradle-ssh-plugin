package org.hidetake.groovy.ssh.test.os

import groovy.util.logging.Slf4j
import org.junit.rules.ExternalResource

@Slf4j
class SshAgent extends ExternalResource {

    @Override
    protected void before() {
        removeAll()
    }

    @Override
    protected void after() {
        removeAll()
    }

    void add(String keyPath) {
        ['chmod', '600', keyPath].execute().waitForProcessOutput(System.out, System.err)
        log.info("Adding key to ssh-agent: $keyPath")
        ['ssh-add', keyPath].execute().waitForProcessOutput(System.out, System.err)
    }

    void removeAll() {
        log.info('Remove all keys from ssh-agent')
        ['ssh-add', '-D'].execute().waitForProcessOutput(System.out, System.err)
    }

}
