package org.hidetake.groovy.ssh.session.execution

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import org.codehaus.groovy.tools.Utilities
import org.hidetake.groovy.ssh.core.settings.CompositeSettings
import org.hidetake.groovy.ssh.core.settings.ToStringProperties
import org.hidetake.groovy.ssh.operation.CommandSettings
import org.hidetake.groovy.ssh.operation.Operations
import org.hidetake.groovy.ssh.session.BadExitStatusException

@Slf4j
class SudoHelper {

    final Operations operations
    final CommandSettings commandSettings
    final SudoSettings sudoSettings

    def SudoHelper(Operations operations1, CompositeSettings mergedSettings, SudoCommandSettings perMethodSettings) {
        operations = operations1
        commandSettings = new CommandSettings.With(mergedSettings, perMethodSettings)
        sudoSettings = new SudoSettings.With(
                new SudoSettings.With(sudoPassword: operations.remote.password),
                mergedSettings,
                perMethodSettings
        )
    }

    String execute(String commandLine) {
        final prompt = UUID.randomUUID().toString()
        final lines = []
        final sudoCommandLine = "$sudoSettings.sudoPath -S -p '$prompt' $commandLine"

        final command = operations.command(commandSettings, sudoCommandLine)
        command.addInteraction {
            when(partial: prompt, from: standardOutput) {
                log.info("Providing password for sudo prompt on $operations.remote.name")
                standardInput << sudoSettings.sudoPassword << '\n'
                standardInput.flush()

                when(line: _, from: standardOutput) {
                    log.debug("Got ACK to the password on $operations.remote.name")

                    when(line: _, from: standardOutput) {
                        lines.clear()
                        lines << it

                        when(partial: prompt, from: standardOutput) {
                            log.error("Failed sudo authentication on $operations.remote.name")
                            throw new SudoException(operations.remote, lines.join('\n'))
                        }

                        when(line: _, from: standardOutput) {
                            lines << it
                        }
                    }
                }
            }
            when(line: _, from: standardOutput) {
                lines << it
            }
        }

        final exitStatus = command.startSync()
        if (exitStatus != 0 && !commandSettings.ignoreError) {
            throw new BadExitStatusException("Command returned exit status $exitStatus: $sudoCommandLine", exitStatus)
        }
        lines.join(Utilities.eol())
    }

    /**
     * Settings class for handling map argument by
     * {@link Sudo#executeSudo(java.util.HashMap, java.lang.String)} and
     * {@link Sudo#executeSudo(java.util.HashMap, java.lang.String, groovy.lang.Closure)}.
     */
    @EqualsAndHashCode
    static class SudoCommandSettings implements CommandSettings, SudoSettings, ToStringProperties {
    }

}
