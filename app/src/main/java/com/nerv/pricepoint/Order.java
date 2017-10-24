package com.nerv.pricepoint;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by NERV on 11.10.2017.
 */

public class Order implements Serializable{
    public String retail = "";
    public String city = "";
    public String address = "";
    public String mark = "";
    public Date startDate;
    public Date endDate;
    public int orderId;

    public int completedTasks;
    public int toEditTasks;
    public int noGoodsTasks;
    public int photos;

    public ArrayList<Task> tasks = new ArrayList<>();
    public LinkedList<Task> completedOrders = new LinkedList<>();
    public Map<String, ArrayList<Task>> categories;

    public Order(JSONObject fields) {
        startDate = Utils.stringToDate(fields.optString("task_start"));
        endDate = Utils.stringToDate(fields.optString("task_end"));
        retail = fields.optString("task_retail");
        city = fields.optString("task_city");
        address = fields.optString("task_address");
        orderId = fields.optInt("task_idorder", 0);
        mark = Utils.nullToEmpty(fields.optString("task_mark"));
    }

    public static void getOrders(JSONArray tasks, HashMap<Integer, Order> orders) {
        try {
            for (int i = 0; i < tasks.length(); i++) {
                int orderId = tasks.getJSONObject(i).getInt("task_idorder");
                Order order;

                if (!orders.containsKey(orderId)) {
                    order = new Order(tasks.getJSONObject(i));
                    orders.put(orderId, order);
                } else {
                    order = orders.get(orderId);
                }

                order.tasks.add(new Task(tasks.getJSONObject(i), order));

                /*Task t = new Task(tasks.getJSONObject(i), order);

                try {
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    filePath += "/PricePoint/test.t";
                    FileOutputStream fileOut =
                            new FileOutputStream(filePath);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(t);
                    out.close();
                    fileOut.close();

                    FileInputStream fileIn = new FileInputStream(filePath);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    t = (Task) in.readObject();
                    in.close();
                    fileIn.close();
                }catch(IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }*/
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sortTasksByCategory() {
        HashMap<String, ArrayList<Task>> res = new HashMap<>();

        for (Task t : tasks) {
            if (res.containsKey(t.category)) {
                res.get(t.category).add(t);
            } else {
                res.put(t.category, new ArrayList<Task>());
                res.get(t.category).add(t);
            }
        }

        categories = new TreeMap<>(res);
    }

    public void computeOrderInfo() {
        completedTasks = 0;
        toEditTasks = 0;
        noGoodsTasks = 0;
        photos = 0;

        for (Task t : tasks) {
            completedTasks += t.done ? 1 : 0;
            toEditTasks += t.edit ? 1 : 0;
            noGoodsTasks += t.noGoods ? 1 : 0;
            photos += t.photosCount;
        }
    }
}
