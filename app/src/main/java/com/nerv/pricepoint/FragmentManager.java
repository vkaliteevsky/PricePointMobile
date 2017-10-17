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
    public static int ORDER_PAGE = 2;
    public static int TASK_PAGE = 3;

    private static Map<Integer, Fragment> fragments = new HashMap<>();
    private static MainActivity main;

    public static void init(MainActivity mainActivity) {
        main = mainActivity;
        addFragment(LOGIN_PAGE, new LoginPageFragment());
        addFragment(ORDERS_PAGE, new OrdersPageFragment());
        addFragment(ORDER_PAGE, new OrderPageFragment());
        addFragment(TASK_PAGE, new TaskPageFragment());
    }

    private static void addFragment(int id, CustomFragment fragment) {
        fragment.init(main);
        fragments.put(id, fragment);
    }

    public static Fragment getFragment(Integer name) {
        return fragments.get(name);
    }
}
