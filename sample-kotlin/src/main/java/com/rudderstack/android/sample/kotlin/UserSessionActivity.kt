package com.rudderstack.android.sample.kotlin

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderClient
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserSessionActivity: AppCompatActivity() {

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_session)
    }

    fun onStartSession(view: View) {
        RudderClient.getInstance()!!.startSession()
    }

    fun onEndSession(view: View) {
        RudderClient.getInstance()!!.endSession()
    }

    fun onStartSessionWithId(view: View) {
        RudderClient.getInstance()!!.startSession(UUID.randomUUID().toString().lowercase())
    }

    fun onReset(view: View) {
        RudderClient.getInstance()!!.reset()
    }

    fun onIncrementTrack(view: View) {
        count += 1
        RudderClient.getInstance()!!.track(String.format("%s_%d", "track", count))
    }

    fun onIncrementScreen(view: View) {
        count += 1
        RudderClient.getInstance()!!.screen(String.format("%s_%d", "screen", count))
    }

    fun onIncrementIdentify(view: View) {
        count += 1
        RudderClient.getInstance()!!.identify(String.format("%s_%d", "user", count))
    }

    fun onIncrementGroup(view: View) {
        count += 1
        RudderClient.getInstance()!!.group(String.format("%s_%d", "group", count))
    }

    fun onIncrementAlias(view: View) {
        count += 1
        RudderClient.getInstance()!!.alias(String.format("%s_%d", "new_user", count))
    }

    fun onMultipleThread(view: View) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()


        executor.submit {
            val executor1: ExecutorService = Executors.newSingleThreadExecutor()
            val executor2: ExecutorService = Executors.newSingleThreadExecutor()
            val executor3: ExecutorService = Executors.newSingleThreadExecutor()

            executor1.submit {
                for (i in 1..1000) {
                    println("Thread-2 - $i")
                    call(i)
                }
            }

            executor2.submit {
                for (i in 1..1000) {
                    println("Thread-3 - $i")
                    call(i)
                }
            }

            executor3.submit {
                for (i in 1..1000) {
                    println("Thread-4 - $i")
                    call(i)
                }
            }

            executor1.shutdown()
            executor2.shutdown()
            executor3.shutdown()
        }

        executor.submit {
            for (i in 1..1000) {
                println("Background Thread-1 - $i")
                call(i)
            }
        }

        executor.shutdown()
    }

    private fun call(index: Int) {
        if (index % 9 == 0) {
            RudderClient.getInstance()!!.track(String.format("%s %d", "Track", index))
        } else if (index % 9 == 1) {
            RudderClient.getInstance()!!.screen(String.format("%s %d", "Screen", index))
        } else if (index % 9 == 2) {
            RudderClient.getInstance()!!.identify(String.format("%s %d", "User", index))
        } else if (index % 9 == 3) {
            RudderClient.getInstance()!!.group(String.format("%s %d", "Group", index))
        } else if (index % 9 == 4) {
            RudderClient.getInstance()!!.alias(String.format("%s %d", "Alias", index))
        } else if (index % 9 == 5) {
            RudderClient.getInstance()!!.startSession()
        } else if (index % 9 == 6) {
            RudderClient.getInstance()!!.startSession(UUID.randomUUID().toString().lowercase())
        } else if (index % 9 == 7) {
            RudderClient.getInstance()!!.endSession()
        } else if (index % 9 == 8) {
            RudderClient.getInstance()!!.reset()
        }
    }
}