package org.hidetake.groovy.ssh.interaction

import groovy.transform.Immutable

/**
 * A rule of interaction with the stream.
 *
 * @author Hidetake Iwata
 */
@Immutable
class InteractionRule {
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

    String toString() {
        "${InteractionRule.getSimpleName()}${condition}"
    }
}
