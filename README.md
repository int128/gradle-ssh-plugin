Groovy SSH [![Build Status](https://travis-ci.org/int128/groovy-ssh.svg?branch=master)](https://travis-ci.org/int128/groovy-ssh) [![Gradle Status](https://gradleupdate.appspot.com/int128/groovy-ssh/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/groovy-ssh/status)
==========

Groovy SSH is an automation tool based on DSL providing the remote command execution and file transfer.


DSL example
-----------

Here is an example script for the typical deployment scenario.

```groovy
// deploy.groovy
ssh.remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = new File('id_rsa')
  }
}

ssh.run {
  session(ssh.remotes.webServer) {
    put from: 'example.war', into: '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```

See [document of Gradle SSH Plugin](https://gradle-ssh-plugin.github.io) for details of DSL.


Run the DSL script
------------------

We have following methods to run the script.

### Download the release

Download the latest `gssh.jar` from [releases](https://github.com/int128/groovy-ssh/releases) and run the script.

```sh
java -jar gssh.jar deploy.groovy
```

### Grape

Add following header to the script for using [Grape](http://groovy.codehaus.org/Grape),

```groovy
@Grab('org.hidetake:groovy-ssh:x.y.z')
@Grab('ch.qos.logback:logback-classic:1.1.2')
def ssh = org.hidetake.groovy.ssh.Ssh.newService()
```

and run the script on Groovy.

```sh
groovy deploy.groovy
```


Embed the Library in your App
-----------------------------

We can embed Groovy SSH library in the Groovy application.

The library is available
on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.hidetake%22%20AND%20a%3A%22groovy-ssh%22)
and [Bintray](https://bintray.com/int128/maven/groovy-ssh).

```groovy
// Gradle
compile 'org.hidetake:groovy-ssh:x.y.z'
```

Instantiate a [Service](src/main/groovy/org/hidetake/groovy/ssh/core/Service.groovy)
by [`Ssh#newService()`](src/main/groovy/org/hidetake/groovy/ssh/Ssh.groovy)
and run the script as follows.

```groovy
import org.hidetake.groovy.ssh.Ssh
def ssh = Ssh.newService()

ssh.remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = new File('id_rsa')
  }
}

ssh.run {
  session(ssh.remotes.webServer) {
    put from: 'example.war', into: '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```


How to Test
-----------

### Unit test

Unit test can be run on IntelliJ or Gradle on any platform.

```sh
./gradlew :core:check
```


### Server integration test

Server integration test can be run on IntelliJ or Gradle on any platform but some specs may fail on Windows.
It uses Apache MINA SSHD server as a test double.

```sh
./gradlew :server-integration-test:check
```


### CLI test

Server integration test can be run on IntelliJ or Gradle on any platform.
It uses Apache MINA SSHD server as a test double.

```sh
./gradlew :cli:check
```


### OS integration test

OS integration test can be run on Gradle on Linux platform.
It requires a disposable server such as an EC2 instance.

```sh
./gradlew :os-integration-test:check
```

It can be run on development environment such as OSX
but some specs depending on ECDSA user key, ECDSA known hosts and passphrase key are skipped.

```sh
EXT_SSH_HOST=... EXT_SSH_USER=... EXT_SSH_KEY_PATH=... ./gradlew :os-integration-test:check
```

If it runs on Travis CI,
some specs are run on localhost to reduce time of network latency.

If it runs on Travis CI triggered by a pull request,
some specs are skipped because credential is not provided.


Category                    | Travis CI | Travis CI (pull request)  | Develop
----------------------------|-----------|---------------------------|--------
RequireAgent                | external  | -                         | external
RequireSudo                 | external  | -                         | external
RequireEcdsaHostKey         | local     | local                     | -
RequireEcdsaUserKey         | local     | local                     | -
RequireKeyWithPassphrase    | local     | local                     | -
Others                      | local     | local                     | external


### Gradle 1.x integration test

Gradle 1.x integration test will be run on Travis CI for inspection of backward compatibility.
See `travis.yml` for details.


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.
