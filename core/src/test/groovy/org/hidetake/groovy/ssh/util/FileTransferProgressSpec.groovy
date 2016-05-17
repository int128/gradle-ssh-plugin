package org.hidetake.groovy.ssh.util

import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.groovy.ssh.util.FileTransferProgress.LOG_INTERVAL_MILLIS

class FileTransferProgressSpec extends Specification {

    def "status should be set when reset() is called"() {
        given:
        def logger = new FileTransferProgress()

        when: null
        then: logger.status == null

        when: logger.reset(1000)
        then: logger.status
        and:  logger.status.maxSize == 1000
    }

    def "status should be updated when report() is called"() {
        given:
        def logger = new FileTransferProgress()
        logger.reset(1000)

        when: logger.report(300)
        then: logger.status.transferredSize == 300

        when: logger.report(500)
        then: logger.status.transferredSize == 800
    }

    def "status.checkpoint() should be called when elapsed time exceeds interval"() {
        given:
        def logger = new FileTransferProgress()
        logger.status = Mock(FileTransferProgress.Status)

        when: logger.report(300)
        then: 1 * logger.status.leftShift(300)
        then: 1 * logger.status.elapsedTimeFromCheckPoint >> (LOG_INTERVAL_MILLIS - 1)
        then: 0 * logger.status.checkPoint()

        when: logger.report(400)
        then: 1 * logger.status.leftShift(400)
        then: 1 * logger.status.elapsedTimeFromCheckPoint >> LOG_INTERVAL_MILLIS
        then: 0 * logger.status.checkPoint()

        when: logger.report(500)
        then: 1 * logger.status.leftShift(500)
        then: 1 * logger.status.elapsedTimeFromCheckPoint >> (LOG_INTERVAL_MILLIS + 1)
        then: 1 * logger.status.checkPoint()
    }



    def "properties should be set when constructor is called"() {
        given:
        def status = new FileTransferProgress.Status(2000)

        when:
        null

        then:
        status.maxSize == 2000
        status.transferredSize == 0
        status.percent == 0.0
    }

    def "properties should be updated when progress is reported"() {
        given:
        def status = new FileTransferProgress.Status(5000)

        when: 'reports the progress that 2,000 bytes was transferred'
        status << 2000

        then:
        status.maxSize == 5000
        status.transferredSize == 2000
        status.percent == 0.4

        when: 'reports the progress that 2,000 bytes was additionally transferred'
        status << 2000

        then:
        status.maxSize == 5000
        status.transferredSize == 4000
        status.percent == 0.8

        when: 'reports the progress that 1,000 bytes was additionally transferred'
        status << 1000

        then:
        status.maxSize == 5000
        status.transferredSize == 5000
        status.percent == 1.0
    }

    def "percent should be zero if estimated size is zero"() {
        given:
        def status = new FileTransferProgress.Status(0)

        when: 'reports the progress that 2,000 bytes was transferred'
        status << 2000

        then:
        status.maxSize == 0
        status.transferredSize == 2000
        status.percent == 0
    }

    @ConfineMetaClassChanges(FileTransferProgress.Status)
    def "elapsedTime should be relative time"() {
        given:
        FileTransferProgress.Status.metaClass.static.currentTime = { -> 100 }
        def status = new FileTransferProgress.Status(1000)

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 300 }
        then: status.elapsedTime == 200

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 600 }
        then: status.elapsedTime == 500
    }

    @ConfineMetaClassChanges(FileTransferProgress.Status)
    def "elapsedTime should be time from the last checkpoint"() {
        given:
        FileTransferProgress.Status.metaClass.static.currentTime = { -> 100 }
        def status = new FileTransferProgress.Status(1000)

        when: 'commit the checkpoint on time 300'
        FileTransferProgress.Status.metaClass.static.currentTime = { -> 300 }
        status.checkPoint()

        then: status.elapsedTimeFromCheckPoint == 0

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 600 }
        then: status.elapsedTimeFromCheckPoint == 300

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 800 }
        then: status.elapsedTimeFromCheckPoint == 500

        when: 'commit the checkpoint on time 900'
        FileTransferProgress.Status.metaClass.static.currentTime = { -> 900 }
        status.checkPoint()

        then: status.elapsedTimeFromCheckPoint == 0

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 1000 }
        then: status.elapsedTimeFromCheckPoint == 100
    }

    @ConfineMetaClassChanges(FileTransferProgress.Status)
    def "kiloBytesPerSecond should be transfer rate"() {
        given:
        FileTransferProgress.Status.metaClass.static.currentTime = { -> 1000 }
        def status = new FileTransferProgress.Status(10000)

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 3000 /* milli-sec */ }
        and:  status << 5000 /* bytes */
        then: status.kiloBytesPerSecond == (5.0 /* kB */ / 2 /* sec */)

        when: FileTransferProgress.Status.metaClass.static.currentTime = { -> 6000 /* milli-sec */ }
        and:  status << 4000 /* bytes */
        then: status.kiloBytesPerSecond == (9.0 /* kB */ / 5 /* sec */)
    }

    @ConfineMetaClassChanges(FileTransferProgress.Status)
    def "kiloBytesPerSecond should be zero if no time is elapsed"() {
        given:
        FileTransferProgress.Status.metaClass.static.currentTime = { -> 1000 }
        def status = new FileTransferProgress.Status(10000)

        when: null
        then: status.kiloBytesPerSecond == 0

        when: status << 5000 /* bytes */
        then: status.kiloBytesPerSecond == 0
    }

}
