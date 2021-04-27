package com.example.rudderandroidjs;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    public static String anonymousIdAndrid;
    Context mContext;
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    public String setAnonymousIdAndrid(String androidID) {
        anonymousIdAndrid = androidID;
        return anonymousIdAndrid;
    }

    /** return the anonymousID
     * @return*/
    @JavascriptInterface
    public String showAnonId() {
         return anonymousIdAndrid;
    }
}