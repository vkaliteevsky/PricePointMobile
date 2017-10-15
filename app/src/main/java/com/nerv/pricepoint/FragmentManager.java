package com.nerv.pricepoint;

import android.app.Fragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by NERV on 10.10.2017.
 */

public class FragmentManager {
    public static int LOGIN_PAGE = 0;
    public static int ORDERS_PAGE = 1;

    private static Map<Integer, Fragment> fragments = new HashMap<>();
    private static MainActivity main;

    public static void init(MainActivity mainActivity) {
        main = mainActivity;
        addFragment(LOGIN_PAGE, new LoginPageFragment());
        addFragment(ORDERS_PAGE, new OrdersPageFragment());
    }

    private static void addFragment(int id, CustomFragment fragment) {
        fragment.init(main);
        fragments.put(id, fragment);
    }

    public static Fragment getFragment(Integer name) {
        return fragments.get(name);
    }
}
