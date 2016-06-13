package org.hidetake.groovy.ssh

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Runtime info.
 *
 * @author Hidetake Iwata
 */
@Singleton
class Runtime {

    /**
     * Configure logback.
     * @param settings
     *      level: log level ({@link String} or {@link Level}),
     *      pattern: format ({@link String})
     */
    void logback(Map settings) {
        def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        assert root instanceof ch.qos.logback.classic.Logger
        def originalLevel = root.level

        def encoder = new PatternLayoutEncoder()
        encoder.context = root.loggerContext
        encoder.pattern = settings.pattern ?: '%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %msg%n'
        encoder.start()

        def appender = new ConsoleAppender()
        appender.context = root.loggerContext
        appender.encoder = encoder
        appender.name = 'console'
        appender.start()

        root.loggerContext.reset()
        root.addAppender(appender)

        switch (settings.level) {
            case String:
                root.level = Level.toLevel(settings.level as String)
                break
            case Level:
                root.level = settings.level
                break
            default:
                root.level = originalLevel
                break
        }
    }

    /**
     * Path to self Groovy SSH JAR, or null if it is unknown
     */
    @Lazy
    File jar = {
        def url = Runtime.getResource("/${Runtime.name.replace('.', '/')}.class")
        if (url.protocol == 'jar') {
            def m = url.file =~ /^file:(.+?)!/
            if (m) {
                def jarFile = new File(m.group(1))
                assert jarFile.exists()
                jarFile
            } else {
                null
            }
        } else {
            null
        }
    }()

}
