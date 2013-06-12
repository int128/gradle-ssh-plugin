package org.hidetake.gradle.ssh.internal

import com.jcraft.jsch.JSchException
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Test cases for {@link DefaultSshService}.
 *
 * @author hidetake.org
 *
 */
class DefaultSshServiceTest {
    protected final Logger logger = LoggerFactory.getLogger(DefaultSshServiceTest)

    @Test
    void retry_0_success() {
        int called = 0
        DefaultSshService.instance.retry(0, 0, logger) {
            called++
            assert called == 1
        }
        assert called == 1
    }

    @Test(expected = JSchException)
    void retry_0_exception() {
        int called = 0
        DefaultSshService.instance.retry(0, 0, logger) {
            assert called == 0
            throw new JSchException()
        }
    }

    @Test
    void retry_1_success() {
        int called = 0
        DefaultSshService.instance.retry(1, 0, logger) {
            called++
            assert called == 1
        }
        assert called == 1
    }

    @Test
    void retry_1_exceptionOnce() {
        int called = 0
        DefaultSshService.instance.retry(1, 0, logger) {
            called++
            if (called == 1) {
                throw new JSchException('this should be handled by retry() method')
            }
            assert called == 2
        }
        assert called == 2
    }

    @Test(expected = JSchException)
    void retry_1_exception2times() {
        int called = 0
        DefaultSshService.instance.retry(1, 0, logger) {
            called++
            assert (1..2).contains(called)
            if (called == 1) {
                throw new JSchException('this exception should be handled by retry() method')
            }
            if (called == 2) {
                throw new JSchException()
            }
        }
    }

    @Test
    void retry_2_success() {
        int called = 0
        DefaultSshService.instance.retry(2, 0, logger) { called++ }
        assert called == 1
    }

    @Test
    void retry_2_exceptionOnce() {
        int called = 0
        DefaultSshService.instance.retry(2, 0, logger) {
            called++
            assert (1..2).contains(called)
            if (called == 1) {
                throw new JSchException('this exception should be handled by retry() method')
            }
        }
        assert called == 2
    }

    @Test
    void retry_2_exception2times() {
        int called = 0
        DefaultSshService.instance.retry(2, 0, logger) {
            called++
            assert (1..3).contains(called)
            if (called == 1) {
                throw new JSchException('this exception should be handled by retry() method')
            }
            if (called == 2) {
                throw new JSchException('this exception should be handled by retry() method')
            }
        }
        assert called == 3
    }

    @Test(expected = JSchException)
    void retry_2_exception3times() {
        int called = 0
        DefaultSshService.instance.retry(2, 0, logger) {
            called++
            assert (1..3).contains(called)
            if (called == 1) {
                throw new JSchException('this exception should be handled by retry() method')
            }
            if (called == 2) {
                throw new JSchException('this exception should be handled by retry() method')
            }
            if (called == 3) {
                throw new JSchException()
            }
        }
    }
}
