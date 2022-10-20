    package com.example.rudderandroidjs;

    import android.annotation.SuppressLint;
    import android.app.Activity;
    import android.net.http.SslError;
    import android.os.Build;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.KeyEvent;
    import android.view.Window;
    import android.webkit.SslErrorHandler;
    import android.webkit.WebChromeClient;
    import android.webkit.WebResourceRequest;
    import android.webkit.WebResourceResponse;
    import android.webkit.WebSettings;
    import android.webkit.WebView;
    import android.webkit.WebViewClient;

    import androidx.annotation.Nullable;

    import com.rudderstack.android.sdk.core.*;

    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;

    public class MainActivity extends Activity {
        public String androidAnonymousID;
        private WebView mWebView;
        Map<String,Object> traitsMap = new HashMap<>();

        @SuppressLint("JavascriptInterface")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.main_activity);
            RudderClient rudderClient = RudderClient.getInstance(
                    this,
                    "25YL7MIwWWL3HPeEFT53GI5MxVh",
                    new RudderConfig.Builder()
                            .withControlPlaneUrl("https://api.rudderstack.com")
                            .withDataPlaneUrl("https://rudderstacgwyx.dataplane.rudderstack.com")
                            .withTrackLifecycleEvents(true)
                            .withLogLevel(1)
                            .withRecordScreenViews(true)
                            .build()
            );
            RudderTraits traits = new RudderTraits();
            traits.putBirthday(new Date());
            traits.putEmail("abc@123.com");
            traits.putFirstName("First");
            traits.putLastName("Last");
            traits.putGender("m");
            traits.putPhone("5555555555");

            RudderTraits.Address address = new RudderTraits.Address();
            address.putCity("City");
            address.putCountry("USA");
            traits.putAddress(address);

            traits.put("boolean", Boolean.TRUE);
            traits.put("integer", 50);
            traits.put("float", 120.4f);
            traits.put("long", 1234L);
            traits.put("string", "hello");
            traits.put("date", new Date(System.currentTimeMillis()));

            rudderClient.with(this).identify("test_user_id", traits, null);
            rudderClient.with(this).track(
                    "Product Added",
                    new RudderProperty()
                            .putValue("product_id", "product_001")
            );
            androidAnonymousID = "anonIDfromAndroid";

//            traitsMap.put("rudderTraits",traits);
//            androidAnonymousID = RudderTraits.getAnonymousId(traitsMap);
//            System.out.println("AnonymousID is "+ traitsMap.toString());

            new WebAppInterface(this).setAnonymousIdAndrid(androidAnonymousID);

            mWebView=(WebView) this.findViewById(R.id.webView1);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.getSettings().setDatabaseEnabled(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                mWebView.getSettings().setDatabasePath("/data/data/" + mWebView.getContext().getPackageName() + "/databases/");
            }
            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
//            mWebView.loadUrl("https://odd-rat-19.loca.lt/Rectified.html");

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    Log.e("webview", request.getUrl().toString());
                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    Log.e("webview", "SSL error" + error.toString());
                    super.onReceivedSslError(view, handler, error);
                }
            });
            mWebView.loadUrl("file:///android_asset/sample.html");
        }

        @Override
        public boolean onKeyDown(final int keyCode, final KeyEvent event) {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }