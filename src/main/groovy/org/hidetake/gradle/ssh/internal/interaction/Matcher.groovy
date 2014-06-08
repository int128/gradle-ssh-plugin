package org.hidetake.gradle.ssh.internal.interaction

import org.hidetake.gradle.ssh.plugin.interaction.Stream
import org.hidetake.gradle.ssh.plugin.interaction.Wildcard

import java.util.regex.Pattern

/**
 * Matcher classes.
 *
 * @author hidetake.org
 */
class Matcher {
    static generate(Map condition) {
        def streamMatcher = StreamMatcher.generate(condition)
        def eventMatcher = EventMatcher.generate(condition)
        return { Stream stream, Event event, long lineNumber, String text ->
            streamMatcher(stream) && eventMatcher(event, lineNumber, text)
        }
    }

    static enum Event {
        Line,
        Partial
    }

    private static enum EventMatcher {
        nextLine ({ Event e, long n -> e == Event.Line && n == 1 }),
        line     ({ Event e, long n -> e == Event.Line }),
        partial  ({ Event e, long n -> e == Event.Partial })

        final Closure<Boolean> closure

        private EventMatcher(Closure<Boolean> closure1) {
            closure = closure1
        }

        static Closure<Boolean> generate(Map condition) {
            def supportedKeys = values()*.name()
            def eventKeys = supportedKeys.intersect(condition.keySet())
            assert eventKeys.size() == 1, "Key should be one of $supportedKeys but found $eventKeys"
            def eventKey = eventKeys.first()

            def eventMatcher = valueOf(eventKey).closure
            def textMatcher = TextMatcher.generate(condition[eventKey])
            return { Event e, long n, String s ->
                eventMatcher(e, n) && textMatcher(s)
            }
        }
    }

    private static enum TextMatcher {
        any     ({ String s -> true }),
        pattern ({ Pattern e, String s -> s.matches(e) }),
        exact   ({ String e, String s -> e == s })

        final Closure<Boolean> closure

        private TextMatcher(Closure<Boolean> closure1) {
            closure = closure1
        }

        static Closure<Boolean> generate(expected) {
            switch (expected) {
                case Wildcard: return any.closure
                case Pattern:  return pattern.closure.curry(expected)
                case String:   return exact.closure.curry(expected)
                default:       throw new IllegalArgumentException("Invalid value: $expected")
            }
        }
    }

    private static enum StreamMatcher {
        any   ({ Stream s -> true }),
        exact ({ Stream e, Stream s -> e == s })

        final Closure<Boolean> closure

        private StreamMatcher(Closure<Boolean> closure1) {
            closure = closure1
        }

        static Closure<Boolean> generate(Map condition) {
            switch (condition.from) {
                case null:     return any.closure
                case Wildcard: return any.closure
                case Stream:   return exact.closure.curry(condition.from)
                default:       throw new IllegalArgumentException("Invalid stream: from=${condition.from}")
            }
        }
    }
}
