package org.hidetake.gradle.ssh.plugin

import spock.lang.Specification
import spock.lang.Unroll

import static org.hidetake.gradle.ssh.test.TestDataHelper.createRemote

class SshTaskDelegateSpec extends Specification {

    SshTaskDelegate sshTaskDelegate

    def setup() {
        sshTaskDelegate = new SshTaskDelegate()
    }

    def "add session"() {
        given:
        def remote = createRemote()
        def operationClosure = {-> println "whatever" }

        when:
        sshTaskDelegate.session(remote, operationClosure)


        then:
        sshTaskDelegate.sessionSpecs.size() == 1
        sshTaskDelegate.sessionSpecs[0].remote == remote
        sshTaskDelegate.sessionSpecs[0].operationClosure == operationClosure
    }

    @Unroll("add session with remote: #theRemote, user: #theUser, host: #theHost")
    def "add session with invalid params throws assertion error"() {
        given:
        def remote

        when:
        remote = theRemote ? createRemote([user: theUser, host: theHost]) : null
        sshTaskDelegate.session(remote, theOperationClosure)

        then:
        AssertionError e = thrown()
        e.message.contains(errorContains)


        where:
        theRemote  | theUser  | theHost          | theOperationClosure | errorContains
        null       | "myUser" | "www.myhost.com" | {-> println it }    | "remote"
        "myRemote" | null     | "www.myhost.com" | {-> println it }    | "user"
        "myRemote" | "myUser" | null             | {-> println it }    | "host"
        "myRemote" | "myUser" | "www.myhost.com" | null                | "closure"
    }



    def "add session for multiple remotes"() {
        given:
        def remote1 = createRemote([name: "remote1"])
        def remote2 = createRemote([name: "remote2"])
        def closure = {-> println "whatever" }

        when:
        sshTaskDelegate.session([remote1, remote2], closure)

        then:
        sshTaskDelegate.sessionSpecs.size() == 2
    }


    def "add session for multiple remotes with illegal args throws assertion error"() {
        given:
        def remote = createRemote()

        when:
        sshTaskDelegate.session([], {-> println "whatever" })

        then:
        AssertionError ex = thrown()
        ex.message.contains("remotes")

        when:
        sshTaskDelegate.session([remote], null)

        then:
        AssertionError ex2 = thrown()
        ex2.message.contains("closure")

    }

}
