package com.nerv.pricepoint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private View view;
    private View commentBtn;
    private RecyclerView promosRV;
    private PromoRecyclerAdapter promoRecyclerAdapter;

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
