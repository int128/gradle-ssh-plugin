package org.hidetake.groovy.ssh

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

class LogbackConfigSpec extends Specification {

    def "log level can be set by logback method in the script"() {
        given:
        def root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)

        when:
        Main.main '-e', '''
            logback level: 'ERROR'
        '''

        then:
        root.level == logbackLogLevel('ERROR')
    }


    private static final logbackLogLevel(String level) {
        Class.forName('ch.qos.logback.classic.Level').invokeMethod('toLevel', level)
    }

}
