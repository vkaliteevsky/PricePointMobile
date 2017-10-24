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

    transient public ArrayList<Task> tasks = new ArrayList<>();
    transient public LinkedList<Task> doneTasks = new LinkedList<>();
    transient public Map<String, ArrayList<Task>> categories = new HashMap<>();
    transient public Map<Integer, Task> taskById = new HashMap<>();
    transient public String orderFile = "";
    transient public String orderDir = "";

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
                    Date newEndDate = Utils.stringToDate(tasks.getJSONObject(i).optString("task_end"));
                    Date newStartDate = Utils.stringToDate(tasks.getJSONObject(i).optString("task_start"));

                    if (order.endDate.compareTo(newEndDate) == -1) {
                        order.endDate = newEndDate;
                    }

                    if (order.startDate.compareTo(newStartDate) == 1) {
                        order.startDate = newStartDate;
                    }
                }

                order.tasks.add(new Task(tasks.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addTask(Task t) {
        if (taskById.containsKey(t.id)) {
            return;
        }

        tasks.add(t);

        if (endDate.compareTo(t.endDate) == -1) {
            endDate.setTime(t.endDate.getTime());
        }

        if (startDate.compareTo(t.startDate) == 1) {
            startDate.setTime(t.startDate.getTime());
        }

        if (categories.containsKey(t.category)) {
            categories.get(t.category).add(t);
        } else {
            categories.put(t.category, new ArrayList<Task>());
            categories.get(t.category).add(t);
        }

        taskById.put(t.id, t);

        String filePath = orderDir + "task" + String.valueOf(t.id) + ".t";
        t.taskFile = filePath;
        Utils.serialize(filePath, t);
    }

    public void sortTasksByCategory() {
        HashMap<String, ArrayList<Task>> res = new HashMap<>();
        doneTasks = new LinkedList<>();
        taskById = new HashMap<>();

        for (Task t : tasks) {
            if (res.containsKey(t.category)) {
                res.get(t.category).add(t);
            } else {
                res.put(t.category, new ArrayList<Task>());
                res.get(t.category).add(t);
            }

            if (t.done) {
                doneTasks.add(t);
            }

            taskById.put(t.id, t);
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
