package org.mariotaku.reactnativeplugin

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger


class LogOutputStream extends ByteArrayOutputStream {
    private final Logger logger
    private final LogLevel level

    LogOutputStream(Logger logger, LogLevel level) {
        this.logger = logger
        this.level = level
    }

    @Override
    void flush() throws IOException {
        logger.log(level, toString())
        reset()
    }
}