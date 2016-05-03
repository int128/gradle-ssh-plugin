package org.hidetake.groovy.ssh.interaction

import spock.lang.Specification
import spock.lang.Unroll

class RuleSpec extends Specification {

    static final WILDCARD = Wildcard.instance
    static final ACTION = {}

    def "factory should throw error if no buffer rule is given"() {
        when:
        new Rule(condition, ACTION)

        then:
        AssertionError e = thrown()
        e.message.contains('should contain one of')

        where:
        condition << [[:], [from: Stream.StandardOutput]]
    }

    @Unroll
    def "rule (line: #param) should return #expected and leave #remainBytes bytes on #input"() {
        given:
        def rule = new Rule(line: param, ACTION)
        def buffer = new Buffer('UTF-8')
        buffer.append(input)

        when:
        def matcher = rule.match(Stream.StandardOutput, buffer)

        then:
        matcher?.resultAsString == expected
        buffer.size() == remainBytes

        where:
        param    | input            | expected      | remainBytes
        'some'   | 'some'           | null          | 4
        'some'   | 'some\r'         | 'some'        | 0
        'some'   | 'some\n'         | 'some'        | 0
        'some'   | 'some\r\n'       | 'some'        | 0
        'some'   | 'some\rsome'     | 'some'        | 4
        'some'   | 'some\nsome'     | 'some'        | 4
        'some'   | 'some\r\nsome'   | 'some'        | 4
        'some'   | 'none'           | null          | 4
        'some'   | 'none\r'         | null          | 5
        'some'   | 'none\n'         | null          | 5
        'some'   | 'none\r\n'       | null          | 6
        WILDCARD | '\r'             | ''            | 0
        WILDCARD | '\n'             | ''            | 0
        WILDCARD | '\r\n'           | ''            | 0
        WILDCARD | '\rsome'         | ''            | 4
        WILDCARD | '\nsome'         | ''            | 4
        WILDCARD | '\r\nsome'       | ''            | 4
        WILDCARD | 'some'           | null          | 4
        WILDCARD | 'some\r'         | 'some'        | 0
        WILDCARD | 'some\n'         | 'some'        | 0
        WILDCARD | 'some\r\n'       | 'some'        | 0
        WILDCARD | 'some\rsome'     | 'some'        | 4
        WILDCARD | 'some\nsome'     | 'some'        | 4
        WILDCARD | 'some\r\nsome'   | 'some'        | 4
        ~/.+me/  | 'some'           | null          | 4
        ~/.+me/  | 'some\r'         | 'some\r'      | 0
        ~/.+me/  | 'some\n'         | 'some\n'      | 0
        ~/.+me/  | 'some\r\n'       | 'some\r\n'    | 0
        ~/.+me/  | 'some\rsome'     | 'some\r'      | 4
        ~/.+me/  | 'some\nsome'     | 'some\n'      | 4
        ~/.+me/  | 'some\r\nsome'   | 'some\r\n'    | 4
        ~/.+me/  | 'none'           | null          | 4
        ~/.+me/  | 'none\r'         | null          | 5
        ~/.+me/  | 'none\n'         | null          | 5
        ~/.+me/  | 'none\r\n'       | null          | 6
    }

    def "factory should throw error if {line: null} is given"() {
        when:
        new Rule(line: null, ACTION)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "rule (partial: #param) should return #expected and leave #remainBytes bytes on #input"() {
        given:
        def rule = new Rule(partial: param, ACTION)
        def buffer = new Buffer('UTF-8')
        buffer.append(input)

        when:
        def matcher = rule.match(Stream.StandardOutput, buffer)

        then:
        matcher?.resultAsString == expected
        buffer.size() == remainBytes

        where:
        param    | input            | expected  | remainBytes
        'some'   | 'some'           | 'some'    | 0
        'some'   | 'some\r'         | null      | 5
        'some'   | 'some\n'         | null      | 5
        'some'   | 'some\r\n'       | null      | 6
        'some'   | 'some\rsome'     | null      | 9
        'some'   | 'some\nsome'     | null      | 9
        'some'   | 'some\r\nsome'   | null      | 10
        'some'   | 'none'           | null      | 4
        'some'   | 'none\r'         | null      | 5
        'some'   | 'none\n'         | null      | 5
        'some'   | 'none\r\n'       | null      | 6
        WILDCARD | '\r'             | null      | 1
        WILDCARD | '\n'             | null      | 1
        WILDCARD | '\r\n'           | null      | 2
        WILDCARD | '\rsome'         | null      | 5
        WILDCARD | '\nsome'         | null      | 5
        WILDCARD | '\r\nsome'       | null      | 6
        WILDCARD | 'some'           | 'some'    | 0
        WILDCARD | 'some\r'         | null      | 5
        WILDCARD | 'some\n'         | null      | 5
        WILDCARD | 'some\r\n'       | null      | 6
        WILDCARD | 'some\rsome'     | null      | 9
        WILDCARD | 'some\nsome'     | null      | 9
        WILDCARD | 'some\r\nsome'   | null      | 10
        ~/.+me/  | 'some'           | 'some'    | 0
        ~/.+me/  | 'some\r'         | null      | 5
        ~/.+me/  | 'some\n'         | null      | 5
        ~/.+me/  | 'some\r\n'       | null      | 6
        ~/.+me/  | 'some\rsome'     | null      | 9
        ~/.+me/  | 'some\nsome'     | null      | 9
        ~/.+me/  | 'some\r\nsome'   | null      | 10
        ~/.+me/  | 'none'           | null      | 4
        ~/.+me/  | 'none\r'         | null      | 5
        ~/.+me/  | 'none\n'         | null      | 5
        ~/.+me/  | 'none\r\n'       | null      | 6
    }

    def "factory should throw error if {partial: null} is given"() {
        when:
        new Rule(partial: null, ACTION)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "rule (from: #param) should return #expected on the event from #input"() {
        given:
        def rule = new Rule(line: WILDCARD, from: param, ACTION)
        def buffer = new Buffer('UTF-8')
        buffer.append('something\n')

        when:
        def matcher = rule.match(input, buffer)

        then:
        matcher?.resultAsString == expected

        where:
        param                 | input                 | expected
        Stream.StandardOutput | Stream.StandardOutput | 'something'
        Stream.StandardError  | Stream.StandardError  | 'something'
        null                  | Stream.StandardOutput | 'something'
        null                  | Stream.StandardError  | 'something'
        Stream.StandardOutput | Stream.StandardError  | null
        Stream.StandardError  | Stream.StandardOutput | null
    }

}
