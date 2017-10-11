package com.nerv.pricepoint;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
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
                headers.put("Accept", "application/json;odata.metadata=none");
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
}
