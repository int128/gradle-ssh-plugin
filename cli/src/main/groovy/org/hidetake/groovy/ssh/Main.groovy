package org.hidetake.groovy.ssh

import ch.qos.logback.classic.Level
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.core.Service

import static org.hidetake.groovy.ssh.LogbackConfig.configureLogback

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
        cli.q longOpt: 'quiet', 'Set log level to warn.'
        cli.i longOpt: 'info',  'Set log level to info. (default)'
        cli.d longOpt: 'debug', 'Set log level to debug.'
        cli._ longOpt: 'stdin', 'Specify standard input as a source.'
        cli.e args: 1,          'Specify a command line script.'
        cli.n longOpt: 'dry-run', 'Do a dry run without connections.'
        cli._ longOpt: 'version', 'Shows version.'
        cli.s longOpt: 'stacktrace', 'Print out the stacktrace for the exception.'

        def options = cli.parse(args)
        if (!options || options.h) {
            cli.usage()
        } else if (options.version) {
            println "${Ssh.product.name}-${Ssh.product.version}"
        } else {
            configureLogback(level: logLevel(options) as String,
                    pattern: '%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %msg%n')

            if (!options.s) {
                Thread.currentThread().uncaughtExceptionHandler = { Thread t, Throwable e ->
                    log.error("Error: $e")
                    log.info('Run with -s or --stacktrace option to get the stack trace')
                }
            }

            def shell = newShellWith(options)
            def extra = options.arguments()

            if      (options.e)     { shell.run(options.e as String,   'script.groovy', extra) }
            else if (options.stdin) { shell.run(System.in.newReader(), 'script.groovy', extra) }
            else if (!extra.empty)  { shell.run(new File(extra.head()), extra.tail()) }
            else                    { cli.usage() }
        }
    }

    private static logLevel(options) {
        if      (options.d) { Level.DEBUG }
        else if (options.i) { Level.INFO }
        else if (options.q) { Level.WARN }
        else                { Level.INFO }
    }

    private static newShellWith(options) {
        def shell = Ssh.newShell()
        if (options.n) {
            def service = shell.getVariable('ssh') as Service
            service.settings {
                dryRun = true
            }
        }
        shell.setVariable('logback', new LogbackConfig())
        shell
    }
}
