package org.hidetake.groovy.ssh.interaction

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A rule to inspect and manipulate buffer.
 *
 * @param <E> type of match result
 * @author Hidetake Iwata
 */
interface BufferRule<E> {

    /**
     * Inspect the buffer and return match result.
     *
     * @param buffer
     * @return match result ({@link String}, {@link Matcher} or {@code byte[]}) or null
     */
    E match(Buffer buffer)

    /**
     * Same as {@link #match(org.hidetake.groovy.ssh.interaction.Buffer)} but try match at end of stream.
     *
     * @param buffer
     * @return match result ({@link String}, {@link Matcher} or {@code byte[]}) or null
     */

    /**
     * A rule to be matched to line of given {@link String}.
     * At end of the stream, it should be matched even if it is not terminated by new-line.
     */
    static class LineStringRule implements BufferRule<String> {
        private final String expectedString
        private final Pattern pattern

        def LineStringRule(String expected) {
            expectedString = expected
            pattern = ~/^${Pattern.quote(expectedString)}(?:\r\n|[\r\n\u0085\u2028\u2029])/
        }

        @Override
        String match(Buffer buffer) {
            def matcher = pattern.matcher(buffer.toString())
            if (matcher.find()) {
                buffer.dropChars(matcher.group())
                expectedString
            } else {
                null
            }
        }
    }

    /**
     * A rule to be matched to line of given {@link Pattern}.
     * At end of the stream, it should be matched even if it is not terminated by new-line.
     */
    static class LinePatternRule implements BufferRule<Matcher> {
        private final Pattern pattern

        def LinePatternRule(Pattern expected) {
            pattern = ~/^${expected.pattern()}(?:\r\n|[\r\n\u0085\u2028\u2029])/
        }

        @Override
        Matcher match(Buffer buffer) {
            def matcher = pattern.matcher(buffer.toString())
            if (matcher.find()) {
                buffer.dropChars(matcher.group())
                matcher
            } else {
                null
            }
        }
    }

    /**
     * A rule to be matched to any line.
     * At end of the stream, it should be matched even if it is not terminated by new-line.
     */
    @Singleton
    static class AnyLineRule implements BufferRule<String> {
        @Override
        String match(Buffer buffer) {
            def matcher = buffer.toString() =~ /(.*?)(?:\r\n|[\r\n\u0085\u2028\u2029])/
            if (matcher.find()) {
                buffer.dropChars(matcher.group())
                matcher.group(1)
            } else {
                null
            }
        }
    }

    /**
     * A rule to be matched to partial string.
     */
    static class PartialStringRule implements BufferRule<String> {
        private final String expectedString

        def PartialStringRule(String expected) {
            expectedString = expected
        }

        @Override
        String match(Buffer buffer) {
            if (buffer.toString() == expectedString) {
                buffer.dropChars(expectedString)
                expectedString
            } else {
                null
            }
        }
    }

    /**
     * A rule to be matched to partial string of given {@link Pattern}.
     */
    static class PartialPatternRule implements BufferRule<Matcher> {
        private final Pattern expectedPattern

        def PartialPatternRule(Pattern expected) {
            expectedPattern = expected
        }

        @Override
        Matcher match(Buffer buffer) {
            def matcher = expectedPattern.matcher(buffer.toString())
            if (matcher.matches()) {
                buffer.dropChars(matcher.group())
                matcher
            } else {
                null
            }
        }
    }

    /**
     * A rule to be matched to any string if it is not terminated by new-line.
     */
    @Singleton
    static class AnyPartialStringRule implements BufferRule<String> {
        @Override
        String match(Buffer buffer) {
            def matcher = buffer.toString() =~ /.+(?<!\r\n|[\r\n\u0085\u2028\u2029])/
            if (matcher.matches()) {
                buffer.dropChars(matcher.group())
                matcher.group()
            } else {
                null
            }
        }
    }

    /**
     * A rule to be matched to next bytes up to given length.
     */
    static class BytesRule implements BufferRule<byte[]> {
        private final Number length

        def BytesRule(Number length1) {
            length = length1
        }

        @Override
        byte[] match(Buffer buffer) {
            long lengthAsLong = length.longValue()
            if (lengthAsLong > 0 && buffer.size() > 0) {
                buffer.dropBytes(toIntSafe(lengthAsLong))
            } else {
                null
            }
        }

        private static int toIntSafe(long number) {
            number > Integer.MAX_VALUE ? Integer.MAX_VALUE : number
        }
    }

    static class Factory {
        private static final Map<String, Closure> keys = [
                line: { value ->
                    switch (value) {
                        case Wildcard: return AnyLineRule.instance
                        case Pattern: return new LinePatternRule(value as Pattern)
                        case String: return new LineStringRule(value as String)
                        default: throw new IllegalArgumentException("line parameter accepts Wildcard, Pattern or String: $value")
                    }
                },
                partial: { value ->
                    switch (value) {
                        case Wildcard: return AnyPartialStringRule.instance
                        case Pattern: return new PartialPatternRule(value as Pattern)
                        case String: return new PartialStringRule(value as String)
                        default: throw new IllegalArgumentException("partial parameter accepts Pattern or String: $value")
                    }
                },
                bytes: { value ->
                    switch (value) {
                        case Number: return new BytesRule(value as Number)
                        default: throw new IllegalArgumentException("bytes rule accepts only integer: $value")
                    }
                },
        ]

        static BufferRule create(Map<String, Object> condition) {
            def parameter = extractParameter(condition)
            keys[parameter.key](parameter.value)
        }

        private static extractParameter(Map<String, Object> condition) {
            def availableKeys = keys.keySet()
            def parameters = condition.findAll { it.key in availableKeys }
            assert parameters.size() == 1, "Condition should contain one of $availableKeys: $condition"
            parameters.entrySet().iterator().next()
        }
    }

}
