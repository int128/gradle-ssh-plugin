package org.hidetake.gradle.ssh.api

import org.gradle.api.logging.LogLevel

/**
 * Specification of a SSH task.
 *
 * @author hidetake.org
 *
 */
class SshSpec {
    /**
     * Identity key file for public-key authentication.
     */
    File identity = null

    /**
     * Pass-phrase for the identity key.
     * This may be null.
     */
    String passphrase = null

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

        specs.findResult { spec ->
            spec.identity ? [identity: spec.identity, passphrase: spec.passphrase] : null
        }?.with { Map map ->
            merged.identity = map.identity
            merged.passphrase = map.passphrase
        }

        merged.dryRun = specs.findResult(false) { it.dryRun } as boolean
        merged.retryCount = specs.findResult(0) { it.retryCount } as int
        merged.retryWaitSec = specs.findResult(0) { it.retryWaitSec } as int
        merged.outputLogLevel = specs.findResult(LogLevel.QUIET) { it.outputLogLevel } as LogLevel
        merged.errorLogLevel = specs.findResult(LogLevel.ERROR) { it.errorLogLevel } as LogLevel
        merged.encoding = specs.findResult('UTF-8') { it.encoding } as String
        merged
    }
}
