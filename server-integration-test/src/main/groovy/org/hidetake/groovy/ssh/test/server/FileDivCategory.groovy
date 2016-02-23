package org.hidetake.groovy.ssh.test.server

@Category(File)
class FileDivCategory {

    File div(String child) {
        new File(this as File, child)
    }

    File div(DirectoryType type) {
        assert type == DirectoryType.DIRECTORY
        assert mkdir()
        this
    }

    static enum DirectoryType {
        DIRECTORY
    }

}
