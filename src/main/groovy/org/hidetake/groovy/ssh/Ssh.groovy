package org.hidetake.groovy.ssh

import org.hidetake.groovy.ssh.api.Service
import org.hidetake.groovy.ssh.internal.DefaultService

/**
 * Groovy SSH.
 *
 * @author Hidetake Iwata
 */
class Ssh {
    static final Service ssh

    static {
        ssh = new DefaultService()
    }
}
