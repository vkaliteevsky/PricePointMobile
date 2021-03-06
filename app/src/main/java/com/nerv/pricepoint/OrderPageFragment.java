package com.nerv.pricepoint;

/**
 * Created by NERV on 16.10.2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by NERV on 11.10.2017.
 */

interface CategoryControlInterface {
    void setCategory(String category);
}


public class OrderPageFragment extends CustomFragment implements View.OnClickListener, CategoryControlInterface {

    public static class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView icon;
        private TextView description;
        private TextView ean;
        private TextView fstPrice;
        private TextView sndPrice;
        private ProgressBar loadingBar;
        private View comment;
        private View photos;
        private TextView photosCount;
        private ImageButton status;
        private View updateProgressBar;
        public int pos;

        private Task task;
        private MainActivity main;

        public void init(MainActivity main) {
            this.main = main;
        }

        public TaskHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            icon = (ImageView) itemView.findViewById(R.id.goodsIcon);
            description = (TextView) itemView.findViewById(R.id.description);
            ean = (TextView) itemView.findViewById(R.id.ean);
            fstPrice = (TextView) itemView.findViewById(R.id.fstPrice);
            sndPrice = (TextView) itemView.findViewById(R.id.sndPrice);
            loadingBar = (ProgressBar) itemView.findViewById(R.id.loadingBar);
            loadingBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            comment = itemView.findViewById(R.id.comment);
            photos = itemView.findViewById(R.id.photos);
            photosCount = (TextView) itemView.findViewById(R.id.photosCount);
            status = (ImageButton) itemView.findViewById(R.id.status);
            updateProgressBar = itemView.findViewById(R.id.updateProgressBar);

