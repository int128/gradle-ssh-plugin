package org.hidetake.groovy.ssh.interaction

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * A rule of interaction with the stream.
 *
 * @author Hidetake Iwata
 */
@EqualsAndHashCode
@ToString
class Rule {
    /**
     * A map of condition.
     */
    final Map<String, Object> condition

    /**
     * A closure of matcher.
     * This closure will be called with arguments:
     * <ul>
     *   <li>Stream stream
     *   <li>Event event
     *   <li>long lineNumber
     *   <li>String text
     * </ul>
     * and should return boolean whether condition is satisfied.
     */
    final Closure<Boolean> matcher

    /**
     * An action closure.
     * This closure will be called with a matched string when condition is satisfied.
     */
    final Closure action

    def Rule(Map<String, Object> condition1, Closure action1) {
        condition = condition1
        action = action1
        matcher = Matcher.generate(condition)
        assert condition
        assert action
        assert matcher
    }

    String toString() {
        "${Rule.getSimpleName()}${condition}"
    }
}
