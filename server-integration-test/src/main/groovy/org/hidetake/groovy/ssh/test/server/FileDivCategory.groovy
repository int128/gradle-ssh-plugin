package org.hidetake.groovy.ssh.test.server

@Category(File)
class FileDivCategory {

    File div(String child) {
        new File(this as File, child)
    }

    File div(DirectoryType type) {
        switch (type) {
            case DirectoryType.DIRECTORY:
                assert mkdir()
                break

            case DirectoryType.DIRECTORIES:
                assert mkdirs()
                break

            default:
                throw new IllegalArgumentException("Unknown directory type: $type")
        }
        this
    }

    static enum DirectoryType {
        DIRECTORY,
        DIRECTORIES
    }

}
