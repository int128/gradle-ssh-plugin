package org.hidetake.groovy.ssh

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.hidetake.groovy.ssh.api.Service
import org.hidetake.groovy.ssh.internal.DefaultService

/**
 * Entry point of Groovy SSH library.
 *
 * @author Hidetake Iwata
 */
@CompileStatic
class Ssh {
    /**
     * An implementation of {@link Service}.
     */
    @Lazy
    static final Service ssh = {
        new DefaultService()
    }()

    /**
     * A {@link GroovyShell} object to run a Groovy script.
     */
    @Lazy
    static final GroovyShell shell = {
        def importCustomizer = new ImportCustomizer()
        importCustomizer.addStaticImport(Ssh.class.name, 'ssh')
        def configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer)
        new GroovyShell(configuration)
    }()
}
