package org.hidetake.groovy.ssh.connection

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.settings.SettingsHelper
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for establishing the SSH connection.
 *
 * @author Hidetake Iwata
 */
trait ConnectionSettings implements UserAuthenticationSettings, HostAuthenticationSettings, ProxyConnectionSettings {
    /**
     * Gateway host.
     * This may be null.
     */
    Remote gateway

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


    @EqualsAndHashCode
    static class With implements ConnectionSettings, ToStringProperties {
        def With() {}
        def With(ConnectionSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final ConnectionSettings DEFAULT = new ConnectionSettings.With(
                user: null,
                authentications: ['publickey', 'keyboard-interactive', 'password'],
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
}
