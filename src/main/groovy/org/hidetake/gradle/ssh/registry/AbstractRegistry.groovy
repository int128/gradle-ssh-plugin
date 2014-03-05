package org.hidetake.gradle.ssh.registry

/**
 * A component registry.
 *
 * @author hidetake.org
 */
abstract class AbstractRegistry {
    private final Map<Class<?>, Object> map = [:]

    AbstractRegistry() {
        reset()
    }

    abstract void wire()

    /**
     * Resets to default wiring.
     */
    void reset() {
        map.clear()
        wire()
    }

    /**
     * Returns the instance.
     *
     * @param clazz class of the instance
     * @return the instance
     */
    def <T> T getAt(Class<T> clazz) {
        map.get(clazz) as T
    }

    /**
     * Sets the instance.
     *
     * @param clazz class of the instance
     * @param instance the instance
     */
    def <T> void putAt(Class<T> clazz, T instance) {
        map.put(clazz, instance)
    }

    /**
     * Sets a auto-generated factory object.
     *
     * @param factory factory class
     * @param instanceClass instance class
     * @return an auto-generated factory
     */
    def <F> F factory(Class<F> factory, Class<?> instanceClass) {
        this[factory] = [create: { Object[] args -> instanceClass.newInstance(args) }] as F
    }
}
