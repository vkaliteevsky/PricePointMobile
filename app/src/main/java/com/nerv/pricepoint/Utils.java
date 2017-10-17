package com.nerv.pricepoint;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NERV on 06.10.2017.
 */

public class Utils {
    public static void requestJSONObject(Context context, final String accessToken, int method, String url
            , Response.Listener<JSONObject> responceListener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(method, url, null, responceListener, errorListener)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Accept", "application/json;odata=verbose");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public static void requestJSONObject1(Context context, final String accessToken, int method, String url
            , Response.Listener<JSONObject> responceListener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(method, url, null, responceListener, errorListener)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public static void removeFragment(Activity activity, Fragment fragment) {
        if (fragment == null) {
            return;
        }

        FragmentTransaction fragT = activity.getFragmentManager().beginTransaction();
        fragT.remove(fragment);
        fragT.commit();
    }

    public static void setFragment(Activity activity, int containerId, Fragment fragment) {
        if (fragment == null) {
            return;
        }

        FragmentTransaction fragT = activity.getFragmentManager().beginTransaction();
        fragT.replace(containerId, fragment);
        fragT.commit();
    }

    public static Date stringToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = null;

        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        return date;
    }

    public static String dateToDBString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        return dateFormat.format(date);
    }

    public static String intWithSpaces(int v) {
        String strV = String.valueOf(v);
        int width = 5 - strV.length();

        return String.format("%1$" + String.valueOf(width) + "s", v);
    }

    public static String dateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        return dateFormat.format(date);
    }

    public static String nullToEmpty(String str) {
        if (str.equals("null")) {
            return "";
        } else {
            return str;
        }
    }

    private static class SaveTaskImageRunnable implements  Runnable {
        private Bitmap imgToSave;
        private Context context;
        private Task.Img img;

        public SaveTaskImageRunnable(Context context, Bitmap imgToSave, Task.Img img) {
            this.imgToSave = imgToSave;
            this.img = img;
            this.context = context;
        }
        @Override
        public void run() {
            String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
            folder += "/PricePoint/" + String.valueOf(img.taskId) + "/";

            File createDir = new File(folder);
            createDir.mkdirs();

            String fname = img.fname();
            String fullPath = folder + fname + ".jpg";

            /*ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fname);
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.ImageColumns.DATA, fullPath);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);*/

            try {
                //OutputStream outStream = context.getContentResolver().openOutputStream(uri);
                File file = new File(fullPath);
                FileOutputStream outStream = new FileOutputStream(file);

                imgToSave.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
                outStream.flush();
                outStream.close();

                img.path = fullPath;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImage(Context context, Bitmap imgToSave, Task.Img img) {
        SaveTaskImageRunnable r = new SaveTaskImageRunnable(context, imgToSave, img);
        Thread t = new Thread(r);
        t.start();
    }
}
