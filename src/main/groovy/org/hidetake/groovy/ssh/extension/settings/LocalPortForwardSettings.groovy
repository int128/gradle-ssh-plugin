package org.hidetake.groovy.ssh.extension.settings

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hidetake.groovy.ssh.core.settings.Settings

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

/**
 * Settings for the local port forwarding.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
@ToString
class LocalPortForwardSettings implements Settings<LocalPortForwardSettings> {
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

    static final DEFAULT = new LocalPortForwardSettings(
            port: 0,
            bind: '127.0.0.1',
            host: '127.0.0.1',
    )

    @Override
    LocalPortForwardSettings plus(LocalPortForwardSettings right) {
        new LocalPortForwardSettings(
                port: findNotNull(right.port, port),
                bind: findNotNull(right.bind, bind),
                hostPort: findNotNull(right.hostPort, hostPort),
                host: findNotNull(right.host, host)
        )
    }
}
