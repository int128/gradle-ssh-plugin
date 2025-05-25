package org.hidetake.groovy.ssh.session.execution

import spock.lang.Specification
import spock.lang.Unroll

class EscapeSpec extends Specification {

    @Unroll
    def "escape() should return escaped string when #args"() {
        when:
        def escaped = Escape.escape(args)

        then:
        escaped == expected

        where:
        args    | expected
        []      | ''
        ['']    | /''/
        [/hello/, /!"#$%&'()*+,-.\/:;<=>?@[\]^_`{|}~/]  | /'hello' '!"#$%&'\''()*+,-.\/:;<=>?@[\]^_`{|}~'/
    }

}
