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
     * Encoding of input and output stream.
     */
    String encoding = null

    /**
     * JSch configuration.
     */
    final config = [:] as Map<String, Object>

    /**
     * Sessions.
     */
    final sessionSpecs = [] as List<SessionSpec>

    /**
     * Adds configuration. For example:
     * <pre>
     * config(StrictHostKeyChecking: 'no')
     * </pre>
     *
     * @param pairs key value pairs of configuration
     */
    void config(Map<String, Object> pairs) {
        assert pairs != null, 'pairs should be set'
        config.putAll(pairs)
    }

    /**
     * Adds a session.
     *
     * @param remote the remote
     * @param operationClosure closure for {@link OperationHandler} (run in execution phase)
     */
    void session(Remote remote, Closure operationClosure) {
        assert remote != null, 'remote should be set'
        assert remote.user != null, "user of remote ${remote.name} should be set"
        assert remote.host != null, "host of remote ${remote.name} should be set"
        assert operationClosure != null, 'operationClosure should be set'
        sessionSpecs.add(new SessionSpec(remote, operationClosure))
    }

    /**
     * Adds sessions.
     *
     * @param remotes collection of {@link Remote}s
     * @param operationClosure closure for {@link OperationHandler} (run in execution phase)
     */
    void session(Collection<Remote> remotes, Closure operationClosure) {
        assert remotes, 'remotes should contain at least one'
        assert operationClosure != null, 'operationClosure should be set'
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
        merged.encoding = specs.collect { it.encoding }.findResult('UTF-8') { it }
        merged
    }
}
