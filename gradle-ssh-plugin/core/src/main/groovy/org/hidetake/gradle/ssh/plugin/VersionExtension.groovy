package org.hidetake.gradle.ssh.plugin

import org.hidetake.groovy.ssh.Ssh

/**
 * An extension class for {@code project.ssh} instance.
 *
 * @author Hidetake Iwata
 */
class VersionExtension {

    @Lazy
    String version = {
        def resourcePath = '/META-INF/gradle-plugins/org.hidetake.ssh.properties'
        VersionExtension.getResourceAsStream(resourcePath)?.withStream { stream ->
            def properties = new Properties()
            properties.load(stream)
            def name = properties.getProperty('product.name')
            def version = properties.getProperty('product.version')
            "$name-$version (" +
                    "groovy-ssh-${Ssh.release.version}, " +
                    "jsch-${Ssh.release.jschVersion}, " +
                    "groovy-${Ssh.release.groovyVersion}, " +
                    "java-${Ssh.release.javaVersion})"
        }
    }()

}
