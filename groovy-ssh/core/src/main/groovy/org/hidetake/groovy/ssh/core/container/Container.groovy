package org.hidetake.groovy.ssh.core.container

import static org.hidetake.groovy.ssh.util.Utility.callWithDelegate

/**
 * A container.
 *
 * @author Hidetake Iwata
 */
trait Container<T> implements Map<String, T> {
    /**
     * Add an item.
     * The item must have <code>name</code> property.
     * If this map already contains an item with same name, it will be overwritten.
     *
     * @param item
     * @return true if this map did not already contain the same name
     */
    boolean add(T item) {
        assert item.name instanceof String
        put(item.name as String, item) ? false : true
    }

    /**
     * Add items.
     * Each item must have <code>name</code> property.
     * If this map already contains an item with same name, it will be overwritten.
     *
     * @param items
     */
    void addAll(Collection<T> items) {
        putAll(items.collectEntries { item ->
            assert item.name instanceof String
            [(item.name): item]
        })
    }

    /**
     * Create an item and add it.
     * If this map already contains an item with same name, it will be overwritten.
     *
     * @param name
     * @param closure
     * @return item
     */
    T create(String name, Closure closure) {
        assert name
        assert getContainerElementType() instanceof Class<T>
        T namedObject = getContainerElementType().newInstance(name)
        callWithDelegate(closure, namedObject)
        add(namedObject)
        namedObject
    }

    /**
     * Type of the container element.
     */
    abstract Class<T> getContainerElementType()
}
