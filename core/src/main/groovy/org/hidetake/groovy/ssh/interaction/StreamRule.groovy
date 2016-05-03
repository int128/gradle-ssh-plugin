package org.hidetake.groovy.ssh.interaction

interface StreamRule {
    boolean matches(Stream stream)

    static final anyRule            = [matches: { Stream s -> true }] as StreamRule
    static final standardOutputRule = [matches: { Stream s -> s == Stream.StandardOutput }] as StreamRule
    static final standardErrorRule  = [matches: { Stream s -> s == Stream.StandardError }] as StreamRule

    static class Factory {
        static StreamRule create(fromParameter) {
            switch (fromParameter) {
                case null:                  return anyRule
                case Wildcard:              return anyRule
                case Stream.StandardOutput: return standardOutputRule
                case Stream.StandardError:  return standardErrorRule
                default: throw new IllegalArgumentException("parameter must be Wildcard or Stream: from=$fromParameter")
            }
        }
    }
}
