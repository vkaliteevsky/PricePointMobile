package com.nerv.pricepoint;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by NERV on 10.10.2017.
 */

public class DatabaseManager {

    public static final String TAG = MainActivity.class.getSimpleName();

    public interface AuthCallback {
        void authCallback();
    }

    public enum LogInResult {
        WRONG_PASSWORD, USER_NOT_FOUND, OK, NO_CONNECTION
    }

    public interface LogInCallback {
        void logInCallback(LogInResult result);
    }

    public interface Callback {
        void callback();
    }

    private final static String CLIENT_ID = "e5bd4077-60fc-4d61-a359-a8c80de8d17a";
    private final static String AUTHORITY = "https://login.microsoftonline.com/common/oauth2/authorize";
    private final static String RESOURCE = "https://pointbox.sharepoint.com";
    private final static String REDIRECT_URI = "http://pricepointmobile";
    private final static String APP_PREFERENCES = "appSettings";
    private final static String TASK_FIELDS = "&$select=task_start,task_end,task_retail,task_city,task_address,task_idorder," +
            "task_mark,GUID,task_costreg,task_costcard,task_costpromo,task_commet,task_lat,task_lon,task_class,task_ean," +
            "Title,task_photo,task_id,task_edit,task_no,task_done,task_sync,task_stock,task_eanscan,ID";

    private final static String STOCK_FIELDS = "?$select=Title,stock_id";
    private final static String STOCK_ITEMS = "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('Stock')/items" + STOCK_FIELDS;


    private AuthenticationCallback<AuthenticationResult> callback = new AuthenticationCallback<AuthenticationResult>() {

        @Override
        public void onError(Exception exc) {
            if (exc instanceof AuthenticationException) {
                Log.d(TAG, "Cancelled");
            } else {
                Log.d(TAG, "Authentication error:" + exc.getMessage());
            }
        }

        @Override
        public void onSuccess(AuthenticationResult result) {
            authRes = result;

            if (result == null || result.getAccessToken() == null
                    || result.getAccessToken().isEmpty()) {
                Log.d(TAG, "Token is empty");
            } else {
                aadUserId = authRes.getUserInfo().getUserId();
                saveAADUserId();
                authCallback.authCallback();
            }
        }
    };

    private Activity activity;
    private AuthCallback authCallback;

    private int userId = -1;
    private String userLogin;
    private String userPassword;
    public String userName;

    public ArrayList<Order> orders;
    public HashMap<Integer, Promo> promos = new HashMap<>();
    private HashMap<Integer, Order> ordersHM;

    public AuthenticationContext authContext;
    public AuthenticationResult authRes;
    private String aadUserId = "";
    private SharedPreferences appSettings;

    public Order selectedOrder;
    public Task selectedTask;

    private String formDigestValue = "";

