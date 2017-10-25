package com.nerv.pricepoint;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

/**
 * Created by NERV on 10.10.2017.
 */

public class LoginPageFragment extends CustomFragment implements View.OnClickListener {
    private View view;
    private EditText loginET;
    private EditText passwordET;
    private DatabaseManager databaseManager;
    private PageController pageController;
    private MainActivity main;
    private boolean transitionFlag = true;
    private ProgressBar progressBar;
    private Button logInBtn;

    @Override
    public void init(MainActivity main) {
        this.main = main;
        pageController = main.getPageController();
        databaseManager = main.getDatabaseManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login_page_layout, null);

        bgAnimation();

        logInBtn = (Button)  view.findViewById(R.id.logInBtn);
        logInBtn.setOnClickListener(this);
        loginET = (EditText) view.findViewById(R.id.loginET);
        passwordET = (EditText) view.findViewById(R.id.passwordET);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        progressBar.setVisibility(View.GONE);

        SharedPreferences sets = databaseManager.appSettings;

        if (sets.contains("lastUserId")) {
            loginET.setText(sets.getString("lastUserLogin", ""));
            passwordET.setText(sets.getString("lastUserPass", ""));
        }

        pageController.backPressListener = null;

        return view;
    }

    private void bgAnimation() {
        if (view == null) {
            return;
        }


        final TransitionDrawable trans = (TransitionDrawable) view.getBackground();

        Handler hand = new Handler();
        hand.postDelayed(new Runnable()
        {
            private boolean flag = true;

            @Override
            public void run()
            {
                change();
            }
            private void change()
            {
                if (transitionFlag)
                {
                    trans.startTransition(2000);
                    transitionFlag = false;
                } else
                {
                    trans.reverseTransition(2000);
                    transitionFlag = true;
                }
                bgAnimation();
            }
        }, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        view = null;
        loginET = null;
        passwordET = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logInBtn) {
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();

            if (login.isEmpty()) {
                Utils.showToast(main, "Введите логин");
                return;
            }

            if (password.isEmpty()) {
                Utils.showToast(main, "Введите пароль");
                return;
            }

            loginET.setEnabled(false);
            passwordET.setEnabled(false);
            logInBtn.setEnabled(false);
            logInBtn.setText("");
            progressBar.setVisibility(View.VISIBLE);


            databaseManager.checkLoginPassword(login/*"2222@mail.ru"*/, password/*"123456"*/, new DatabaseManager.LogInCallback() {
                @Override
                public void logInCallback(DatabaseManager.LogInResult result) {
                    switch (result) {
                        case OK:
                            databaseManager.retrieveUserTasks(new DatabaseManager.Callback() {
                                @Override
                                public void callback() {
                                    pageController.setPage(PageController.Page.ORDERS);
                                }
                            });
                            return;
                        case USER_NOT_FOUND:
                            Utils.showToast(main, "Пользователь не найден");
                            break;
                        case WRONG_PASSWORD:
                            Utils.showToast(main, "Неверный пароль");
                            break;
                        case NO_CONNECTION:
                            Utils.showToast(main, "Нет подключения к интернету");
                            break;
                    }

                    loginET.setEnabled(true);
                    passwordET.setEnabled(true);
                    logInBtn.setEnabled(true);
                    logInBtn.setText("Войти");
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}
