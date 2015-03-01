package org.hidetake.groovy.ssh.core.container

import groovy.util.logging.Slf4j

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * An evaluator of a closure which contains named objects.
 *
 * @param < T > type of the named object
 *
 * @author Hidetake Iwata
 */
@Slf4j
class ContainerBuilder<T> {
    private final Class<T> clazz

    private final Container<T> container

    def ContainerBuilder(Class<T> clazz, Container<T> container) {
        this.clazz = clazz
        this.container = container
    }

    T methodMissing(String name, args) {
        assert args instanceof Object[]

        try {
            assert args.length == 1 && args[0] instanceof Closure,
                    'ContainerBuilder handles a method only if argument is one closure'
            assert args[0].delegate instanceof Closure && !clazz.isInstance(args[0].delegate.delegate),
                    "ContainerBuilder does not handle any methods in the closure of $clazz"
        } catch (AssertionError e) {
            log.debug(e.localizedMessage)
            throw new MissingMethodException(name, ContainerBuilder, args)
        }

        def closure = args[0] as Closure
        T namedObject = clazz.newInstance(name)
        callWithDelegate(closure, namedObject)
        container.add(namedObject)
        namedObject
    }
}
