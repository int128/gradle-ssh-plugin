import groovy.transform.ToString

@ToString
class FeatureDelegate {
    String task = null
    final List<String> categories = []

    void task(String path) {
        assert task == null, 'feature task is already set'
        task = path
    }

    void category(String name) {
        categories << name
    }
}

class FeatureSupport {
    void feature(String name, Closure featureClosure) {
        def feature = new FeatureDelegate()
        featureClosure.delegate = feature
        featureClosure.resolveStrategy = Closure.DELEGATE_FIRST
        featureClosure.call()

        assert feature.task, "task of feature ($name) must be set"
        assert feature.categories, "category of feature ($name) must be set"
        feature.categories.each { project.tasks.getByName(it).dependsOn(feature.task) }
    }
}
