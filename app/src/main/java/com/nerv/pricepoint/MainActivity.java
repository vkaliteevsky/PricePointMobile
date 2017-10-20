package com.nerv.pricepoint;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static String[] PERMISSIONS = {Manifest.permission.INTERNET
            , Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private DatabaseManager databaseManager;
    private PageController pageController;
    private boolean hasPermissions = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;


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
    }
}
