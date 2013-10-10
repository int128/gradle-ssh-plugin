package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import spock.lang.Specification
import spock.lang.Unroll

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
        // a terminated line separator should be ignored
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']
    }

    @Unroll
    def "a line terminated with #n line separators"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write("single line${'\n' * n}".toString().bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines[0] == 'single line'
        stream.lines[1..(n - 1)].each { it == '' }
        stream.lines.size() == n

        where:
        n << (2..3)
    }

    @Unroll
    def "a line starting with #n line separators"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write("${'\n' * n}single line".toString().bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')

        stream.lines[0..(n - 1)].each { it == '' }
        stream.lines[n] == 'single line'
        stream.lines.size() == (n + 1)

        where:
        n << (1..3)
    }

    @Unroll
    def "a line with #n line separators in the middle"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write("line1${'\n' * n}line2".toString().bytes)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'line1')
        1 * logger.log(LogLevel.QUIET, 'line2')

        stream.lines[0] == 'line1'
        stream.lines[1..n].each { it == '' }
        stream.lines[n] == 'line2'
        stream.lines.size() == (n + 1)

        where:
        n << (1..3)
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

    @Unroll
    def "flush in line #n times"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.with {
            write('lin'.bytes)
            (1..n).each {
                flush()
            }
            write('e3\n'.bytes)
            (1..n).each {
                flush()
            }
            write('li'.bytes)
            (1..n).each {
                flush()
            }
            write('ne4'.bytes)
            close()
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line3')
        1 * logger.log(LogLevel.QUIET, 'line4')
        stream.lines == ['line3', 'line4']

        where:
        n << (1..3)
    }

    @Unroll
    def "close #n times, a line"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('single line'.bytes)
        (1..n).each {
            stream.close()
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']

        where:
        n << (1..3)
    }

    @Unroll
    def "close #n times, a line terminated with a line separator"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.write('single line\n'.bytes)
        (1..n).each {
            stream.close()
        }

        then:
        // a terminated line separator should be ignored
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']

        where:
        n << (1..3)
    }

    def "with writer"() {
        given:
        def logger = createLoggerMock(LogLevel.QUIET)
        def stream = new LoggingOutputStream(logger, LogLevel.QUIET)

        when:
        stream.withWriter {
            it.append 'line3\nline4\nline5'
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line3')
        1 * logger.log(LogLevel.QUIET, 'line4')
        1 * logger.log(LogLevel.QUIET, 'line5')
        stream.lines == ['line3', 'line4', 'line5']
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
