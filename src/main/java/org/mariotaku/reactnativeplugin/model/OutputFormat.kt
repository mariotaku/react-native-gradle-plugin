package org.mariotaku.reactnativeplugin.model

enum class OutputFormat(val extension: String, val formatName: String) {
    PNG("png", "png"), JPEG("jpg", "jpeg"), WEBP("webp", "webp");

    companion object {
        fun forExtension(extension: String): OutputFormat? {
            return OutputFormat.values().first { it.extension == extension }
        }
    }
}