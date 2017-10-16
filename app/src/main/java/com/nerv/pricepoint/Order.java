package com.nerv.pricepoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by NERV on 11.10.2017.
 */

public class Order {
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

    public Order(JSONObject fields) {
        startDate = Utils.stringToDate(fields.optString("task_start"));
        endDate = Utils.stringToDate(fields.optString("task_end"));
        retail = fields.optString("task_retail");
        city = fields.optString("task_city");
        address = fields.optString("task_address");
        orderId = fields.optInt("task_idorder", 0);
        mark = Utils.nullToEmpty(fields.optString("task_mark"));
    }

    public static ArrayList<Order> getOrders(JSONArray tasks) {
        HashMap<Integer, Order> orders = new HashMap<>();
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return new ArrayList<>(orders.values());
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
