package org.hidetake.groovy.ssh.core.settings

import spock.lang.Specification
import spock.lang.Unroll

class SettingsHelperSpec extends Specification {

    static trait ExampleSettings {
        String name
        Integer count

        static class With implements ExampleSettings {
            def With() {}
            def With(ExampleSettings... sources) {
                SettingsHelper.mergeProperties(this, sources)
            }
        }
    }

    static trait SecretSettings {
        String user
        String password
        def plus__password(SecretSettings prior) {
            "$password/$prior.password"
        }

        static class With implements SecretSettings {
            def With() {}
            def With(SecretSettings... sources) {
                SettingsHelper.mergeProperties(this, sources)
            }
        }
    }

    static trait ConstantSettings {
        String description
        final constValue = 'constant'

        static class With implements ConstantSettings {
            def With() {}
            def With(ConstantSettings... sources) {
                SettingsHelper.mergeProperties(this, sources)
            }
        }
    }

    static class CompositeSettings implements ExampleSettings, SecretSettings, ConstantSettings {
        def CompositeSettings() {}
        def CompositeSettings(CompositeSettings... sources) {
            SettingsHelper.mergeProperties(this, sources)
        }
    }

    @Unroll
    def "mergeProperties() should merge each value of properties"() {
        given:
        def settings1 = new ExampleSettings.With(name: name1, count: count1)
        def settings2 = new ExampleSettings.With(name: name2, count: count2)

        when:
        def settings = new ExampleSettings.With(settings1, settings2)

        then:
        settings.name == mergedName
        settings.count == mergedCount

        where:
        name1 | count1 | name2 | count2 || mergedName | mergedCount

        'foo' | 1000   | 'bar' | 9999   || 'bar'      | 9999
        'foo' | 1000   | null  | 9999   || 'foo'      | 9999
        'foo' | 1000   | 'bar' | null   || 'bar'      | 1000
        'foo' | 1000   | null  | null   || 'foo'      | 1000

        null  | null   | 'bar' | 9999   || 'bar'      | 9999
        null  | null   | null  | 9999   || null       | 9999
        null  | null   | 'bar' | null   || 'bar'      | null
        null  | null   | null  | null   || null       | null
    }

    def "mergeProperties() should exclude read only keys"() {
        given:
        def settings1 = new ConstantSettings.With(description: 'foo')
        def settings2 = new ConstantSettings.With(description: 'baz')

        when:
        def settings = new ConstantSettings.With(settings1, settings2)

        then:
        settings.description == 'baz'
    }

    def "mergeProperties() should exclude read only keys on delegated class"() {
        given:
        def settings1 = new CompositeSettings(name: 'foo')
        def settings2 = new CompositeSettings(name: 'baz')

        when:
        def settings = new CompositeSettings(settings1, settings2)

        then:
        settings.name == 'baz'
    }

    def "mergeProperties() should use plus__... if defined"() {
        given:
        def settings1 = new SecretSettings.With(user: 'foo', password: 'some')
        def settings2 = new SecretSettings.With(user: 'baz', password: 'thing')

        when:
        def settings = new SecretSettings.With(settings1, settings2)

        then:
        settings.password == 'some/thing'
    }

    def "mergeProperties() should use plus__... if defined on delegated class"() {
        given:
        def settings1 = new CompositeSettings(name: 'foo', password: 'some')
        def settings2 = new CompositeSettings(name: 'baz', password: 'thing')

        when:
        def settings = new CompositeSettings(settings1, settings2)

        then:
        settings.password == 'some/thing'
    }

}
