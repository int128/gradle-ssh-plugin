package org.hidetake.groovy.ssh.core.settings

import static org.hidetake.groovy.ssh.util.Utility.findNotNull

/**
 * A trait providing plus operator to compute a merged settings.
 *
 * @param < T > type of right side
 */
trait PlusProperties<T> {
    /**
     * Compute a merged settings.
     * Class should implements method <code>plus__<i>propertyName</i></code>
     * to customize merge logic of the property.
     * See test for details of specification.
     *
     * @param right prior settings
     * @return merged settings
     */
    def T plus(T right) {
        def mergedProperties = properties.findAll { key, value ->
            // prevents recursion
            !(value instanceof PlusProperties) &&
            // excludes class
            key != 'class'
        }.collectEntries { key, value ->
            try {
                [(key): "plus__$key"(right)]
            } catch (MissingMethodException ignore) {
                try {
                    [(key): "plus__$key"()]
                } catch (MissingMethodException ignored) {
                    [(key): findNotNull(right.properties.get(key), value)]
                }
            }
        }.findAll { key, value ->
            // excludes if it is not set or plus__ method returned null
            value != null
        }
        this.class.newInstance(mergedProperties) as T
    }
}
