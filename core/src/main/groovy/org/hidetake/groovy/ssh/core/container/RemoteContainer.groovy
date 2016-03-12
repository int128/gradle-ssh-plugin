package org.hidetake.groovy.ssh.core.container

import org.hidetake.groovy.ssh.core.Remote

import java.util.concurrent.ConcurrentSkipListMap

/**
 * A container of remote hosts.
 *
 * @author Hidetake Iwata
 */
class RemoteContainer extends ConcurrentSkipListMap<String, Remote> implements Container<Remote>, RoleAccessible {
    final Class containerElementType = Remote
}
