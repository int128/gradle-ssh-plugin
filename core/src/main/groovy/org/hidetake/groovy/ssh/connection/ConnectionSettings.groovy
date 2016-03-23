package org.hidetake.groovy.ssh.connection

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hidetake.groovy.ssh.core.Proxy
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.Settings

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

/**
 * Settings for establishing the SSH connection.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
@ToString(excludes = 'password, identity, passphrase, allowAnyHosts')
class ConnectionSettings implements Settings<ConnectionSettings> {
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
     * Identity key file for public-key authentication.
     * This must be a {@link File}, {@link String} or null.
     * Leave as null if the public key authentication is not needed.
     */
    def identity

    /**
     * Pass-phrase for the identity key.
     * This may be null.
     */
    String passphrase

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

    ConnectionSettings plus(ConnectionSettings right) {
        new ConnectionSettings(
                user:         findNotNull(right.user, user),
                password:     findNotNull(right.password, password),
                identity:     findNotNull(right.identity, identity),
                passphrase:   plusOfPassphrase(right),
                gateway:      findNotNull(right.gateway, gateway),
                proxy:        findNotNull(right.proxy, proxy),
                agent:        findNotNull(right.agent, agent),
                knownHosts:   findNotNull(right.knownHosts, knownHosts),
                retryCount:   findNotNull(right.retryCount, retryCount),
                retryWaitSec: findNotNull(right.retryWaitSec, retryWaitSec),
                keepAliveSec: findNotNull(right.keepAliveSec, keepAliveSec),
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