            status.setOnClickListener(this);
        }

        public void setPosition(int pos) {
            this.pos = pos;
        }

        public void setTask(final Task task) {
            this.task = task;

            description.setText(task.description);
            ean.setText(task.ean);
            fstPrice.setText(Utils.formattedPrice(task.costReg));
            sndPrice.setText(Utils.formattedPrice(task.costCard));
            loadingBar.setVisibility(View.GONE);

            comment.setVisibility(task.comment.isEmpty() ? View.INVISIBLE : View.VISIBLE);

            //photos count
            if (task.photosCount > 0) {
                photos.setVisibility(View.VISIBLE);
                photosCount.setText(String.valueOf(task.photosCount));
            } else {
                photos.setVisibility(View.GONE);
            }

            //update status button
            int id;

            if (task.sync) {
                id = R.drawable.check;
            } else if (task.done) {
                id = R.drawable.refresh;
            } else {
                id = R.drawable.error;
            }

            status.setImageResource(id);

            status.invalidate();
            comment.invalidate();
            status.invalidate();

            //load goods icon
            Task.Img img = task.imgs.get(Task.ImgType.ICON);

            icon.setPadding(0, 0, 0, 0);

            if (!img.url.isEmpty()) {

                if (!img.loading) {
                    if (img.path.isEmpty()) {
                        loadIconFromServer(task);
                    } else {
                        File f = new File(img.path);
                        Picasso.with(main).load(f).into(icon);
                    }
                } else {
                    final Task targetTask = task;

                    img.addImgLoadListener(new Task.ImgLoadListener() {
                        @Override
                        public void imageLoaded(Task.Img img) {
                            if (targetTask == task) {
                                File f = new File(img.path);
                                Picasso.with(main).load(f).into(icon);
                                loadingBar.setVisibility(View.GONE);
                            }
                        }
                    });
                    loadingBar.setVisibility(View.VISIBLE);
                }
            } else {
                int pad = Utils.convertDpToPixels(20, main);
                icon.setPadding(pad, pad, pad, pad);
                icon.setImageResource(R.drawable.pp);
            }
        }

        private void loadIconFromServer(final Task targetTask) {
            OkHttpClient okHttpClient = new OkHttpClient().setProtocols(Collections.singletonList(Protocol.HTTP_1_1));;
            okHttpClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + main.getDatabaseManager().authRes.getAccessToken())
                            .build();
                    return chain.proceed(newRequest);
                }
            });

            loadingBar.setVisibility(View.VISIBLE);

            final Task.Img img = task.imgs.get(Task.ImgType.ICON);
            Picasso picasso = new Picasso.Builder(main).downloader(new OkHttpDownloader(okHttpClient)).build();
            img.loading = true;

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (targetTask == task) {
                        icon.setImageBitmap(bitmap);
                    }

                    Utils.saveImage(main, bitmap, img);
                    loadingBar.setVisibility(View.INVISIBLE);
                    PicassoTargetManager.removeTarget(this);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    loadingBar.setVisibility(View.INVISIBLE);
                    icon.setImageResource(R.drawable.pp);
                    PicassoTargetManager.removeTarget(this);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            PicassoTargetManager.addTarget(target);

            picasso.load(img.url).into(target);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() != R.id.status) {
                DatabaseManager dbManager = main.getDatabaseManager();
                dbManager.selectedTask = task;
                dbManager.selectedTaskHolder = this;
                main.getPageController().setPage(PageController.Page.TASK);
            } else {
                if (task.done && !task.sync) {
                    status.setVisibility(View.GONE);
                    updateProgressBar.setVisibility(View.VISIBLE);

                    main.getDatabaseManager().sendData(task, new DatabaseManager.Callback() {
                        @Override
                        public void callback() {
                            if (!task.sync) {
                                Utils.showToast(main, "Не удалось синхронизировать");
                            }

                            status.setImageResource(task.sync ? R.drawable.check : R.drawable.refresh);
                            status.setVisibility(View.VISIBLE);
                            updateProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
    }

    public static class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskHolder> {
        private MainActivity main;
        private List<Task> tasks;

        public TaskRecyclerAdapter(MainActivity main, List<Task> tasks) {
            this.main = main;
            this.tasks = tasks;
        }

        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            TaskHolder orderHolder = new TaskHolder(view);

            orderHolder.init(main);

            return orderHolder;
        }

        @Override
        public void onBindViewHolder(TaskHolder holder, int position) {
            holder.setPosition(position);
            holder.setTask(tasks.get(position)); //holder.setTask(main.getDatabaseManager().selectedOrder.tasks.get(position));
        }

        @Override
        public int getItemCount() {
            return tasks.size(); //main.getDatabaseManager().selectedOrder.tasks.size();
        }
    }

    private static class CategoryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String category;
        private TextView name;
        private CategoryControlInterface categoryControl;

        public CategoryHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(R.id.name);
        }

        public void init(CategoryControlInterface categoryControl) {
            this.categoryControl = categoryControl;
        }

        public void setCategory(String category) {
            this.category = category;
            name.setText(category);
        }

        @Override
        public void onClick(View v) {
            categoryControl.setCategory(category);
        }
    }

    public static class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryHolder> {

        private List<String> categories;
        private CategoryControlInterface categoryControl;

        public CategoryRecyclerAdapter(List<String> categories, CategoryControlInterface categoryControl) {
            this.categories = categories;
            this.categoryControl = categoryControl;
        }

        @Override
        public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
            CategoryHolder holder = new CategoryHolder(view);
            holder.init(categoryControl);

            return holder;
        }

        @Override
        public void onBindViewHolder(CategoryHolder holder, int position) {
            holder.setCategory(categories.get(position));
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }
    }

    private enum SideMenuType {
        NONE, LEFT, RIGHT
    }

    private float SIDE_MENU_WIDTH;

    private View view;

    private MainActivity main;
    private RecyclerView tasksRV;
    private TaskRecyclerAdapter taskRecyclerAdapter;
    private View leftSideMenu;
    private View rightSideMenu;
    private View tasksLayout;
    private View space;
    private TextView categoryName;
    private SideMenuType openedSideMenu = SideMenuType.NONE;
    private Order selectedOrder;
    private EditText searchField;
    private CheckBox allTasksCheckbox;
    private CheckBox inWorkCheckbox;
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
        if (view == null) {
            view = inflater.inflate(R.layout.order_page_layout, null);

            view.findViewById(R.id.leftSideMenuBtn).setOnClickListener(this);
            view.findViewById(R.id.rightSideMenuBtn).setOnClickListener(this);
            view.findViewById(R.id.allCategories).setOnClickListener(this);
            view.findViewById(R.id.searchBtn).setOnClickListener(this);
            view.findViewById(R.id.allTasks).setOnClickListener(this);
            view.findViewById(R.id.inWork).setOnClickListener(this);

            inWorkCheckbox = (CheckBox) view.findViewById(R.id.inWorkCheckbox);
            allTasksCheckbox = (CheckBox) view.findViewById(R.id.allTasksCheckbox);

            searchField = (EditText) view.findViewById(R.id.searchField);

            searchField.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        search();
                        hideKeyboard();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        if (selectedOrder != main.getDatabaseManager().selectedOrder) {
            selectedOrder = main.getDatabaseManager().selectedOrder;

            tasksRV = (RecyclerView) view.findViewById(R.id.tasksRV);
            tasksRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
            taskRecyclerAdapter = new TaskRecyclerAdapter(main, selectedOrder.tasks);
            tasksRV.setAdapter(taskRecyclerAdapter);

            tasksLayout = view.findViewById(R.id.tasksLayout);
            categoryName = (TextView) view.findViewById(R.id.category);
            categoryName.setText("ВСЕ");

            inWorkCheckbox.setChecked(false);
            allTasksCheckbox.setChecked(true);
            searchField.setText("");

            initSideMenus();
        }


        if (main.getDatabaseManager().selectedTaskHolder != null) {
            taskRecyclerAdapter.notifyItemChanged(main.getDatabaseManager().selectedTaskHolder.pos);
        }

        main.getPageController().backPressListener = new PageController.OnBackPressListener() {
            @Override
            public void backPressed() {
                if (openedSideMenu != SideMenuType.NONE) {
                    if (!isSideMenuTranslation) {
                        isSideMenuTranslation = true;

                        switch (openedSideMenu) {
                            case LEFT:
                                Utils.translateSideMenu(leftSideMenu, tasksLayout, -SIDE_MENU_WIDTH, onEndTranslationListener);
                                break;
                            case RIGHT:
                                Utils.translateSideMenu(rightSideMenu, tasksLayout, SIDE_MENU_WIDTH, onEndTranslationListener);
                                break;
                        }

                        space.setVisibility(View.GONE);
                        openedSideMenu = SideMenuType.NONE;
                    }
                } else {
                    if (!isSideMenuTranslation) {
                        main.getPageController().setPage(PageController.Page.ORDERS);
                    }
                }
            }
        };


        return view;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) main.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
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

    private void initSideMenus() {
        Point size = new Point();
        main.getWindowManager().getDefaultDisplay().getSize(size);
        SIDE_MENU_WIDTH = size.x * 0.7f;

        tasksLayout.getLayoutParams().width = size.x;

        leftSideMenu = view.findViewById(R.id.leftSideMenu);
        leftSideMenu.getLayoutParams().width = (int) SIDE_MENU_WIDTH;
        leftSideMenu.setTranslationX(-SIDE_MENU_WIDTH);

        RecyclerView categoryRV = (RecyclerView) leftSideMenu.findViewById(R.id.categoryRV);
        categoryRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));

        CategoryRecyclerAdapter categoryRecyclerAdapter = new CategoryRecyclerAdapter(
                new ArrayList<>(selectedOrder.categories.keySet()), this);
        categoryRV.setAdapter(categoryRecyclerAdapter);

        rightSideMenu = view.findViewById(R.id.rightSideMenu);
        rightSideMenu.getLayoutParams().width = (int) SIDE_MENU_WIDTH;
        rightSideMenu.setTranslationX(size.x);

        space = view.findViewById(R.id.space);
        space.setOnClickListener(this);

        bgAnimation((TransitionDrawable) leftSideMenu.getBackground(), true);
        bgAnimation((TransitionDrawable) rightSideMenu.getBackground(), true);
        bgAnimation((TransitionDrawable) (view.findViewById(R.id.top)).getBackground(), true);
    }

    @Override
    public void setCategory(String category) {
        categoryName.setText(category);

        taskRecyclerAdapter = new TaskRecyclerAdapter(main, main.getDatabaseManager().selectedOrder.categories.get(category));
        tasksRV.setAdapter(taskRecyclerAdapter);

        isSideMenuTranslation = true;
        Utils.translateSideMenu(leftSideMenu, tasksLayout, -SIDE_MENU_WIDTH, onEndTranslationListener);
        space.setVisibility(View.GONE);
        openedSideMenu = SideMenuType.NONE;

        searchField.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void search() {
        hideKeyboard();
        String text = searchField.getText().toString();

        if (!text.isEmpty()) {
            selectedOrder = main.getDatabaseManager().selectedOrder;

            List<Task> res = selectedOrder.search(text);

            tasksRV = (RecyclerView) view.findViewById(R.id.tasksRV);
            tasksRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
            taskRecyclerAdapter = new TaskRecyclerAdapter(main, res);
            tasksRV.setAdapter(taskRecyclerAdapter);

            tasksLayout = view.findViewById(R.id.tasksLayout);
            categoryName = (TextView) view.findViewById(R.id.category);
            categoryName.setText("Результаты поиска");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchBtn:
                search();
                break;
            case R.id.inWork:
                //if (!inWorkCheckbox.isChecked()) {
                    if (!searchField.getText().toString().isEmpty()) {
                        searchField.setText("");
                        categoryName.setText("Все");
                    }

                    allTasksCheckbox.setChecked(false);
                    inWorkCheckbox.setChecked(true);

                    tasksRV = (RecyclerView) view.findViewById(R.id.tasksRV);
                    tasksRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
                    taskRecyclerAdapter = new TaskRecyclerAdapter(main, selectedOrder.getTasksInWork());
                    tasksRV.setAdapter(taskRecyclerAdapter);
                //}
                break;
            case R.id.allTasks:
                //if (!allTasksCheckbox.isChecked()) {
                    if (!searchField.getText().toString().isEmpty()) {
                        searchField.setText("");
                        categoryName.setText("Все");
                    }

                    allTasksCheckbox.setChecked(true);
                    inWorkCheckbox.setChecked(false);

                    tasksRV = (RecyclerView) view.findViewById(R.id.tasksRV);
                    tasksRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
                    taskRecyclerAdapter = new TaskRecyclerAdapter(main, selectedOrder.tasks);
                    tasksRV.setAdapter(taskRecyclerAdapter);
                //}
                break;
            case R.id.leftSideMenuBtn:
                if (!isSideMenuTranslation) {
                    isSideMenuTranslation = true;
                    Utils.translateSideMenu(leftSideMenu, tasksLayout, SIDE_MENU_WIDTH, onEndTranslationListener);
                    openedSideMenu = SideMenuType.LEFT;
                    space.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rightSideMenuBtn:
                if (!isSideMenuTranslation) {
                    isSideMenuTranslation = true;
                    Utils.translateSideMenu(rightSideMenu, tasksLayout, -SIDE_MENU_WIDTH, onEndTranslationListener);
                    openedSideMenu = SideMenuType.RIGHT;
                    space.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.space:
                isSideMenuTranslation = true;
                switch (openedSideMenu) {
                    case LEFT:
                        Utils.translateSideMenu(leftSideMenu, tasksLayout, -SIDE_MENU_WIDTH, onEndTranslationListener);
                        break;
                    case RIGHT:
                        Utils.translateSideMenu(rightSideMenu, tasksLayout, SIDE_MENU_WIDTH, onEndTranslationListener);
                        break;
                }

                space.setVisibility(View.GONE);
                openedSideMenu = SideMenuType.NONE;
                break;
            case R.id.allCategories:
                categoryName.setText("ВСЕ");

                taskRecyclerAdapter = new TaskRecyclerAdapter(main, main.getDatabaseManager().selectedOrder.tasks);
                tasksRV.setAdapter(taskRecyclerAdapter);

                isSideMenuTranslation = true;
                Utils.translateSideMenu(leftSideMenu, tasksLayout, -SIDE_MENU_WIDTH, onEndTranslationListener);
                space.setVisibility(View.GONE);
                openedSideMenu = SideMenuType.NONE;
                break;
        }
    }
}


