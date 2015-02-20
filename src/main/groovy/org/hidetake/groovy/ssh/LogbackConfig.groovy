package org.hidetake.groovy.ssh

import groovy.util.logging.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Logback configurator.
 * These method should not fail if Logback is not found.
 *
 * @author Hidetake Iwata
 */
@Slf4j
class LogbackConfig {
    /**
     * Configure Logback.
     *
     * @param map level: log level (string), pattern: format (string)
     */
    void call(Map map) {
        configureLogback(map)
    }

    /**
     * Configure Logback.
     * See also ch.qos.logback.classic.BasicConfigurator
     *
     * @param map level: log level (string), pattern: format (string)
     */
    static void configureLogback(Map settings) {
        try {
            def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            def originalLevel = root.level

            def encoder = Class.forName('ch.qos.logback.classic.encoder.PatternLayoutEncoder').newInstance()
            encoder.context = root.loggerContext
            encoder.pattern = settings.pattern
            encoder.start()

            def appender = Class.forName('ch.qos.logback.core.ConsoleAppender').newInstance()
            appender.context = root.loggerContext
            appender.encoder = encoder
            appender.name = 'console'
            appender.start()

            root.loggerContext.reset()
            root.addAppender(appender)

            if (settings.level) {
                root.level = Class.forName('ch.qos.logback.classic.Level').toLevel(settings.level)
            } else {
                root.level = originalLevel
            }
        } catch (ClassNotFoundException e) {
            log.info("Could not configure Logback: ${e.localizedMessage}")
        }
    }
}
