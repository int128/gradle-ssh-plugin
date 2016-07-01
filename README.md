Gradle SSH Plugin [![CircleCI](https://circleci.com/gh/int128/gradle-ssh-plugin.svg?style=svg)](https://circleci.com/gh/int128/gradle-ssh-plugin) [![Gradle Status](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status)
=================

Gradle SSH Plugin provides SSH facilities such as command execution or file transfer on Gradle.

Read [the document](https://gradle-ssh-plugin.github.io) for details,
and get [the template project](https://github.com/gradle-ssh-plugin/template) for quick start.


Getting Started
---------------

Add the plugin into your build script.
Check the latest version in [releases](https://github.com/int128/gradle-ssh-plugin/releases).

```groovy
plugins {
  id 'org.hidetake.ssh' version 'x.y.z'
}
```

Define remote hosts and describe SSH operations in the task.

```groovy
remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = file('id_ecdsa')
  }
}

task deploy << {
  ssh.run {
    session(remotes.webServer) {
      put 'example.war', '/webapps'
      execute 'sudo service tomcat restart'
    }
  }
}
```

Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.

### Development

Gradle SSH Plugin internally uses [Groovy SSH](https://github.com/int128/groovy-ssh) library.
It depends on [JSch](http://www.jcraft.com/jsch/).

The document is maintained on the repository of Groovy SSH.

#### Acceptance Test

We can run acceptance tests to verify behavior of the plugin on Gradle environment.
It expects a SSH server is running at localhost port 8022.

It runs on the current version and 1.12 of Gradle at default.
Target versions can be given by `target.gradle.versions` property as follows:

```sh
./gradlew -Ptarget.gradle.versions=3.0,2.0,1.12 :acceptance-tests:test
```

#### Release

Build with JDK 7 for compatibility. Do not build with JDK 8.

Push a versioned tag to GitHub and CI will upload the artifact to Bintray.
