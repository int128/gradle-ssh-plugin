Gradle SSH Plugin
=================

This plugin provides SSH execution and SFTP transfer.


How to use
----------

To use the plugin, add a dependency and apply it in your build.gradle:

```groovy
buildscript {
  repositories {
    /* FIXME */
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:1.0'
  }
}

apply plugin: 'ssh'
```


Define remote hosts
-------------------

At first, you should define remote hosts:

```groovy
remotes {
  web01 {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = file('config/identity.key')
  }
}
```

A remote instance has following properties:

  * `host` - Hostname or IP address
  * `user` - User name for authentication
  * `identity` - Private key for authentication

Also you can define remote groups:

```groovy
remoteGroups {
  webServers {
    add web01
    add web02
    add web03
  }
}
```

Remote group is just a `List<Remote>`.
Use `add()` to add a remote, and `addAll()` to add remotes.


Create a SSH task
-----------------

To define a SSH task, use `task(type: SshTask)` like:

```groovy
task reloadWebServers(type: SshTask) {
  dryRun = true
  session(remoteGroups.webServers) {
    execute 'sudo service httpd reload'
  }
}
```

Within `SshTask` closure, following properties and methods are available:
  * `session()` - Adds an session. Pass a remote or remote group as first argument.
  * `config(key: value)` - Adds an configuration entry. All configurations are given to JSch. This method overwrites entries if same defined in convention.
  * `dryRun` - Dry run flag. If true, performs no action. Default is false.
  * `logger` - Default is `project.logger`

Within `session` closure, following methods are available:
  * `execute(command)` - Executes a command. This method blocks until the command is completed.
  * `executeBackground(command)` - Executes a command in background. Other operations will be performed concurrently.
  * `get(remote, local)` - Fetches a file or directory from remote host.
  * `put(local, remote)` - Sends a file or directory to remote host.


Use in the task
---------------

To execute SSH in the task, call `sshexec()` method and give a closure:

```groovy
task prepareEnvironment {
  doLast {
    def operation = 'reload'
    // do something...
    sshexec {
      dryRun = true
      session(remoteGroups.webServers) {
        execute "sudo service httpd ${operation}"
      }
    }
  }
}
```


Convention properties
---------------------

To define global settings, describe in `ssh` closure:

```groovy
ssh {
  dryRun = true
  config(StrictHostKeyChecking: 'no')
}
```

Following properties and methods are available:

  * `config(key: value)` - Adds an configuration entry. All configurations are given to JSch.
  * `dryRun` - Dry run flag. If true, performs no action. Default is false.
  * `logger` - Default is `project.logger`


Examples
--------

See exmaple/build.gradle


Future works
------------

Currently, some features are not implemented yet:

  * Password authentication.
  * Concurrent files transfer.


