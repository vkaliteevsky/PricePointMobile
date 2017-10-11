package com.nerv.pricepoint;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.microsoft.identity.client.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by NERV on 10.10.2017.
 */

public class DatabaseManager {
    public interface AuthCallback {
        void authCallback();
    }

    public enum LogInResult {
        WRONG_PASSWORD, USER_NOT_FOUND, OK
    }

    public interface LogInCallback {
        void logInCallback(LogInResult result);
    }

    private final static String BASE_URL = "https://graph.microsoft.com/v1.0/sites/";
    private final static String CLIENT_ID = "e1d891f5-b7b2-4a7d-88ba-964fb697d332";
    private final static String SCOPES [] = {"https://graph.microsoft.com/User.Read"
                                            ,"https://graph.microsoft.com/Sites.Read.All"
                                            , "https://graph.microsoft.com/Sites.ReadWrite.All"};

    private String siteId;
    private String taskListId;
    private String cityListId;
    private String userListId;
    private String stockListId;

    private Activity activity;
    public PublicClientApplication clientApp;
    private AuthenticationResult authResult;
    private User user;
    private AuthCallback authCallback;
    private DBRequest dbRequest;

    private int userId = -1;
    private String userLogin;
    private String userPassword;

    public DatabaseManager(Activity activity) {
        this.activity = activity;

        clientApp = new PublicClientApplication(activity, CLIENT_ID);

        List<User> users;

        try {
            users = clientApp.getUsers();

            if (users != null && users.size() == 1) {
                user = users.get(0);
            }
        } catch (MsalClientException e) {
            Log.d(MainActivity.TAG, "MSAL Exception Generated while getting users: " + e.toString());
        }
    }

    public void retrieveUserTasks() {
        if (userId == -1) {
            return;
        }

        dbRequest.retrieveListItems(taskListId, new DBRequest.RequestCallback() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("", "");
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });

        dbRequest.expandFields(null);
        dbRequest.filter("fields/task_idman eq " + String.valueOf(userId));
        dbRequest.request();
    }

    public void checkLoginPassword(final String login, final String password, final LogInCallback callback) {
        if (callback == null) {
            return;
        }

        dbRequest.retrieveListItems(userListId, new DBRequest.RequestCallback() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray lists = response.getJSONArray("value");

                    if (lists.length() == 0) {
                        callback.logInCallback(LogInResult.USER_NOT_FOUND);
                    } else {
                        JSONObject fields = lists.getJSONObject(0).getJSONObject("fields");
                        String user_pass = fields.getString("user_pass");

                        if (password.equals(user_pass)) {
                            userId = fields.getInt("user_id");
                            userLogin = login;
                            userPassword = user_pass;

                            callback.logInCallback(LogInResult.OK);
                        } else {
                            callback.logInCallback(LogInResult.WRONG_PASSWORD);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });

        dbRequest.expandFields("user_pass,user_id");
        dbRequest.filter("fields/user_mail eq '" + login + "'");
        dbRequest.request();

        //retrieve from User list
        /*Utils.requestJSONObject(activity, authResult.getAccessToken(), Request.Method.GET
                , BASE_URL + siteId + "/lists/" + userListId + "/items?$filter=fields/user_mail eq '"
                        + login + "'&$expand=fields($select=user_pass)"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lists = response.getJSONArray("value");

                            if (lists.length() == 0) {
                                callback.logInCallback(LogInResult.USER_NOT_FOUND);
                            } else {
                                String user_pass = lists.getJSONObject(0).getJSONObject("fields").getString("user_pass");

                                if (password.equals(user_pass)) {
                                    callback.logInCallback(LogInResult.OK);
                                } else {
                                    callback.logInCallback(LogInResult.WRONG_PASSWORD);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });*/
    }

    public boolean silentConnect(AuthCallback callback) {
        if (user != null) {
            authCallback = callback;
            clientApp.acquireTokenSilentAsync(SCOPES, user, getAuthCallback());
            return true;
        }

        return false;
    }

    public void interactiveConnect(AuthCallback callback) {
        authCallback = callback;
        clientApp.acquireToken(activity, SCOPES, getAuthCallback());
    }

    private AuthenticationCallback getAuthCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                authResult = authenticationResult;
                retrieveListsInfo();
            }

            @Override
            public void onError(MsalException exception) {
                Log.d(MainActivity.TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                } else if (exception instanceof MsalServiceException) {
                } else if (exception instanceof MsalUiRequiredException) {
                }
            }

            @Override
            public void onCancel() {
                Log.d(MainActivity.TAG, "User cancelled login.");
            }
        };
    }

    private void retrieveListsInfo() {
        if (authResult.getAccessToken() == null) {
            return;
        }

        Utils.requestJSONObject(activity, authResult.getAccessToken(), Request.Method.GET
                , BASE_URL + "pointbox.sharepoint.com:/boxpoint"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            siteId = response.getString("id");

                            dbRequest = new DBRequest(activity, siteId, authResult.getAccessToken());

                            getListsInfo();
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

    private void getListsInfo() {
        Utils.requestJSONObject(activity, authResult.getAccessToken(), Request.Method.GET
                , BASE_URL + siteId + "/lists"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lists = response.getJSONArray("value");
                            for (int i = 0; i < lists.length(); i++) {
                                JSONObject list = (JSONObject) lists.get(i);
                                String name = list.getString("name");
                                String id = list.getString("id");

                                switch (name) {
                                    case "Task":
                                        taskListId = id;
                                        break;
                                    case "City":
                                        cityListId = id;
                                        break;
                                    case "User":
                                        userListId = id;
                                        break;
                                    case "Stock":
                                        stockListId = id;
                                        break;
                                }
                            }

                            if (authCallback != null) {
                                authCallback.authCallback();
                            }
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
}
