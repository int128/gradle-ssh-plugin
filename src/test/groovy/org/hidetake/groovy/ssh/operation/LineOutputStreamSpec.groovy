package org.hidetake.groovy.ssh.operation

import org.hidetake.groovy.ssh.operation.LineOutputStream
import spock.lang.Specification
import spock.lang.Unroll

class LineOutputStreamSpec extends Specification {

    LineOutputStream stream
    Closure partialListener
    Closure lineListener
    Closure loggingListener

    def setup() {
        partialListener = Mock(Closure)
        lineListener = Mock(Closure)
        loggingListener = Mock(Closure)

        stream = new LineOutputStream()
        stream.listenPartial(partialListener)
        stream.listenLine(lineListener)
        stream.listenLogging(loggingListener)
    }

    def cleanup() {
        stream.close()
    }


    @Unroll
    def "only last new-line character should be ignored, #n new-line characters"() {
        when:
        utf8bytes("single line${'\n' * n}").each { stream.write(it) }
        stream.close()

        then:
        0 * partialListener.call(_)
        1 * lineListener.call('single line')
        1 * loggingListener.call('single line')

        then:
        0       * partialListener.call(_)
        (n - 1) * lineListener.call('')
        (n - 1) * loggingListener.call('')

        where:
        n << (1..3)
    }

    @Unroll
    def "heading new-line characters should be blank, #n new-line characters"() {
        when:
        utf8bytes("${'\n' * n}single line").each { stream.write(it) }
        stream.close()

        then:
        0 * partialListener.call(_)
        n * lineListener.call('')
        n * loggingListener.call('')

        then:
        0 * partialListener.call(_)
        1 * lineListener.call('single line')
        1 * loggingListener.call('single line')

        where:
        n << (1..3)
    }

    @Unroll
    def "new-line characters should be blank, #n new-line characters"() {
        when:
        utf8bytes("line1${'\n' * n}line2").each { stream.write(it) }
        stream.close()

        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line1')
        1 * loggingListener.call('line1')

        then:
        0       * partialListener.call(_)
        (n - 1) * lineListener.call('')
        (n - 1) * loggingListener.call('')

        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line2')
        1 * loggingListener.call('line2')

        where:
        n << (1..3)
    }


    @Unroll
    def "flush or close has no effect if empty, flush #f, close #c"() {
        when:
        repeat(f) { stream.flush() }
        repeat(c) { stream.close() }

        then:
        0 * partialListener.call(_)
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        where:
        f | c
        0 | 1
        0 | 2
        1 | 0
        1 | 1
        2 | 0
        2 | 1
    }

    @Unroll
    def "listener should be called when received a byte, closed #c"() {
        given:
        stream.write(0x23)

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('#')
        1 * loggingListener.call('#')

        where:
        c << (1..2)
    }

    @Unroll
    def "listener should be called when received a byte, flushed #f and closed #c"() {
        given:
        stream.write(0x23)

        when: repeat(f) { stream.flush() }
        then:
        1 * partialListener.call('#')
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('#')
        1 * loggingListener.call('#')

        where:
        f | c
        1 | 1
        1 | 2
        2 | 1
    }

    @Unroll
    def "listener should be called when received a byte, flushed #f and closed #c, partial matched"() {
        given:
        stream.write(0x23)

        when: repeat(f) { stream.flush() }
        then:
        1 * partialListener.call('#') >> true
        then:
        0 * lineListener.call(_)
        1 * loggingListener.call('#')

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        where:
        f | c
        1 | 1
        1 | 2
        2 | 1
    }

    @Unroll
    def "listener should be called when received a string, closed #c"() {
        given:
        utf8bytes('single line').each { stream.write(it) }

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('single line')
        1 * loggingListener.call('single line')

        where:
        c << (1..2)
    }

    @Unroll
    def "listener should be called when received a string, flushed #f and closed #c"() {
        given:
        utf8bytes('single line').each { stream.write(it) }

        when: repeat(f) { stream.flush() }
        then:
        1 * partialListener.call('single line')
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('single line')
        1 * loggingListener.call('single line')

        where:
        f | c
        1 | 1
        1 | 2
        2 | 1
    }

    @Unroll
    def "listener should be called when received a string, flushed #f and closed #c, partial matched"() {
        given:
        utf8bytes('single line').each { stream.write(it) }

        when: repeat(f) { stream.flush() }
        then:
        1 * partialListener.call('single line') >> true
        then:
        0 * lineListener.call(_)
        1 * loggingListener.call('single line')

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        where:
        f | c
        1 | 1
        1 | 2
        2 | 1
    }

