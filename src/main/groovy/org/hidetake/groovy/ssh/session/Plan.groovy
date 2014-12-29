package org.hidetake.groovy.ssh.session

import groovy.transform.Canonical
import org.hidetake.groovy.ssh.core.Remote

@Canonical
class Plan<T> {
    final Remote remote
    final Closure<T> closure
}
