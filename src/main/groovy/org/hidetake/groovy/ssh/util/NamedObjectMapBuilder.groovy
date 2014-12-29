package org.hidetake.groovy.ssh.util

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An evaluator of a closure which contains named objects.
 *
 * @param < T > type of the named object
 *
 * @author Hidetake Iwata
 */
class NamedObjectMapBuilder<T> {
    private final Class<T> clazz

    private final NamedObjectMap<T> map

    def NamedObjectMapBuilder(Class<T> clazz, NamedObjectMap<T> map) {
        this.clazz = clazz
        this.map = map
    }

    def methodMissing(String name, args) {
        assert name

        assert args instanceof Object[]
        assert args.length == 1
        assert args[0] instanceof Closure
        def closure = args[0] as Closure

        def namedObject = clazz.newInstance(name)
        callWithDelegate(closure, namedObject)
        map.add(namedObject)

        namedObject
    }
}
