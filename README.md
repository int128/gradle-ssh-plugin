Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.png?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

This plugin provides remote command execution and file transfer capabilities.


How to use
----------

Add the plugin dependency into your build.gradle:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:0.2.6'
  }
}

apply plugin: 'ssh'
```

Use [project template](https://github.com/int128/gradle-ssh) for quick start.
See [release notes](https://github.com/int128/gradle-ssh-plugin/releases).


Features
--------

  * Remote command execution
  * Providing a pass-phrase for the sudo prompt
  * Stream interaction with a remote command or shell
  * File transfer via SFTP
  * Password and public key authentication
  * Strict host key checking with a known-hosts file


Define a remote host
--------------------

At first, define remote hosts:

```groovy
remotes {
  web01 {
    role('masterNode')
    host = '192.168.1.101'
    user = 'jenkins'
  }
  web02 {
    host = '192.168.1.102'
    user = 'jenkins'
  }
}
```

A remote host object has following properties:
  * `host` - Hostname or IP address
  * `port` - Port. Default is 22. (Optional)
  * `user` - User name.
  * `password` - Password for password authentication. (Optional)
  * `identity` - Private key file for public-key authentication. This overrides global identity. (Optional)
  * `passphrase` - Pass phrase for the private key. (Optional)
  * `agent` - If this flag is set, Putty Agent or ssh-agent will be used to authentication. (Optional)

Use `role(name)` to associate the host with roles. A remote host can be associated with multiple roles.


Define a SSH task
-----------------

To define a SSH task, use `task(type: SshTask)` like:

```groovy
task checkWebServer(type: SshTask) {
  session(remotes.web01) {
    def pids = execute('pidof nginx').split(/ /)
    assert pids.length > 1
  }
}

