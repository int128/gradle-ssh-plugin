package org.hidetake.gradle.ssh.api

import org.gradle.api.logging.LogLevel

/**
 * Global SSH settings.
 *
 * @author hidetake.org
 *
 */
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
     * Default settings.
     */
    static final DEFAULT = new SshSettings(
            knownHosts: new File("${System.properties['user.home']}/.ssh/known_hosts"),
            dryRun: false,
            retryCount: 0,
            retryWaitSec: 0,
            outputLogLevel: LogLevel.QUIET,
            errorLogLevel: LogLevel.ERROR,
            encoding: 'UTF-8'
    )

    /**
     * Compute a merged settings.
     * Properties of the right side overrides those of this object.
     *
     * @param right
     * @return a merged one (right side is higher priority)
     */
    SshSettings plus(SshSettings right) {
        def o = new SshSettings()
        o.identity = right.identity ?: identity

        // identity and passphrase
        o.passphrase = right.identity ? right.passphrase : passphrase

        o.knownHosts = right.knownHosts ?: knownHosts
        o.dryRun = right.dryRun ?: dryRun
        o.retryCount = right.retryCount ?: retryCount
        o.retryWaitSec = right.retryWaitSec ?: retryWaitSec
        o.outputLogLevel = right.outputLogLevel ?: outputLogLevel
        o.errorLogLevel = right.errorLogLevel ?: errorLogLevel
        o.encoding = right.encoding ?: encoding
        o
    }
}
