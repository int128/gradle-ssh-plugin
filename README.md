Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin is a Gradle plugin providing remote command execution and file transfer features for continuous delivery.

Please see [Gradle SSH Plugin web site](http://gradle-ssh-plugin.github.io/) for details such as getting started or user guide.


Contribution
------------

Please send me your issue or pull request.


How to Build
------------

Run Gradle wrapper.

```groovy
./gradlew build
```

### Known issues

Currently 1 test will be failed on Mac due to lack of support hashed known hosts. See #69.


Acceptance Test
---------------

We can run acceptance tests to verify behavior of the plugin on Gradle environment.

Prerequiste of acceptance tests:

* SSH service must be started on localhost port 22
* Current user must be able to log in with a private key placed at `~/.ssh/id_rsa` without any passphrase
* SSH service must accept SFTP subsystem

Upload a built JAR into the local repository and invoke a test.

```groovy
./gradlew uploadArchives
./gradlew -p acceptance-tests -i test
```

### Aggressive tests

Above does not contain aggressive tests such as creating users or changing system configration.
Invoke with aggressiveTest to run aggressive tests.
It is strongly recommended to run on a disposable instance such as Travis CI.

```groovy
./gradlew -p acceptance-tests -i aggressiveTest
```


