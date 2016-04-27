package org.hidetake.groovy.ssh.interaction

import groovy.transform.EqualsAndHashCode

import java.util.regex.Pattern

/**
 * A rule of interaction with the stream.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
class Rule {

    final Map<String, Object> condition

    private final Closure<Boolean> streamMatcher
    private final Closure<Boolean> eventMatcher
    private final Closure /* <String, Matcher or null> */ textMatcher

    final Closure action

    /**
     * Constructor.
     *
     * @param condition1
     * @param action1 action to be run when condition is satisfied
     * @return an instance
     */
    def Rule(Map<String, Object> condition1, Closure action1) {
        condition = condition1
        action = action1

        streamMatcher = StreamMatcher.generate(condition1.from)
        def eventParameter = EventMatcher.extractParameter(condition1)
        eventMatcher = EventMatcher.generate(eventParameter)
        textMatcher = TextMatcher.generate(eventParameter)
    }

    /**
     * Examine match.
     *
     * @param stream
     * @param event
     * @param lineNumber
     * @param text
     * @return a {@link String} if wildcard or string rule is matched, or a {@link java.util.regex.Matcher} if pattern rule is matched, null otherwise
     */
    def matcher(Stream stream, Event event, long lineNumber, String text) {
        (streamMatcher(stream) && eventMatcher(event, lineNumber)) ? textMatcher(text) : null
    }

    String toString() {
        "${Rule.simpleName}${condition}"
    }


    static class StreamMatcher {
        private static final streamMatcherAny = { Stream s -> true }
        private static final streamMatcherExact = { Stream expected, Stream s -> expected == s }

        static Closure<Boolean> generate(fromParameter) {
            switch (fromParameter) {
                case null: return streamMatcherAny
                case Wildcard: return streamMatcherAny
                case Stream: return streamMatcherExact.curry(fromParameter)
                default: throw new IllegalArgumentException("parameter must be Wildcard or Stream: from=$fromParameter")
            }
        }
    }


    static class EventMatcher {
        private static final eventMatcherMap = [
                nextLine: { Event e, long n -> e == Event.Line && n == 1L },
                line    : { Event e, long n -> e == Event.Line },
                partial : { Event e, long n -> e == Event.Partial }
        ]

        static extractParameter(Map<String, Object> condition) {
            def availableEvents = eventMatcherMap.keySet()
            def events = condition.findAll { it.key in availableEvents }
            assert events.size() == 1, "Condition should contain one of $availableEvents: $condition"
            events.entrySet().iterator().next()
        }

        static generate(Map.Entry<String, Object> event) {
            eventMatcherMap[event.key]
        }
    }


    static class TextMatcher {
        private static final textMatcherAny = { String actual -> actual }
        private static final textMatcherPattern = { Pattern expected, String actual ->
            def matcher = expected.matcher(actual)
            matcher.matches() ? matcher : null
        }
        private static final textMatcherString = { String expected, String actual ->
            (expected == actual) ? actual : null
        }

        static generate(Map.Entry<String, Object> event) {
            switch (event.value) {
                case Wildcard: return textMatcherAny
                case Pattern: return textMatcherPattern.curry(event.value)
                case String: return textMatcherString.curry(event.value)
                default: throw new IllegalArgumentException("parameter must be Wildcard, Pattern or String: $event")
            }
        }
    }

}