task reloadServers(type: SshTask) {
  session(remotes.role('webServers', 'appServers')) {
    executeBackground('sudo service httpd reload', pty: true)
  }
}
```

Note that closure of a task is called in **evaluation** phase on Gradle.


### Task specific settings

In the `SshTask` closure, following properties are available:
  * `dryRun` - Dry run flag. If true, performs no action.
  * `outputLogLevel` - Log level of standard output for executing commands.
  * `errorLogLevel` - Log level of standard error for executing commands.
  * `encoding` - Encoding of input and output for executing commands.

Also following method is available:
  * `config(key: value)` - _(deprecated; removed in v0.3.0)_ Pass config to the JSch.

Task specific setting overrides the global setting.


### Open a session

In the `SshTask` closure, following methods are available:
  * `session(remote)` - Adds a session to the remote host.
  * `session(remotes)` - Adds each session of remote hosts. If a list is given, sessions will be executed in order. Otherwise, order is not defined.

Note that closure of a session is called in **execution** phase on Gradle.


#### Specify a remote host by name or role

`session` method takes one or more remote hosts.
  * `remotes.hostname` - Specifies the remote host.
  * `remotes.role(A)` - Specifies remote hosts associated with A.
  * `remotes.role(A, B)` - Specifies remote hosts associated with A _or_ B.


### Execute a command

In the `session` closure, following methods are available:
  * `execute(command)` - Executes a command. This method blocks until the command is completed and returns output of the command.
  * `executeSudo(command)` - Executes a command as sudo (prepends sudo -S -p). Used to support sudo commands requiring password. This method blocks until the command is completed and returns output of the command.
  * `executeBackground(command)` - Executes a command in background. Other operations will be performed concurrently.
  * `shell` - Opens a shell. This method blocks until the shell is finished. Note that you should provide termination input such as `exit` or `quit` with the interaction closure.

Also following property is available:
  * `remote` - Remote host of current session. (Read only)


#### Execution settings

These methods accept following settings as map:
  * `pty` - Requests PTY allocation if true. Default is false. Only valid for command execution.
  * `logging` -  Turns off logging of standard output and error if false. e.g. hiding credential. Default is true.

Other settings are still available (directly passed to JSch) but will not be supported in v0.3.0.


#### Handle the result

These methods raise an exception and stop the Gradle if error occurs:
  * `execute` throws an exception if exit status of the remote command is not zero.
  * `executeSudo` throws an exception if exit status of the remote command is not zero, including sudo authentication failure.
  * `executeBackground` throws an exception if exit status of the remote command is not zero, but does not interrupt any other background operations. If any command cause error, the task will be failed.
  * `shell` throws an exception if exit status of the shell is not zero.

These methods return value:
  * `execute` returns a string from standard output of the remote command. Line separators are converted to platform native.
  * `executeSudo` returns a string from standard output of the remote command, excluding sudo interactions. Line separators are same as above.


#### Interact with the stream

`execute` and `shell` method can take a closure for interaction.
```groovy
execute('passwd', pty: true) {
  interaction {
    when(partial: ~/.+[Pp]assowrd: */) {
      standardInput << oldPassword << '\n'
      when(partial: ~/.+[Pp]assowrd: */) {
        standardInput << newPassword << '\n'
      }
    }
  }
}
```

In the `interaction` closure, use `when` methods to declare rules:
  * `when(nextLine: pattern, from: stream) { action }` - When next line from the `stream` matches to the `pattern`, the action will be called.
  * `when(line: pattern, from: stream) { action }` - When a line from the `stream` matches to the `pattern`, the action will be called.
  * `when(partial: pattern, from: stream) { action }` - Performs match when the stream is flushed. This is useful for answering to a prompt such as `yes or no?`.

`pattern` is one of following:
  * If pattern is a string, it performs exact match.
  * If pattern is a regular expression, it performs regular expression match. Groovy provides pretty notation such as `~/pattern/`.
  * If pattern is `_`, it matches to any line even if empty.

`stream` is one of following:
  * `standardOutput` - Standard output of the command.
  * `standardError` - Standard error of the command.
  * If stream is omitted, it means any.

Rules will be evaluated in order. First rule has the highest priority.

In the action closure, following property is available:
  * `standardInput` - Output stream to the remote command. (Read only)


### Transfer files

In the `session` closure, following methods are available:
  * `get(remote, local)` - Fetches a file from remote host.
  * `put(local, remote)` - Sends a file to remote host.


#### Handle the result

These methods raise an exception and stop Gradle if error occurs.


### Port forwarding

In the `session` closure, following methods are available:
  * `forwardLocalPortTo(remoteHost, remotePort)` - Forwards local port to the remote port. This method returns a local port automatically assigned.


Use SSH in the task
-------------------

To execute SSH in the task, call `sshexec` method with a closure:

```groovy
task reloadService << {
  def serviceName = /* given in execution phase */
  sshexec {
    session(remotes.server01) {
      execute("sudo service $serviceName reload")
    }
  }
}
```

In `sshexec` closure, same properties and methods as `SshTask` are available.


### Manipulate the remotes container

Since `remotes` is a [NamedDomainObjectContainer](http://www.gradle.org/docs/current/javadoc/org/gradle/api/NamedDomainObjectContainer.html),
a remote host can be defined dynamically using `remotes.create(name)`:
```groovy
remotes.create('dynamic1') {
  host = /* given in execution phase */
  user = /* given in execution phase */
}
sshexec {
  session(remotes.dynamic1) {
    execute('...')
  }
}
```


Global settings
---------------

Global settings can be defined in the `ssh` closure:

```groovy
ssh {
  dryRun = true
  identity = file('config/identity.key')
  knownHosts = allowAnyHosts
}
```

Following properties are available:
  * `identity` - Private key file for public-key authentication. This can be overridden by remote specific one.
  * `passphrase` - Pass phrase for the private key.
  * `knownHosts` - Known hosts file. Default is `~/.ssh/known_hosts`. If `allowAnyHosts` is set, strict host key checking is turned off (only for testing purpose).
  * `dryRun` - Dry run flag. If true, performs no action. Default is false.
  * `retryCount` - Retrying count to establish connection. Default is 0 (no retry).
  * `retryWaitSec` - Time in seconds between each retries. Default is 0 (immediately).
  * `outputLogLevel` - Log level of standard output while command execution. Default is `LogLevel.QUIET`.
  * `errorLogLevel` - Log level of standard error while command execution. Default is `LogLevel.ERROR`.
  * `encoding` - Encoding of input and output for executing commands. Default is UTF-8.

Also following method is available:
  * `config(key: value)` - _(deprecated; removed in v0.3.0)_ Pass config to the JSch.


Contributions
-------------

Thanks for contributions.
Please send me your issue reports or pull requests.
