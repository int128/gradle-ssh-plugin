package org.hidetake.gradle.ssh.test

import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Confines any changes made to the registry to the annotated scope.
 *
 * @author hidetake.org
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.METHOD])
@ExtensionAnnotation(ConfineRegistryChangesExtension)
@interface ConfineRegistryChanges {
}
