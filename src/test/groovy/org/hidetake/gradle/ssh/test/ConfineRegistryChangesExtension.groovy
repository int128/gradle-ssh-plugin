package org.hidetake.gradle.ssh.test

import org.hidetake.gradle.ssh.registry.Registry
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

class ConfineRegistryChangesExtension extends AbstractAnnotationDrivenExtension<ConfineRegistryChanges> {
    private static final interceptor = [intercept: { IMethodInvocation invocation ->
        try {
            invocation.proceed()
        } finally {
            Registry.instance.reset()
        }
    }] as IMethodInterceptor

    @Override
    void visitSpecAnnotation(ConfineRegistryChanges annotation, SpecInfo spec) {
        spec.cleanupMethod.addInterceptor(interceptor)
    }

    @Override
    void visitFeatureAnnotation(ConfineRegistryChanges annotation, FeatureInfo feature) {
        feature.featureMethod.addInterceptor(interceptor)
    }
}
