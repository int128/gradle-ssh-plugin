package org.hidetake.gradle.ssh.internal.operation

import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.internal.operation.FileTransferLogger.LOG_INTERVAL_MILLIS

class FileTransferLoggerSpec extends Specification {

    def "status should be set when init() is called"() {
        given:
        def logger = new FileTransferLogger()

        when: null
        then: logger.status == null

        when: logger.init(0, 'source', 'destination', 1000)
        then: logger.status
        and:  logger.status.maxSize == 1000
    }

    def "status should be updated when count() is called"() {
        given:
        def logger = new FileTransferLogger()
        logger.init(0, 'source', 'destination', 1000)

        when: logger.count(300)
        then: logger.status.transferredSize == 300

        when: logger.count(500)
        then: logger.status.transferredSize == 800
    }

    def "status.checkpoint() should be called when elapsed time exceeds interval"() {
        given:
        def logger = new FileTransferLogger()
        logger.status = Mock(FileTransferLogger.Status)

        when: logger.count(300)
        then: 1 * logger.status.leftShift(300)
        then: 1 * logger.status.elapsedTimeFromCheckPoint >> (LOG_INTERVAL_MILLIS - 1)
        then: 0 * logger.status.checkPoint()

        when: logger.count(400)
        then: 1 * logger.status.leftShift(400)
        then: 1 * logger.status.elapsedTimeFromCheckPoint >> LOG_INTERVAL_MILLIS
        then: 0 * logger.status.checkPoint()

        when: logger.count(500)
        then: 1 * logger.status.leftShift(500)
        then: 1 * logger.status.elapsedTimeFromCheckPoint >> (LOG_INTERVAL_MILLIS + 1)
        then: 1 * logger.status.checkPoint()
    }

    def "nothing happens when end() is called"() {
        given:
        def logger = new FileTransferLogger()
        logger.init(0, 'source', 'destination', 1000)

        when:
        logger.end()

        then:
        noExceptionThrown()
    }



    def "properties should be set when constructor is called"() {
        given:
        def status = new FileTransferLogger.Status(2000)

        when:
        null

        then:
        status.maxSize == 2000
        status.transferredSize == 0
        status.percent == 0.0
    }

    def "properties should be updated when progress is reported"() {
        given:
        def status = new FileTransferLogger.Status(5000)

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
        def status = new FileTransferLogger.Status(0)

        when: 'reports the progress that 2,000 bytes was transferred'
        status << 2000

        then:
        status.maxSize == 0
        status.transferredSize == 2000
        status.percent == 0
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "elapsedTime should be relative time"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 100 }
        def status = new FileTransferLogger.Status(1000)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 300 }
        then: status.elapsedTime == 200

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 600 }
        then: status.elapsedTime == 500
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "elapsedTime should be time from the last checkpoint"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 100 }
        def status = new FileTransferLogger.Status(1000)

        when: 'commit the checkpoint on time 300'
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 300 }
        status.checkPoint()

        then: status.elapsedTimeFromCheckPoint == 0

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 600 }
        then: status.elapsedTimeFromCheckPoint == 300

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 800 }
        then: status.elapsedTimeFromCheckPoint == 500

        when: 'commit the checkpoint on time 900'
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 900 }
        status.checkPoint()

        then: status.elapsedTimeFromCheckPoint == 0

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 1000 }
        then: status.elapsedTimeFromCheckPoint == 100
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "kiloBytesPerSecond should be transfer rate"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 1000 }
        def status = new FileTransferLogger.Status(10000)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 3000 /* milli-sec */ }
        and:  status << 5000 /* bytes */
        then: status.kiloBytesPerSecond == (5.0 /* kB */ / 2 /* sec */)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 6000 /* milli-sec */ }
        and:  status << 4000 /* bytes */
        then: status.kiloBytesPerSecond == (9.0 /* kB */ / 5 /* sec */)
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "kiloBytesPerSecond should be zero if no time is elapsed"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 1000 }
        def status = new FileTransferLogger.Status(10000)

        when: null
        then: status.kiloBytesPerSecond == 0

        when: status << 5000 /* bytes */
        then: status.kiloBytesPerSecond == 0
    }

}
