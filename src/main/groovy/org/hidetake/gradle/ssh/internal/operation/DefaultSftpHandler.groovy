package org.hidetake.gradle.ssh.internal.operation

import com.jcraft.jsch.ChannelSftp
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j
import org.hidetake.gradle.ssh.api.operation.SftpHandler

@TupleConstructor
@Slf4j
class DefaultSftpHandler implements SftpHandler {
    final ChannelSftp channel

    @Override
    void getFile(String remote, String local) {
        log.info("Get a remote file ($remote) to local ($local)")
        channel.get(remote, local, new FileTransferLogger())
    }

    @Override
    void putFile(String local, String remote) {
        log.info("Put a local file ($local) to remote ($remote)")
        channel.put(local, remote, new FileTransferLogger(), ChannelSftp.OVERWRITE)
    }

    @Override
    void mkdir(String path) {
        log.info("Create a directory ($path)")
        channel.mkdir(path)
    }

    @Override
    List<ChannelSftp.LsEntry> ls(String path) {
        log.info("Get a directory listing of ($path)")
        channel.ls(path).toList()
    }

    @Override
    void cd(String path) {
        log.info("Change current directory to ($path)")
        channel.cd(path)
    }
}
