package org.hidetake.gradle.ssh.test

import groovy.util.logging.Slf4j

@Slf4j
class RegistryHelper {
    /**
     * Replace factory object of the instance class.
     *
     * @param instanceClass the instance class
     * @return object with left shift method
     */
    static factoryOf(Class instanceClass) {
        assert instanceClass.hasProperty('factory'), 'instance class should have factory property'

        [leftShift: { mock ->
            log.debug("Replaced factory of ${instanceClass.name} with ${mock}")
            instanceClass.metaClass.static.getFactory = { ->
                log.debug("Returned ${mock} as factory of ${instanceClass.name}")
                mock
            }
        }] as Object
    }
}
