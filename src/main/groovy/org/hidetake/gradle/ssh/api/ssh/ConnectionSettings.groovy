package org.hidetake.gradle.ssh.api.ssh

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hidetake.gradle.ssh.api.Settings

@EqualsAndHashCode
@ToString
class ConnectionSettings extends Settings<ConnectionSettings> {
    static final allowAnyHosts = new File(UUID.randomUUID().toString())

    /**
     * Remote user.
     */
    String user

    /**
     * Password.
     * Leave as null if public key authentication.
     */
    String password

    /**
     * Identity key file for public-key authentication.
     */
    File identity

    /**
     * Pass-phrase for the identity key.
     * This may be null.
     */
    String passphrase

    /**
     * Use agent flag.
     * If <code>true</code>, Putty Agent or ssh-agent will be used to authenticate.
     */
    Boolean agent

    /**
     * Known hosts file.
     * If {@link #allowAnyHosts}, strict host key checking is turned off.
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

    static final DEFAULT = new ConnectionSettings(
            user: null,
            password: null,
            identity: null,
            passphrase: null,
            agent: false,
            knownHosts: new File("${System.properties['user.home']}/.ssh/known_hosts"),
            retryCount: 0,
            retryWaitSec: 0
    )

    ConnectionSettings plus(ConnectionSettings right) {
        new ConnectionSettings(
                user:         findNotNull(right.user, user),
                password:     findNotNull(right.password, password),
                identity:     findNotNull(right.identity, identity),
                passphrase:   plusOfPassphrase(right),
                agent:        findNotNull(right.agent, agent),
                knownHosts:   findNotNull(right.knownHosts, knownHosts),
                retryCount:   findNotNull(right.retryCount, retryCount),
                retryWaitSec: findNotNull(right.retryWaitSec, retryWaitSec)
        )
    }

    private plusOfPassphrase(ConnectionSettings right) {
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
}
