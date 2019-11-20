Gradle SSH Plugin [![CircleCI](https://circleci.com/gh/int128/gradle-ssh-plugin.svg?style=shield)](https://circleci.com/gh/int128/gradle-ssh-plugin) [![Gradle Status](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status)
=================

Gradle SSH Plugin provides SSH facilities such as command execution or file transfer on Gradle.

https://gradle-ssh-plugin.github.io


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.

### Development

Gradle SSH Plugin internally uses [Groovy SSH](https://github.com/int128/groovy-ssh) library.
It depends on [JSch](http://www.jcraft.com/jsch/).

The document is maintained on the repository of Groovy SSH.

#### Acceptance Test

We can run the acceptance test to verify behavior of the plugin on Gradle.

```sh
# Run sshd
./gradle-ssh-plugin/acceptance-test/fixture/run-sshd.sh

# Run the test
./gradlew :gradle-ssh-plugin:acceptance-test:test

# Stop sshd
docker stop sshd
```

Note that the test depends on [int128/sshd](https://github.com/int128/docker-sshd) and keys are hardcoded in `.circleci/config.yml`.

You can regenerate keys by the following commands:

```sh
ssh-keygen -m PEM -t rsa -f keys/id_rsa
ssh-keygen -A -f keys/
```

### Release

Push a versioned tag to GitHub and CI will upload the artifact to Bintray.
