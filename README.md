Groovy SSH [![Build Status](https://travis-ci.org/int128/groovy-ssh.svg?branch=master)](https://travis-ci.org/int128/groovy-ssh)
==========

Groovy SSH is a Groovy library which provides remote command execution and file transfer features.


User Guide
----------

Groovy SSH is part of Gradle SSH Plugin.
See [the user guide of Gradle SSH Plugin](http://gradle-ssh-plugin.github.io/).


Contributions
-------------

Groovy SSH is a open source software developed on GitHub and licensed under the Apache License Version 2.0.


### Bug report and feature request

Please open an issue or pull request. Any issue is welcome. 日本語もOKです.


Build
-----

Just run Gradle wrapper.

```bash
./gradlew build
```

The build and acceptance test will be performed when the branch is pushed. See the [build report](http://gradle-ssh-plugin.github.io/build-report.html) of every branches.


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


