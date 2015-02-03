package org.hidetake.groovy.ssh.session

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.OperationSettings
import org.hidetake.groovy.ssh.extension.CoreExtensions
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.operation.SftpOperations

/**
 * A handler of {@link org.hidetake.groovy.ssh.core.RunHandler#session(Remote, groovy.lang.Closure)}.
 *
 * @author hidetake.org
 */
@Slf4j
class SessionHandler implements SessionExtension {
    private final Operations operations

    private final OperationSettings operationSettings

    static def create(Operations operations, OperationSettings operationSettings) {
        log.debug("Extensions: ${operationSettings.extensions}")
        def handler = new SessionHandler(operations, operationSettings)
        handler.withTraits(CoreExtensions).withTraits(operationSettings.extensions as Class[])
    }

    private def SessionHandler(Operations operations1, OperationSettings operationSettings1) {
        operations = operations1
        operationSettings = operationSettings1
    }

    @Override
    Operations getOperations() {
        operations
    }

    @Override
    OperationSettings getOperationSettings() {
        operationSettings
    }

    @Override
    Remote getRemote() {
        operations.remote
    }

    /**
     * Performs a shell operation.
     * This method blocks until channel is closed.
     *
     * @param settings shell settings
     * @return output value of the command
     */
    void shell(HashMap settings) {
        assert settings != null, 'settings must not be null'
        log.info("Execute a shell with settings ($settings)")
        operations.shell(operationSettings + new OperationSettings(settings))
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param command
     * @return output value of the command
     */
    String execute(String command) {
        assert command, 'command must be given'
        log.info("Execute a command ($command)")
        operations.execute(operationSettings, command, null)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param command
     * @param callback closure called with an output value of the command
     * @return output value of the command
     */
    void execute(String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        log.info("Execute a command ($command) with callback")
        operations.execute(operationSettings, command, callback)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @return output value of the command
     */
    String execute(HashMap settings, String command) {
        assert command, 'command must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings)")
        operations.execute(operationSettings + new OperationSettings(settings), command, null)
    }

    /**
     * Performs an execution operation.
     * This method blocks until channel is closed.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     * @return output value of the command
     */
    void execute(HashMap settings, String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings) and callback")
        operations.execute(operationSettings + new OperationSettings(settings), command, callback)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param command
     */
    void executeBackground(String command) {
        assert command, 'command must be given'
        log.info("Execute a command ($command) in background")
        operations.executeBackground(operationSettings, command, null)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeBackground(String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        log.info("Execute a command ($command) with callback in background")
        operations.executeBackground(operationSettings, command, callback)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     */
    void executeBackground(HashMap settings, String command) {
        assert command, 'command must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings) in background")
        operations.executeBackground(operationSettings + new OperationSettings(settings), command, null)
    }

    /**
     * Performs an execution operation.
     * This method returns immediately and executes the command concurrently.
     *
     * @param settings execution settings
     * @param command
     * @param callback closure called with an output value of the command
     */
    void executeBackground(HashMap settings, String command, Closure callback) {
        assert command, 'command must be given'
        assert callback, 'callback must be given'
        assert settings != null, 'settings must not be null'
        log.info("Execute a command ($command) with settings ($settings) and callback in background")
        operations.executeBackground(operationSettings + new OperationSettings(settings), command, callback)
    }

    @Override
    void sftp(@DelegatesTo(SftpOperations) Closure closure) {
        assert closure, 'closure must be given'
        log.info("Execute a SFTP subsystem")
        operations.sftp(closure)
    }
}
