class ProjectLocator {
    static project
}

trait ProjectInjection {
    def getProject() {
        ProjectLocator.project
    }
}

trait RemoteFileExtension {
    void eachFile(String directory, Closure closure) {
        sftp {
            ls(directory).each(closure)
        }
    }
}

trait ScriptExtension {
    def executeGroovyScript(String script) {
        def temporaryPath = "/tmp/${UUID.randomUUID()}"
        try {
            execute "mkdir -vp $temporaryPath"
            put from: project.configurations.groovyRuntime, into: temporaryPath
            put text: script, into: "$temporaryPath/script.groovy"
            execute "java -jar $temporaryPath/groovy-all-*.jar $temporaryPath/script.groovy"
        } finally {
            execute "rm -vfr $temporaryPath"
        }
    }
}
