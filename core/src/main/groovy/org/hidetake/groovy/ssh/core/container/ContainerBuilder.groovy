package org.hidetake.groovy.ssh.core.container

/**
 * An evaluator of a closure which contains named objects.
 *
 * @param < T > type of the named object
 *
 * @author Hidetake Iwata
 */
class ContainerBuilder<T> {
    private final Container<T> container

    def ContainerBuilder(Container<T> container1) {
        container = container1
    }

    T methodMissing(String name, args) {
        assert args instanceof Object[]

        try {
            assert args.length == 1

            def configurationClosure = args[0]
            assert configurationClosure instanceof Closure

            def containerClosure = configurationClosure.delegate
            assert containerClosure instanceof Closure

            def delegateOfContainerClosure = containerClosure.delegate
            assert !container.getContainerElementType().isInstance(delegateOfContainerClosure)
        } catch (AssertionError ignore) {
            throw new MissingMethodException(name, ContainerBuilder, args)
        }

        container.create(name, args[0] as Closure)
    }
}
