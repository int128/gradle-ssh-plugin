package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import spock.lang.Specification

class LoggingOutputStreamSpec extends Specification {

    def "constructor args"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)

        when:
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        then:
        stream.logger == logger
        stream.logLevel == LogLevel.QUIET
    }

    def "logging disabled"() {
        given:
        def logger = Mock(Logger)
        def stream = new LoggingOutputStream(logger, LogLevel.ERROR)

        logger.isEnabled(LogLevel.INFO) >> true

        when:
        stream.write(0x23)
        stream.close()

        then:
        0 * logger.log(_, _)
    }

    def "a byte"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write(0x23)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, '#')
        stream.lines == ['#']
    }

    def "a line"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('single line'.bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']
    }

    def "a line terminated with a line separator"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('single line\n'.bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']
    }

    def "a line terminated with 2 line separators"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('single line\n\n'.bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')
        1 * logger.log(LogLevel.QUIET, '')
        stream.lines == ['single line', '']
    }

    def "blank lines"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('line1\n\nline2\n\n\n'.bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'line1')
        1 * logger.log(LogLevel.QUIET, 'line2')
        3 * logger.log(LogLevel.QUIET, '')
        stream.lines == ['line1', '', 'line2', '', '']
    }

    def "multi-platforms"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('line1\r\n\rline2\u2028line3\n\u2029line4\u0085line5'.bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'line1')
        1 * logger.log(LogLevel.QUIET, 'line2')
        1 * logger.log(LogLevel.QUIET, 'line3')
        1 * logger.log(LogLevel.QUIET, 'line4')
        1 * logger.log(LogLevel.QUIET, 'line5')
        2 * logger.log(LogLevel.QUIET, '')
        stream.lines == ['line1', '', 'line2', 'line3', '', 'line4', 'line5']
    }

    def "flush on each lines"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.with {
            write('line1\n'.bytes)
            flush()
            write('line2\n'.bytes)
            close()
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line1')
        1 * logger.log(LogLevel.QUIET, 'line2')
        stream.lines == ['line1', 'line2']
    }

    def "flush before line separator"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.with {
            write('lin'.bytes)
            flush()
            write('e3\n'.bytes)
            flush()
            write('li'.bytes)
            flush()
            write('ne4'.bytes)
            close()
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line3')
        1 * logger.log(LogLevel.QUIET, 'line4')
        stream.lines == ['line3', 'line4']
    }

    def "apply filter"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)
        stream.filter = { String line -> line.contains('4') }

        when:
        stream.with {
            write('lin'.bytes)
            flush()
            write('e3\n'.bytes)
            flush()
            write('li'.bytes)
            flush()
            write('ne4'.bytes)
            close()
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line4')
        stream.lines == ['line4']
    }

    protected createLoggerMock(LogLevel level) {
        def logger = Mock(Logger)
        logger.isEnabled(level) >> true
        logger
    }

}
