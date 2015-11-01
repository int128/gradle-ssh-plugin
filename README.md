Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin) [![Gradle Status](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status)
=================

Gradle SSH Plugin provides SSH facilities such as command execution or file transfer on Gradle.

Read [the document](https://gradle-ssh-plugin.github.io) for details,
and get [the template project](https://github.com/gradle-ssh-plugin/template) for quick start.


Getting Started
---------------

Add the plugin into your build script.

```groovy
plugins {
  id 'org.hidetake.ssh' version '1.1.4'
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

Prerequisite:

* SSH service must be started on localhost port 22
* Current user must be able to log in with a private key placed at `~/.ssh/id_rsa` without any passphrase
* SSH service must accept SFTP subsystem

Run `test` task.

```sh
./gradlew install
./gradlew -p acceptance-tests test
```

Some tests need to change the system configuration so they are separated to `testWithSideEffect` task.
It should be run on a disposable container such as Travis CI or Docker.

#### Release

Build with JDK 7 for compatibility. Do not build with JDK 8.

Push a versioned tag to GitHub and Travis CI will upload the artifact to Bintray.
