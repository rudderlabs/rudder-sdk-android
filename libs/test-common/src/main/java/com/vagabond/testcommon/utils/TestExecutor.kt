package com.vagabond.testcommon.utils

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class TestExecutor : AbstractExecutorService() {
    private var _isShutdown = false
    override fun execute(command: Runnable) {
        command.run()
    }

    override fun shutdown() {
        //No op
        _isShutdown = true
    }

    override fun shutdownNow(): MutableList<Runnable> {
        // No op
        shutdown()
        return mutableListOf()
    }

    override fun isShutdown(): Boolean {
        return _isShutdown
    }

    override fun isTerminated(): Boolean {
        return _isShutdown
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        return false
    }
}
