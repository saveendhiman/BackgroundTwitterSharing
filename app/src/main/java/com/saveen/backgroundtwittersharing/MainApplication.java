package com.saveen.backgroundtwittersharing;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by saveen 10-23-16.
 * Initialization of required libraries
 */
public class MainApplication extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    public static final String TWITTER_KEY = "BOknC0HA9xZQpaXTzWrGYMbkL";
    public static final String TWITTER_SECRET = "K6boIqroex3C9qqdZqMSsmSJH99X7PDEnAsEjCmhdWmqChADBd";


    @Override
    public void onCreate() {
        super.onCreate();

        //crashlytics
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));

    }

}
