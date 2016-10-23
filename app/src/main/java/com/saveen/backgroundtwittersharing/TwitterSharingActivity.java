package com.saveen.backgroundtwittersharing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.File;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;

/**
 * Created by saveen 10-23-16.
 *
 */
public class TwitterSharingActivity extends AppCompatActivity {

    TwitterAuthClient twitterAuthClient;
    ImageUtility imageUtility;
    Uri uriCamera;
    String imgPath;
    Button button,button2,button3;
    ImageView imageView;
    TwitterSharingActivity activity;
    EditText etsharing_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_sharing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity = TwitterSharingActivity.this;
        imageUtility = new ImageUtility(activity);
        twitterAuthClient = new TwitterAuthClient();

        etsharing_text = (EditText)findViewById(R.id.etsharing_text);
        imageView = (ImageView)findViewById(R.id.imageView);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
                    shareOnTwitter(imgPath, etsharing_text.getText().toString());
                } else {
                    loginWithTwitter(twitterAuthClient);
                }

            }
        });
        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
                    shareOnTwitter(imgPath, etsharing_text.getText().toString());
                } else {
                    loginWithTwitter(twitterAuthClient);
                }

            }
        });
        button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkMultipleCameraPermissions(ValueConstants.REQUEST_CAMERA_ASK_MULTIPLE_PERMISSIONS);
                } else {
                    cameraPermissionsGranted();
                }
            }
        });


    }

    //login user using twitter
    public void loginWithTwitter(TwitterAuthClient mTwitterAuthClient) {

        mTwitterAuthClient.authorize(activity, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                // Success
                shareOnTwitter(imgPath, etsharing_text.getText().toString());
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
                twitterAuthClient.onActivityResult(requestCode, resultCode, data);
            } else if (requestCode == ValueConstants.REQUEST_CAMERA) {
                imgPath = imageUtility.compressImage(uriCamera.getPath());
                imageUtility.loadImage(imgPath, imageView, R.mipmap.ic_launcher);
            } else if (requestCode == ValueConstants.REQUEST_Gallery) {
                imgPath = imageUtility.compressImage(imageUtility.getRealPathFromURI(data.getData()));
                imageUtility.loadImage(imgPath, imageView, R.mipmap.ic_launcher);

            }
        }
    }

    // share image or video on twitter
    public void shareOnTwitter(String filePath, String title) {
        uploadToTwitter(filePath, title);
    }

    public void uploadToTwitter(String filePath, String title) {

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
            ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();
            twitterConfigBuilder.setDebugEnabled(true);
            twitterConfigBuilder.setOAuthConsumerKey(MainApplication.TWITTER_KEY);
            twitterConfigBuilder.setOAuthConsumerSecret(MainApplication.TWITTER_SECRET);
            twitterConfigBuilder.setOAuthAccessToken(TwitterCore.getInstance().getSessionManager().getActiveSession().getAuthToken().token);
            twitterConfigBuilder.setOAuthAccessTokenSecret(TwitterCore.getInstance().getSessionManager().getActiveSession().getAuthToken().secret);

            Twitter twitter = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
            File file = new File(filePath);

            StatusUpdate status = new StatusUpdate(title);
            status.setMedia(file); // set the image to be uploaded here.
            try {
                twitter.updateStatus(status);
            } catch (twitter4j.TwitterException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if user has allowed application to use Location Permissions else ask for permissions
     */

    @TargetApi(Build.VERSION_CODES.M)
    public void checkMultipleCameraPermissions(int permissionCode) {

        String[] PERMISSIONS = {ValueConstants.CAMERA_PERMISSION, ValueConstants.READ_EXTERNAL_STORAGE_PERMISSION, ValueConstants.WRITE_EXTERNAL_STORAGE_PERMISSION};
        if (!hasPermissions(activity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, permissionCode);
        } else {
            cameraPermissionsGranted();
        }
    }

    private void cameraPermissionsGranted() {

        uriCamera = imageUtility.CameraGalleryIntent(ValueConstants.REQUEST_CAMERA, ValueConstants.REQUEST_Gallery);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ValueConstants.REQUEST_CAMERA_ASK_MULTIPLE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    cameraPermissionsGranted();
                } else {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter_sharing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
