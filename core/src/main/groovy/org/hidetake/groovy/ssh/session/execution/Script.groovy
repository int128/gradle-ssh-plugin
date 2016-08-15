package org.hidetake.groovy.ssh.session.execution

import groovy.util.logging.Slf4j

/**
 * An extension class for script execution.
 *
 * @author Hidetake Iwata
 */
@Slf4j
trait Script implements Command {

    String executeScript(HashMap settings = [:], String script) {
        assert script, 'script must be given'
        execute(Helper.createSettings(settings, script), Helper.guessInterpreter(script))
    }

    void executeScript(HashMap settings = [:], String script, Closure callback) {
        assert script, 'script must be given'
        execute(Helper.createSettings(settings, script), Helper.guessInterpreter(script), callback)
    }

    String executeScript(HashMap settings = [:], File script) {
        assert script, 'script must be given'
        execute(Helper.createSettings(settings, script), Helper.guessInterpreter(script))
    }

    void executeScript(HashMap settings = [:], File script, Closure callback) {
        assert script, 'script must be given'
        execute(Helper.createSettings(settings, script), Helper.guessInterpreter(script), callback)
    }

    private static class Helper {
        static HashMap createSettings(HashMap settings, String script) {
            if (settings.inputStream) {
                throw new IllegalArgumentException("executeScript does not work with inputStream: $settings")
            }
            [:] << settings << [inputStream: script] as HashMap
        }

        static HashMap createSettings(HashMap settings, File script) {
            if (settings.inputStream) {
                throw new IllegalArgumentException("executeScript does not work with inputStream: $settings")
            }
            [:] << settings << [inputStream: script.newInputStream()] as HashMap
        }

        static String guessInterpreter(String script) {
            script.find(~/^#!.+/) { m -> m.substring(2) } ?: '/bin/sh'
        }

        static String guessInterpreter(File script) {
            script.withReader { reader ->
                reader.readLine().find(~/^#!.+/) { m -> m.substring(2) }
            } ?: '/bin/sh'
        }
    }

}
