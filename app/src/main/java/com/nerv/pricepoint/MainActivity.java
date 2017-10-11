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
    public static final String TAG = MainActivity.class.getSimpleName();
    Button callGraphButton;

    private DatabaseManager databaseManager;
    private PageController pageController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callGraphButton = (Button) findViewById(R.id.callGraph);


        callGraphButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCallGraphClicked();
            }
        });

        pageController = new PageController(this);

        databaseManager = new DatabaseManager(this);

        FragmentManager.init(this);

        databaseManager.silentConnect(new DatabaseManager.AuthCallback() {
            @Override
            public void authCallback() {
                pageController.setPage(PageController.Page.LOGIN);
            }
        });
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    private void onCallGraphClicked() {
        databaseManager.interactiveConnect(new DatabaseManager.AuthCallback() {
            @Override
            public void authCallback() {

            }
        });
    }

    /* Handles the redirect from the System Browser */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        databaseManager.clientApp.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    /*private void getTasks() {
        Utils.requestJSONObject(this, authResult.getAccessToken(), Request.Method.GET
                , "https://graph.microsoft.com/v1.0/sites/" + siteId + "/lists/" + taskListId
                        + "/items?expand=fields(select=task_ean,task_id,task_start)"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lists = response.getJSONArray("value");

                            LinearLayout tasks = (LinearLayout) findViewById(R.id.tasks);
                            tasks.removeAllViews();

                            for (int i = 0; i < lists.length(); i++) {
                                JSONObject obj = lists.getJSONObject(i);
                                JSONObject fs = obj.getJSONObject("fields");

                                TextView tv = new TextView(MainActivity.this);
                                tv.setText("task_id: " + fs.getString("task_id") + ", task_ean: " + fs.getString("task_ean"));

                                tasks.addView(tv);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("KURWA", "");
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("KURWA", "");
                    }
                });
    }*/

    /*private void onSignOutClicked() {
        List<User> users = null;

        try {
            users = sampleApp.getUsers();

            if (users == null) {

            } else if (users.size() == 1) {
                sampleApp.remove(users.get(0));
                updateSignedOutUI();

            }
            else {
                for (int i = 0; i < users.size(); i++) {
                    sampleApp.remove(users.get(i));
                }
            }

            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT)
                    .show();

        } catch (MsalClientException e) {
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }
    }*/
}
