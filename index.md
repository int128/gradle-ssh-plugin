---
layout: default
title: Home
---

Gradle SSH Plugin provides remote command execution and file transfer.


[![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)


Features
--------

### Integrated with Gradle

Seamlessly integrated with Gradle DSL.


### Remote command execution

```groovy
task reloadServers(type: SshTask) {
  session(remotes.role('webServers')) {
    execute 'sudo service httpd reload'
  }
}
```


### File transfer

```groovy
task deployApp(type: SshTask) {
  session(remotes.role('webServers')) {
    put("$buildDir/libs/hello.war", '/webapps')
    execute('sudo service tomcat restart')
  }
}
```


### Authentication and security

* Password authentication
* Public key authentication
* Host key verification with an OpenSSH compatible `known_hosts` file


### Stream interaction

* Providing a password for sudo prompt
* Interaction with shell such as bash or Cisco IOS


Getting Started
---------------

Add the plugin dependency in your build.gradle:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:0.3.3'
  }
}

apply plugin: 'ssh'
```

Get the [Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template) for quick start.


### Add a remote host

```groovy
ssh {
  identity = file('id_rsa')     // Enables public key authentication
  knownHosts = allowAnyHosts    // Disables host key verification
}

remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
  }
}
```

### Add a deployment task

```groovy
task deployApp(type: SshTask, dependsOn: war) {
  ssh {
    // Enables PTY allocation for sudo
    pty = true
  }
  session(remotes.role('webServers')) {
    // Puts built WAR to server
    put(war.archivePath, '/webapps')
    // Restarts the application server
    execute('sudo service tomcat restart')
  }
}
```


User Guide
----------

* Overview
* Using SSH task
* Using SSH in the task
* Defining remote hosts
  * Adding a remote host
  * Adding a remote host in execution phase
* Operations
  * Command execution
  * Shell execution
  * File transfer
  * Stream interaction
* Global settings
  * Connection settings
  * Operation settings


Contribution
------------

Send your issue or pull request to [GitHub repository](https://github.com/int128/gradle-ssh-plugin).
