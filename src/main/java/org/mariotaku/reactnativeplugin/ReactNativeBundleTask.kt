package org.mariotaku.reactnativeplugin

import org.gradle.api.tasks.Exec
import java.io.File

open class ReactNativeBundleTask : Exec() {
    lateinit var jsBundleDir: File
    lateinit var resourcesDir: File
}
