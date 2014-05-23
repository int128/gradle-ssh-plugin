Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin is a Gradle plugin which provides remote command execution and file transfer features.


User Guide
----------

Please visit [gradle-ssh-plugin.github.io](http://gradle-ssh-plugin.github.io/).


Contributions
-------------

Gradle SSH Plugin is a open source software developed on GitHub and licensed under the Apache License Version 2.0.


### Bug report and feature request

Please open an issue or pull request. Any issue is welcome.


### Documentation

The user guide is maintained on the [GitHub Pages repository](https://github.com/gradle-ssh-plugin/gradle-ssh-plugin.github.io).


### Example scripts

TODO

Please open a pull request on the [Gradle SSH Plugin template project](https://github.com/gradle-ssh-plugin/template).


Build
-----

Just run Gradle wrapper.

```bash
./gradlew build
```


### Known issues

1 test will be failed on Mac OS X, due to a bug of JSch to handle a hashed `known_hosts` on Mac OS X.

2 tests will be failed on Windows, because Apache sshd server does not support SFTP operations on Windows.


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


