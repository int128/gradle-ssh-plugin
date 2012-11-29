Gradle SSH Plugin
=================

This plugin provides SSH execution and SFTP transfer capabilities.


How to use
----------

To use the plugin, add a dependency and apply it in your build.gradle:

```groovy
buildscript {
  repositories {
    mavenCentral()
    add(new org.apache.ivy.plugins.resolver.URLResolver()) {
      name = 'GitHub'
      addArtifactPattern 'http://cloud.github.com/downloads/int128/[module]/[module]-[revision].[ext]'
    }
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:0.1.1'
    classpath 'com.jcraft:jsch:0.1.48'
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
  * `port` - Port. Default is 22. (Optional)
  * `user` - User name.
  * `password` - Password for password authentication. (Optional)
  * `identity` - Private key for public-key authentication. (Optional)

Also remote hosts may be associated with roles, using `role(name)` like:

```groovy
remotes {
  web01 {
    role('webServers')
    host = '192.168.1.101'
    //...
  }
  web02 {
    role('webServers')
    host = '192.168.1.102'
    //...
  }
  web03 {
    role('webServers')
    host = '192.168.1.103'
    //...
  }
}
```


Create a SSH task
-----------------

To define a SSH task, use `task(type: SshTask)` like:

```groovy
task reloadMasterServer(type: SshTask) {
  session(remotes.web01) {
    execute('sudo service httpd reload', pty: true)
  }
}

task reloadWebServers(type: SshTask) {
  session(remotes.role('webServers')) {
    executeBackground('sudo service httpd reload', pty: true)
  }
}
```

Within `SshTask` closure, following properties and methods are available:
  * `session(remote)` - Adds a session to the remote host.
  * `session(remotes)` - Adds each session of remote hosts. If a list is given, sessions will be executed in order. Otherwise, order is not defined.
  * `config(key: value)` - Adds an configuration entry. All configurations are given to JSch. This method overwrites entries if same defined in convention.
  * `dryRun` - Dry run flag. If true, performs no action. Default is according to the convention property.
  * `logger` - Default is `project.logger`

Within `session` closure, following operations are available:
  * `execute(command)` - Executes a command. This method blocks until the command is completed.
  * `executeBackground(command)` - Executes a command in background. Other operations will be performed concurrently.
  * `get(remote, local)` - Fetches a file or directory from remote host.
  * `put(local, remote)` - Sends a file or directory to remote host.

These operations also accepts option arguments.
For instance, adding `pty: true` makes the channel to request PTY allocation (popular way to execute sudo).


Use SSH in the task
-------------------

To execute SSH in the task, call `sshexec()` method with a closure:

```groovy
task prepareEnvironment {
  doLast {
    def operation = 'reload'
    sshexec {
      dryRun = true
      session(remotes.role('webServers')) {
        execute("sudo service httpd ${operation}", pty: true)
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


Complete example
----------------

See [/example/build.gradle](gradle-ssh-plugin/blob/master/example/build.gradle)


Future work
-----------

Currently, some features are not implemented yet:

  * Concurrent files transfer.


