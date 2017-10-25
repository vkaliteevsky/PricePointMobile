package com.nerv.pricepoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NERV on 06.10.2017.
 */

interface InputDialogCallback {
    void callback(String text);
}

public class Utils {
    public static String ROOT_DIR;
    public static final int MAX_PHOTO_SIZE = 1024;
    public static int SCREEN_WIDTH = 1;
    public static int SCREEN_HEIGHT = 1;

    private static Toast toast;


    public static void init(MainActivity main) {
        ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PricePoint/";

        Point size = new Point();
        main.getWindowManager().getDefaultDisplay().getSize(size);
        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
    }

    public static int convertDpToPixels(float dp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

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
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public static void sendJSONObject(Context context, final String accessToken
            , final String formDigestValue, final String data, String url
            , Response.Listener<String> responceListener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST, url, responceListener, errorListener) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return data.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json;odata=verbose";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Accept", "application/json;odata=verbose");
                headers.put("X-RequestDigest", formDigestValue);
                headers.put("If-Match", "*");
                headers.put("X-HTTP-Method", "MERGE");
                headers.put("content-type", "application/json;odata=verbose");
                headers.put("content-length", String.valueOf(data.length()));
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public static void sendImage(Context context, final String accessToken
            , final String formDigestValue, final byte[] data, final boolean update, String url
            , Response.Listener<String> responceListener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST, url, responceListener, errorListener) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return data;
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("X-RequestDigest", formDigestValue);

                if (update) {
                    headers.put("X-HTTP-Method", "PUT");
                }

                headers.put("content-length", String.valueOf(data.length));
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                2,
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

                img.setPath(fullPath);
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

    public static void expand(final View v, final int targetWidth) {
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().width = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().width = (int)(targetWidth * interpolatedTime);

                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetWidth / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void translateSideMenu(final View sideMenu, final View mainView, final float targetTranslation) {
        final int initialSideMenuTranslation = (int) sideMenu.getTranslationX();
        final int initialmainViewTranslation = (int) mainView.getTranslationX();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                sideMenu.setTranslationX(initialSideMenuTranslation + (int)(targetTranslation * interpolatedTime));
                mainView.setTranslationX(initialmainViewTranslation + (int)(targetTranslation * interpolatedTime));

                sideMenu.requestLayout();
                mainView.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(Math.abs(targetTranslation) / mainView.getContext().getResources().getDisplayMetrics().density));
        mainView.startAnimation(a);
    }

    public static void inputTextDialog(Context context, int type, String title, String text, final InputDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(type);
        input.setText(text);
        input.setSingleLine(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.callback(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static String formattedPrice(double price) {
        return price == 0 ? "..." : String.valueOf(price).replace(".", ",") + "\u20BD";
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    || (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void serialize(String filePath, Object obj) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (Exception e) {

        }
    }

    public static Object deserialize(String filePath) {
        Object res = null;

        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            res = in.readObject();
            in.close();
            fileIn.close();
        } catch (Exception e) {

        }

        return res;
    }

    public static File[] getFilesList(String path) {
        File directory = new File(path);
        return directory.listFiles();
    }

    public static void showToast(Activity main, String message) {
        if (toast != null) {
            toast.cancel();
        }

        LayoutInflater inflater = main.getLayoutInflater();
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.toast_layout, null);

        int marginBottom = convertDpToPixels(50, main);

        toast = Toast.makeText(main.getApplicationContext(),
                message, Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM, 0, marginBottom);
        TextView messageTextView = (TextView) layout.findViewById(R.id.toast_text);
        messageTextView.setText(message);

        toast.show();
    }
}
