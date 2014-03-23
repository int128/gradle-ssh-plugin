package org.hidetake.gradle.ssh.internal.operation.interaction

import groovy.transform.Immutable
import groovy.transform.TupleConstructor
import org.hidetake.gradle.ssh.api.operation.interaction.Stream
import org.hidetake.gradle.ssh.api.operation.interaction.Wildcard

import java.util.regex.Pattern

/**
 * A rule of interaction with the stream.
 *
 * @author hidetake.org
 */
@Immutable
class InteractionRule {
    final Map<String, Object> condition
    final Closure<Boolean> matcher
    final Closure action

    String toString() {
        "${InteractionRule.getSimpleName()}${condition}"
    }

    static enum Event {
        Line,
        Partial
    }

    static create(Map condition, Closure action) {
        new InteractionRule(condition, generateMatcher(condition), action)
    }

    private static generateMatcher(Map condition) {
        def m = EventMatcher.find(condition)
        def e = m.key.closure
        def t = TextMatcher.generate(m.value)
        def s = StreamMatcher.generate(condition.from)
        return { Stream stream, Event event, long lineNumber, String text ->
            e(event, lineNumber) && s(stream) && t(text)
        }
    }

    @TupleConstructor
    private static enum EventMatcher {
        nextLine ({ Event e, long n -> e == Event.Line && n == 1 }),
        line     ({ Event e, long n -> e == Event.Line }),
        partial  ({ Event e, long n -> e == Event.Partial })

        final Closure<Boolean> closure
        @Lazy static names = { values()*.name() }()

        static find(Collection keys) {
            def and = names.intersect(keys)
            assert and.size() == 1, "Key should be one of $names but found $and"
            valueOf(and.first())
        }

        static find(Map map) {
            def k = find(map.keySet())
            [key: k, value: map[k.name()]]
        }
    }

    @TupleConstructor
    private static enum TextMatcher {
        any     ({ String s -> true }),
        pattern ({ Pattern e, String s -> s.matches(e) }),
        exact   ({ String e, String s -> e == s })

        final Closure<Boolean> closure

        static generate(expected) {
            switch (expected) {
                case Wildcard: return any.closure
                case Pattern:  return pattern.closure.curry(expected)
                case String:   return exact.closure.curry(expected)
            }
            throw new IllegalArgumentException("Invalid value: $expected")
        }
    }

    @TupleConstructor
    private static enum StreamMatcher {
        any   ({ Stream s -> true }),
        exact ({ Stream e, Stream s -> e == s })

        final Closure<Boolean> closure

        static generate(expected) {
            switch (expected) {
                case null:     return any.closure
                case Wildcard: return any.closure
                case Stream:   return exact.closure.curry(expected)
            }
            throw new IllegalArgumentException("Invalid stream: from=$expected")
        }
    }
}
