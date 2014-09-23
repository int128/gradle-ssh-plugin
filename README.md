Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin is a Gradle plugin which provides remote command execution and file transfer features.


User Guide
----------

Please visit [gradle-ssh-plugin.github.io](https://gradle-ssh-plugin.github.io/).


Contributions
-------------

Gradle SSH Plugin is an open source software developed on GitHub and licensed under the Apache License Version 2.0.


### Bug report or feature request

Please let me know on GitHub issues or pull requests.


### Documentation

The user guide is maintained on [gradle-ssh-plugin.github.io repository](https://github.com/gradle-ssh-plugin/gradle-ssh-plugin.github.io).


### Example scripts

Send me a pull request on [Gradle SSH Plugin template repository](https://github.com/gradle-ssh-plugin/template).


Build
-----

Just run Gradle wrapper.

```bash
./gradlew build
```

The build and acceptance test will be performed when the branch is pushed.
See the [build report](https://gradle-ssh-plugin.github.io/build-report.html) of every branches.


### Known issues

* 1 test will be failed on Mac OS X, due to a bug of JSch to handle a hashed `known_hosts` on Mac OS X.
* 2 tests will be failed on Windows, because Apache sshd server does not support SFTP operations on Windows.


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


Publish
-------

### Publish to Bintray

Prerequisite:

* `~/.gradle/gradle.properties` must contain following
  * bintrayUser
  * bintrayKey

Invoke the upload task:

```bash
./gradlew -Pversion=x.y.z bintrayUpload
```


### Publish to the local Maven repository

The install task will do it.

```bash
./gradlew install
```