    public DatabaseManager(Activity activity) {
        this.activity = activity;

        authContext = new AuthenticationContext(activity, AUTHORITY, true);
        appSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void saveAADUserId() {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putString("aadUserId", aadUserId);
        editor.apply();
    }

    private void getAADUserId() {
        aadUserId = appSettings.getString("aadUserId", "");
    }

    public void getFormDigest() {
        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.POST
                , "https://pointbox.sharepoint.com/boxpoint/_api/contextinfo"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            formDigestValue = response.getJSONObject("d")
                                    .getJSONObject("GetContextWebInformation")
                                    .getString("FormDigestValue");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("","");
                    }
                });
    }

    public void sendData(final Task task, final Callback callback) {
        String data = "{'__metadata':{'type':'SP.Data.TaskListItem'}}";
        JSONObject json = null;

        try {
            json = new JSONObject(data);

            json.put("Title", "we");

            /*json.put("task_costreg", task.costReg);
            json.put("task_costcard", task.costCard);
            json.put("task_costpromo", task.costPromo);
            json.put("task_commet", task.comment);
            json.put("task_lat", task.latitude);
            json.put("task_lon", task.longitude);
            json.put("task_photo", task.photosCount);
            json.put("task_edit", false);
            json.put("task_no", task.noGoods);
            json.put("task_done", true);
            json.put("task_stock", task.promo);
            json.put("task_sync", true);
            json.put("task_eanscan", task.eanscan);*/

            Utils.sendJSONObject(activity
                    , authRes.getAccessToken()
                    , formDigestValue
                    , "\"56\""
                    , json.toString()
                    , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle(%27Task%27)/items(" + task.dbId + ")"
                    , new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            task.sync = true;
                        }
                    }
                    , new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            task.sync = false;
                            String msg = new String(error.networkResponse.data);
                            Log.d("", "");
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void retrievePromos(String url) {
        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , url
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject d = response.getJSONObject("d");

                            Promo.getPromos(d.getJSONArray("results"), promos);

                            if (d.has("__next")) {
                                retrievePromos(d.getString("__next"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("","");
                    }
                });
    }

    public void retrieveUserTasks(final Callback callback) {
        if (userId == -1) {
            return;
        }

        ordersHM = new HashMap<>();

        retrievePromos(STOCK_ITEMS);

        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('Task')/items" +
                        "?$filter=task_idman%20eq%20" + String.valueOf(1) /*+ TASK_FIELDS*/ + "&$expand=AttachmentFiles"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject d = response.getJSONObject("d");

                            Order.getOrders(response.getJSONObject("d").getJSONArray("results"), ordersHM);

                            if (d.has("__next")) {
                                retrieveUserTasksNext(d.getString("__next"), callback);
                            } else {
                                orders = new ArrayList<>(ordersHM.values());

                                for (Order o : orders) {
                                    o.sortTasksByCategory();
                                }

                                callback.callback();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("","");
                    }
                });
    }

    private void retrieveUserTasksNext(String url, final Callback callback) {
        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET, url
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject d = response.getJSONObject("d");

                            Order.getOrders(response.getJSONObject("d").getJSONArray("results"), ordersHM);

                            if (d.has("__next")) {
                                retrieveUserTasksNext(d.getString("__next"), callback);
                            } else {
                                orders = new ArrayList<>(ordersHM.values());
                                ordersHM = null;

                                for (Order o : orders) {
                                    o.sortTasksByCategory();
                                }

                                callback.callback();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("","");
                    }
                });
    }

    public void checkLoginPassword(final String login, final String password, final LogInCallback callback) {
        if (callback == null) {
            return;
        }

        getFormDigest();

        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('User')/items" +
                        "?$filter=user_mail%20eq%20%27" + login + "%27" +
                        "&$select=user_id,user_pass,Title"
                //"https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('User')/items?$filter=user_mail%20eq%20%27box@delcom.ru%27&$select=user_id,user_pass"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lists = response.getJSONObject("d").getJSONArray("results");

                            if (lists.length() == 0) {
                                callback.logInCallback(LogInResult.USER_NOT_FOUND);
                            } else {
                                JSONObject fields = lists.getJSONObject(0);
                                String user_pass = fields.getString("user_pass");

                                if (password.equals(user_pass)) {
                                    userId = fields.getInt("user_id");
                                    userLogin = login;
                                    userPassword = user_pass;
                                    userName = fields.optString("Title");

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
                        callback.logInCallback(LogInResult.NO_CONNECTION);
                    }
                });
    }

    public boolean silentConnect(AuthCallback authCallback) {
        getAADUserId();

        if (!aadUserId.isEmpty()) {
            this.authCallback = authCallback;
            authContext.acquireTokenSilentAsync(RESOURCE, CLIENT_ID, aadUserId, callback);
            return true;
        }

        return false;
    }

    public void interactiveConnect(AuthCallback authCallback) {
        this.authCallback = authCallback;

        authContext.acquireToken(activity, RESOURCE, CLIENT_ID, REDIRECT_URI, "", PromptBehavior.Auto, "", callback);
    }
}
