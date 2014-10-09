Groovy SSH [![Build Status](https://travis-ci.org/int128/groovy-ssh.svg?branch=master)](https://travis-ci.org/int128/groovy-ssh)
==========

Groovy SSH is a Groovy library which provides remote command execution and file transfer features.


User Guide
----------

At this time, Groovy SSH is part of Gradle SSH Plugin.
See [the user guide of Gradle SSH Plugin](http://gradle-ssh-plugin.github.io/).


Contributions
-------------

Groovy SSH is an open source software developed on GitHub and licensed under the Apache License Version 2.0.


### Bug report or feature request

Please let me know on GitHub issues or pull requests.


Build
-----

Run the build task.

```sh
./gradlew build
```


Publish
-------

Build with JDK 7 for compatibility.
Run `bintrayUpload` task with Bintray credential.

```sh
docker build -t groovy-ssh .
docker run -v /tmp/.gradle:/root/.gradle groovy-ssh \
  -PbintrayUser=$bintrayUser -PbintrayKey=$bintrayKey -Pversion=x.y.z bintrayUpload
```
