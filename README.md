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


Build
-----

Run the build task.

```sh
./gradlew build
```

The build and acceptance test will be performed when the branch is pushed.
See the [build report](https://gradle-ssh-plugin.github.io/build-report.html) of every branches.

Known issues:

* 1 test will be failed on Mac OS X
  * Due to a bug of JSch to handle a hashed `known_hosts` on Mac OS X.
* 2 tests will be failed on Windows
  * Apache sshd server does not support SFTP operations on Windows.


Acceptance Test
---------------

We can run acceptance tests to verify behavior of the plugin on Gradle environment.

Prerequisite:

* SSH service must be started on localhost port 22
* Current user must be able to log in with a private key placed at `~/.ssh/id_rsa` without any passphrase
* SSH service must accept SFTP subsystem


### Run on the development environment

Install an artifact to the local repository and run tests.

```sh
./gradlew install
./gradlew -p acceptance-tests -i test
```


### Run on a disposable container

We can run also aggressive tests changing system configuration.
It is strongly recommended to run on a disposable container, e.g. Docker.

```sh
docker build -t gradle-ssh-plugin acceptance-test
docker run -v /tmp/.gradle:/root/.gradle gradle-ssh-plugin
```


Publish
-------

Build with JDK 7 for compatibility.
Run `bintrayUpload` task with Bintray credential.

```sh
docker build -t gradle-ssh-plugin .
docker run -v /tmp/.gradle:/root/.gradle gradle-ssh-plugin \
  -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -Pversion=x.y.z bintrayUpload
```
