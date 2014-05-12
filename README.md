Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin is a Gradle plugin which provides remote command execution and file transfer features.


Getting Started and User Guide
------------------------------

Please see [Gradle SSH Plugin web site](http://gradle-ssh-plugin.github.io/).


Contribution
------------

Please open your issue or pull request. Any issue is welcome.


Related repositories
--------------------

* [Gradle SSH Plugin web site repository](https://github.com/gradle-ssh-plugin/gradle-ssh-plugin.github.io)
* [Gradle SSH Plugin template project](https://github.com/gradle-ssh-plugin/template)


Build
-----

Run Gradle wrapper.

```bash
./gradlew build
```

Known issues: Currently 1 test will be failed on Mac due to lack of support hashed known hosts.


Acceptance Test
---------------

We can run acceptance tests to verify behavior of the plugin on Gradle environment.

Prerequisite:

* SSH service must be started on localhost port 22
* Current user must be able to log in with a private key placed at `~/.ssh/id_rsa` without any passphrase
* SSH service must accept SFTP subsystem

Upload a built JAR into the local repository and invoke a test.

```bash
./gradlew uploadArchives
./gradlew -p acceptance-tests -i test
```

### Aggressive tests

Above does not contain aggressive tests such as creating users or changing system configration.
Invoke with aggressiveTest to run aggressive tests.
It is strongly recommended to run on a disposable instance such as Travis CI.

```bash
./gradlew -p acceptance-tests -i aggressiveTest
```


Publish to Maven Central
------------------------

Prerequisite:

* `~/.gradle/gradle.properties` must contain 
  * signing.keyId
  * signing.secretKeyRingFile
  * sonatypeUsername
  * sonatypeFullname
* PGP key must be placed at signing.secretKeyRingFile
* Passphrase of key pair must be known
* Password of Sonatype must be known

Invoke the publishing task:

```bash
./gradlew publishMavenCentral
```


