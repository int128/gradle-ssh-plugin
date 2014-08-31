package org.hidetake.groovy.ssh.internal.util

import org.hidetake.groovy.ssh.api.util.NamedObjectMap

/**
 * A default implementation of {@link NamedObjectMap}.
 *
 * @param < T > type of an element
 *
 * @author Hidetake Iwata
 */
class DefaultNamedObjectMap<T> implements NamedObjectMap<T> {
    @Delegate
    private final Map<String, T> map = [:]

    boolean add(T item) {
        assert item.name instanceof String
        put(item.name, item) ? false : true
    }

    T put(String name, T item) {
        assert name == item.name
        map.put(item.name as String, item)
    }
}
