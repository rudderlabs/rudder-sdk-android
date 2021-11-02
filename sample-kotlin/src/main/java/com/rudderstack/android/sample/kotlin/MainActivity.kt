package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sample.kotlin.MainApplication
import com.rudderstack.android.sample.kotlin.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Thread1().start()
//        Thread2().start()
//        Thread3().start()
//        Thread4().start()
//        Thread5().start()
//        Thread6().start()
//        Thread7().start()
//        Thread8().start()
//        Thread9().start()
//        Thread10().start()


        var count = 1

        while (count <= 10) {
            println("Main Thread Event: " + count);
            MainApplication.rudderClient!!.track("Main Thread Event : " + count)
            count++;
        }
    }
}


class Thread1 : Thread() {
    public override fun run() {
        var count = 1
        while (count <= 5000) {
            println("Thread 1 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 1 Event : " + count)
            count++;
        }
    }
}

class Thread2 : Thread() {
    public override fun run() {
        var count = 1
        while (count <= 5000) {
            println("Thread 2 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 2 Event : " + count)
            count++;
        }
    }
}

class Thread3 : Thread() {
    public override fun run() {
        var count = 1
        while (count <= 5000) {
            println("Thread 3 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 3 Event : " + count)
            count++;
        }
    }
}

class Thread4 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 4 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 4 Event : " + count)
            count++;
        }
    }
}

class Thread5 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 5 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 5 Event : " + count)
            count++;
        }
    }
}

class Thread6 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 6 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 6 Event : " + count)
            count++;
        }
    }
}

class Thread7 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 7 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 7 Event : " + count)
            count++;
        }
    }
}

class Thread8 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 8 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 8 Event : " + count)
            count++;
        }
    }
}

class Thread9 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 9 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 9 Event : " + count)
            count++;
        }
    }
}

class Thread10 : Thread() {
    public override fun run() {
        var count = 1

        while (count <= 5000) {
            println("Thread 10 Event: " + count);
            MainApplication.rudderClient!!.track("Thread 10 Event : " + count)
            count++;
        }
    }
}