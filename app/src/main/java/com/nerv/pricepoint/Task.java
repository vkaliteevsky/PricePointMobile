package com.nerv.pricepoint;

import android.net.Uri;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by NERV on 12.10.2017.
 */

public class Task implements Serializable{
    public enum ImgType {
        NONE(0), ICON(1), PRICE(2), GOODS(3), BARCODE(4), SHELF(5);

        private int i;
        ImgType(int i) {
            this.i = i;
        }
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

        public static ImgType getPhotoType(int v) {
            switch (v) {
                case 0:
                    return PRICE;
                case 1:
                    return GOODS;
                case 2:
                    return BARCODE;
                case 3:
                    return SHELF;
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

    public interface ImgLoadListener {
        void imageLoaded(Img img);
    }


    public class Img implements Serializable{
        public String url = "";
        public String path = "";
        public boolean changed = false;
        public ImgType type;
        public int taskId;
        public boolean loading = false;
        private LinkedList<ImgLoadListener> listeners = new LinkedList<>();

        public Img() {

        }

        public Img(ImgType type, int taskId) {
            this.type = type;
            this.taskId = taskId;
        }

        public Img(String url, ImgType type, int taskId) {
            this.url = url;
            this.type = type;
            this.taskId = taskId;
        }

        public Img clone() {
            Img img = new Img();

            img.url = url;
            img.path = path;
            img.changed = changed;
            img.type = type;
            img.taskId = taskId;

            return img;
        }

        public void set(Img img) {
            url = img.url;

            if (changed) {
                path = img.path;
            }

            changed = img.changed;
            type = img.type;
            taskId = img.taskId;
        }

        public String fname() {
            return type.fname(taskId);
        }

        public void addImgLoadListener(ImgLoadListener l) {
            listeners.add(l);
        }

        public void removeLoadListener(ImgLoadListener l) {
            listeners.remove(l);
        }

        public void callListeners() {
            for (ImgLoadListener l : listeners) {
                l.imageLoaded(this);
            }

            listeners.clear();
        }

        public void setPath(String path) {
            this.path = path;
            loading = false;

            if (!listeners.isEmpty()) {
                Message msg = MainActivity.handler.obtainMessage(MainActivity.IMAGE_LOADED, this);
                msg.sendToTarget();
            }
        }
    }

    public double costReg = 0;
    public double costCard = 0;
    public double costPromo = 0;

    public String comment = "";
    public String latitude = "";
    public String longitude= "";
    public String ean = "";
    public String eanscan = "";
    public String description = "";

    public int photosCount = 0;
    public int id;
    public int promo;

    public boolean edit;
    public boolean noGoods;
    public boolean done;
    public boolean sync;

    public String dbId;
    public String eTag;

    public String category;

    public HashMap<ImgType, Img> imgs = new HashMap<>();

    public Task() {

    }

    public Task(JSONObject fields, Order order) {
        try {
            dbId = fields.getString("ID");
            eTag = fields.getJSONObject("__metadata").getString("etag");
            costReg = fields.optDouble("task_costreg", 0);
            costCard = fields.optDouble("task_costcard", 0);
            costPromo = fields.optDouble("task_costpromo", 0);
            comment = Utils.nullToEmpty(fields.optString("task_commet", ""));
            latitude = Utils.nullToEmpty(fields.optString("task_lat", ""));
            longitude = Utils.nullToEmpty(fields.optString("task_lon", ""));
            category = Utils.nullToEmpty(fields.optString("task_class", ""));
            ean = Utils.nullToEmpty(fields.optString("task_ean", ""));
            description = Utils.nullToEmpty(fields.optString("Title", ""));
            eanscan = Utils.nullToEmpty(fields.optString("task_eanscan", ""));
            photosCount = fields.optInt("task_photo", 0);
            promo = fields.optInt("task_stock", -1);
            id = fields.optInt("task_id", 0);
            edit = fields.getBoolean("task_edit");
            noGoods = fields.getBoolean("task_no");
            done = fields.getBoolean("task_done");
            sync = fields.getBoolean("task_sync");

            JSONArray files = fields.getJSONObject("AttachmentFiles").getJSONArray("results");

            imgs.put(ImgType.ICON, new Img(ImgType.ICON, id));

            for (int i = 0; i < 4; i++) {
                ImgType type = ImgType.getPhotoType(i);
                imgs.put(type, new Img(type, id));
            }

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
                    Img img = imgs.get(type);
                    img.url = imgUrl(file);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updatePhotosCount() {
        photosCount = 0;

        for (Img i : imgs.values()) {
            if (i.type != ImgType.ICON && (!i.url.isEmpty() || !i.path.isEmpty())) {
                photosCount++;
            }
        }
    }

    public Task clone() {
        Task task = new Task();

        task.dbId = dbId;
        task.costReg = costReg;
        task.costCard = costCard;
        task.costPromo = costPromo;
        task.comment = comment;
        task.latitude = latitude;
        task.longitude = longitude;
        task.category = category;
        task.ean = ean;
        task.description = description;
        task.photosCount = photosCount;
        task.promo = promo;
        task.id = id;
        task.edit = edit;
        task.noGoods = noGoods;
        task.done = done;
        task.sync = sync;

        for (ImgType type : imgs.keySet()) {
            task.imgs.put(type, imgs.get(type).clone());
        }

        return task;
    }

    public void set(Task task) {
        dbId = task.dbId;
        costReg = task.costReg;
        costCard = task.costCard;
        costPromo = task.costPromo;
        comment = task.comment;
        latitude = task.latitude;
        longitude = task.longitude;
        category = task.category;
        ean = task.ean;
        description = task.description;
        photosCount = task.photosCount;
        promo = task.promo;
        id = task.id;
        edit = task.edit;
        noGoods = task.noGoods;
        done = task.done;
        sync = task.sync;

        for (ImgType type : imgs.keySet()) {
            imgs.get(type).set(task.imgs.get(type));
        }
    }

    private String imgUrl(JSONObject file) {
        return "https://pointbox.sharepoint.com/_api/web/getfilebyserverrelativeurl('" +
                file.opt("ServerRelativeUrl") + "')/$value";
    }

}
