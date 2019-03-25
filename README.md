Groovy SSH [![CircleCI](https://circleci.com/gh/int128/groovy-ssh.svg?style=shield)](https://circleci.com/gh/int128/groovy-ssh) [![Gradle Status](https://gradleupdate.appspot.com/int128/groovy-ssh/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/groovy-ssh/status)
==========

Groovy SSH is an automation tool based on DSL providing the remote command execution and file transfer.

https://gradle-ssh-plugin.github.io


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.


### Unit test

We can run the unit test as follows:

```sh
./gradlew :core:check
```


### Server integration test

We can run the server integration test using Apache MINA SSHD server as follows:

```sh
./gradlew :server-integration-test:check
```


### CLI test

We can run the integration test of CLI as follows:

```sh
./gradlew :cli:check
```


### OS integration test

We can run the OS integration tests using [int128/sshd](https://github.com/int128/docker-sshd) image as follows:

```sh
# Run a sshd container
./os-integration-test/run-sshd.sh

# Run the tests
./gradlew :os-integration-test:check
```


### Gradle SSH Plugin integration test

We can run the test with Gradle SSH Plugin.
See `plugin-integration/run-plugin-integration-test.sh` for details.

If you are planning to release with specification change breaking backward compatibility,
create `groovy-ssh-acceptance-test` branch on Gradle SSH Plugin to pass the acceptance test.


### Release

Push a versioned tag to GitHub and CI will upload the artifact to Bintray.
