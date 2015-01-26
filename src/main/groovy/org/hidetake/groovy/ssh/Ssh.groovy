package org.hidetake.groovy.ssh

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Service

/**
 * Entry point of Groovy SSH library.
 *
 * @author Hidetake Iwata
 */
@Slf4j
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

    /**
     * Product version.
     * This property should be an empty string if resource is not found.
     */
    @Lazy
    static String version = {
        final resourcePath = '/org.hidetake.groovy.ssh.version'
        try {
            def stream = Ssh.getResourceAsStream(resourcePath)
            stream ? stream.text : ''
        } catch (IOException e) {
            log.warn("Could not find resource: ${resourcePath}", e)
            ''
        }
    }()
}
