package org.hidetake.groovy.ssh.connection

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.PlusProperties
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for establishing the SSH connection.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
class ConnectionSettings implements PlusProperties<ConnectionSettings>, ToStringProperties {
    /**
     * Remote user.
     */
    String user

    /**
     * Password.
     * Leave as null if the password authentication is not needed.
     */
    String password

    /**
     * Hides credential from result of {@link #toString()}.
     */
    final toString__password() { '...' }

    /**
     * Identity key file for public-key authentication.
     * This must be a {@link File}, {@link String} or null.
     * Leave as null if the public key authentication is not needed.
     */
    def identity

    /**
     * {@link #toString()} formatter to hide credential.
     */
    final toString__identity() { identity instanceof File ? identity : '...' }

    /**
     * Pass-phrase for the identity key.
     * This may be null.
     */
    String passphrase

    /**
     * Hides credential from result of {@link #toString()}.
     */
    final toString__passphrase() { '...' }

    final plus__passphrase(right) {
        if (right.identity == null) {
            if (identity == null) {
                null
            } else {
                passphrase
            }
        } else {
            right.passphrase
        }
    }

    /**
     * Gateway host.
     * This may be null.
     */
    Remote gateway

    /**
     * Proxy configuration for connecting to a host.
     * This may be null.
     */
    Proxy proxy

    /**
     * Use agent flag.
     * If <code>true</code>, Putty Agent or ssh-agent will be used to authenticate.
     */
    Boolean agent

    /**
     * Known hosts file.
     * If {@link #allowAnyHosts} is set, strict host key checking is turned off.
     */
    File knownHosts

    /**
     * Retry count for connecting to a host.
     */
    Integer retryCount

    /**
     * Interval time in seconds between retries.
     */
    Integer retryWaitSec

    /**
     * Interval time in seconds between keep-alive packets.
     */
    Integer keepAliveSec


    /**
     * Represents that strict host key checking is turned off and any host is allowed.
     * @see ConnectionSettings#knownHosts
     */
    final File allowAnyHosts = Constants.allowAnyHosts

    /**
     * Hides constant from result of {@link #toString()}.
     */
    final toString__allowAnyHosts() {}

    /**
     * Excludes allowAnyHosts from properties to plus.
     */
    final plus__allowAnyHosts() {}

    static class Constants {
        static final allowAnyHosts = new File("${ConnectionSettings.class.name}#allowAnyHosts")
    }


    static final DEFAULT = new ConnectionSettings(
            user: null,
            password: null,
            identity: null,
            passphrase: null,
            gateway: null,
            proxy: null,
            agent: false,
            knownHosts: new File("${System.properties['user.home']}/.ssh/known_hosts"),
            retryCount: 0,
            retryWaitSec: 0,
            keepAliveSec: 60,
    )
}
