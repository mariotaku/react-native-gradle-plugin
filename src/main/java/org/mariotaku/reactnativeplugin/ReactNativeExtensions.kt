package org.mariotaku.reactnativeplugin

import org.gradle.api.internal.model.DefaultObjectFactory

open class ReactNativeExtensions(factory: DefaultObjectFactory) {

    // the root of your project, i.e. where "package.json" lives
    var root: String? = null
    var cliPath: String? = null

    // the name of the generated asset file containing your JS bundle
    var bundleAssetName: String? = null
    var bundleCommand: String? = null

    var bundleConfig: String? = null

    // the entry file for bundle generation
    var entryFile: String? = null

    // whether to bundle JS and assets in debug mode
    var bundleInDebug: Boolean = false

    // whether to bundle JS and assets in release mode
    var bundleInRelease: Boolean = true

    var bundleIn: Map<String, Boolean>? = null

    // by default the gradle tasks are skipped if none of the JS files or assets change; this means
    // that we don't look at files in android/ or ios/ to determine whether the tasks are up to
    // date; if you have any other folders that you want to ignore for performance reasons (gradle
    // indexes the entire tree), add them here. Alternatively, if you have JS files in android/
    // for example, you might want to remove it from here.
    var inputExcludes: Array<String>? = null

    var nodeExecutableAndArgs: Array<String>? = null
    var extraPackagerArgs: Array<String>? = null

    var extraEnvironments: MutableMap<String, String>? = null
        private set

    fun extraEnvironment(key: String, value: String) {
        if (extraEnvironments == null) {
            extraEnvironments = mutableMapOf(key to value)
        } else {
            extraEnvironments!![key] = value
        }
    }
}
