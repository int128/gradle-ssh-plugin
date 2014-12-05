Groovy SSH [![Build Status](https://travis-ci.org/int128/groovy-ssh.svg?branch=master)](https://travis-ci.org/int128/groovy-ssh)
==========

Groovy SSH is a Groovy library which provides remote command execution and file transfer features.

See [groovy-ssh.github.io](https://groovy-ssh.github.io) for details.


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Any issues or pull requests are welcome.

The web site is maintained on [the repository of groovy-ssh.github.io](https://github.com/groovy-ssh/groovy-ssh.github.io).


Development
-----------

JDK 7 or later is needed.
All dependencies are downloaded by Gradle wrapper.

The continuous integration is enabled on Travis CI.

Artifacts are released on [Bintray](https://bintray.com/int128/maven/groovy-ssh) JCenter and [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.hidetake%22%20AND%20a%3A%22groovy-ssh%22).


### Build

Run the build task.

```sh
./gradlew build
```


### Publish

Build with JDK 7 for compatibility.
Run the upload task with Bintray credential.

```sh
docker build -t groovy-ssh .
docker run -v /tmp/.gradle:/root/.gradle groovy-ssh \
  -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -Pversion=x.y.z bintrayUpload
```
