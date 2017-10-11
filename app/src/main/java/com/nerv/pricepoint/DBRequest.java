package com.nerv.pricepoint;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by NERV on 11.10.2017.
 */

public class DBRequest {
    public interface RequestCallback {
        void onResponse(JSONObject response);
        void onErrorResponse(VolleyError error);
    }

    private final static String BASE_URL = "https://graph.microsoft.com/v1.0/sites/";

    private Context context;
    private String siteId;
    private String accessToken;

    private String listId;
    private boolean getItems;
    private boolean filter;
    private boolean expandFields;
    private boolean select;
    private String filterCondition;
    private String selectFields;
    private int requestMethod;

    private RequestCallback callback;

    public DBRequest(Context context, String siteId, String accessToken) {
        this.context = context;
        this.siteId = siteId;
        this.accessToken = accessToken;
    }

    public void retrieveLists(RequestCallback callback) {
        getItems = false;
        requestMethod = Request.Method.GET;
        this.callback = callback;
    }

    public void retrieveListItems(String listId, RequestCallback callback) {
        this.listId = listId;
        this.callback = callback;
        expandFields = false;
        filter = false;
        getItems = true;
        requestMethod = Request.Method.GET;
    }

    public void expandFields(String fields) {
        expandFields = true;

        if (fields == null || fields.isEmpty()) {
            select = false;
        } else {
            select = true;
            selectFields = fields;
        }
    }

    public void filter(String condition) {
        if (condition == null || condition.isEmpty()) {
            return;
        }

        filter = true;
        filterCondition = condition;
    }

    private String buildUrl() {
        String url = BASE_URL + siteId + "/lists";

        if (getItems) {
            url += "/" + listId + "/items";

            if (filter) {
                url += "?$filter=" + filterCondition;
            }

            if (expandFields) {
                url += (filter ? "&" : "?") + "$expand=fields";

                if (select) {
                    url += "($select=" + selectFields + ")";
                }
            }
        }

        return url;
    }

    public void request() {
        String url = buildUrl();

        Utils.requestJSONObject(context, accessToken, requestMethod
                , url
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onResponse(response);
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onErrorResponse(error);
                    }
                });
    }
}
