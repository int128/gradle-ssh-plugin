package org.hidetake.groovy.ssh.connection

import org.hidetake.groovy.ssh.core.Proxy

trait ProxyConnectionSettings {

    /**
     * Proxy configuration for connecting to a host.
     * This may be null.
     */
    Proxy proxy

}
