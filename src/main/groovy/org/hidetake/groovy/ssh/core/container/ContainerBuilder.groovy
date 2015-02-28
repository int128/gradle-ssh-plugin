package org.hidetake.groovy.ssh.core.container

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An evaluator of a closure which contains named objects.
 *
 * @param < T > type of the named object
 *
 * @author Hidetake Iwata
 */
class ContainerBuilder<T> {
    private final Class<T> clazz

    private final Container<T> container

    def ContainerBuilder(Class<T> clazz, Container<T> container) {
        this.clazz = clazz
        this.container = container
    }

    T methodMissing(String name, args) {
        assert args instanceof Object[]

        if (!(args.length == 1 && args[0] instanceof Closure)) {
            throw new MissingMethodException(name, ContainerBuilder, args)
        }

        def closure = args[0] as Closure
        T namedObject = clazz.newInstance(name)
        callWithDelegate(closure, namedObject)
        container.add(namedObject)

        namedObject
    }
}
