package org.hidetake.gradle.ssh.internal.operation

import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.internal.operation.FileTransferLogger.LOG_INTERVAL_MILLIS

class FileTransferLoggerSpec extends Specification {

    def "status is initialized when init() is called"() {
        given:
        def logger = new FileTransferLogger()

        when: null
        then: logger.status == null

        when: logger.init(0, 'source', 'destination', 1000)
        then: logger.status
        and:  logger.status.maxSize == 1000
    }

    def "status is updated when count() is called"() {
        given:
        def logger = new FileTransferLogger()
        logger.init(0, 'source', 'destination', 1000)

        when: logger.count(300)
        then: logger.status.transferredSize == 300

        when: logger.count(500)
        then: logger.status.transferredSize == 800
    }

    def "checkpoint is called when elapsed time exceeds interval"() {
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

    def "end"() {
        given:
        def logger = new FileTransferLogger()
        logger.init(0, 'source', 'destination', 1000)

        when:
        logger.end()

        then:
        noExceptionThrown()
    }



    def "initialize status"() {
        given:
        def status = new FileTransferLogger.Status(2000)

        when:
        null

        then:
        status.maxSize == 2000
        status.transferredSize == 0
        status.percent == 0.0
    }

    def "count up"() {
        given:
        def status = new FileTransferLogger.Status(5000)

        when:
        status << 2000

        then:
        status.maxSize == 5000
        status.transferredSize == 2000
        status.percent == 0.4

        when:
        status << 2000

        then:
        status.maxSize == 5000
        status.transferredSize == 4000
        status.percent == 0.8

        when:
        status << 1000

        then:
        status.maxSize == 5000
        status.transferredSize == 5000
        status.percent == 1.0
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "get elapsed time"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 100 }
        def status = new FileTransferLogger.Status(1000)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 300 }
        then: status.elapsedTime == 200

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 600 }
        then: status.elapsedTime == 500
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "get elapsed time from checkpoint"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 100 }
        def status = new FileTransferLogger.Status(1000)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 300 }
        and:  status.checkPoint()
        then: status.elapsedTimeFromCheckPoint == 0

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 600 }
        then: status.elapsedTimeFromCheckPoint == 300

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 800 }
        then: status.elapsedTimeFromCheckPoint == 500

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 900 }
        and:  status.checkPoint()
        then: status.elapsedTimeFromCheckPoint == 0

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 1000 }
        then: status.elapsedTimeFromCheckPoint == 100
    }

    @ConfineMetaClassChanges(FileTransferLogger.Status)
    def "get bytes per second"() {
        given:
        FileTransferLogger.Status.metaClass.static.currentTime = { -> 1000 }
        def status = new FileTransferLogger.Status(1000)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 3000 }
        and:  status << 500
        then: status.kiloBytesPerSecond == (0.5 /* kB */ / 2 /* sec */)

        when: FileTransferLogger.Status.metaClass.static.currentTime = { -> 6000 }
        and:  status << 300
        then: status.kiloBytesPerSecond == (0.8 /* kB */ / 5 /* sec */)
    }

}
