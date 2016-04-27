package org.hidetake.groovy.ssh.interaction

import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Matcher

class RuleSpec extends Specification {

    static final WILDCARD = Wildcard.instance
    static final ACTION = {}

    def "generate should throw error if event is not given"() {
        when:
        new Rule(condition, ACTION)

        then:
        AssertionError e = thrown()
        e.message.contains('should contain one of')

        where:
        condition << [[:], [from: Stream.StandardOutput]]
    }

    @Unroll
    def "rule (line: #expected) should return #matched on #actual, #event, #lineNumber"() {
        given:
        def rule = new Rule(line: expected, ACTION)

        expect:
        rule.matcher(Stream.StandardOutput, event, lineNumber, actual) == matched

        where:
        expected | actual | event         | lineNumber | matched
        'some'   | 'some' | Event.Line    | 1          | 'some'
        'some'   | 'some' | Event.Line    | 2          | 'some'
        'some'   | 'none' | Event.Line    | 1          | null
        'some'   | 'some' | Event.Partial | 1          | null
        'some'   | 'none' | Event.Partial | 1          | null
        WILDCARD | 'some' | Event.Line    | 1          | 'some'
        WILDCARD | 'some' | Event.Line    | 2          | 'some'
        WILDCARD | 'some' | Event.Partial | 1          | null
        ~/.+me$/ | 'none' | Event.Line    | 1          | null
        ~/.+me$/ | 'some' | Event.Partial | 1          | null
        ~/.+me$/ | 'none' | Event.Partial | 1          | null
    }

    @Unroll
    def "rule (line: #expected) should return Matcher on #actual, #event, #lineNumber"() {
        given:
        def rule = new Rule(line: expected, ACTION)

        when:
        Matcher matcher = rule.matcher(Stream.StandardOutput, event, lineNumber, actual)

        then:
        matcher.matches()
        matcher.group() == group0

        where:
        expected | actual | event         | lineNumber | group0
        ~/.+me$/ | 'some' | Event.Line    | 1          | 'some'
        ~/.+me$/ | 'some' | Event.Line    | 2          | 'some'
    }

    def "generate should throw error if line is null"() {
        when:
        new Rule(line: null, ACTION)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "rule (nextLine: #expected) should return #matched on #actual, #event, #lineNumber"() {
        given:
        def rule = new Rule(nextLine: expected, ACTION)

        expect:
        rule.matcher(Stream.StandardOutput, event, lineNumber, actual) == matched

        where:
        expected | actual | event         | lineNumber | matched
        'some'   | 'some' | Event.Line    | 1          | 'some'
        'some'   | 'some' | Event.Line    | 2          | null
        'some'   | 'none' | Event.Line    | 1          | null
        'some'   | 'none' | Event.Line    | 2          | null
        'some'   | 'some' | Event.Partial | 1          | null
        'some'   | 'none' | Event.Partial | 1          | null
        WILDCARD | 'some' | Event.Line    | 1          | 'some'
        WILDCARD | 'some' | Event.Line    | 2          | null
        WILDCARD | 'some' | Event.Partial | 1          | null
        WILDCARD | 'some' | Event.Partial | 2          | null
        ~/.+me$/ | 'some' | Event.Line    | 2          | null
        ~/.+me$/ | 'none' | Event.Line    | 1          | null
        ~/.+me$/ | 'none' | Event.Line    | 2          | null
        ~/.+me$/ | 'some' | Event.Partial | 1          | null
        ~/.+me$/ | 'none' | Event.Partial | 1          | null
    }

    @Unroll
    def "rule (nextLine: #expected) should return Matcher on #actual, #event, #lineNumber"() {
        given:
        def rule = new Rule(nextLine: expected, ACTION)

        when:
        Matcher matcher = rule.matcher(Stream.StandardOutput, event, lineNumber, actual)

        then:
        matcher.matches()
        matcher.group() == group0

        where:
        expected | actual | event         | lineNumber | group0
        ~/.+me$/ | 'some' | Event.Line    | 1          | 'some'
    }

    def "generate should throw error if nextLine is null"() {
        when:
        new Rule(nextLine: null, ACTION)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "rule (partial: #expected) should return #matched on #actual, #event, #lineNumber"() {
        given:
        def rule = new Rule(partial: expected, ACTION)

        expect:
        rule.matcher(Stream.StandardOutput, event, lineNumber, actual) == matched

        where:
        expected | actual | event         | lineNumber | matched
        'some'   | 'some' | Event.Partial | 1          | 'some'
        'some'   | 'some' | Event.Partial | 2          | 'some'
        'some'   | 'none' | Event.Partial | 1          | null
        'some'   | 'some' | Event.Line    | 1          | null
        'some'   | 'none' | Event.Line    | 1          | null
        WILDCARD | 'some' | Event.Partial | 1          | 'some'
        WILDCARD | 'some' | Event.Partial | 2          | 'some'
        WILDCARD | 'some' | Event.Line    | 1          | null
        ~/.+me$/ | 'none' | Event.Partial | 1          | null
        ~/.+me$/ | 'some' | Event.Line    | 1          | null
        ~/.+me$/ | 'none' | Event.Line    | 1          | null
    }


    @Unroll
    def "rule (partial: #expected) should return Matcher on #actual, #event, #lineNumber"() {
        given:
        def rule = new Rule(partial: expected, ACTION)

        when:
        Matcher matcher = rule.matcher(Stream.StandardOutput, event, lineNumber, actual)

        then:
        matcher.matches()
        matcher.group() == group0

        where:
        expected | actual | event         | lineNumber | group0
        ~/.+me$/ | 'some' | Event.Partial | 1          | 'some'
        ~/.+me$/ | 'some' | Event.Partial | 2          | 'some'
    }

    def "generate should throw error if partial is null"() {
        when:
        new Rule(partial: null, ACTION)

        then:
        thrown(IllegalArgumentException)
    }


    @Unroll
    def "rule (from: #expected) should return #matched on the event from #actual"() {
        given:
        def rule = new Rule(line: WILDCARD, from: expected, ACTION)

        expect:
        rule.matcher(actual, Event.Line, 1, 'something') == matched

        where:
        expected              | actual                | matched
        Stream.StandardOutput | Stream.StandardOutput | 'something'
        Stream.StandardError  | Stream.StandardError  | 'something'
        null                  | Stream.StandardOutput | 'something'
        null                  | Stream.StandardError  | 'something'
        Stream.StandardOutput | Stream.StandardError  | null
        Stream.StandardError  | Stream.StandardOutput | null
    }

}
