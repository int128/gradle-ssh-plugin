package org.hidetake.groovy.ssh.test.server

import groovy.util.logging.Slf4j
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.command.Command

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

@Slf4j
class CommandHelper {

    static class CommandContext {
        InputStream inputStream
        OutputStream outputStream
        OutputStream errorStream
        ExitCallback exitCallback
        Environment environment
    }

    static command(int status, @DelegatesTo(CommandContext) Closure interaction = {}) {
        def context = new CommandContext()
        [setInputStream: { InputStream inputStream ->
            context.inputStream = inputStream
        },
         setOutputStream: { OutputStream outputStream ->
             context.outputStream = outputStream
         },
         setErrorStream: { OutputStream errorStream ->
             context.errorStream = errorStream
         },
         setExitCallback: { ExitCallback callback ->
             context.exitCallback = callback
         },
         start: { Environment env ->
             context.environment = env
             Thread.start {
                 log.debug("[ssh-server-mock] Started interaction thread")
                 try {
                     callWithDelegate(interaction, context)
                     context.exitCallback.onExit(status)
                 } catch (Throwable t) {
                     log.error("[ssh-server-mock] Error occurred on interaction thread", t)
                     context.exitCallback.onExit(-1, t.message)
                 }
                 log.debug("[ssh-server-mock] Terminated interaction thread")
             }
         },
         destroy: { ->
         }] as Command
    }

}
