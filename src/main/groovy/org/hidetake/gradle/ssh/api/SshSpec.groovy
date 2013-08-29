package org.hidetake.gradle.ssh.api

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

/**
 * Specification of a SSH task.
 *
 * @author hidetake.org
 *
 */
class SshSpec {
    /**
     * Dry-run flag.
     * If <code>true</code>, performs no action.
     */
    Boolean dryRun = null

    /**
     * Retry count for connecting to a host.
     */
    Integer retryCount = null

    /**
     * Interval time in seconds between retries.
     */
    Integer retryWaitSec = null

    /**
     * Logger.
     */
    Logger logger = null

    /**
     * Log level for standard output of commands.
     */
    LogLevel outputLogLevel = null

    /**
     * Log level for standard error of commands.
     */
    LogLevel errorLogLevel = null

    /**
     * JSch configuration.
     */
    final Map config = [:]

    /**
     * Sessions.
     */
    final List<SessionSpec> sessionSpecs = []

    /**
     * Adds configuration. For example:
     * <pre>
     * config(StrictHostKeyChecking: 'no')
     * </pre>
     *
     * @param pairs key value pairs of configuration
     */
    void config(Map pairs) {
        assert pairs != null, 'config map should not be null'
        config.putAll(pairs)
    }

    /**
     * Adds a session.
     *
     * @param remote the remote
     * @param operationClosure closure for {@link OperationSpec} (run in execution phase)
     */
    void session(Remote remote, Closure operationClosure) {
        assert remote != null, 'remote should not be null'
        assert remote.user != null, "user name of remote ${remote.name} should not be null"
        assert remote.host != null, "host name of remote ${remote.name} should not be null"
        assert operationClosure != null, 'operation closure should not be null'
        sessionSpecs.add(new SessionSpec(remote, operationClosure))
    }

    /**
     * Adds sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param operationClosure closure for {@link OperationSpec} (run in execution phase)
     */
    void session(Collection<Remote> remotes, Closure operationClosure) {
        assert remotes, 'remotes should be set and contain atleast one remote'
        assert operationClosure != null, 'operation closure should not be null'
        remotes.each { Remote remote -> session(remote, operationClosure) }
    }

    /**
     * Computes merged settings.
     *
     * @param specs list of {@link SshSpec}s in priority order (first item is highest priority)
     * @return merged one
     */
    static SshSpec computeMerged(SshSpec... specs) {
        def merged = new SshSpec()
        specs.reverse().each { spec ->
            merged.config.putAll(spec.config)
            merged.sessionSpecs.addAll(spec.sessionSpecs)
        }
        merged.dryRun = specs.collect { it.dryRun }.findResult(false) { it }
        merged.retryCount = specs.collect { it.retryCount }.findResult(0) { it }
        merged.retryWaitSec = specs.collect { it.retryWaitSec }.findResult(0) { it }
        merged.logger = specs.collect { it.logger }.find()
        merged.outputLogLevel = specs.collect { it.outputLogLevel }.findResult(LogLevel.QUIET) { it }
        merged.errorLogLevel = specs.collect { it.errorLogLevel }.findResult(LogLevel.ERROR) { it }
        merged
    }
}
