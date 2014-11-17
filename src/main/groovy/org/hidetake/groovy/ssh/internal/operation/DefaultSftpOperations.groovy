package org.hidetake.groovy.ssh.internal.operation

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpException as JschSftpException
import groovy.util.logging.Slf4j
import org.hidetake.groovy.ssh.api.operation.SftpException
import org.hidetake.groovy.ssh.api.operation.SftpOperations

@Slf4j
class DefaultSftpOperations implements SftpOperations {
    private final ChannelSftp channel

    def DefaultSftpOperations(ChannelSftp channel1) {
        channel = channel1
        assert channel
    }

    @Override
    void getFile(String remote, String local) {
        log.info("Get a remote file ($remote) to local ($local)")
        try {
            channel.get(remote, local, new FileTransferLogger())
        } catch (JschSftpException e) {
            throw new SftpException('Failed to get a file from the remote host', e)
        }
    }

    @Override
    void putFile(String local, String remote) {
        log.info("Put a local file ($local) to remote ($remote)")
        try {
            channel.put(local, remote, new FileTransferLogger(), ChannelSftp.OVERWRITE)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to put a file into the remote host', e)
        }
    }

    @Override
    void mkdir(String path) {
        log.info("Create a directory ($path)")
        try {
            channel.mkdir(path)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to create a directory on the remote host', e)
        }
    }

    @Override
    List<ChannelSftp.LsEntry> ls(String path) {
        log.info("Get a directory listing of ($path)")
        try {
            channel.ls(path).toList()
        } catch (JschSftpException e) {
            throw new SftpException('Failed to fetch a directory listing on the remote host', e)
        }
    }

    @Override
    void cd(String path) {
        log.info("Change current directory to ($path)")
        try {
            channel.cd(path)
        } catch (JschSftpException e) {
            throw new SftpException('Failed to change directory on the remote host', e)
        }
    }
}
