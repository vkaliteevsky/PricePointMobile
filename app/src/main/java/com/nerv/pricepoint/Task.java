package com.nerv.pricepoint;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by NERV on 12.10.2017.
 */

public class Task {
    public double costReg = 0;
    public double costCard = 0;
    public double costPromo = 0;

    public String comment = "";
    public String latitude = "";
    public String longitude= "";
    public String ean = "";

    public int photosCount = 0;

    public boolean edit;
    public boolean noGoods;
    public boolean done;
    public boolean sync;

    public Order order;

    public String dbId;

    public Task(JSONObject fields, Order order) {
        try {
            dbId = fields.getString("GUID");
            this.order = order;

            costReg = fields.optDouble("task_costreg", 0);
            costCard = fields.optDouble("task_costcard", 0);
            costPromo = fields.optDouble("task_costpromo", 0);
            comment = Utils.nullToEmpty(fields.optString("task_commet", ""));
            latitude = Utils.nullToEmpty(fields.optString("task_lat", ""));
            longitude = Utils.nullToEmpty(fields.optString("task_lon", ""));
            ean = Utils.nullToEmpty(fields.optString("task_ean", ""));
            photosCount = fields.optInt("task_photo", 0);
            edit = fields.getBoolean("task_edit");
            noGoods = fields.getBoolean("task_no");
            done = fields.getBoolean("task_done");
            sync = fields.getBoolean("task_sync");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
