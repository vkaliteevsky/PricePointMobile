package com.nerv.pricepoint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
            "Title,task_photo,task_id,task_edit,task_no,task_done,task_sync,task_stock,task_eanscan,ID,Modified";

    private final static String STOCK_FIELDS = "?$select=Title,stock_id";
    private final static String STOCK_ITEMS = "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle(%27Stock%27)/items" + STOCK_FIELDS;
    private final static String UPDATE_IMAGE = "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle(%27Task%27)" +
            "/items(@taskId)/AttachmentFiles(%27@fname.jpg%27)/$value";
    private final static String ADD_IMAGE = "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle(%27Task%27)" +
            "/items(@taskId)/AttachmentFiles/add(FileName=%27@fname.jpg%27)";


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

    private LocationManager locationManager;

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
    public SharedPreferences appSettings;

    public Order selectedOrder;
    public Task selectedTask;
    public OrderPageFragment.TaskHolder selectedTaskHolder;
    public Date curDate = new Date();
    public Date dDate = new Date();
    public int D = 0;

    private String formDigestValue = "";

    public DatabaseManager(Activity activity) {
        this.activity = activity;

        //locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        authContext = new AuthenticationContext(activity, AUTHORITY, true);
        appSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        D = appSettings.getInt("D", 0);

        getCurDate();

        /*LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);*/
    }

    /*public void setLocation(final Task task) {
        Location l = getLastKnownLocation();

        if (l != null) {
            task.latitude = String.valueOf(l.getLatitude());
            task.longitude = String.valueOf(l.getLongitude());
        }
    }

    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }

            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }*/

    public void setD(int d) {
        D = d;
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putInt("D", D);
        editor.apply();
    }

    public void getCurDate() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SntpClient client = new SntpClient();

                if (client.requestTime("0.ru.pool.ntp.org", 30000)) {
                    long time = client.getNtpTime();

                    Calendar calendar = Calendar.getInstance();
                    try {
                        calendar.setTimeInMillis(time);
                        curDate = calendar.getTime();
                        calendar.add(Calendar.DATE, 4);
                        dDate = calendar.getTime();
                    } catch (Exception e) {
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(curDate);
                    calendar.add(Calendar.DATE, D);
                    dDate = calendar.getTime();
                }
            }
        });
        t.start();
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

            if (task.costReg != 0) {
                json.put("task_costreg", task.costReg);
            }

            if (task.costCard != 0) {
                json.put("task_costcard", task.costCard);
            }

            if (task.costPromo != 0) {
                json.put("task_costpromo", task.costPromo);
            }

            try {
                json.put("task_commet", task.comment.getBytes("UTF-8"));
            } catch (Exception e) {

            }

            json.put("task_lat", task.latitude);
            json.put("task_lon", task.longitude);
            json.put("task_photo", task.photosCount);
            json.put("task_edit", false);
            json.put("task_no", task.noGoods);
            json.put("task_done", true);
            json.put("task_date", Utils.dateToDBString(curDate));
            String time = Utils.getTime(curDate);
            json.put("task_time", time);

            if (task.promo != -1) {
                json.put("task_stock", task.promo);
            }
            json.put("task_sync", true);
            json.put("task_eanscan", task.eanscan);

            Utils.sendJSONObject(activity
                    , authRes.getAccessToken()
                    , formDigestValue
                    , json.toString()
                    , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle(%27Task%27)/items(" + task.dbId + ")"
                    , new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            task.sync = true;
                            callback.callback();
                        }
                    }
                    , new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            task.sync = false;
                            callback.callback();

                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendImage(0, task);
    }

    private void sendImage(final int imgIndex, final Task task) {
        Task.ImgType type = Task.ImgType.getPhotoType(imgIndex);
        Task.Img img;

        if (type == Task.ImgType.NONE) {
            return;
        } else {
            img = task.imgs.get(type);

            if (!img.changed) {
                sendImage(imgIndex + 1, task);
                return;
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds  = true;
        BitmapFactory.decodeFile(img.path, options);
        options.inSampleSize = Utils.calculateInSampleSize(options, Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(img.path, options);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        Utils.sendImage(activity
                , authRes.getAccessToken()
                , formDigestValue
                , byteArray//Base64.encode(byteArray, Base64.DEFAULT)
                , !img.url.isEmpty()
                , img.url.isEmpty()
                        ? ADD_IMAGE.replace("@taskId", task.dbId).replace("@fname", img.fname())
                        : UPDATE_IMAGE.replace("@taskId", task.dbId).replace("@fname", img.fname())
                , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        sendImage(imgIndex + 1, task);
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("", "");
                    }
                });
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
                            } else {
                                //Utils.serialize(Utils.ROOT_DIR + "promos.p", promos);
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

    private boolean retrieveUserTasksFromCache(final Callback callback) {
        String ordersPath = appSettings.getString("orders" + String.valueOf(userId), "");
        final File[] dirs = Utils.getFilesList(ordersPath);

        if (dirs == null || dirs != null && dirs.length == 0) {
            return false;
        }


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                orders = new ArrayList<>();
                Date today = Utils.dateWithoutTime(curDate);

                for (int i = 0; i < dirs.length; i++) {
                    if (dirs[i].isDirectory()) {
                        String orderPath = dirs[i].getPath();
                        File[] files = Utils.getFilesList(orderPath);
                        ArrayList<Task> tasks = new ArrayList<>();
                        Order order = null;

                        for (int j = 0; j < files.length; j++) {
                            if (files[j].isFile()) {
                                String filePath = files[j].getPath();
                                Object obj = Utils.deserialize(filePath);

                                if (files[j].getName().endsWith(".o")) {
                                    order = (Order) obj;
                                    ordersHM.put(order.orderId, order);
                                } else {
                                    Task t = (Task) obj;

                                    if (t.endDate.compareTo(today) != -1) {
                                        t.taskFile = filePath;
                                        tasks.add(t);
                                    } else {
                                        files[j].delete();
                                    }
                                }
                            }
                        }

                        if (order != null) {
                            order.tasks = tasks;
                            order.sortTasksByCategory();
                            orders.add(order);
                        }
                    }
                }

                retrieveEditTrueTask(callback, false);
            }
        });
        t.start();

        return true;
    }

    private void retrieveEditTrueTask(final Callback callback, final boolean save) {
        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle(%27Task%27)/items" +
                        "?$filter=task_idman%20eq%20" + String.valueOf(userId)
                        + "%20and%20%28task_edit%20eq%201"
                        + "%20or%20%28task_start%20ge%20datetime%27" + Utils.dateToDBString(curDate) + "%27%20" +
                        "and%20task_start%20le%20datetime%27" + Utils.dateToDBString(dDate) + "%27%20and%20task_end%20ge%20datetime%27" + Utils.dateToDBString(curDate) + "%27%29%29"
                        + TASK_FIELDS + "&$expand=AttachmentFiles"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray res = response.getJSONObject("d").getJSONArray("results");

                            for (int i = 0; i < res.length(); i++) {
                                JSONObject fields = res.getJSONObject(i);
                                int orderId = fields.getInt("task_idorder");
                                int taskId = fields.getInt("task_id");
                                boolean edit = fields.getBoolean("task_edit");

                                if (ordersHM.containsKey(orderId)) {
                                    Order o = ordersHM.get(orderId);

                                    if (o.taskById.containsKey(taskId) && edit) {
                                        o.taskById.get(taskId).update(fields);
                                    }

                                    if (!o.taskById.containsKey(taskId)) {
                                        o.addTask(new Task(fields));
                                    }
                                } else {
                                    Order o = new Order(fields);
                                    o.addTask(new Task(fields));
                                    orders.add(o);
                                    ordersHM.put(o.orderId, o);
                                }
                            }

                            if (!save) {
                                Message msg = MainActivity.handler.obtainMessage(MainActivity.DB_CALLBACK, callback);
                                msg.sendToTarget();
                            } else {
                                saveTasks(callback);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String msg = new String(error.networkResponse.data);
                        Log.d("","");
                    }
                });
    }

    private void saveUserTasksToCache() {
        String userDir = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PricePoint/user" + String.valueOf(userId) + "/";
        File createDir = new File(userDir);
        createDir.mkdirs();

        for (Order o : orders) {
            String orderDir = userDir + String.valueOf(o.orderId) + "/";
            createDir = new File(orderDir);
            createDir.mkdirs();

            String orderFilePath = orderDir + "order" + String.valueOf(o.orderId) + ".o";
            o.orderFile = orderFilePath;
            o.orderDir = orderDir;
            Utils.serialize(orderFilePath, o);

            for (Task t: o.tasks) {
                String filePath = orderDir + "task" + String.valueOf(t.id) + ".t";
                t.taskFile = filePath;
                Utils.serialize(filePath, t);
            }
        }

        SharedPreferences.Editor editor = appSettings.edit();
        editor.putString("orders" + String.valueOf(userId), userDir);
        editor.apply();
    }

    private void saveTasks(final Callback callback) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                saveUserTasksToCache();
                Message msg = MainActivity.handler.obtainMessage(MainActivity.DB_CALLBACK, callback);
                msg.sendToTarget();
            }
        });
        t.start();
    }

    public void retrieveUserTasks(final Callback callback) {
        if (userId == -1) {
            return;
        }

        ordersHM = new HashMap<>();

        retrievePromos(STOCK_ITEMS);

        if (retrieveUserTasksFromCache(callback)) {
            return;
        }

        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('Task')/items" +
                        "?$filter=task_idman%20eq%20" + String.valueOf(userId)
                        + "%20and%20task_start%20le%20datetime%27" + Utils.dateToDBString(dDate) + "%27"
                        + "%20and%20task_end%20ge%20datetime%27" + Utils.dateToDBString(curDate) + "%27"
                        + TASK_FIELDS + "&$expand=AttachmentFiles"
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

                                retrieveEditTrueTask(callback, true);
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

                                for (Order o : orders) {
                                    o.sortTasksByCategory();
                                }

                                retrieveEditTrueTask(callback, true);
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

                                    SharedPreferences.Editor editor = appSettings.edit();
                                    editor.putInt("lastUserId", userId);
                                    editor.putString("lastUserName", userName);
                                    editor.putString("lastUserLogin", login);
                                    editor.putString("lastUserPass", userPassword);
                                    editor.apply();

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
