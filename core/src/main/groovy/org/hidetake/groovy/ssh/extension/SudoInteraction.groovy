package org.hidetake.groovy.ssh.extension

import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities

@Slf4j
class SudoInteraction {

    private final prompt = UUID.randomUUID().toString()

    private final lines = []

    def command(String command) {
        "sudo -S -p '$prompt' $command"
    }

    def settings(String password, HashMap settings = [:]) {
        [:] << settings << [interaction: { ->
            when(partial: prompt, from: standardOutput) {
                log.info('Providing the password for sudo authentication')
                standardInput << password << '\n'

                when(nextLine: _, from: standardOutput) {
                    when(nextLine: 'Sorry, try again.') {
                        throw new RuntimeException('sudo authentication failed')
                    }
                    when(line: _, from: standardOutput) {
                        log.info('sudo authentication passed')
                        lines << it
                    }
                }
            }
            when(line: _, from: standardOutput) {
                lines << it
            }
        }]
    }

    def getText() {
        lines.join(Utilities.eol())
    }

}
