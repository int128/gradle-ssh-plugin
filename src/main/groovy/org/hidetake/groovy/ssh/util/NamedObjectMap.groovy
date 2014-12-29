package org.hidetake.groovy.ssh.util

/**
 * A map of T.
 * An instance of T must have name property.
 *
 * @param < T > type of an element
 *
 * @author Hidetake Iwata
 */
class NamedObjectMap<T> {
    @Delegate
    private final Map<String, T> map = [:]

    /**
     * Add an item.
     * The item must have name property.
     * If this map already contains an item with same name, it will be overwritten.
     *
     * @param item
     * @return true if this map did not already contain the same name
     */
    boolean add(T item) {
        assert item.name instanceof String
        put(item.name, item) ? false : true
    }

    T put(String name, T item) {
        assert name == item.name
        map.put(item.name as String, item)
    }
}
