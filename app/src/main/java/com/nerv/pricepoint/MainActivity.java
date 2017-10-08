package com.nerv.pricepoint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.microsoft.identity.client.*;

public class MainActivity extends AppCompatActivity {

    final static String CLIENT_ID = "e1d891f5-b7b2-4a7d-88ba-964fb697d332";
    final static String SCOPES [] = {"https://graph.microsoft.com/User.Read"
            ,"https://graph.microsoft.com/Sites.Read.All"
            , "https://graph.microsoft.com/Sites.ReadWrite.All"};
    final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me";

    /* UI & Debugging Variables */
    private static final String TAG = MainActivity.class.getSimpleName();
    Button callGraphButton;
    Button signOutButton;

    /* Azure AD Variables */
    private PublicClientApplication sampleApp;
    private AuthenticationResult authResult;

    private String siteId;
    private String userListId;
    private String taskListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callGraphButton = (Button) findViewById(R.id.callGraph);
        signOutButton = (Button) findViewById(R.id.clearCache);

        callGraphButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCallGraphClicked();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSignOutClicked();
            }
        });

        sampleApp = null;

        if (sampleApp == null) {
            sampleApp = new PublicClientApplication(
                    this.getApplicationContext(),
                    CLIENT_ID);
        }

        List<User> users = null;

        try {
            users = sampleApp.getUsers();

            if (users != null && users.size() == 1) {
                sampleApp.acquireTokenSilentAsync(SCOPES, users.get(0), getAuthSilentCallback());
            } else {
                sampleApp.acquireToken(this, SCOPES, getAuthInteractiveCallback());
            }
        } catch (MsalClientException e) {
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }

    }

    //
    // App callbacks for MSAL
    // ======================
    // getActivity() - returns activity so we can acquireToken within a callback
    // getAuthSilentCallback() - callback defined to handle acquireTokenSilent() case
    // getAuthInteractiveCallback() - callback defined to handle acquireToken() case
    //

    public MainActivity getActivity() {
        return this;
    }

    /* Callback method for acquireTokenSilent calls
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     */
    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                authResult = authenticationResult;

                getSiteId();

                updateSuccessUI();
            }

            @Override
            public void onError(MsalException exception) {
            /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                } else if (exception instanceof MsalServiceException) {
                } else if (exception instanceof MsalUiRequiredException) {
                }
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled login.");
            }
        };
    }


    /* Callback used for interactive request.  If succeeds we use the access
         * token to call the Microsoft Graph. Does not check cache
         */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                authResult = authenticationResult;
                getSiteId();

                updateSuccessUI();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                     /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    /* Set the UI for successful token acquisition data */
    private void updateSuccessUI() {
        callGraphButton.setVisibility(View.INVISIBLE);
        signOutButton.setVisibility(View.VISIBLE);
        findViewById(R.id.welcome).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.welcome)).setText("Welcome, " +
                authResult.getUser().getName());
        findViewById(R.id.graphData).setVisibility(View.VISIBLE);
    }

    /* Use MSAL to acquireToken for the end-user
     * Callback will call Graph api w/ access token & update UI
     */
    private void onCallGraphClicked() {
        sampleApp.acquireToken(getActivity(), SCOPES, getAuthInteractiveCallback());
    }

    /* Handles the redirect from the System Browser */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        sampleApp.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    private void getSiteId() {
        if (authResult.getAccessToken() == null) {return;}

        Utils.requestJSONObject(this, authResult.getAccessToken(), Request.Method.GET
                , "https://graph.microsoft.com/v1.0/sites/pointbox.sharepoint.com:/boxpoint"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            siteId = response.getString("id");
                            getLists();
                        } catch (JSONException e) {

                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
    }

    private void getLists() {

        Utils.requestJSONObject(this, authResult.getAccessToken(), Request.Method.GET
                , "https://graph.microsoft.com/v1.0/sites/" + siteId + "/lists"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lists = response.getJSONArray("value");
                            for (int i = 0; i < lists.length(); i++) {
                                JSONObject list = (JSONObject) lists.get(i);
                                if (list.getString("name").equals("Task")) {
                                    taskListId = list.getString("id");
                                    break;
                                }
                            }

                            getTasks();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
    }

    private void getTasks() {
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

                            /*JSONObject fst = (JSONObject) lists.get(0);
                            JSONObject snd = (JSONObject) lists.get(1);

                            JSONObject fs1 = fst.getJSONObject("fields");
                            JSONObject fs2 = snd.getJSONObject("fields");

                            TextView t1 = (TextView) findViewById(R.id.task1);
                            TextView t2 = (TextView) findViewById(R.id.task2);

                            t1.setText("task_id: " + fs1.getString("task_id") + ", task_ean: " + fs1.getString("task_ean"));
                            t2.setText("task_id: " + fs2.getString("task_id") + ", task_ean: " + fs2.getString("task_ean"));*/

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
    }

    /* Sets the Graph response */
    private void updateGraphUI(JSONObject graphResponse) {
        TextView graphText = (TextView) findViewById(R.id.graphData);
        graphText.setText(graphResponse.toString());
    }

    /* Clears a user's tokens from the cache.
    * Logically similar to "sign out" but only signs out of this app.
    */
    private void onSignOutClicked() {

        /* Attempt to get a user and remove their cookies from cache */
        List<User> users = null;

        try {
            users = sampleApp.getUsers();

            if (users == null) {
            /* We have no users */

            } else if (users.size() == 1) {
                /* We have 1 user */
                /* Remove from token cache */
                sampleApp.remove(users.get(0));
                updateSignedOutUI();

            }
            else {
                 /* We have multiple users */
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
    }

    /* Set the UI for signed-out user */
    private void updateSignedOutUI() {
        callGraphButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.INVISIBLE);
        findViewById(R.id.welcome).setVisibility(View.INVISIBLE);
        findViewById(R.id.graphData).setVisibility(View.INVISIBLE);
        ((TextView) findViewById(R.id.graphData)).setText("No Data");
    }
}
