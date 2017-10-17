package com.nerv.pricepoint;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by NERV on 12.10.2017.
 */

public class Task {
    public enum ImgType {
        NONE, ICON, PRICE, GOODS, BARCODE, SHELF;

        public static ImgType getType(String v) {
            switch (v) {
                case "A":
                    return PRICE;
                case "B":
                    return GOODS;
                case "C":
                    return BARCODE;
                case "D":
                    return SHELF;
                case "ICON":
                    return ICON;
                default:
                    return NONE;
            }
        }

        public String fname(int taskId) {
            String id = String.valueOf(taskId);

            switch (this) {
                case PRICE:
                    return "A" + id;
                case GOODS:
                    return "B" + id;
                case BARCODE:
                    return "C" + id;
                case SHELF:
                    return "D" + id;
                case ICON:
                    return "ICON";
                default:
                    return "";
            }
        }
    }

    public class Img {
        public String url = "";
        public String path = "";
        public boolean changed = false;
        public ImgType type;
        public int taskId;

        public Img(String url, ImgType type, int taskId) {
            this.url = url;
            this.type = type;
            this.taskId = taskId;
        }

        public String fname() {
            return type.fname(taskId);
        }
    }

    public double costReg = 0;
    public double costCard = 0;
    public double costPromo = 0;

    public String comment = "";
    public String latitude = "";
    public String longitude= "";
    public String ean = "";
    public String description = "";

    public int photosCount = 0;
    public int id;

    public boolean edit;
    public boolean noGoods;
    public boolean done;
    public boolean sync;

    public Order order;

    public String dbId;

    public String category;

    public HashMap<ImgType, Img> imgs = new HashMap<>();

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
            id = fields.optInt("task_id", 0);
            edit = fields.getBoolean("task_edit");
            noGoods = fields.getBoolean("task_no");
            done = fields.getBoolean("task_done");
            sync = fields.getBoolean("task_sync");

            JSONArray files = fields.getJSONObject("AttachmentFiles").getJSONArray("results");

            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.getJSONObject(i);
                final String fileName = file.optString("FileName");
                ImgType type;

                if (fileName.startsWith("ICON")) {
                    type = ImgType.ICON;
                } else {
                    type = ImgType.getType(String.valueOf(fileName.charAt(0)));
                }

                if (type != ImgType.NONE) {
                    imgs.put(type, new Img(imgUrl(file), type, id));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String imgUrl(JSONObject file) {
        return "https://pointbox.sharepoint.com/_api/web/getfilebyserverrelativeurl('" +
                file.opt("ServerRelativeUrl") + "')/$value";
    }

}