    @Unroll
    def "listener should be called on each lines when closed #c"() {
        given:
        utf8bytes('line3\nline4\nline5').each { stream.write(it) }

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line5')
        1 * loggingListener.call('line5')

        where:
        c << (1..2)
    }

    @Unroll
    def "listener should be called on each lines when flushed #f and closed #c"() {
        given:
        utf8bytes('line3\nline4\nline5').each { stream.write(it) }

        when: repeat(f) { stream.flush() }
        then:
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')
        then:
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')
        then:
        1 * partialListener.call('line5')

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line5')
        1 * loggingListener.call('line5')

        where:
        f | c
        1 | 1
        1 | 2
        2 | 1
    }

    @Unroll
    def "listener should be called on each lines when flushed #f and closed #c, partial matched"() {
        given:
        utf8bytes('line3\nline4\nline5').each { stream.write(it) }

        when: repeat(f) { stream.flush() }
        then:
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')
        then:
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')
        then:
        1 * partialListener.call('line5') >> true
        then:
        0 * lineListener.call(_)
        1 * loggingListener.call('line5')

        when: repeat(c) { stream.close() }
        then:
        0 * partialListener.call(_)
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        where:
        f | c
        1 | 1
        1 | 2
        2 | 1
    }

    @Unroll
    def "listener should be called on each lines if it flushed in the middle of line, #f times"() {
        when:
        utf8bytes('lin').each { stream.write(it) }
        repeat(f) { stream.flush() }
        then:
        1 * partialListener.call('lin')
        0 * lineListener.call(_)
        0 * loggingListener.call(_)

        when:
        utf8bytes('e3\nli').each { stream.write(it) }
        repeat (f) { stream.flush() }
        then:
        1 * partialListener.call('li')
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')

        when:
        utf8bytes('ne4').each { stream.write(it) }
        stream.close()
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')

        where:
        f << (1..3)
    }

    @Unroll
    def "listener should be called on each lines if it flushed in the middle of line, #f times, partial matched"() {
        when:
        utf8bytes('lin').each { stream.write(it) }
        repeat(f) { stream.flush() }
        then:
        1 * partialListener.call('lin') >> true
        then:
        0 * lineListener.call(_)
        1 * loggingListener.call('lin')

        when:
        utf8bytes('e3\nli').each { stream.write(it) }
        repeat(f) { stream.flush() }
        then:
        1 * lineListener.call('e3')
        1 * loggingListener.call('e3')
        then:
        1 * partialListener.call('li')

        when:
        utf8bytes('ne4').each { stream.write(it) }
        stream.close()
        then:
        0 * partialListener.call(_)
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')

        where:
        f << (1..3)
    }


    def "listener should be called even by left shift operator"() {
        when:
        stream << utf8bytes('line3\nline4\nline5')
        stream.close()

        then:
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')
        then:
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')
        then:
        1 * lineListener.call('line5')
        1 * loggingListener.call('line5')
    }

    def "listener should be called even with a writer"() {
        when:
        stream.withWriter('UTF-8') { it << 'line3\nline4\nline5' }

        then:
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')
        then:
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')
        then:
        1 * lineListener.call('line5')
        1 * loggingListener.call('line5')
    }

    def "listener should handle new-line character of multi-platforms"() {
        when:
        stream << utf8bytes('line1\r\n\rline2\u2028line3\n\u2029line4\u0085line5')
        stream.close()

        then:
        1 * lineListener.call('line1')
        1 * loggingListener.call('line1')
        then:
        1 * lineListener.call('')
        1 * loggingListener.call('')
        then:
        1 * lineListener.call('line2')
        1 * loggingListener.call('line2')
        then:
        1 * lineListener.call('line3')
        1 * loggingListener.call('line3')
        then:
        1 * lineListener.call('')
        1 * loggingListener.call('')
        then:
        1 * lineListener.call('line4')
        1 * loggingListener.call('line4')
        then:
        1 * lineListener.call('line5')
        1 * loggingListener.call('line5')
    }

    def "listener should recognize charset"() {
        given:
        stream = new LineOutputStream('Shift_JIS')
        stream.listenLine(lineListener)

        when:
        stream.withWriter('Shift_JIS') {
            it << 'これは\n日本語です'
        }

        then: 1 * lineListener.call('これは')
        then: 1 * lineListener.call('日本語です')
    }


    static repeat(int n, Closure doing) {
        assert n >= 0
        ([null] * n).each(doing)
    }

    static utf8bytes(String s) {
        s.getBytes('UTF-8')
    }

}
