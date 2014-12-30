package org.hidetake.groovy.ssh

import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * CLI main class.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class Main {
    static void main(String[] args) {
        def cli = new CliBuilder(
            usage: '[option...] [-e script-text] [script-filename | --stdin] [script-args...]',
            width: 120
        )
        cli.h longOpt: 'help',  'Shows this help message.'
        cli.q longOpt: 'quite', 'Set log level to warn.'
        cli.i longOpt: 'info',  'Set log level to info. (default)'
        cli.d longOpt: 'debug', 'Set log level to debug.'
        cli._ longOpt: 'stdin', 'Specify standard input as a source.'
        cli.e args: 1,          'Specify a command line script.'
        cli.n longOpt: 'dry-run', 'Do a dry run without connections.'

        def options = cli.parse(args)
        if (!options || options.h) {
            cli.usage()
        } else {
            if (options.d) {
                configureLogLevel('DEBUG')
            } else if (options.i) {
                configureLogLevel('INFO')
            } else if (options.q) {
                configureLogLevel('WARN')
            } else {
                configureLogLevel('INFO')
            }

            def extraArguments = options.arguments()
            if (options.e) {
                newShellWith(options).run(options.e as String, 'script.groovy', extraArguments)
            } else if (options.stdin) {
                newShellWith(options).run(System.in.newReader(), 'script.groovy', extraArguments)
            } else if (extraArguments.size() > 0) {
                newShellWith(options).run(new File(extraArguments.head()), extraArguments.tail())
            } else {
                cli.usage()
            }
        }
    }

    private static newShellWith(options) {
        def shell = Ssh.newShell()
        if (options.n) {
            def service = shell.getVariable('ssh') as Service
            service.settings {
                dryRun = true
            }
        }
        shell
    }

    /**
     * Configure log level of Logback.
     * This method should not fail if Logback is not loaded.
     *
     * @param level log level as a string
     */
    private static void configureLogLevel(String level) {
        try {
            def levelClass = Class.forName('ch.qos.logback.classic.Level')
            def levelObject = levelClass.invokeMethod('toLevel', level)
            def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            root.setLevel(levelObject)
        } catch (ClassNotFoundException e) {
            log.info("Could not set log level to $level: ${e.localizedMessage}")
        }
    }
}
