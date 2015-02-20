package org.hidetake.groovy.ssh.core.container

import org.hidetake.groovy.ssh.core.Remote

/**
 * A container of remote hosts.
 *
 * @author Hidetake Iwata
 */
trait RemoteContainer implements Container<Remote>, RoleAccessible {
}
