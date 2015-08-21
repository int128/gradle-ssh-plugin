package org.hidetake.groovy.ssh

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.Logger as Slf4jLogger
import org.slf4j.LoggerFactory as Slf4jLoggerFactory

/**
 * Logback configurator.
 *
 * @author Hidetake Iwata
 */
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
        def root = Slf4jLoggerFactory.getLogger(Slf4jLogger.ROOT_LOGGER_NAME)
        assert root instanceof Logger
        def originalLevel = root.level

        def encoder = new PatternLayoutEncoder()
        encoder.context = root.loggerContext
        encoder.pattern = settings.pattern
        encoder.start()

        def appender = new ConsoleAppender()
        appender.context = root.loggerContext
        appender.encoder = encoder
        appender.name = 'console'
        appender.start()

        root.loggerContext.reset()
        root.addAppender(appender)

        if (settings.level) {
            root.level = Level.toLevel(settings.level as String)
        } else {
            root.level = originalLevel
        }
    }
}
