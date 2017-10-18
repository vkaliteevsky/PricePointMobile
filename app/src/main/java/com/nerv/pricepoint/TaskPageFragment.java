package com.nerv.pricepoint;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.StackView;

import java.util.ArrayList;
import java.util.List;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

/**
 * Created by NERV on 17.10.2017.
 */

public class TaskPageFragment extends CustomFragment implements View.OnClickListener {

    private static class PromoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MainActivity main;

        public void init(MainActivity main) {
            this.main = main;
        }

        public PromoHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

    public static class PromoRecyclerAdapter extends RecyclerView.Adapter<PromoHolder> {
        private MainActivity main;

        public PromoRecyclerAdapter(MainActivity main) {
            this.main = main;
        }

        @Override
        public PromoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.promo_item, parent, false);
            PromoHolder orderHolder = new PromoHolder(view);

            orderHolder.init(main);

            return orderHolder;
        }

        @Override
        public void onBindViewHolder(PromoHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    public static class PhotoStackAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public PhotoStackAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int position) {
            return "";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            View itemView = view;

            if (itemView == null) {
                itemView = inflater.inflate(R.layout.sv_photos_item, parent, false);
            }

            return itemView;
        }
    }

    public static class PhotoItemFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.sv_photos_item, null);
        }
    }

    public static class PhotoItemFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public PhotoItemFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    private class MyPagerAdapter extends PagerAdapter {

        LayoutInflater inflater;

        public MyPagerAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.sv_photos_item,null);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }

    private View view;
    private View commentBtn;
    private RecyclerView promosRV;
    private PromoRecyclerAdapter promoRecyclerAdapter;
    PagerContainer pc;

    private MainActivity main;

    @Override
    public void init(MainActivity main) {
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.task_page_layout, null);

        promosRV = (RecyclerView) view.findViewById(R.id.promosRV);
        promosRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
        promoRecyclerAdapter = new PromoRecyclerAdapter(main);
        promosRV.setAdapter(promoRecyclerAdapter);

        view.findViewById(R.id.promoBtn).setOnClickListener(this);

        commentBtn = view.findViewById(R.id.commentBtn);
        commentBtn.setOnClickListener(this);

        List<Fragment> fs = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            fs.add(new PhotoItemFragment());
        }

        /*photosSV = (FlippableStackView) view.findViewById(R.id.photosSV);
        photosSV.initStack(4, StackPageTransformer.Orientation.HORIZONTAL, 0.53f, 0.46f, 0.46f, StackPageTransformer.Gravity.CENTER);
        photosSV.setAdapter(new PhotoItemFragmentAdapter(main.getSupportFragmentManager(), fs));*/

        pc =  (PagerContainer) view.findViewById(R.id.photosPC);
        pc.setOverlapEnabled(true);
        pc.setLayerType(View.LAYER_TYPE_HARDWARE, null);


        final ViewPager pager = pc.getViewPager();
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(new PhotoItemFragmentAdapter(main.getSupportFragmentManager(), fs));

        new CoverFlow.Builder().with(pager)
                .scale(0.3f)
                .pagerMargin(main.getResources().getDimensionPixelSize(R.dimen.overlapMargin))
                .spaceSize(0)
                .build();


        LinearLayout pricesLayout = (LinearLayout) view.findViewById(R.id.prices);
        LayoutTransition lt = pricesLayout.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);

        /*pager.post(new Runnable() {
            @Override public void run() {
                RelativeLayout v = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                ViewCompat.setElevation(v, 8.0f);
            }
        });*/

        /*pager.post(new Runnable() {
            @Override public void run() {
                RelativeLayout view = (RelativeLayout) pager.getAdapter().instantiateItem(pager, 0);
                ViewCompat.setElevation(view, 8.0f);
            }
        });*/

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        view = null;
        promoRecyclerAdapter = null;
        promosRV = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promoBtn:
                int visibility = promosRV.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;

                promosRV.setVisibility(visibility);
                commentBtn.setVisibility(visibility);
                break;
        }
    }
}
