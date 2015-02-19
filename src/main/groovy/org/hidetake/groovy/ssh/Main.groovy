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
        cli._ longOpt: 'version', 'Shows version.'

        def options = cli.parse(args)
        if (!options || options.h) {
            cli.usage()
        } else if (options.version) {
            println "${Ssh.product.name}-${Ssh.product.version}"
        } else {
            if      (options.d) { configureLogLevel('DEBUG') }
            else if (options.i) { configureLogLevel('INFO') }
            else if (options.q) { configureLogLevel('WARN') }
            else                { configureLogLevel('INFO') }

            def shell = newShellWith(options)
            def extra = options.arguments()

            if      (options.e)     { shell.run(options.e as String,   'script.groovy', extra) }
            else if (options.stdin) { shell.run(System.in.newReader(), 'script.groovy', extra) }
            else if (!extra.empty)  { shell.run(new File(extra.head()), extra.tail()) }
            else                    { cli.usage() }
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
            def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            root.level = Class.forName('ch.qos.logback.classic.Level').toLevel(level)
        } catch (ClassNotFoundException e) {
            log.info("Could not set log level to $level: ${e.localizedMessage}")
        }
    }
}
