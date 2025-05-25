package org.hidetake.groovy.ssh.interaction

import java.util.regex.Matcher

class MatchResult<E> {

    private final Rule rule
    private final E result

    def MatchResult(Rule rule1, E result1) {
        rule = rule1
        result = result1
    }

    def getActionWithResult() {
        rule.action.curry(result)
    }

    def getResultAsString() {
        if (result instanceof Matcher) {
            result.group()
        } else if (result instanceof byte[]) {
            "byte[$result.length]"
        } else {
            result.toString()
        }
    }

    @Override
    String toString() {
        "$rule -> $resultAsString"
    }

}
