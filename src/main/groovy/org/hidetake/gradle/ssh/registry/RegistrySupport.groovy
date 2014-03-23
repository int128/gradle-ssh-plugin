package org.hidetake.gradle.ssh.registry

/**
 * A support class for component registry.
 *
 * @author hidetake.org
 */
class RegistrySupport {
    private final Map<Class<?>, Object> map = [:]

    /**
     * Returns the instance.
     *
     * @param clazz class of the instance
     * @return the instance
     */
    def <T> T getAt(Class<T> clazz) {
        def instance = map.get(clazz) as T
        assert instance, "No registered component: ${clazz}"
        instance
    }

    // Dummy type for generating proxy objects.
    private interface FactoryProxy {}

    interface FactoryHandler {
        void rightShift(Class instanceClass)
    }

    /**
     * Sets a auto-generated factory object.
     *
     * @param factoryClass factory class
     * @param instanceClass instance class
     * @return an auto-generated factory
     */
    FactoryHandler factory(Class factoryClass) {
        [rightShift: { Class instanceClass ->
            def factoryProxy = [:] as FactoryProxy
            factoryProxy.metaClass.create << { Object[] args -> instanceClass.newInstance(args) }
            factoryProxy.metaClass.create << { -> instanceClass.newInstance() }
            map[factoryClass] = factoryProxy
        }] as FactoryHandler
    }

    interface SingletonHandler<T> {
        void rightShift(T singleton)
    }

    /**
     * Sets a singleton object.
     *
     * @param factoryClass factory class
     * @param instanceClass instance class
     * @return an auto-generated factory
     */
    def <T> SingletonHandler singleton(Class<T> factoryClass) {
        [rightShift: { T singleton ->
            map[factoryClass] = singleton
        }] as SingletonHandler<T>
    }
}
