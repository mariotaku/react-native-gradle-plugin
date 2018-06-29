package org.mariotaku.reactnativeplugin

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.ByteArrayOutputStream

class LogOutputStream(val logger: Logger, val level: LogLevel) : ByteArrayOutputStream() {

    override fun flush() {
        logger.log(level, toString())
        reset()
    }
}