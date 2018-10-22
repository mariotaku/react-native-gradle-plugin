package org.mariotaku.reactnativeplugin

import com.android.build.gradle.api.BaseVariant
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Exec

class ReactNativePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.hasProperty("android")) {
            throw IllegalArgumentException("Project ${project.name} is not an Android project")
        }
        def config = project.extensions.create("reactNative", ReactNativeExtensions)
        project.afterEvaluate {
            setupTasks(it, config)
        }
    }

    private void setupTasks(Project project, ReactNativeExtensions config) {
        def cliPath = config.cliPath ?: "node_modules/react-native/local-cli/cli.js"
        def bundleAssetName = config.bundleAssetName ?: "index.android.bundle"
        def entryFile = config.entryFile ?: "index.android.js"
        def bundleCommand = config.bundleCommand ?: "bundle"

        def reactRoot = config.root ?: project.file("../../")
        def inputExcludes = config.inputExcludes ?: ["android/**", "ios/**"]
        def bundleConfig = config.bundleConfig

        def extraEnvironments = config.extraEnvironments

        project.android.applicationVariants.all { BaseVariant variant ->
            // React js bundle directories
            def jsBundleDir = project.file("${project.buildDir}/react/${variant.dirName}/assets")
            def resourcesDir = project.file("${project.buildDir}/react/${variant.dirName}/res")
            def sourceMapFile = project.file("${project.buildDir}/react/${variant.dirName}/sourcemap.js")
            def jsBundleFile = project.file("$jsBundleDir/$bundleAssetName")

            // Bundle task name for variant
            def bundleJsAndAssetsTaskName = "bundle${variant.name.capitalize()}JsAndAssets"

            // Additional node and packager commandline arguments
            def nodeExecutableAndArgs = config.nodeExecutableAndArgs ?: ["node"]
            def extraPackagerArgs = config.extraPackagerArgs ?: []

            project.android.sourceSets.maybeCreate(variant.name).with {
                assets.srcDir(jsBundleDir)
            }

            def task = project.tasks.create(bundleJsAndAssetsTaskName, Exec) { Exec it ->
                it.group = "react-native"
                it.description = "bundle JS and assets for ${variant.name}."

                it.standardOutput = new LogOutputStream(project.logger, LogLevel.INFO)
                it.errorOutput = new LogOutputStream(project.logger, LogLevel.ERROR)

                it.doFirst {
                    // Create dirs if they are not there (e.g. the "clean" task just ran)
                    jsBundleDir.mkdirs()
                    resourcesDir.mkdirs()
                }

                // Set up inputs and outputs so gradle can cache the result
                it.inputs.files(project.fileTree(reactRoot) {
                    it.exclude("node_modules/")
                    it.exclude(inputExcludes)
                })
                
                it.outputs.dir(jsBundleDir)
                it.outputs.dir(resourcesDir)

                // Set up the call to the react-native cli
                it.workingDir(reactRoot)

                // Set up dev mode
                def devEnabled = variant.buildType.debuggable

                it.enabled = config.bundleIn?.get(variant.name) ?: (variant.buildType.debuggable ? config.bundleInDebug : config.bundleInRelease)

                def cmd = new LinkedList<Object>()

                if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                    cmd.addAll("cmd", "/c")
                }

                cmd.addAll(*nodeExecutableAndArgs, cliPath, bundleCommand)
                cmd.addAll("--platform", "android")
                cmd.addAll("--dev", devEnabled)
                cmd.addAll("--reset-cache")
                cmd.addAll("--entry-file", entryFile)
                cmd.addAll("--bundle-output", jsBundleFile.absolutePath)
                cmd.addAll("--assets-dest", resourcesDir.absolutePath)
                if (bundleConfig != null) {
                    cmd.addAll("--config", bundleConfig)
                }
                if (config.generateSourceMap) {
                    cmd.addAll("--sourcemap-output", sourceMapFile)
                }
                cmd.addAll(extraPackagerArgs)

                it.commandLine(cmd)

                if (extraEnvironments != null) {
                    it.environment(extraEnvironments)
                }
            }

            variant.registerGeneratedResFolders(project.files(resourcesDir).builtBy(task))
        }
    }

}
