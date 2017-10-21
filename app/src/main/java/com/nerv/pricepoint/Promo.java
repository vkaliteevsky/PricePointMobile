package com.nerv.pricepoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Promo {
    public int id;
    public String name;

    public Promo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void getPromos(JSONArray promos, HashMap<Integer, Promo> res) {
        try {
            for (int i = 0; i < promos.length(); i++) {
                JSONObject p = promos.getJSONObject(i);
                Promo promo = new Promo(p.optInt("stock_id"), p.optString("Title"));

                res.put(promo.id, promo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
