package org.hidetake.groovy.ssh.core.settings

import spock.lang.Specification
import spock.lang.Unroll

class ToStringPropertiesSpec extends Specification {

    static trait ExampleSettings {
        String name
        Integer count

        static class Empty implements ExampleSettings, ToStringProperties {}
    }

    static trait SecretSettings {
        String user
        String password
        def toString__password() { '...' }

        static class Empty implements SecretSettings, ToStringProperties {}
    }

    static trait ConstantSettings {
        String description
        final constValue = 'constant'
        def toString__constValue() {}

        static class Empty implements ConstantSettings, ToStringProperties {}
    }

    static class CompositeSettings implements ExampleSettings, SecretSettings, ConstantSettings, ToStringProperties {
    }

    def "toString should contains each key and value of properties"() {
        given:
        def settings = new ExampleSettings.Empty(name: 'foo', count: 1000)

        when:
        def string = settings.toString()

        then:
        string in ['{count=1000, name=foo}', '{name=foo, count=1000}']
    }

    def "toString should exclude keys if each value is null"() {
        given:
        def settings = new ExampleSettings.Empty(name: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{name=foo}'
    }

    @Unroll
    def "toString should use formatter if defined"() {
        given:
        def settings = new SecretSettings.Empty(user: 'foo', password: password)

        when:
        def string = settings.toString()

        then:
        string == mapString

        where:
        password | mapString
        'SECRET' | '{password=..., user=foo}'
        ''       | '{password=..., user=foo}'
        null     | '{user=foo}'
    }

    def "toString should exclude key if formatter returns null"() {
        given:
        def settings = new ConstantSettings.Empty(description: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{description=foo}'
    }

    def "toString should contains each key and value of properties on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo', count: 1000)

        expect:
        settings.toString() in ['{count=1000, name=foo}', '{name=foo, count=1000}']
    }

    def "toString should exclude keys if each value is null on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo')

        when:
        def string = settings.toString()

        then:
        string == '{name=foo}'
    }

    @Unroll
    def "toString should use formatter if defined on delegated class"() {
        given:
        def settings = new CompositeSettings(user: 'foo', password: password)

        expect:
        settings.toString() == mapString

        where:
        password | mapString
        'SECRET' | '{password=..., user=foo}'
        ''       | '{password=..., user=foo}'
        null     | '{user=foo}'
    }

    def "toString should exclude key if formatter returns null on delegated class"() {
        given:
        def settings = new CompositeSettings(name: 'foo')

        expect:
        settings.toString() == '{name=foo}'
    }

}
