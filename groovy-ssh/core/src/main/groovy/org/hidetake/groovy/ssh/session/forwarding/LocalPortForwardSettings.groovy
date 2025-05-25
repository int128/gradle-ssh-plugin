package org.hidetake.groovy.ssh.session.forwarding

import groovy.transform.EqualsAndHashCode
import org.hidetake.groovy.ssh.core.settings.SettingsHelper
import org.hidetake.groovy.ssh.core.settings.ToStringProperties

/**
 * Settings for the local port forwarding.
 *
 * @author Hidetake Iwata
 */
trait LocalPortForwardSettings {
    /**
     * Local port to bind. Defaults to 0 (allocate free port).
     */
    Integer port

    /**
     * Local host to bind. Defaults to localhost.
     */
    String bind

    /**
     * Remote port to connect. (Mandatory)
     */
    Integer hostPort

    /**
     * Remote host to connect. Default to localhost of the remote host.
     */
    String host


    @EqualsAndHashCode
    static class With implements LocalPortForwardSettings, ToStringProperties {
        def With() {}
        def With(LocalPortForwardSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }

        static final DEFAULT = new With(port: 0, bind: '127.0.0.1', host: '127.0.0.1')
    }
}
