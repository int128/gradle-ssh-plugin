Groovy SSH [![CircleCI](https://circleci.com/gh/int128/groovy-ssh.svg?style=svg)](https://circleci.com/gh/int128/groovy-ssh) [![Gradle Status](https://gradleupdate.appspot.com/int128/groovy-ssh/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/groovy-ssh/status)
==========

Groovy SSH is an automation tool based on DSL providing the remote command execution and file transfer.


DSL example
-----------

Here is an example script for the typical deployment scenario.

```groovy
// deploy.groovy
ssh.remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = new File('id_rsa')
  }
}

ssh.run {
  session(ssh.remotes.webServer) {
    put from: 'example.war', into: '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```

See [document of Gradle SSH Plugin](https://gradle-ssh-plugin.github.io) for details.


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.


### Unit test

Unit test can be run on IntelliJ or Gradle on any platform.

```sh
./gradlew :core:check
```


### Server integration test

Server integration test can be run on IntelliJ or Gradle on any platform but some specs may fail on Windows.
It uses Apache MINA SSHD server as a test double.

```sh
./gradlew :server-integration-test:check
```


### CLI test

Server integration test can be run on IntelliJ or Gradle on any platform.
It uses Apache MINA SSHD server as a test double.

```sh
./gradlew :cli:check
```


### OS integration test

OS integration test can be run on Gradle on Linux platform.
It expects a SSH server is running at localhost port 8022 and sudo is available without a password.

```sh
./gradlew :os-integration-test:check
```


### Gradle SSH Plugin integration test

Gradle SSH Plugin integration test can be run on Linux platform.
It expects a SSH server is running at localhost port 8022 and sudo is available without a password.
See `circle.yml` for details.


License
-------

```
Copyright 2012-2016 Hidetake Iwata

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
