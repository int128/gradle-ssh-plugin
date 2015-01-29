package org.hidetake.groovy.ssh.extension.settings

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hidetake.groovy.ssh.core.settings.Settings

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

/**
 * Settings for the remote port forwarding.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
@ToString
class RemotePortForwardSettings implements Settings<RemotePortForwardSettings> {
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

    static final DEFAULT = new RemotePortForwardSettings(
            host: '127.0.0.1',
            bind: '127.0.0.1',
    )

    @Override
    RemotePortForwardSettings plus(RemotePortForwardSettings right) {
        new RemotePortForwardSettings(
                hostPort: findNotNull(right.hostPort, hostPort),
                host: findNotNull(right.host, host),
                port: findNotNull(right.port, port),
                bind: findNotNull(right.bind, bind)
        )
    }
}
