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
        CONNECT, LOGIN, ORDERS
    }

    private Page page = Page.CONNECT;
    private Fragment pageFragment;

    private Activity activity;

    private View connectBtn;

    public PageController(Activity activity) {
        this.activity = activity;
        connectBtn = activity.findViewById(R.id.callGraph);
    }

    public void setPage(Page page) {
        if (this.page != page) {
            this.page = page;
        }

        Utils.removeFragment(activity, pageFragment);

        switch (page) {
            case CONNECT:
                connectBtn.setVisibility(View.VISIBLE);
                pageFragment = null;
                break;
            case LOGIN:
                connectBtn.setVisibility(View.GONE);
                pageFragment = FragmentManager.getFragment(FragmentManager.LOGIN_PAGE);
                break;
        }

        Utils.setFragment(activity, R.id.pageContainer, pageFragment);
    }

}
