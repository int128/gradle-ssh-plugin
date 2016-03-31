package org.hidetake.groovy.ssh.core.settings

/**
 * A trait for overriding {@link Object#toString()} to show properties of this object.
 * This may help debug by user friendly log.
 *
 * @author Hidetake Iwata
 */
trait ToStringProperties {
    /**
     * Returns a string representation of this settings.
     * Class should implements method <code>toString__<i>propertyName</i></code>
     * to exclude or customize property representation.
     * See test for details of specification.
     *
     * @returns string representation of this settings
     */
    @Override
    String toString() {
        '{' + SettingsHelper.computePropertiesToString(this).collect { key, value ->
            "$key=${value.toString()}"
        }.join(', ') + '}'
    }
}