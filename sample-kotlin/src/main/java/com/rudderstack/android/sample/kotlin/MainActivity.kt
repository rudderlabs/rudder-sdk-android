package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderstack.android.sdk.core.RudderLogger


class MainActivity : AppCompatActivity() {

    companion object {
        var count: Int = 0;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        count++
//        TestCase #6
//        MainApplication.rudderClient!!.track("Test Event ${count}");
//        if(count == 3) {
//            RudderLogger.logDebug("Ending the manual session on ${count+1} App Open")
//            MainApplication.rudderClient!!.endSession();
//        }

//        TestCase #7
//          MainApplication.rudderClient!!.track("Test Event ${count}");
//        if(count == 2) {
//            RudderLogger.logDebug("Starting the manual session on ${count+1} App Open")
//            MainApplication.rudderClient!!.startSession();
//        }

//        TestCase #26, 27
//        MainApplication.rudderClient!!.track("Activity Started ${count}");

    //        TestCase #28
//        MainApplication.rudderClient!!.track("Activity Started ${count}");
//        if (count == 2) {
//            RudderLogger.logDebug("User is opting back for tracking his activity on ${count + 1} App Open")
//            MainApplication.rudderClient!!.optOut(false);
//        }

    }
}