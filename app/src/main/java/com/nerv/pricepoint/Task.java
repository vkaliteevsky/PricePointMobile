package com.nerv.pricepoint;

import org.json.JSONArray;
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
    public String description = "";

    public int photosCount = 0;

    public boolean edit;
    public boolean noGoods;
    public boolean done;
    public boolean sync;

    public Order order;

    public String dbId;

    public String category;

    public String iconUrl = "";

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
            category = Utils.nullToEmpty(fields.optString("task_class", ""));
            ean = Utils.nullToEmpty(fields.optString("task_ean", ""));
            description = Utils.nullToEmpty(fields.optString("Title", ""));
            photosCount = fields.optInt("task_photo", 0);
            edit = fields.getBoolean("task_edit");
            noGoods = fields.getBoolean("task_no");
            done = fields.getBoolean("task_done");
            sync = fields.getBoolean("task_sync");

            JSONArray imgs = fields.getJSONObject("AttachmentFiles").getJSONArray("results");

            for (int i = 0; i < imgs.length(); i++) {
                JSONObject img = imgs.getJSONObject(i);

                if (img.optString("FileName").startsWith("ICON")) {
                    iconUrl = "https://pointbox.sharepoint.com/_api/web/getfilebyserverrelativeurl('" +
                            img.opt("ServerRelativeUrl") + "')/$value";
                    //iconUrl = "https://pointbox.sharepoint.com" + img.opt("ServerRelativeUrl");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
