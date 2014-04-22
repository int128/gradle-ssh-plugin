---
layout: default
title: Home
---

Gradle SSH Plugin provides remote command execution and file transfer features for continuous delivery.

An open source software developed on [GitHub project](https://github.com/int128/gradle-ssh-plugin).


Features
--------

### Integrated with Gradle

Build and deploy seamlessly on Gradle.

```groovy
apply plugin: 'war'
apply plugin: 'ssh'

task deploy(type: SshTask, dependsOn: war) {
  // TODO: put a WAR file and reload server
}
```


### Simple DSL

Intuitive syntax for command execution and file transfer.

```groovy
task deploy(type: SshTask, dependsOn: war) {
  session(remotes.webServer) {
    put(war.archivePath, '/webapps')
    execute('sudo service tomcat restart')
  }
}
```


### Authentication and security

* Password authentication
* Public key authentication with an OpenSSH compatible key file
* Host key verification with an OpenSSH compatible `known_hosts` file


### Stream interaction

* Providing a password for sudo prompt
* Interaction with the shell such as bash or Cisco IOS


Getting Started
---------------

### Add a dependency

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

[Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template) for quick start.


### Add a task

Here is an example for trivial deployment scenario.

```groovy
// Global settings
ssh {
  identity = file('id_rsa')     // Enable public key authentication
  knownHosts = allowAnyHosts    // Disable host key verification
}

// Add a remote host
remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
  }
}

task deploy(type: SshTask, dependsOn: war) {
  description = 'Deploys an application to the server.'
  ssh {
    // Enable PTY allocation for sudo
    pty = true
  }
  session(remotes.webServer) {
    // Put a built WAR to the server
    put(war.archivePath, '/webapps')
    // Restart the application server
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

Please send your issue report or pull request via [GitHub project](https://github.com/int128/gradle-ssh-plugin).
