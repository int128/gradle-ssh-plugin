package org.hidetake.groovy.ssh

import groovy.util.logging.Slf4j
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
        def cli = new CliBuilder(usage: '[option...] [-e script-text] [script-filename] [script-args...]')
        cli.h longOpt: 'help',  'Shows this help message.'
        cli.i longOpt: 'info',  'Set log level to info.'
        cli.d longOpt: 'debug', 'Set log level to debug.'
        cli.e args: 1,          'Specify a command line script.'

        def options = cli.parse(args)
        if (!options || options.h) {
            cli.usage()
        } else {
            if (options.d) {
                configureLogLevel('DEBUG')
            } else if (options.i) {
                configureLogLevel('INFO')
            } else {
                configureLogLevel('WARN')
            }

            if (options.e) {
                Ssh.shell.run(options.e as String, 'script.groovy', options.arguments())
            } else {
                def extraArguments = options.arguments()
                if (extraArguments.size() > 0) {
                    if (extraArguments.head() == '-') {
                        Ssh.shell.run(System.in.newReader(), 'script.groovy', extraArguments.tail())
                    } else {
                        Ssh.shell.run(new File(extraArguments.head()), extraArguments.tail())
                    }
                } else {
                    Ssh.shell.run(System.in.newReader(), 'script.groovy')
                }
            }
        }
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
