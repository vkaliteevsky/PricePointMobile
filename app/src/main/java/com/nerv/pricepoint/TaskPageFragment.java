package com.nerv.pricepoint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Point;
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
import android.widget.TextView;

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

        private String name;
        private Task.ImgType type;

        public void init(String name, Task.ImgType type) {
            this.name = name;
            this.type = type;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.sv_photos_item, null);
            ((TextView) view.findViewById(R.id.name)).setText(name);

            return view;
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

    private static final String[] PHOTO_NAMES = new String[]{"Ценник", "Товар", "Штрихкод", "Полка"};

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
            PhotoItemFragment f = new PhotoItemFragment();

            f.init(PHOTO_NAMES[i], Task.ImgType.getPhotoType(i));
            fs.add(f);
        }



        pc = (PagerContainer) view.findViewById(R.id.photosPC);
        pc.setOverlapEnabled(true);
        pc.setLayerType(View.LAYER_TYPE_HARDWARE, null);


        final ViewPager pager = pc.getViewPager();
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(new PhotoItemFragmentAdapter(main.getSupportFragmentManager(), fs));

        ViewGroup.LayoutParams params = pager.getLayoutParams();
        Point size = new Point();
        main.getWindowManager().getDefaultDisplay().getSize(size);
        params.width = (int) (size.x * 0.8);
        params.height = (int) (0.6 * params.width);
        pager.setLayoutParams(params);

        params = pc.getLayoutParams();
        params.height = (int) (size.x * 0.8 * 0.6 * 1.1);
        pc.setLayoutParams(params);

        new CoverFlow.Builder().with(pager)
                .scale(0.3f)
                .pagerMargin(main.getResources().getDimensionPixelSize(R.dimen.overlapMargin))
                .spaceSize(0)
                .build();


        LinearLayout pricesLayout = (LinearLayout) view.findViewById(R.id.prices);
        LayoutTransition lt = pricesLayout.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);

        fillGoodsInfo();

        return view;
    }

    private void fillGoodsInfo() {
        Task task = main.getDatabaseManager().selectedTask;

        ((AutoResizeTextView) view.findViewById(R.id.description)).setText(task.description);
        ((AutoResizeTextView) view.findViewById(R.id.ean)).setText(task.ean);
        ((TextView) view.findViewById(R.id.costReg)).setText(task.costReg != 0
                ? String.valueOf(task.costReg).replace(".", ",") + "\u20BD" : "...");
        ((TextView) view.findViewById(R.id.costCard)).setText(task.costCard != 0
                ? String.valueOf(task.costCard).replace(".", ",") + "\u20BD" : "...");
        ((TextView) view.findViewById(R.id.costPromo)).setText(task.costPromo != 0
                ? String.valueOf(task.costPromo).replace(".", ",") + "\u20BD" : "...");
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
                View promoTypes = view.findViewById(R.id.promoTypes);
                int visibility = promoTypes.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                promoTypes.setVisibility(visibility);
                break;
        }
    }
}
