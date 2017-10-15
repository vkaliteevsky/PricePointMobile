package com.nerv.pricepoint;

import android.net.Uri;
import android.os.AsyncTask;
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

    private DatabaseManager databaseManager;
    private PageController pageController;

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

        pageController = new PageController(this);

        databaseManager = new DatabaseManager(this);

        FragmentManager.init(this);

        if (!databaseManager.silentConnect(authCallback)) {
            databaseManager.interactiveConnect(authCallback);
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
