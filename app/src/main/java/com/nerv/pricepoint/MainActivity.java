package com.nerv.pricepoint;

import android.Manifest;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String[] PERMISSIONS = {Manifest.permission.INTERNET
            , Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    public static final int IMAGE_LOADED = 0;
    public static final int DB_CALLBACK = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 0;


    private DatabaseManager databaseManager;
    private PageController pageController;
    private boolean hasPermissions = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    public static Handler handler;
    private Uri imageUri;


    public boolean hasPermissions(String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 111: {
                hasPermissions = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        System.out.println("Permissions --> " + "Permission Granted: " + permissions[i]);

                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.out.println("Permissions --> " + "Permission Denied: " + permissions[i]);
                        hasPermissions = false;
                    }
                }

                if (hasPermissions) {
                    finish();
                    startActivity(getIntent());
                } else {
                    finish();
                    //android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void checkPermissions() {
        if (!hasPermissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!hasPermissions(PERMISSIONS)) {
                    requestPermissions(PERMISSIONS, 111);
                } else {
                    hasPermissions = true;
                }
            }
        }
    }

    private DatabaseManager.AuthCallback authCallback = new DatabaseManager.AuthCallback() {
        @Override
        public void authCallback() {
            pageController.setPage(PageController.Page.LOGIN);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        hasPermissions = hasPermissions(PERMISSIONS);

        if (hasPermissions) {
            Utils.init(this);

            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    switch (inputMessage.what) {
                        case IMAGE_LOADED:
                            Task.Img img = (Task.Img) inputMessage.obj;
                            img.callListeners();
                            break;
                        case DB_CALLBACK:
                            DatabaseManager.Callback callback = (DatabaseManager.Callback) inputMessage.obj;
                            callback.callback();
                            break;
                        default:
                    }
                    super.handleMessage(inputMessage);
                }
            };


            pageController = new PageController(this);

            databaseManager = new DatabaseManager(this);

            FragmentManager.init(this);

            if (!databaseManager.silentConnect(authCallback)) {
                databaseManager.interactiveConnect(authCallback);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasPermissions) {
            checkPermissions();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PageController getPageController() {
        return pageController;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (databaseManager != null) {
            databaseManager.authContext.onActivityResult(requestCode, resultCode, data);
        }

        switch(requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    cameraPhotoCallback.photoTaken(currentPhotoPath);
                } else {
                    super.onResume();
                }
                break;
        }
    }

    private String currentPhotoPath;
    private CameraPhotoCallback cameraPhotoCallback;

    public void getPhotoFromCamera(CameraPhotoCallback callback) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraPhotoCallback = callback;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.nerv.pricepoint.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (pageController != null) {
            if (!pageController.onBackPress()) {
                minimizeApp();
            }
        } else {
            minimizeApp();
        }
    }


    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private File createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "temp",
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
