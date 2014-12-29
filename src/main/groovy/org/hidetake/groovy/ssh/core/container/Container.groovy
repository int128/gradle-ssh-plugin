package org.hidetake.groovy.ssh.core.container

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
}
