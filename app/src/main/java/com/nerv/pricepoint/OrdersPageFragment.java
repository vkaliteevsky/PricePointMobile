package com.nerv.pricepoint;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by NERV on 11.10.2017.
 */

public class OrdersPageFragment extends CustomFragment implements View.OnClickListener {

    private static class OrderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        public Order order;

        private MainActivity main;

        public void init(MainActivity main) {
            this.main = main;
        }

        public OrderHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

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
            this.order = order;

            orderNumber.setText(String.valueOf(order.orderId) + " " + order.mark);
            retail.setText(order.retail + ", " + order.city);
            address.setText(order.address);
            startDate.setText(Utils.dateToString(order.startDate));
            endDate.setText(Utils.dateToString(order.endDate));

            tasksCount.setText(String.valueOf(order.tasks.size()));

            order.computeOrderInfo();

            completeTasksCount.setText(String.valueOf(order.completedTasks));
            noGoodsTasksCount.setText(String.valueOf(order.noGoodsTasks));
            toEditTasksCount.setText(String.valueOf(order.toEditTasks));
            fotosCount.setText(String.valueOf(order.photos));
        }

        @Override
        public void onClick(View v) {
            main.getDatabaseManager().selectedOrder = order;
            main.getPageController().setPage(PageController.Page.ORDER);
        }
    }

    public static class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderHolder> {
        private MainActivity main;

        public OrderRecyclerAdapter(MainActivity main) {
            this.main = main;
        }

        @Override
        public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
            OrderHolder orderHolder = new OrderHolder(view);

            orderHolder.init(main);

            return orderHolder;
        }

        @Override
        public void onBindViewHolder(OrderHolder holder, int position) {
            holder.setOrder(main.getDatabaseManager().orders.get(position));
        }

        @Override
        public int getItemCount() {
            return main.getDatabaseManager().orders.size();
        }
    }

    private static final String[] D_TEXT = new String[]{"сегодня", "завтра", "послезавтра"
            , "через 3 дня", "через 4 дня", "через 5 дней", "через 6 дней", "через неделю"};
    private float SIDE_MENU_WIDTH;

    private View view;

    private MainActivity main;
    private RecyclerView ordersRV;
    private OrderRecyclerAdapter orderRecyclerAdapter;
    private View leftSideMenu;
    private View ordersLayout;
    private int screenWidth;
    private View space;
    private View dVariants;
    private TextView curD;
    private ImageView expandIcon;
    private boolean isLeftSideMenuOpen = false;
    private boolean isSideMenuTranslation = false;
    private Callback onEndTranslationListener = new Callback() {
        @Override
        public void callback() {
            isSideMenuTranslation = false;
        }
    };

    @Override
    public void init(MainActivity main) {
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.orders_page_layout, null);

        ordersRV = (RecyclerView) view.findViewById(R.id.ordersRV);
        ordersRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
        orderRecyclerAdapter = new OrderRecyclerAdapter(main);
        ordersRV.setAdapter(orderRecyclerAdapter);

        view.findViewById(R.id.leftSideMenuBtn).setOnClickListener(this);
        view.findViewById(R.id.logOutBtn).setOnClickListener(this);
        ((TextView) view.findViewById(R.id.username)).setText(main.getDatabaseManager().userName);

        Point size = new Point();
        main.getWindowManager().getDefaultDisplay().getSize(size);
        screenWidth = size.x;

        SIDE_MENU_WIDTH = size.x * 0.7f;

        leftSideMenu = view.findViewById(R.id.leftSideMenu);
        leftSideMenu.getLayoutParams().width = (int) SIDE_MENU_WIDTH;
        leftSideMenu.setTranslationX(-SIDE_MENU_WIDTH);

        ordersLayout = view.findViewById(R.id.ordersLayout);
        ordersLayout.getLayoutParams().width = screenWidth;

        space = view.findViewById(R.id.space);
        space.setOnClickListener(this);

        bgAnimation((TransitionDrawable) ordersLayout.getBackground(), true);
        bgAnimation((TransitionDrawable) leftSideMenu.getBackground(), true);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.dVariantsLayout);
        LayoutTransition lt = layout.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);

        layout = (LinearLayout) view.findViewById(R.id.dVariants);
        lt = layout.getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);

        lt = ((LinearLayout) leftSideMenu).getLayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);

        view.findViewById(R.id.expandDVariants1).setOnClickListener(this);
        view.findViewById(R.id.expandDVariants2).setOnClickListener(this);

        view.findViewById(R.id.d0).setOnClickListener(this);
        view.findViewById(R.id.d1).setOnClickListener(this);
        view.findViewById(R.id.d2).setOnClickListener(this);
        view.findViewById(R.id.d3).setOnClickListener(this);
        view.findViewById(R.id.d4).setOnClickListener(this);
        view.findViewById(R.id.d5).setOnClickListener(this);
        view.findViewById(R.id.d6).setOnClickListener(this);
        view.findViewById(R.id.d7).setOnClickListener(this);

        expandIcon = (ImageView) view.findViewById(R.id.expandIcon);
        curD = (TextView) view.findViewById(R.id.curD);
        curD.setText(D_TEXT[main.getDatabaseManager().D]);

        dVariants = view.findViewById(R.id.dVariants);

        main.getPageController().backPressListener = new PageController.OnBackPressListener() {
            @Override
            public void backPressed() {
                if (isLeftSideMenuOpen) {
                    hideLeftSideMenu();
                } else {
                    if (!isSideMenuTranslation) {
                        main.getPageController().setPage(PageController.Page.LOGIN);
                    }
                }
            }
        };

        return view;
    }

    private void bgAnimation(final TransitionDrawable trans, final boolean flag) {
        if (view == null) {
            return;
        }

        Handler hand = new Handler();
        hand.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                change();
            }
            private void change()
            {
                if (flag)
                {
                    trans.startTransition(2000);
                } else
                {
                    trans.reverseTransition(2000);
                }
                bgAnimation(trans, !flag);
            }
        }, 2000);
    }

    private void hideLeftSideMenu() {
        if (!isSideMenuTranslation) {
            isLeftSideMenuOpen = false;
            isSideMenuTranslation = true;
            space.setVisibility(View.GONE);
            Utils.translateSideMenu(leftSideMenu, ordersLayout, -SIDE_MENU_WIDTH, onEndTranslationListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ordersRV = null;
        orderRecyclerAdapter = null;
        leftSideMenu = null;
        space = null;
        view = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftSideMenuBtn:
                if (!isSideMenuTranslation) {
                    isLeftSideMenuOpen = true;
                    isSideMenuTranslation = true;
                    space.setVisibility(View.VISIBLE);
                    Utils.translateSideMenu(leftSideMenu, ordersLayout, SIDE_MENU_WIDTH, onEndTranslationListener);
                }
                break;
            case R.id.space:
                hideLeftSideMenu();
                break;
            case R.id.logOutBtn:
                main.getPageController().setPage(PageController.Page.LOGIN);
                break;
            case R.id.expandDVariants1:
            case R.id.expandDVariants2:
                int visibility = dVariants.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
                dVariants.setVisibility(visibility);
                expandIcon.setImageResource(visibility == View.GONE ? R.drawable.expand : R.drawable.collapse);
                break;
            case R.id.d0:
            case R.id.d1:
            case R.id.d2:
            case R.id.d3:
            case R.id.d4:
            case R.id.d5:
            case R.id.d6:
            case R.id.d7:
                int d = 0;
                switch (v.getId()) {
                    case R.id.d0:
                        d = 0;
                        break;
                    case R.id.d1:
                        d = 1;
                        break;
                    case R.id.d2:
                        d = 2;
                        break;
                    case R.id.d3:
                        d = 3;
                        break;
                    case R.id.d4:
                        d = 4;
                        break;
                    case R.id.d5:
                        d = 5;
                        break;
                    case R.id.d6:
                        d = 6;
                        break;
                    case R.id.d7:
                        d = 7;
                        break;
                }

                main.getDatabaseManager().setD(d);

                curD.setText(D_TEXT[d]);
                dVariants.setVisibility(View.GONE);
                expandIcon.setImageResource(R.drawable.expand);

                break;
        }
    }
}

