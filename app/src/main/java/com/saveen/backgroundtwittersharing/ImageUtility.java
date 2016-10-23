package com.saveen.backgroundtwittersharing;

/**
 * created by saveen on 10-23-16
 * Image related utility methods
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtility {
    private Context context;

    public ImageUtility(Context context) {
        this.context = context;
    }

    public Uri CameraGalleryIntent(final int cameraRequestCode, final int galleryRequestCode) {
        final File root = getFile();
        root.mkdirs();
        String filename = getUniqueImageFilename();
        File sdImageMainDirectory = new File(root, filename);
        final Uri outputFileUri = Uri.fromFile(sdImageMainDirectory);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        CharSequence items[] = new CharSequence[]{context.getResources().getString(R.string.camera), context.getResources().getString(R.string.gallery)};
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.e("tag", "request :" + which);
                try {
                    if (which == 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        ((Activity) context).startActivityForResult(intent, cameraRequestCode);
                    } else if (which == 1) {
                        Intent pickIntent = new Intent(Intent.ACTION_PICK);
                        pickIntent.setType("image/* ");
                        pickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        ((Activity) context).startActivityForResult(pickIntent, galleryRequestCode);
                    }
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
            }
        });
        dialog.setTitle(context.getResources().getString(R.string.selection));
        if (outputFileUri != null) {
            dialog.show();
        }
        return outputFileUri;
    }

    /**
     * Compresses the image
     *
     * @param filePath : Location of image file
     * @return
     */
    @SuppressWarnings("deprecation")
    public String compressImage(String filePath) {
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        options.inSampleSize = ImageUtility.calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(
                Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Log.e("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.e("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.e("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.e("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                    matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bmp != null) {
                bmp.recycle();
                bmp = null;
            }
            if (scaledBitmap != null) {
                scaledBitmap.recycle();
            }
        }
        return filename;

    }

    public String getFilename() {

        final File root = getFile();
        root.mkdirs();
        final String filename = getUniqueImageFilename();
        File file = new File(root, filename);
        return file.getAbsolutePath();
    }

    private File getFile() {
        return new File(Environment.getExternalStorageDirectory() + File.separator + "MakerApp"
                + File.separator);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static String getUniqueImageFilename() {
        return "img_" + System.currentTimeMillis() + ".png";
    }

    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = ((Activity) context).managedQuery(contentUri, proj, // Which
                // columns
                // to
                // return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public void loadImage(String url, ImageView imageView, @DrawableRes int placeholder) {
        if (placeholder != 0) {
            Glide.with(context).load(url).placeholder(placeholder).error(placeholder).into(imageView);
        } else {
            Glide.with(context).load(url).into(imageView);
        }
    }

    /**
     * convert image to base 64 string
     *
     * @param filePath path of image
     * @return base 64 string
     */
    public String getBase64Image(String filePath) {
        filePath = getCompressedImage(filePath);
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * convert bitmap to base 64 image
     */
    public String convertBitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * get compressed image
     */
    private String getCompressedImage(String filePath) {
        String newFilePath;
        int file_size = Integer.parseInt(String.valueOf(new File(filePath).length() >> 10));
        if (file_size >= 120) {
            newFilePath = compressImage(filePath);
            ;
        } else {
            newFilePath = filePath;
        }
        return filePath;
    }


}