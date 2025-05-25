package org.hidetake.groovy.ssh

import com.jcraft.jsch.JSch

/**
 * Release metadata.
 *
 * @author Hidetake Iwata
 */
class Release {

    private final bundle = ResourceBundle.getBundle(Release.class.name)

    final String name = bundle.getString('product.name')
    final String version = bundle.getString('product.version')

    final String javaVersion = System.getProperty('java.version')
    final String groovyVersion = GroovySystem.version
    final String jschVersion = JSch.VERSION

    @Override
    String toString() {
        "$name-$version (java-$javaVersion, groovy-$groovyVersion, jsch-$jschVersion)"
    }

}
