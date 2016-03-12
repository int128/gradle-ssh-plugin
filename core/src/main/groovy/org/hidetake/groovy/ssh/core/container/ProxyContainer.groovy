package org.hidetake.groovy.ssh.core.container

import org.hidetake.groovy.ssh.core.Proxy

import java.util.concurrent.ConcurrentSkipListMap

/**
 * A container of proxies.
 *
 * @author Hidetake Iwata
 */
class ProxyContainer extends ConcurrentSkipListMap<String, Proxy> implements Container<Proxy> {
}
