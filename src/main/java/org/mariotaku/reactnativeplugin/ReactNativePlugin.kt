package org.mariotaku.reactnativeplugin

import com.android.build.gradle.AndroidConfig
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskContainer
import org.mariotaku.reactnativeplugin.model.FlavorScope

class ReactNativePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.hasProperty("android")) {
            throw IllegalArgumentException("Project ${project.name} is not an Android project")
        }
        val config = project.extensions.create("reactNative",
                ReactNativeExtensions::class.java, project.objects)
        project.afterEvaluate {
            setupTasks(it, config)
        }
    }

    private fun setupTasks(project: Project, config: ReactNativeExtensions) {
        val android = project.property("android") as AndroidConfig
        val buildTypeNames = android.buildTypes.map { type -> type.name }
        val buildVariants = android.buildVariants

        val cliPath = config.cliPath ?: "node_modules/react-native/local-cli/cli.js"
        val bundleAssetName = config.bundleAssetName ?: "index.android.bundle"
        val entryFile = config.entryFile ?: "index.android.js"
        val bundleCommand = config.bundleCommand ?: "bundle"

        val reactRoot = config.root ?: project.file("../../")
        val inputExcludes = config.inputExcludes ?: arrayOf("android/**", "ios/**")
        val bundleConfig = config.bundleConfig

        val extraEnvironments = config.extraEnvironments

        buildVariants.forEach { buildVariant ->
            buildTypeNames.forEach { buildTypeName ->
                val targetName = buildVariant.camelCaseName(buildTypeName)

                val targetPath = "${buildVariant.camelCaseName}/$buildTypeName"

                // React js bundle directories
                val jsBundleDir = project.file("${project.buildDir}/react/$targetPath/assets")
                val resourcesDir = project.file("${project.buildDir}/react/$targetPath/res")
                val sourceMapFile = project.file("${project.buildDir}/react/$targetPath/sourcemap.js")
                val jsBundleFile = project.file("$jsBundleDir/$bundleAssetName")

                // Bundle task name for variant
                val bundleJsAndAssetsTaskName = buildVariant.camelCaseName(buildTypeName, "bundle", "JsAndAssets")

                // Additional node and packager commandline arguments
                val nodeExecutableAndArgs = config.nodeExecutableAndArgs ?: arrayOf("node")
                val extraPackagerArgs = config.extraPackagerArgs ?: arrayOf()

                android.sourceSets.maybeCreate(buildVariant.camelCaseName(buildTypeName)).also {
                    it.assets.srcDir(jsBundleDir)
                    it.res.srcDir(resourcesDir)
                }

                val task = project.tasks.create(bundleJsAndAssetsTaskName, Exec::class.java) {
                    it.group = "react-native"
                    it.description = "bundle JS and assets for $targetName."

                    it.standardOutput = LogOutputStream(project.logger, LogLevel.INFO)
                    it.errorOutput = LogOutputStream(project.logger, LogLevel.ERROR)

                    it.doFirst {
                        // Create dirs if they are not there (e.g. the "clean" task just ran)
                        jsBundleDir.mkdirs()
                        resourcesDir.mkdirs()
                    }


                    // Set up inputs and outputs so gradle can cache the result
                    it.inputs.files(project.fileTree(reactRoot) {
                        it.exclude(*inputExcludes)
                    })
                    it.outputs.dir(jsBundleDir)
                    it.outputs.dir(resourcesDir)

                    // Set up the call to the react-native cli
                    it.workingDir(reactRoot)

                    // Set up dev mode
                    val devEnabled = buildTypeName.equals("debug", ignoreCase = true).toString()

                    it.enabled = config.bundleIn?.get(targetName) ?: if (buildTypeName == "release") {
                        config.bundleInRelease
                    } else {
                        config.bundleInDebug
                    }

                    it.commandLine = mutableListOf<Any?>().also { cmd ->

                        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                            cmd.add("cmd", "/c")
                        }
                        cmd.add(*nodeExecutableAndArgs, cliPath, bundleCommand)
                        cmd.add("--platform", "android")
                        cmd.add("--dev", devEnabled)
                        cmd.add("--reset-cache")
                        cmd.add("--entry-file", entryFile)
                        cmd.add("--bundle-output", jsBundleFile.absolutePath)
                        cmd.add("--assets-dest", resourcesDir.absolutePath)
                        if (bundleConfig != null) {
                            cmd.add("--config", bundleConfig)
                        }
                        if (config.generateSourceMap) {
                            cmd.add("--sourcemap-output", sourceMapFile)
                        }
                        cmd.addAll(extraPackagerArgs)
                    }

                    if (extraEnvironments != null) {
                        it.environment(extraEnvironments.toMap())
                    }
                }

                project.tasks.injectDependency(buildVariant.camelCaseName(buildTypeName, "generate", "Resources"), task)
            }
        }
    }

    companion object {

        fun TaskContainer.injectDependency(path: String, dependsOn: Task) {
            findByPath(path)?.dependsOn(dependsOn)
        }

        fun <T> MutableList<T>.add(vararg items: T) {
            addAll(items)
        }

        val AndroidConfig.buildVariants: List<FlavorScope>
            get() {
                val dimensions = flavorDimensionList?.takeIf(Collection<*>::isNotEmpty)
                        ?: return listOf(FlavorScope())
                val flavors = productFlavors?.takeIf(Collection<*>::isNotEmpty)
                        ?: return listOf(FlavorScope())
                return dimensions.map { dimension ->
                    flavors.filter { flavor ->
                        flavor.dimension == dimension
                    }.map { flavor ->
                        flavor.name
                    }
                }.combinations().map(::FlavorScope)
            }
    }

}
