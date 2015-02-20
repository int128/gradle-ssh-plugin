package org.hidetake.groovy.ssh.operation;

/**
 * SFTP errors defined in the SFTP protocol.
 * Implemented as Java native enum for Gradle 1.x compatibility.
 *
 * @link http://tools.ietf.org/html/draft-ietf-secsh-filexfer-13#section-9.1
 * @author Hidetake Iwata
 */
public enum SftpError {
    SSH_FX_OK(0, "Successful completion of the operation"),
    SSH_FX_EOF(1, "An attempt to read past the end-of-file was made; or, there are no more directory entries to return"),
    SSH_FX_NO_SUCH_FILE(2, "A reference was made to a file which does not exist"),
    SSH_FX_PERMISSION_DENIED(3, "The user does not have sufficient permissions to perform the operation"),
    SSH_FX_FAILURE(4, "An error occurred"),
    SSH_FX_BAD_MESSAGE(5, "A badly formatted packet or other SFTP protocol incompatibility was detected"),
    SSH_FX_NO_CONNECTION(6, "There is no connection to the server"),
    SSH_FX_CONNECTION_LOST(7, "The connection to the server was lost"),
    SSH_FX_OP_UNSUPPORTED(8, "An attempted operation could not be completed by the server because the server does not support the operation"),
    SSH_FX_INVALID_HANDLE(9, "The handle value was invalid"),
    SSH_FX_NO_SUCH_PATH(10, "The file path does not exist or is invalid"),
    SSH_FX_FILE_ALREADY_EXISTS(11, "The file already exists"),
    SSH_FX_WRITE_PROTECT(12, "The file is on read-only media, or the media is write protected"),
    SSH_FX_NO_MEDIA(13, "The requested operation cannot be completed because there is no media available in the drive"),
    SSH_FX_NO_SPACE_ON_FILESYSTEM(14, "The requested operation cannot be completed because there is insufficient free space on the filesystem"),
    SSH_FX_QUOTA_EXCEEDED(15, "The operation cannot be completed because it would exceed the user\"s storage quota"),
    SSH_FX_UNKNOWN_PRINCIPAL(16, "A principal referenced by the request was unknown"),
    SSH_FX_LOCK_CONFLICT(17, "The file could not be opened because it is locked by another process"),
    SSH_FX_DIR_NOT_EMPTY(18, "The directory is not empty"),
    SSH_FX_NOT_A_DIRECTORY(19, "The specified file is not a directory"),
    SSH_FX_INVALID_FILENAME(20, "The filename is not valid"),
    SSH_FX_LINK_LOOP(21, "Too many symbolic links encountered"),
    SSH_FX_CANNOT_DELETE(22, "The file cannot be deleted. One possible reason is that the advisory READONLY attribute-bit is set"),
    SSH_FX_INVALID_PARAMETER(23, "One of the parameters was out of range"),
    SSH_FX_FILE_IS_A_DIRECTORY(24, "The specified file was a directory in a context where a directory cannot be used"),
    SSH_FX_BYTE_RANGE_LOCK_CONFLICT(25, "An read or write operation failed because another process\"s mandatory byte-range lock overlaps with the request"),
    SSH_FX_BYTE_RANGE_LOCK_REFUSED(26, "A request for a byte range lock was refused"),
    SSH_FX_DELETE_PENDING(27, "An operation was attempted on a file for which a delete operation is pending"),
    SSH_FX_FILE_CORRUPT(28, "The file is corrupt"),
    SSH_FX_OWNER_INVALID(29, "The principal specified can not be assigned as an owner of a file"),
    SSH_FX_GROUP_INVALID(30, "The principal specified can not be assigned as the primary group of a file"),
    SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK(31, "The requested operation could not be completed because the specified byte range lock has not been granted");

    private final int code;
    private final String message;

    SftpError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static SftpError find(int code) {
        for (SftpError e : values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }
}
