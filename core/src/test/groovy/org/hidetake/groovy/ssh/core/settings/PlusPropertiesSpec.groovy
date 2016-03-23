package org.hidetake.groovy.ssh.core.settings

import spock.lang.Specification
import spock.lang.Unroll

class PlusPropertiesSpec extends Specification {

    static class ExampleSettings implements PlusProperties<ExampleSettings> {
        String name
        Integer count
    }

    static class SecretSettings implements PlusProperties<SecretSettings> {
        String user
        String password
        final plus__password(right) {
            "$password/$right.password"
        }
    }

    static class ConstantSettings implements PlusProperties<ConstantSettings> {
        String description
        final constValue = 'constant'
        final plus__constValue() {}
    }

    static class CompositeSettings implements PlusProperties<CompositeSettings> {
        @Delegate
        ExampleSettings exampleSettings = new ExampleSettings()

        @Delegate
        SecretSettings secretSettings = new SecretSettings()

        @Delegate
        ConstantSettings constantSettings = new ConstantSettings()
    }

    @Unroll
    def "plus should merge each value of properties"() {
        given:
        def settings1 = new ExampleSettings(name: name1, count: count1)
        def settings2 = new ExampleSettings(name: name2, count: count2)

        when:
        def settings = settings1 + settings2

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

    def "plus should exclude key if plus__... returns null"() {
        given:
        def settings1 = new ConstantSettings(description: 'foo')
        def settings2 = new ConstantSettings(description: 'baz')

        when:
        def settings = settings1 + settings2

        then:
        settings.description == 'baz'
    }

    def "plus should exclude key if plus__... returns null on delegated class"() {
        given:
        def settings1 = new CompositeSettings(name: 'foo')
        def settings2 = new CompositeSettings(name: 'baz')

        when:
        def settings = settings1 + settings2

        then:
        settings.name == 'baz'
        settings.exampleSettings.name == 'baz'
    }

    def "plus should use plus__... if defined"() {
        given:
        def settings1 = new SecretSettings(user: 'foo', password: 'some')
        def settings2 = new SecretSettings(user: 'baz', password: 'thing')

        when:
        def settings = settings1 + settings2

        then:
        settings.password == 'some/thing'
    }

    def "plus should use plus__... if defined on delegated class"() {
        given:
        def settings1 = new CompositeSettings(name: 'foo', password: 'some')
        def settings2 = new CompositeSettings(name: 'baz', password: 'thing')

        when:
        def settings = settings1 + settings2

        then:
        settings.password == 'some/thing'
        settings.secretSettings.password == 'some/thing'
    }

}
