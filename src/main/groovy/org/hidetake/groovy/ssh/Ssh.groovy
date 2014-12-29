package org.hidetake.groovy.ssh

import groovy.transform.CompileStatic
import org.hidetake.groovy.ssh.core.Service

/**
 * Entry point of Groovy SSH library.
 *
 * @author Hidetake Iwata
 */
@CompileStatic
class Ssh {
    /**
     * Create an instance of {@link Service}.
     */
    static Service newService() {
        new Service()
    }

    /**
     * Create a {@link GroovyShell} object to run a Groovy script.
     */
    static GroovyShell newShell() {
        def binding = new Binding()
        binding.variables.ssh = newService()
        new GroovyShell(binding)
    }
}
