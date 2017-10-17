package com.nerv.pricepoint;

/**
 * Created by NERV on 16.10.2017.
 */

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.util.Currency;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Callback;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by NERV on 11.10.2017.
 */

public class OrderPageFragment extends CustomFragment {

    private static class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView icon;
        private TextView description;
        private TextView ean;
        private TextView fstPrice;
        private TextView sndPrice;
        private ProgressBar loadingBar;

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
        }

        public void setTask(final Task task) {
            this.task = task;

            description.setText(task.description);
            ean.setText(task.ean);
            fstPrice.setText(task.costReg == 0 ? "..." : String.valueOf(task.costReg).replace(".", ",") + "\u20BD");
            sndPrice.setText(task.costCard == 0 ? "..." : String.valueOf(task.costCard).replace(".", ",") + "\u20BD");

            if (task.imgs.containsKey(Task.ImgType.ICON)) {

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

                Picasso picasso = new Picasso.Builder(main).downloader(new OkHttpDownloader(okHttpClient)).build();

                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        icon.setImageBitmap(bitmap);
                        Utils.saveImage(main, bitmap, task.imgs.get(Task.ImgType.ICON));
                        loadingBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        loadingBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };

                picasso.load(task.imgs.get(Task.ImgType.ICON).url).into(target);
            }
        }

        @Override
        public void onClick(View v) {
            main.getPageController().setPage(PageController.Page.TASK);
        }
    }

    public static class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskHolder> {
        private MainActivity main;

        public TaskRecyclerAdapter(MainActivity main) {
            this.main = main;
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
            holder.setTask(main.getDatabaseManager().selectedOrder.tasks.get(position));
        }

        @Override
        public int getItemCount() {
            return main.getDatabaseManager().selectedOrder.tasks.size();
        }
    }

    private View view;

    private MainActivity main;
    private RecyclerView tasksRV;
    private TaskRecyclerAdapter taskRecyclerAdapter;

    @Override
    public void init(MainActivity main) {
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.order_page_layout, null);

        tasksRV = (RecyclerView) view.findViewById(R.id.tasksRV);
        tasksRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
        taskRecyclerAdapter = new TaskRecyclerAdapter(main);
        tasksRV.setAdapter(taskRecyclerAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tasksRV = null;
        taskRecyclerAdapter = null;
    }
}


