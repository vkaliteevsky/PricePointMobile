package com.nerv.pricepoint;

import com.squareup.picasso.Target;

import java.util.LinkedList;

/**
 * Created by NERV on 21.10.2017.
 */

public class PicassoTargetManager {
    private static LinkedList<Target> targets = new LinkedList<>();

    public static void addTarget(Target t) {
        targets.add(t);
    }

    public static void removeTarget(Target t) {
        targets.remove(t);
    }
}
