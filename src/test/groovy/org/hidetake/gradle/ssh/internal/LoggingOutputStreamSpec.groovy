package org.hidetake.gradle.ssh.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import spock.lang.Specification
import spock.lang.Unroll

class LoggingOutputStreamSpec extends Specification {

    LoggingOutputStream stream
    Logger logger

    def setup() {
        logger = GroovySpy(OperationEventLogger.logger.class, global: true) {
            isEnabled(LogLevel.QUIET) >> true
            isEnabled(_) >> false
        }
        stream = new LoggingOutputStream(LogLevel.QUIET)
    }


    def "write a byte"() {
        when:
        stream.write(0x23)
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, '#')
        stream.lines == ['#']
    }

    def "write bytes"() {
        when:
        stream << utf8bytes('line3\nline4\nline5')
        stream.close()

        then:
        1 * logger.log(LogLevel.QUIET, 'line3')
        1 * logger.log(LogLevel.QUIET, 'line4')
        1 * logger.log(LogLevel.QUIET, 'line5')
        stream.lines == ['line3', 'line4', 'line5']
    }

    def "write lines with writer"() {
        when:
        stream.withWriter('UTF-8') {
            it << 'line3\nline4\nline5'
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line3')
        1 * logger.log(LogLevel.QUIET, 'line4')
        1 * logger.log(LogLevel.QUIET, 'line5')
        stream.lines == ['line3', 'line4', 'line5']
    }

    def "a line without line separator"() {
        when:
        stream.withWriter('UTF-8') {
            it << 'single line'
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']
    }

    def "a line terminated with a line separator"() {
        when:
        stream.withWriter('UTF-8') {
            it << 'single line\n'
        }

        then:
        // a terminated line separator should be ignored
        1 * logger.log(LogLevel.QUIET, 'single line')
        stream.lines == ['single line']
    }

    @Unroll
    def "a line terminated with #n line separators"() {
        when:
        stream.withWriter('UTF-8') {
            it << "single line${'\n' * n}"
        }

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
        when:
        stream.withWriter('UTF-8') {
            it << "${'\n' * n}single line"
        }

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
        when:
        stream.withWriter('UTF-8') {
            it << "line1${'\n' * n}line2"
        }

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
        when:
        stream.withWriter('UTF-8') {
            it << 'line1\r\n\rline2\u2028line3\n\u2029line4\u0085line5'
        }

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
        when:
        stream.with {
            write(utf8bytes('lin'))
            (1..n).each {
                flush()
            }
            write(utf8bytes('e3\n'))
            (1..n).each {
                flush()
            }
            write(utf8bytes('li'))
            (1..n).each {
                flush()
            }
            write(utf8bytes('ne4'))
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
        when:
        stream.write(utf8bytes('single line'))
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
        when:
        stream.write(utf8bytes('single line\n'))
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

    def "apply filter"() {
        given:
        stream.filter = { String line -> line.contains('4') }

        when:
        stream.with {
            write(utf8bytes('lin'))
            flush()
            write(utf8bytes('e3\n'))
            flush()
            write(utf8bytes('li'))
            flush()
            write(utf8bytes('ne4'))
            close()
        }

        then:
        1 * logger.log(LogLevel.QUIET, 'line4')
        stream.lines == ['line4']
    }


    def "logging disabled"() {
        given:
        stream = new LoggingOutputStream(LogLevel.DEBUG)

        when:
        stream.write(0x23)
        stream.close()

        then:
        0 * logger.log(_, _ as String)
    }


    def "explicit charset"() {
        given:
        stream = new LoggingOutputStream(LogLevel.QUIET, 'Shift_JIS')

        when:
        stream.withWriter('Shift_JIS') {
            it << 'これは\n日本語です'
        }

        then:
        stream.lines == ['これは', '日本語です']
    }


    static utf8bytes(String s) {
        s.getBytes('UTF-8')
    }

}
