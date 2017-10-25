package com.nerv.pricepoint;

import android.animation.LayoutTransition;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

/**
 * Created by NERV on 17.10.2017.
 */

interface PromoInterface {
    void promoSelected(Promo promo);
}

interface CameraPhotoCallback {
    void photoTaken(String path);
}

public class TaskPageFragment extends CustomFragment implements View.OnClickListener, PromoInterface {


    private static class PromoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MainActivity main;
        private Promo promo;
        private TextView name;
        private CheckBox checkBox;
        private DatabaseManager db;
        private PromoInterface promoInterface;

        public void init(MainActivity main, PromoInterface promoInterface) {
            this.main = main;
            db = main.getDatabaseManager();
            this.promoInterface = promoInterface;
        }

        public PromoHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(R.id.name);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }

        public void setProme(Promo p) {
            promo = p;
            name.setText("«" + p.name + "»");
            boolean selected = db.selectedTask.promo == promo.id;
            checkBox.setChecked(selected);
            name.setTextColor(ContextCompat.getColor(main, selected ? R.color.textColor2 : R.color.textColor1));
        }

        @Override
        public void onClick(View v) {
            promoInterface.promoSelected(promo);
        }
    }

    public static class PromoRecyclerAdapter extends RecyclerView.Adapter<PromoHolder> {
        private MainActivity main;
        private List<Promo> promos;
        private PromoInterface promoInterface;

        public PromoRecyclerAdapter(MainActivity main, PromoInterface promoInterface) {
            this.main = main;
            promos = new ArrayList<>(main.getDatabaseManager().promos.values());
            this.promoInterface = promoInterface;
        }

        @Override
        public PromoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.promo_item, parent, false);
            PromoHolder orderHolder = new PromoHolder(view);

            orderHolder.init(main, promoInterface);

            return orderHolder;
        }

        @Override
        public void onBindViewHolder(PromoHolder holder, int position) {
            holder.setProme(promos.get(position));
        }

        @Override
        public int getItemCount() {
            return promos.size();
        }
    }

    public static class PhotoItemFragment extends Fragment implements View.OnClickListener {

        private String name;
        private Task.Img img;
        private MainActivity main;
        private ProgressBar loadingBar;
        private ImageView photo;

        public void init(String name, Task.Img img,  MainActivity main) {
            this.name = name;
            this.img = img;
            this.main = main;
        }

        public void setImg(Task.Img img) {
            this.img = img;

            loadImage();
        }

        private void loadImage() {
            if (!img.url.isEmpty() || !img.path.isEmpty()) {
                if (img.path.isEmpty()) {
                    loadIconFromServer();
                } else {
                    File f = new File(img.path);

                    if (f.exists()) {
                        Picasso.with(main).load(f)
                                .transform(new BitmapTransform(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE))
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .resize(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE)
                                .centerCrop()
                                .into(photo);
                    } else {
                        photo.setScaleType(ImageView.ScaleType.CENTER);
                        photo.setImageResource(R.drawable.camera);
                    }
                }
            } else {
                photo.setScaleType(ImageView.ScaleType.CENTER);
                photo.setImageResource(R.drawable.camera);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.sv_photos_item, null);
            ((TextView) view.findViewById(R.id.name)).setText(name);
            loadingBar = (ProgressBar) view.findViewById(R.id.progressBar);
            loadingBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            photo = (ImageView) view.findViewById(R.id.photo);
            photo.setOnClickListener(this);

            loadImage();

            return view;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            photo = null;
            loadingBar = null;
            img = null;
        }

        private void loadIconFromServer() {
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

            photo.setImageDrawable(null);

            loadingBar.setVisibility(View.VISIBLE);

            Picasso picasso = new Picasso.Builder(main).downloader(new OkHttpDownloader(okHttpClient)).build();

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if (photo != null) {
                        photo.setImageBitmap(bitmap);
                    }

                    Utils.saveImage(main, bitmap, img);
                    loadingBar.setVisibility(View.INVISIBLE);
                    PicassoTargetManager.removeTarget(this);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    loadingBar.setVisibility(View.INVISIBLE);
                    PicassoTargetManager.removeTarget(this);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            PicassoTargetManager.addTarget(target);

            picasso.load(img.url)
                    .transform(new BitmapTransform(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .resize(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE)
                    .centerCrop()
                    .into(target);
        }

        @Override
        public void onClick(View v) {
            main.getPhotoFromCamera(new CameraPhotoCallback() {
                @Override
                public void photoTaken(String path) {
                    img.path = path;
                    img.changed = true;

                    main.getDatabaseManager().selectedTask.updatePhotosCount();

                    photo.setImageDrawable(null);

                    if (img.type == Task.ImgType.BARCODE) {
                        Target t = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap b, Picasso.LoadedFrom from) {
                                int[] intArray = new int[b.getWidth() * b.getHeight()];
                                b.getPixels(intArray, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
                                LuminanceSource source = new RGBLuminanceSource(b.getWidth(), b.getHeight(), intArray);
                                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                                Reader reader = new MultiFormatReader();
                                try {
                                    Result result = reader.decode(bitmap);
                                    main.getDatabaseManager().selectedTask.eanscan = result.getText();
                                    Utils.showToast(main, "Штрих-код считан");
                                } catch (NotFoundException e) {
                                    Utils.showToast(main, "Не удалось считать штрих-код\nРаспожите его горизонтально");
                                } catch (ChecksumException e) {
                                    Utils.showToast(main, "Не удалось считать штрих-код");
                                } catch (FormatException e) {
                                    Utils.showToast(main, "Не удалось считать штрих-код");
                                }

                                intArray = null;

                                photo.setImageBitmap(b);

                                PicassoTargetManager.removeTarget(this);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                PicassoTargetManager.removeTarget(this);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        };

                        PicassoTargetManager.addTarget(t);

                        File f = new File(path);
                        Picasso.with(main).load(f)
                                .transform(new BitmapTransform(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE))
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .resize(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE)
                                .centerCrop()
                                .into(t);
                    } else {
                        File f = new File(path);
                        Picasso.with(main).load(f)
                                .transform(new BitmapTransform(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE))
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .resize(Utils.MAX_PHOTO_SIZE, Utils.MAX_PHOTO_SIZE)
                                .centerCrop()
                                .into(photo);
                    }
                }
            });
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
    private TextView selectedPromoName;
    private TextView costReg;
    private TextView costCard;
    private TextView costPromo;
    private RecyclerView promosRV;
    private PromoRecyclerAdapter promoRecyclerAdapter;
    private PagerContainer pc;
    private Task task;
    private Task backup;
    private ImageView expandIcon;
    private ImageView goodsIcon;
    private ProgressBar iconProgressBar;
    private List<Fragment> photoItemFragments = new ArrayList<>();

    private MainActivity main;
    public boolean animateBg = true;
    private LinearLayout taskInfo;
    private View space;
    private View saveBtn;

    @Override
    public void init(MainActivity main) {
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        task = main.getDatabaseManager().selectedTask;
        backup = task.clone();

        if (view == null) {
            view = inflater.inflate(R.layout.task_page_layout, null);

            promosRV = (RecyclerView) view.findViewById(R.id.promosRV);
            promosRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
            promoRecyclerAdapter = new PromoRecyclerAdapter(main, this);
            promosRV.setAdapter(promoRecyclerAdapter);

            expandIcon = (ImageView) view.findViewById(R.id.expandIcon);
            goodsIcon = (ImageView) view.findViewById(R.id.goodsIcon);
            iconProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            iconProgressBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

            view.findViewById(R.id.promoBtn).setOnClickListener(this);
            view.findViewById(R.id.noGoodsBtn).setOnClickListener(this);
            view.findViewById(R.id.cancelBtn).setOnClickListener(this);
            view.findViewById(R.id.saveBtn).setOnClickListener(this);
            view.findViewById(R.id.commentBtn).setOnClickListener(this);
            saveBtn = view.findViewById(R.id.saveBtn);

            costReg = (TextView) view.findViewById(R.id.costReg);
            costCard = (TextView) view.findViewById(R.id.costCard);
            costPromo = (TextView) view.findViewById(R.id.costPromo);

            costReg.setOnClickListener(this);
            costCard.setOnClickListener(this);
            costPromo.setOnClickListener(this);

            commentBtn = view.findViewById(R.id.commentBtn);
            commentBtn.setOnClickListener(this);

            selectedPromoName = (TextView) view.findViewById(R.id.selectedPromoName);

            for (int i = 0; i < 4; i++) {
                PhotoItemFragment f = new PhotoItemFragment();
                f.init(PHOTO_NAMES[i], task.imgs.get(Task.ImgType.getPhotoType(i)), main);
                photoItemFragments.add(f);
            }

            pc = (PagerContainer) view.findViewById(R.id.photosPC);
            pc.setOverlapEnabled(true);
            pc.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            final ViewPager pager = pc.getViewPager();
            pager.setOffscreenPageLimit(4);
            pager.setAdapter(new PhotoItemFragmentAdapter(main.getSupportFragmentManager(), photoItemFragments));

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

            taskInfo = (LinearLayout) view.findViewById(R.id.taskInfo);
            space = view.findViewById(R.id.space);

            LinearLayout pricesLayout = (LinearLayout) view.findViewById(R.id.prices);
            LayoutTransition lt = pricesLayout.getLayoutTransition();
            lt.enableTransitionType(LayoutTransition.CHANGING);

            pricesLayout = (LinearLayout) view.findViewById(R.id.taskInfo);
            lt = pricesLayout.getLayoutTransition();
            lt.enableTransitionType(LayoutTransition.CHANGING);
        } else {
            for (int i = 0; i < 4; i++) {
                PhotoItemFragment f = (PhotoItemFragment) photoItemFragments.get(i);

                f.setImg(task.imgs.get(Task.ImgType.getPhotoType(i)));
            }
        }

        ViewTreeObserver vto = taskInfo.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                taskInfo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams params = space.getLayoutParams();
                int h = taskInfo.getHeight() + Utils.convertDpToPixels(20, main) - params.height;

                if (h > Utils.SCREEN_HEIGHT) {
                    params.height = 0;
                } else {
                    int value = Utils.SCREEN_HEIGHT - h;

                    if (value != 0) {
                        params.height = value;
                    }
                }

                space.setLayoutParams(params);
            }
        });

        fillGoodsInfo();

        Task.Img icon = task.imgs.get(Task.ImgType.ICON);

        if (icon.loading) {
            final Task targetTask = task;

            icon.addImgLoadListener(new Task.ImgLoadListener() {
                @Override
                public void imageLoaded(Task.Img img) {
                    if (targetTask == task) {
                        File f = new File(img.path);
                        Picasso.with(main).load(f).into(goodsIcon);
                        iconProgressBar.setVisibility(View.GONE);
                    }
                }
            });
            iconProgressBar.setVisibility(View.VISIBLE);
        } else {
            File f = new File(icon.path);
            Picasso.with(main).load(f).into(goodsIcon);
            iconProgressBar.setVisibility(View.GONE);
        }

        animateBg = true;

        bgAnimation((TransitionDrawable) (view.findViewById(R.id.taskInfo)).getBackground(), true);
        bgAnimation((TransitionDrawable) (view.findViewById(R.id.saveBtn)).getBackground(), true);

        main.getPageController().backPressListener = new PageController.OnBackPressListener() {
            @Override
            public void backPressed() {
                task.set(backup);
                main.getPageController().setPage(PageController.Page.ORDER);
            }
        };

        return view;
    }

    private void bgAnimation(final TransitionDrawable trans, final boolean flag) {
        if (!animateBg) {
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

    @Override
    public void promoSelected(Promo p) {
        selectedPromoName.setText("«" + p.name + "»");
        task.promo = p.id;
        promoRecyclerAdapter.notifyDataSetChanged();
    }

    private void fillGoodsInfo() {
        Task task = main.getDatabaseManager().selectedTask;

        ((AutoResizeTextView) view.findViewById(R.id.description)).setText(task.description);
        ((AutoResizeTextView) view.findViewById(R.id.ean)).setText(task.ean);

        costReg.setText(Utils.formattedPrice(task.costReg));
        costCard.setText(Utils.formattedPrice(task.costCard));
        costPromo.setText(Utils.formattedPrice(task.costPromo));

        if (task.promo != -1) {
            promoSelected(main.getDatabaseManager().promos.get(task.promo));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        animateBg = false;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.promoBtn:
                ViewTreeObserver vto = taskInfo.getViewTreeObserver();
                vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        taskInfo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ViewGroup.LayoutParams params = space.getLayoutParams();
                        int h = taskInfo.getHeight() + Utils.convertDpToPixels(20, main) - params.height;

                        if (h > Utils.SCREEN_HEIGHT) {
                            params.height = 0;
                        } else {
                            int value = Utils.SCREEN_HEIGHT - h;

                            if (value != 0) {
                                params.height = value;
                            }
                        }

                        space.setLayoutParams(params);
                    }
                });

                View promoTypes = view.findViewById(R.id.promoTypes);
                int visibility = promoTypes.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                expandIcon.setImageResource(visibility == View.GONE ? R.drawable.expand : R.drawable.collapse);
                promoTypes.setVisibility(visibility);

                break;
            case R.id.saveBtn:
                DatabaseManager dbManager = main.getDatabaseManager();
                dbManager.selectedTaskHolder.setTask(dbManager.selectedTask);
                task.noGoods = false;
                task.done = true;
                task.edit = false;
                Utils.serialize(task.taskFile, task);
                main.getPageController().setPage(PageController.Page.ORDER);
                break;
            case R.id.noGoodsBtn:
                dbManager = main.getDatabaseManager();
                dbManager.selectedTaskHolder.setTask(dbManager.selectedTask);
                task.noGoods = true;
                task.done = true;
                task.edit = false;
                Utils.serialize(task.taskFile, task);
                main.getPageController().setPage(PageController.Page.ORDER);
                break;
            case R.id.cancelBtn:
                task.set(backup);
                main.getPageController().setPage(PageController.Page.ORDER);
                break;
            case R.id.commentBtn:
                Utils.inputTextDialog(main
                        , InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        , "Комментарий"
                        , task.comment
                        , new InputDialogCallback() {
                            @Override
                            public void callback(String text) {
                                task.comment = text;
                            }
                        });
                break;
            case R.id.costReg:
            case R.id.costCard:
            case R.id.costPromo:
                Utils.inputTextDialog(main
                        , InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        , getTitle(v.getId())
                        , getPriceAsText(v.getId())
                        , new InputDialogCallback() {
                            @Override
                            public void callback(String text) {
                                try {
                                    double price = Double.parseDouble(text);

                                    switch (v.getId()) {
                                        case R.id.costReg:
                                            task.costReg = price;
                                            costReg.setText(Utils.formattedPrice(price));
                                            break;
                                        case R.id.costCard:
                                            task.costCard = price;
                                            costCard.setText(Utils.formattedPrice(price));
                                            break;
                                        case R.id.costPromo:
                                            task.costPromo = price;
                                            costPromo.setText(Utils.formattedPrice(price));
                                            break;
                                    }
                                } catch (NumberFormatException e) {
                                }
                            }
                        });
                break;
        }
    }

    private String getTitle(int id) {
        switch (id) {
            case R.id.costReg:
                return "Цена";
            case R.id.costCard:
                return "Цена по карте";
            case R.id.costPromo:
                return "Цена по акции";
            default:
                return "";
        }
    }

    private String getPriceAsText(int id) {
        double price;

        switch (id) {
            case R.id.costReg:
                price = task.costReg;
                break;
            case R.id.costCard:
                price = task.costCard;
                break;
            case R.id.costPromo:
                price = task.costPromo;
                break;
            default:
                return "";
        }

        return price == 0 ? "" : String.valueOf(price);
    }
}
