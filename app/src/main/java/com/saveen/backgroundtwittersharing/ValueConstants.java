package com.saveen.backgroundtwittersharing;

/**
 * Created by saveen on 10-23-16.
 * contains constant values used in application
 */
public interface ValueConstants {

    //requests for runtime time permissions
    String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
    String READ_EXTERNAL_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    String WRITE_EXTERNAL_STORAGE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

    //request codes used in app
    int REQUEST_CAMERA = 1;
    int REQUEST_Gallery = 2;
    int REQUEST_CAMERA_ASK_MULTIPLE_PERMISSIONS = 3;

}
