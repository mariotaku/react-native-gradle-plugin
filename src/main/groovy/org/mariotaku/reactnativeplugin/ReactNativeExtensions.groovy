package org.mariotaku.reactnativeplugin

class ReactNativeExtensions {

    // the root of your project, i.e. where "package.json" lives
    String root = null
    String cliPath = null

    // the name of the generated asset file containing your JS bundle
    String bundleAssetName = null
    String bundleCommand = null

    String bundleConfig = null

    // the entry file for bundle generation
    String entryFile = null

    // whether to bundle JS and assets in debug mode
    boolean bundleInDebug = false

    // whether to bundle JS and assets in release mode
    boolean bundleInRelease = true

    boolean generateSourceMap = true

    Map<String, Boolean> bundleIn = null

    // by default the gradle tasks are skipped if none of the JS files or assets change; this means
    // that we don't look at files in android/ or ios/ to determine whether the tasks are up to
    // date; if you have any other folders that you want to ignore for performance reasons (gradle
    // indexes the entire tree), add them here. Alternatively, if you have JS files in android/
    // for example, you might want to remove it from here.
    Set<String> inputExcludes = null

    List<String> nodeExecutableAndArgs = null
    List<String> extraPackagerArgs = null

    Map<String, String> extraEnvironments = null

    void extraEnvironment(String key, String value) {
        if (extraEnvironments == null) {
            extraEnvironments = new HashMap<>()
        }
        extraEnvironments[key] = value
    }
}
