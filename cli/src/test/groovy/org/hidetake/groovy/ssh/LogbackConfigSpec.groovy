package org.hidetake.groovy.ssh

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.Logger as Slf4jLogger
import org.slf4j.LoggerFactory as Slf4jLoggerFactory
import spock.lang.Specification

class LogbackConfigSpec extends Specification {

    def "log level can be set by logback method in the script"() {
        given:
        def root = Slf4jLoggerFactory.getLogger(Slf4jLogger.ROOT_LOGGER_NAME)
        assert root instanceof Logger

        when:
        Main.main '-e', '''
            logback level: 'ERROR'
        '''

        then:
        root.level == Level.ERROR
    }

}
