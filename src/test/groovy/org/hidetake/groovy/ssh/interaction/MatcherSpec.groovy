package org.hidetake.groovy.ssh.interaction

import spock.lang.Specification
import spock.lang.Unroll

class MatcherSpec extends Specification {

    static final WILDCARD = Wildcard.instance

    def "generate should throw error if event is not given"() {
        when:
        Matcher.generate(condition)

        then:
        AssertionError e = thrown()
        e.message.contains('should be one of')

        where:
        condition << [[:], [from: Stream.StandardOutput]]
    }

    @Unroll
    def "matcher (line: #expected) should return #matched on #actual, #event, #lineNumber"() {
        given:
        def matcher = Matcher.generate(line: expected)

        expect:
        matcher(Stream.StandardOutput, event, lineNumber, actual) == matched

        where:
        expected | actual | event         | lineNumber | matched
        'some'   | 'some' | Event.Line    | 1          | true
        'some'   | 'some' | Event.Line    | 2          | true
        'some'   | 'none' | Event.Line    | 1          | false
        'some'   | 'some' | Event.Partial | 1          | false
        'some'   | 'none' | Event.Partial | 1          | false
        WILDCARD | 'some' | Event.Line    | 1          | true
        WILDCARD | 'some' | Event.Line    | 2          | true
        WILDCARD | 'some' | Event.Partial | 1          | false
        ~/.+me$/ | 'some' | Event.Line    | 1          | true
        ~/.+me$/ | 'some' | Event.Line    | 2          | true
        ~/.+me$/ | 'none' | Event.Line    | 1          | false
        ~/.+me$/ | 'some' | Event.Partial | 1          | false
        ~/.+me$/ | 'none' | Event.Partial | 1          | false
    }

    def "generate should throw error if line is null"() {
        when:
        Matcher.generate(line: null)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "matcher (nextLine: #expected) should return #matched on #actual, #event, #lineNumber"() {
        given:
        def matcher = Matcher.generate(nextLine: expected)

        expect:
        matcher(Stream.StandardOutput, event, lineNumber, actual) == matched

        where:
        expected | actual | event         | lineNumber | matched
        'some'   | 'some' | Event.Line    | 1          | true
        'some'   | 'some' | Event.Line    | 2          | false
        'some'   | 'none' | Event.Line    | 1          | false
        'some'   | 'none' | Event.Line    | 2          | false
        'some'   | 'some' | Event.Partial | 1          | false
        'some'   | 'none' | Event.Partial | 1          | false
        WILDCARD | 'some' | Event.Line    | 1          | true
        WILDCARD | 'some' | Event.Line    | 2          | false
        WILDCARD | 'some' | Event.Partial | 1          | false
        WILDCARD | 'some' | Event.Partial | 2          | false
        ~/.+me$/ | 'some' | Event.Line    | 1          | true
        ~/.+me$/ | 'some' | Event.Line    | 2          | false
        ~/.+me$/ | 'none' | Event.Line    | 1          | false
        ~/.+me$/ | 'none' | Event.Line    | 2          | false
        ~/.+me$/ | 'some' | Event.Partial | 1          | false
        ~/.+me$/ | 'none' | Event.Partial | 1          | false
    }

    def "generate should throw error if nextLine is null"() {
        when:
        Matcher.generate(nextLine: null)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "matcher (partial: #expected) should return #matched on #actual, #event, #lineNumber"() {
        given:
        def matcher = Matcher.generate(partial: expected)

        expect:
        matcher(Stream.StandardOutput, event, lineNumber, actual) == matched

        where:
        expected | actual | event         | lineNumber | matched
        'some'   | 'some' | Event.Partial | 1          | true
        'some'   | 'some' | Event.Partial | 2          | true
        'some'   | 'none' | Event.Partial | 1          | false
        'some'   | 'some' | Event.Line    | 1          | false
        'some'   | 'none' | Event.Line    | 1          | false
        WILDCARD | 'some' | Event.Partial | 1          | true
        WILDCARD | 'some' | Event.Partial | 2          | true
        WILDCARD | 'some' | Event.Line    | 1          | false
        ~/.+me$/ | 'some' | Event.Partial | 1          | true
        ~/.+me$/ | 'some' | Event.Partial | 2          | true
        ~/.+me$/ | 'none' | Event.Partial | 1          | false
        ~/.+me$/ | 'some' | Event.Line    | 1          | false
        ~/.+me$/ | 'none' | Event.Line    | 1          | false
    }

    def "generate should throw error if partial is null"() {
        when:
        Matcher.generate(partial: null)

        then:
        thrown(IllegalArgumentException)
    }


    @Unroll
    def "matcher (from: #expected) should return #matched on the event from #actual"() {
        given:
        def matcher = Matcher.generate(line: WILDCARD, from: expected)

        expect:
        matcher(actual, Event.Line, 1, 'something') == matched

        where:
        expected              | actual                | matched
        Stream.StandardOutput | Stream.StandardOutput | true
        Stream.StandardError  | Stream.StandardError  | true
        null                  | Stream.StandardOutput | true
        null                  | Stream.StandardError  | true
        Stream.StandardOutput | Stream.StandardError  | false
        Stream.StandardError  | Stream.StandardOutput | false
    }

}
