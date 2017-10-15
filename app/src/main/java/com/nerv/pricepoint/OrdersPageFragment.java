package com.nerv.pricepoint;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by NERV on 11.10.2017.
 */

public class OrdersPageFragment extends CustomFragment {

    private static class OrderHolder extends RecyclerView.ViewHolder {
        private TextView orderNumber;
        private TextView retail;
        private TextView address;
        private TextView startDate;
        private TextView endDate;
        private TextView tasksCount;
        private TextView completeTasksCount;
        private TextView noGoodsTasksCount;
        private TextView toEditTasksCount;
        private TextView fotosCount;

        public OrderHolder(View itemView) {
            super(itemView);

            orderNumber = (TextView) itemView.findViewById(R.id.orderNumber);
            retail = (TextView) itemView.findViewById(R.id.retail);
            address = (TextView) itemView.findViewById(R.id.address);
            startDate = (TextView) itemView.findViewById(R.id.startDate);
            endDate = (TextView) itemView.findViewById(R.id.endDate);

            tasksCount = (TextView) itemView.findViewById(R.id.tasksCount);
            completeTasksCount = (TextView) itemView.findViewById(R.id.completeTasksCount);
            noGoodsTasksCount = (TextView) itemView.findViewById(R.id.noGoodsTasksCount);
            toEditTasksCount = (TextView) itemView.findViewById(R.id.toEditTasksCount);
            fotosCount = (TextView) itemView.findViewById(R.id.fotosCount);
        }

        public void setOrder(Order order) {

        }
    }

    public static class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderHolder> {
        public ArrayList<Order> orders;

        public OrderRecyclerAdapter(ArrayList<Order> orders) {
            this.orders = orders;
        }

        @Override
        public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);

            return new OrderHolder(view);
        }

        @Override
        public void onBindViewHolder(OrderHolder holder, int position) {
            holder.setOrder(orders.get(position));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
    }

    private View view;

    private Context context;
    private DatabaseManager databaseManager;
    private RecyclerView ordersRV;
    private OrderRecyclerAdapter orderRecyclerAdapter;

    @Override
    public void init(MainActivity main) {
        context = main;
        databaseManager = main.getDatabaseManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.orders_page_layout, null);

        ordersRV = (RecyclerView) view.findViewById(R.id.ordersRV);
        ordersRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        orderRecyclerAdapter = new OrderRecyclerAdapter(databaseManager.orders);
        ordersRV.setAdapter(orderRecyclerAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ordersRV = null;
        orderRecyclerAdapter = null;
    }
}

