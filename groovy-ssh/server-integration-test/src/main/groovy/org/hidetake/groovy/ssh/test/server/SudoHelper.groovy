package org.hidetake.groovy.ssh.test.server

import groovy.transform.Immutable
import groovy.util.logging.Slf4j

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

@Slf4j
class SudoHelper {

    @Immutable
    static class ParsedCommandLine {
        final String sudoPath
        final String prompt
        final String command

        static ParsedCommandLine parse(String commandLine) {
            def matcher = commandLine =~ /^(.+?) -S -p '(.+?)' (.+)$/
            assert matcher.matches()
            def groups = matcher[0] as List
            new ParsedCommandLine(sudoPath: groups[1], prompt: groups[2], command: groups[3])
        }
    }

    static sudoCommand(String commandLine, int status,
                       String expectedSudoPath, String expectedCommand, String expectedPassword,
                       String lectureMessage = null,
                       @DelegatesTo(CommandHelper.CommandContext) Closure closure = null) {
        CommandHelper.command(status) {
            def parsedCommandLine = ParsedCommandLine.parse(commandLine)
            assert parsedCommandLine.sudoPath == expectedSudoPath
            assert parsedCommandLine.command == expectedCommand

            if (lectureMessage) {
                log.debug("[sudo] Sending to standard output: $lectureMessage")
                outputStream << lectureMessage << '\n'
            }

            log.debug("[sudo] Sending prompt: $parsedCommandLine.prompt")
            outputStream << parsedCommandLine.prompt
            outputStream.flush()

            log.debug('[sudo] Waiting for password')
            def actualPassword = new ByteArrayOutputStream()
            for (;;) {
                def b = inputStream.read()
                if (b == 0x0a || b == 0x0d || b == -1) {
                    break
                }
                actualPassword.write(b)
            }

            log.debug("[sudo] Got password: $actualPassword")
            assert actualPassword.toString() == expectedPassword

            log.debug('[sudo] Sending ACK to the password')
            outputStream << '\n'
            outputStream.flush()

            if (closure) {
                log.debug('[sudo] Starting interaction')
                delegate.metaClass.parsedCommandLine = parsedCommandLine
                callWithDelegate(closure, delegate)
            }
        }
    }

}
