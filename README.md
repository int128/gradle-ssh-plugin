Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin is a Gradle plugin which provides remote command execution and file transfer features.

See also:

* [User Guide](https://gradle-ssh-plugin.github.io) for details.
* [Template Project](https://github.com/gradle-ssh-plugin/template) for quick start.


Gradle SSH Plugin depends on [Groovy SSH](https://github.com/int128/groovy-ssh) library and delegates SSH operations to the library.


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Any issues or pull requests are welcome.

The user guide is maintained on [the repository of gradle-ssh-plugin.github.io](https://github.com/gradle-ssh-plugin/gradle-ssh-plugin.github.io).


### Environment

JDK 7 or later is required.
Intellij IDEA is recommended.
All dependencies are downloaded by Gradle wrapper.

Travis CI will compile and test the branch on each push and pull request.

Artifacts are published on [Gradle Plugins](http://plugins.gradle.org/plugin/org.hidetake.ssh), [Bintray](https://bintray.com/int128/maven/gradle-ssh-plugin) JCenter and [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.hidetake%22%20AND%20a%3A%22gradle-ssh-plugin%22).


### Build

Run the build task.

```sh
./gradlew build
```


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

Build a release with JDK 7 for compatibility.
Do not build with JDK 8.

Push a versioned tag to GitHub and Travis CI will publish the artifact to Bintray.

```sh
git tag v0.0.0
git push origin --tags
```

Or manually invoke the upload task.

```sh
./gradlew -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -Pversion=0.0.0 bintrayUpload
```
