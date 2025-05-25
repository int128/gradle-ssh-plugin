package org.hidetake.groovy.ssh.session.forwarding

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.SettingsHelper
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for the remote port forwarding.
 *
 * @author Hidetake Iwata
 */
trait RemotePortForwardSettings {
    /**
     * Local port to connect. (Mandatory)
     */
    Integer hostPort

    /**
     * Local host to connect. Defaults to localhost.
     */
    String host

    /**
     * Remote port to bind. (Mandatory)
     */
    Integer port

    /**
     * Remote host to bind. Default to localhost of the remote host.
     */
    String bind


    @EqualsAndHashCode
    static class With implements RemotePortForwardSettings, ToStringProperties {
        def With() {}
        def With(RemotePortForwardSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final DEFAULT = new With(bind: '127.0.0.1', host: '127.0.0.1')
    }
}
