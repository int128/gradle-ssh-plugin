package org.hidetake.gradle.ssh.internal.operation.interaction

import org.hidetake.gradle.ssh.api.operation.interaction.Stream
import org.hidetake.gradle.ssh.api.operation.interaction.Wildcard
import org.hidetake.gradle.ssh.internal.operation.interaction.InteractionRule.Event
import spock.lang.Specification
import spock.lang.Unroll

class InteractionRuleSpec extends Specification {

    static final WILDCARD = Wildcard.instance

    def "event parameter required"() {
        when:
        InteractionRule.create(condition, {})

        then:
        AssertionError e = thrown()
        e.message.contains('should be one of')

        where:
        condition << [[:], [from: Stream.StandardOutput]]
    }

    @Unroll
    def "line match"() {
        given:
        def rule = InteractionRule.create(line: expected, {})
        def stream = Stream.StandardOutput

        expect:
        rule.matcher(stream, event, lineNumber, actual) == matched

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

    def "line match requires non-null"() {
        when:
        InteractionRule.create(line: null, {})

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "next line match"() {
        given:
        def rule = InteractionRule.create(nextLine: expected, {})
        def stream = Stream.StandardOutput

        expect:
        rule.matcher(stream, event, lineNumber, actual) == matched

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

    def "next line match requires non-null"() {
        when:
        InteractionRule.create(nextLine: null, {})

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "partial match"() {
        given:
        def rule = InteractionRule.create(partial: expected, {})
        def stream = Stream.StandardOutput

        expect:
        rule.matcher(stream, event, lineNumber, actual) == matched

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

    def "partial match requires non-null"() {
        when:
        InteractionRule.create(line: null, {})

        then:
        thrown(IllegalArgumentException)
    }

    def "stream match"() {
        given:
        def rule = InteractionRule.create(line: WILDCARD, from: expected, {})

        expect:
        rule.matcher(actual, Event.Line, 1, 'something') == matched

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
