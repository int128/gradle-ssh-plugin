package org.hidetake.groovy.ssh.test.os

@Category(File)
class FileDivCategory {

    File div(String child) {
        new File(this as File, child)
    }

    File div(MkdirType type) {
        switch (type) {
            case MkdirType.DIRECTORY:
                assert mkdir()
                break

            case MkdirType.DIRECTORIES:
                assert mkdirs()
                break

            default:
                throw new IllegalArgumentException("Unknown mkdir type: $type")
        }
        this
    }

}
