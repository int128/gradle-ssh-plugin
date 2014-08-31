package org.hidetake.groovy.ssh.api.util

/**
 * A map of T.
 * An instance of T must have name property.
 *
 * @param < T > type of an element
 *
 * @author Hidetake Iwata
 */
interface NamedObjectMap<T> extends Map<String, T> {
    /**
     * Add an item.
     * The item must have name property.
     * If this map already contains an item with same name, it will be overwritten.
     *
     * @param item
     * @return true if this map did not already contain the same name
     */
    boolean add(T item)
}
