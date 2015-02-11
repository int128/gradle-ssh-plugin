package org.hidetake.groovy.ssh.interaction

import java.util.regex.Pattern

/**
 * Matcher classes.
 *
 * @author hidetake.org
 */
class Matcher {
    static generate(Map condition) {
        def streamMatcher = generateStreamMatcher(condition)
        def eventMatcher = generateEventMatcher(condition)
        return { Stream stream, Event event, long lineNumber, String text ->
            streamMatcher(stream) && eventMatcher(event, lineNumber, text)
        }
    }

    private static generateEventMatcher(Map<String, Object> condition) {
        def supportedKeys = eventMatcherMap.keySet()
        def event = condition.find { it.key in supportedKeys }
        assert event, "Key should be one of $supportedKeys: $condition"

        def eventMatcher = eventMatcherMap[event.key]
        def textMatcher = generateTextMatcher(event.value)
        return { Event e, long n, String s ->
            eventMatcher(e, n) && textMatcher(s)
        }
    }

    private static final eventMatcherMap = [
            nextLine: { Event e, long n -> e == Event.Line && n == 1 },
            line:     { Event e, long n -> e == Event.Line },
            partial:  { Event e, long n -> e == Event.Partial }
    ]

    private static generateTextMatcher(expected) {
        switch (expected) {
            case Wildcard: return textMatcherMap.any
            case Pattern:  return textMatcherMap.pattern.curry(expected)
            case String:   return textMatcherMap.exact.curry(expected)
            default:       throw new IllegalArgumentException("Invalid value: $expected")
        }
    }

    private static final textMatcherMap = [
            any:     { String s -> true },
            pattern: { Pattern e, String s -> s.matches(e) },
            exact:   { String e, String s -> e == s }
    ]

    private static generateStreamMatcher(Map condition) {
        switch (condition.from) {
            case null:     return streamMatcherMap.any
            case Wildcard: return streamMatcherMap.any
            case Stream:   return streamMatcherMap.exact.curry(condition.from)
            default:       throw new IllegalArgumentException("Invalid stream: from=${condition.from}")
        }
    }

    private static final streamMatcherMap = [
            any:   { Stream s -> true },
            exact: { Stream e, Stream s -> e == s }
    ]
}
