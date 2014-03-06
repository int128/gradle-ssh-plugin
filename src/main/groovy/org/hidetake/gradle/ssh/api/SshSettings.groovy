package org.hidetake.gradle.ssh.api

import groovy.util.logging.Slf4j
import org.gradle.api.logging.LogLevel

/**
 * Global SSH settings.
 *
 * @author hidetake.org
 *
 */
@Slf4j
class SshSettings {
    static final allowAnyHosts = new File(UUID.randomUUID().toString())

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
     * Known hosts file.
     * If {@link #allowAnyHosts}, strict host key checking is turned off.
     */
    File knownHosts = null

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
     * Computes merged settings.
     *
     * @param specs list of {@link SshSettings}s in priority order (first item is highest priority)
     * @return merged one
     */
    static SshSettings computeMerged(SshSettings... specs) {
        def merged = new SshSettings()

        specs.findResult { spec ->
            spec.identity ? [identity: spec.identity, passphrase: spec.passphrase] : null
        }?.with { Map map ->
            merged.identity = map.identity
            merged.passphrase = map.passphrase
        }

        merged.knownHosts = specs.findResult(
                new File("${System.properties['user.home']}/.ssh/known_hosts")) { it.knownHosts } as File
        merged.dryRun = specs.findResult(false) { it.dryRun } as boolean
        merged.retryCount = specs.findResult(0) { it.retryCount } as int
        merged.retryWaitSec = specs.findResult(0) { it.retryWaitSec } as int
        merged.outputLogLevel = specs.findResult(LogLevel.QUIET) { it.outputLogLevel } as LogLevel
        merged.errorLogLevel = specs.findResult(LogLevel.ERROR) { it.errorLogLevel } as LogLevel
        merged.encoding = specs.findResult('UTF-8') { it.encoding } as String
        merged
    }
}
