Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin is a Gradle plugin which provides remote command execution and file transfer features.

Please see [the user guide](https://gradle-ssh-plugin.github.io/) for details.


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.

Please let me know on GitHub issues or pull requests.

The user guide is maintained on [the repository of gradle-ssh-plugin.github.io](https://github.com/gradle-ssh-plugin/gradle-ssh-plugin.github.io).

The template project is maintained on [this repository](https://github.com/gradle-ssh-plugin/template).


Development
-----------

JDK 7 or later is needed.
All dependencies are downloaded by Gradle wrapper.

The continuous integration is enabled on Travis CI.
See [the build report](https://gradle-ssh-plugin.github.io/build-report.html) for each branch.

Artifacts are released on [Gradle Plugins](http://plugins.gradle.org/plugin/org.hidetake.ssh), [Bintray](https://bintray.com/int128/maven/gradle-ssh-plugin) JCenter and [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.hidetake%22%20AND%20a%3A%22gradle-ssh-plugin%22).


### Build

Run the build task.

```sh
./gradlew build
```

Known issues:

* Mac OS X
  * 1 test will be failed
  * Due to a bug of JSch to handle a hashed `known_hosts` on Mac OS X.
* Windows
  * 2 tests will be failed
  * Apache sshd server does not support SFTP operations on Windows.


### Acceptance Test

We can run acceptance tests to verify behavior of the plugin on Gradle environment.

Prerequisite:

* SSH service must be started on localhost port 22
* Current user must be able to log in with a private key placed at `~/.ssh/id_rsa` without any passphrase
* SSH service must accept SFTP subsystem

Run the test task.

```sh
./gradlew install
./gradlew -p acceptance-tests -i test
```

We can run also aggressive tests changing system configuration.
It is strongly recommended to run on a disposable container, e.g. Docker.

```sh
docker build -t gradle-ssh-plugin acceptance-test
docker run -v /tmp/.gradle:/root/.gradle gradle-ssh-plugin
```


### Publish

Build with JDK 7 for compatibility.
Run the upload task with Bintray credential.

```sh
docker build -t gradle-ssh-plugin .
docker run -v /tmp/.gradle:/root/.gradle gradle-ssh-plugin \
  -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -Pversion=x.y.z bintrayUpload
```
