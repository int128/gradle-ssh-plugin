package org.hidetake.gradle.ssh.internal.session

import org.hidetake.gradle.ssh.plugin.OperationSettings
import org.hidetake.gradle.ssh.plugin.operation.Operations
import spock.lang.Specification

class DefaultSessionHandlerSpec extends Specification {

    DefaultSessionHandler defaultSessionHandler
    Operations operations
    OperationSettings operationSettings

    def setup() {
        operations = Mock(Operations)
        operationSettings = OperationSettings.DEFAULT + new OperationSettings(dryRun: true)
        defaultSessionHandler = new DefaultSessionHandler(operations, operationSettings)
    }


    def "invoke a shell"() {
        when:
        defaultSessionHandler.with {
            shell()
        }

        then:
        AssertionError e = thrown()
        e.message.contains('settings')
    }

    def "invoke a shell with options"() {
        when:
        defaultSessionHandler.with {
            shell(logging: false)
        }

        then:
        1 * operations.shell(OperationSettings.DEFAULT + new OperationSettings(logging: false, dryRun: true))
    }

    def "execute a command"() {
        when:
        defaultSessionHandler.with {
            execute('ls -l')
        }

        then:
        1 * operations.execute(OperationSettings.DEFAULT + new OperationSettings(dryRun: true), 'ls -l', null)
    }

    def "execute a command with callback"() {
        given:
        def closure = Mock(Closure)

        when:
        defaultSessionHandler.with {
            execute('ls -l', closure)
        }

        then:
        1 * operations.execute(OperationSettings.DEFAULT + new OperationSettings(dryRun: true), 'ls -l', closure)
    }

    def "execute a command with options"() {
        when:
        defaultSessionHandler.with {
            execute('ls -l', pty: true)
        }

        then:
        1 * operations.execute(OperationSettings.DEFAULT + new OperationSettings(pty: true, dryRun: true), 'ls -l', null)
    }

    def "execute a command with options and callback"() {
        given:
        def closure = Mock(Closure)

        when:
        defaultSessionHandler.with {
            execute('ls -l', pty: true, closure)
        }

        then:
        1 * operations.execute(OperationSettings.DEFAULT + new OperationSettings(pty: true, dryRun: true), 'ls -l', closure)
    }

    def "execute a command in background"() {
        when:
        defaultSessionHandler.with {
            executeBackground('ls -l')
        }

        then:
        1 * operations.executeBackground(OperationSettings.DEFAULT + new OperationSettings(dryRun: true), 'ls -l', null)
    }

    def "execute a command in background with callback"() {
        given:
        def closure = Mock(Closure)

        when:
        defaultSessionHandler.with {
            executeBackground('ls -l', closure)
        }

        then:
        1 * operations.executeBackground(OperationSettings.DEFAULT + new OperationSettings(dryRun: true), 'ls -l', closure)
    }

    def "execute a command with options in background"() {
        when:
        defaultSessionHandler.with {
            executeBackground('ls -l', pty: true)
        }

        then:
        1 * operations.executeBackground(OperationSettings.DEFAULT + new OperationSettings(pty: true, dryRun: true), 'ls -l', null)
    }

    def "execute a command with options and callback in background"() {
        given:
        def closure = Mock(Closure)

        when:
        defaultSessionHandler.with {
            executeBackground('ls -l', pty: true, closure)
        }

        then:
        1 * operations.executeBackground(OperationSettings.DEFAULT + new OperationSettings(pty: true, dryRun: true), 'ls -l', closure)
    }

}
