package com.nerv.pricepoint;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by NERV on 10.10.2017.
 */

public class PageController {
    public enum Page {
        NONE, LOGIN, ORDERS
    }

    private Page page = Page.NONE;
    private Fragment pageFragment;

    private Activity activity;


    public PageController(Activity activity) {
        this.activity = activity;
    }

    public void setPage(Page page) {
        if (this.page != page) {
            this.page = page;
        }

        Utils.removeFragment(activity, pageFragment);

        switch (page) {
            case LOGIN:
                pageFragment = FragmentManager.getFragment(FragmentManager.LOGIN_PAGE);
                break;
            case ORDERS:
                pageFragment = FragmentManager.getFragment(FragmentManager.ORDERS_PAGE);
                break;
        }

        Utils.setFragment(activity, R.id.pageContainer, pageFragment);
    }

}
