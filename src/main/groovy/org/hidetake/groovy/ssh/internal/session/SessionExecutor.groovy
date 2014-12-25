package org.hidetake.groovy.ssh.internal.session

import org.hidetake.groovy.ssh.api.CompositeSettings

interface SessionExecutor {
    def <T> List<T> execute(CompositeSettings compositeSettings, List<Plan<T>> plans)
}
