package org.hidetake.groovy.ssh.core.settings

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.lang.reflect.Method

/**
 * A helper class for settings manipulation.
 *
 * @author Hidetake Iwata
 */
class SettingsHelper {
    /**
     * Compute and set merged properties.
     * Class should implements method <code>plus__<i>propertyName</i></code>
     * to customize merge logic of the property.
     * See test for details of specification.
     *
     * @param target
     * @param sources a list of settings in descending order (last one has the highest priority)
     */
    static void mergeProperties(target, ... sources) {
        assert sources.length > 0

        def setters = findAssignableKeys(target) { key, value, setter -> setter }
        def plusMethods = findMethods(target, 'plus__')

        def lowestPriority = sources.head()
        setters.findAll { key, setter ->
            lowestPriority.hasProperty(key)
        }.each { key, setter ->
            invoke(target, setter, lowestPriority[key])
        }

        sources.tail().each { prior ->
            setters.findAll { key, setter ->
                prior.hasProperty(key)
            }.each { key, setter ->
                if (plusMethods.containsKey(key)) {
                    invoke(target, setter, invoke(target, plusMethods[key], prior))
                } else {
                    invoke(target, setter, prior[key] != null ? prior[key] : target[key])
                }
            }
        }
    }

    /**
     * Compute properties for string representation.
     * Class should implements method <code>toString__<i>propertyName</i></code>
     * to exclude or customize property representation.
     * See test for details of specification.
     *
     * @params target
     * @returns map of key and computed value
     */
    static Map<String, Object> computePropertiesToString(target) {
        def toStringMethods = findMethods(target, 'toString__')

        findAssignableKeys(target) { key, value, setter ->
            value
        }.findAll { key, value ->
            // do not show null value
            value != null &&
            // prevent recursion
            !(value instanceof ToStringProperties)
        }.collectEntries { key, value ->
            def toStringMethod = toStringMethods[key]
            if (toStringMethod) {
                [(key): invoke(target, toStringMethod)]
            } else {
                [(key): value]
            }
        }.findAll { key, value ->
            // excludes if the toString method returns null
            value != null
        } as Map<String, Object>
    }

    /**
     * Find methods with the prefix.
     *
     * @param object
     * @param prefix
     * @return map of each name without prefix and method
     */
    static Map<String, Method> findMethods(object, String prefix) {
        object.class.declaredMethods.collectEntries { method ->
            method.name.startsWith(prefix) ? [(method.name.substring(prefix.length())): method] : [:]
        }
    }

    /**
     * Find and map assignable properties of the object.
     *
     * @param object
     * @param transform
     * @return map of each key and transformed value
     */
    static <T> Map<String, T> findAssignableKeys(object, @ClosureParams(value = SimpleType, options = ['java.lang.String', 'java.lang.Object', 'java.lang.reflect.Method']) Closure<T> transform) {
        def methods = object.class.declaredMethods.collectEntries { method ->
            [(method.name): method]
        }

        object.properties.findAll { key, value ->
            key != 'class' && key != 'metaClass'
        }.collectEntries { key, value ->
            assert key instanceof String
            def setter = methods["set${key.capitalize()}"]
            setter ? [(key): transform.call(key, value, setter)] : [:]
        } as Map<String, T>
    }

    private static invoke(object, Method method) {
        method.invoke(object)
    }

    private static invoke(object, Method method, value) {
        method.invoke(object, [value] as Object[])
    }
}
