package org.hidetake.gradle.ssh.plugin

trait ExampleExtension {

    def example() {
        execute 'ls'
        project.name
    }

}
