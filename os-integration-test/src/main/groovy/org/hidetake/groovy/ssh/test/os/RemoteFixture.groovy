package org.hidetake.groovy.ssh.test.os

import groovy.transform.Canonical
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.operation.SftpException
import org.junit.rules.ExternalResource

@Slf4j
class RemoteFixture extends ExternalResource {

    private final Service ssh = Ssh.newService()

    @Override
    protected void before() {
        Fixture.createRemotes(ssh)
    }

    @Override
    protected void after() {
    }

    static enum Mkdir {
        remoteDir
    }

    @Canonical
    static class RemotePath {
        Service ssh
        String path

        RemotePath div(String child) {
            new RemotePath(ssh, "$path/$child")
        }

        RemotePath div(Mkdir ignore) {
            ssh.run {
                session(ssh.remotes.Default) {
                    execute("mkdir $path")
                }
            }
            this
        }

        String getName() {
            path.substring(path.lastIndexOf('/'))
        }

        String getText() {
            ssh.run {
                session(ssh.remotes.Default) {
                    get from: path
                }
            }
        }

        RemotePath leftShift(String text) {
            ssh.run {
                session(ssh.remotes.Default) {
                    put text: text, into: path
                }
            }
            this
        }

        List list() {
            ssh.run {
                session(ssh.remotes.Default) {
                    sftp {
                        ls(path).findAll { !(it.filename in ['.', '..']) }
                    }
                }
            } as List
        }

        boolean exists() {
            try {
                ssh.run {
                    session(ssh.remotes.Default) {
                        sftp {
                            stat(path)
                        }
                    }
                } as List
                true
            } catch (SftpException e) {
                false
            }
        }

        @Override
        String toString() {
            path
        }
    }

    RemotePath newFolder() {
        def path = new RemotePath(ssh, Fixture.remoteTmpPath())
        path / Mkdir.remoteDir
        path
    }

    RemotePath newFile() {
        def folder = newFolder()
        folder / "${UUID.randomUUID()}"
    }

}
