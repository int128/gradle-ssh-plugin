package org.hidetake.groovy.ssh.session.forwarding

import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class of port forwarding.
 *
 * @author Hidetake Iwata
 */
trait PortForward implements SessionExtension {

    /**
     * Forwards local port to remote port.
     *
     * @param settings {@see LocalPortForwardingSettings}
     * @return local port
     */
    int forwardLocalPort(HashMap settings) {
        assert settings != null, 'settings must not be null'
        def merged = new LocalPortForwardSettings.With(LocalPortForwardSettings.With.DEFAULT, new LocalPortForwardSettings.With(settings))
        assert merged.hostPort, 'remote port must be given'
        operations.forwardLocalPort(merged)
    }

    /**
     * Forwards remote port to local port.
     *
     * @param settings {@see RemotePortForwardingSettings}
     */
    void forwardRemotePort(HashMap settings) {
        assert settings != null, 'settings must not be null'
        def merged = new RemotePortForwardSettings.With(RemotePortForwardSettings.With.DEFAULT, new RemotePortForwardSettings.With(settings))
        assert merged.hostPort, 'local port must be given'
        assert merged.port, 'remote port must be given'
        operations.forwardRemotePort(merged)
    }

}