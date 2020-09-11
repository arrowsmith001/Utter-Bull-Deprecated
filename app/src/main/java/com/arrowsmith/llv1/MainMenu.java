package com.arrowsmith.llv1;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavAction;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.Navigator;

import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.arrowsmith.llv1.classes.LoadingDots;
import com.arrowsmith.llv1.dialogs.*;

import static androidx.navigation.NavOptions.*;
import static com.arrowsmith.llv1.MainActivity.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainMenu extends Fragment
 implements MainMenuInteraction {

private View view;
private MainActivity main;

private TextView textCreate, textJoin, btnCreateGame, btnJoinGame;;
private ImageView imgCreate,imgJoin, imgTitle, imgUtter, imgBull, imgBubble, imgBullPic;
private CoordinatorLayout titleFrame;
private EditText editName;
    private Button btnTutorial;
    private Button btnInfo;
    private CheckBox checkBoxAudio;
    private ConstraintLayout layoutNotif;
    private TextView textNotif;
    private LoadingDots notifDots;
    private View layoutBottom;
    private TextView textPerma;
    private ImageView imgSpinyBig;
    private ImageView imgSpinySml;
    private ConstraintLayout layoutTop;
    private ConstraintLayout createGameLayout;
    private ConstraintLayout joinGameLayout;

    boolean showButtonsNow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.fragment_main_menu, container, false);
        main = (MainActivity) getActivity();

        if(getArguments() != null)
        {
            showButtonsNow = MainMenuArgs.fromBundle(getArguments()).getShowButtonsNow();
            Log.i(TAG, "onCreateView: got arg: "+showButtonsNow);
        }
        else showButtonsNow = false;

                /*FragmentTransaction ftr = getFragmentManager().beginTransaction();
        ftr.detach(MainMenu.this).attach(MainMenu.this).commit();*/

        //handleAdSpace();

        // Reset variables
        main.me = null;
        main.myCode = null;
        main.room = null;
        main.playersList = null;

        initialiseViews();

        // Sound file
        //MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.);

        // Set status text to empty String, by default
        textNotif.setText("");

        setButtons();

        updateUsableScreenSize();

        return view;
    }

    private void setButtons() {

        // Button on click listeners set to launch appropriate dialog fragments
        btnCreateGame.setOnClickListener(v ->{

            main.playSound("boop");

            int colorFrom = getResources().getColor(R.color.yellow);
            int colorTo = getResources().getColor(R.color.white);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    textCreate.setTextColor((int) animator.getAnimatedValue());
                    imgCreate.setColorFilter((int) animator.getAnimatedValue(),android.graphics.PorterDuff.Mode.SRC_IN);
                }

            });
            colorAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    imgCreate.setColorFilter(null);
                }
            });
            colorAnimation.start();

            main.launchDialog("create_game_fragment");
        });
        btnJoinGame.setOnClickListener(v ->{

            main.playSound("boop");

            int colorFrom = getResources().getColor(R.color.yellow);
            int colorTo = getResources().getColor(R.color.white);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    textJoin.setTextColor((int) animator.getAnimatedValue());
                    imgJoin.setColorFilter((int) animator.getAnimatedValue(),android.graphics.PorterDuff.Mode.SRC_IN);
                }
            });
            colorAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    imgJoin.setColorFilter(null);
                }
            });
            colorAnimation.start();

            main.launchDialog("join_game_fragment");
        });
        btnTutorial.setOnClickListener(v -> {
            moveToTutorial();
        });
        btnInfo.setOnClickListener(v -> {
            main.launchDialog("info_fragment");
        });
        checkBoxAudio.setChecked(main.prefs.getBoolean("soundOn", true));
        if(checkBoxAudio.isChecked())
        {
            ImageView img = view.findViewById(R.id.imageAudio);
            img.setImageResource(R.drawable.ic_sound_on);
        }else
        {
            ImageView img = view.findViewById(R.id.imageAudio);
            img.setImageResource(R.drawable.ic_sound_off);
        }
        checkBoxAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = main.getSharedPreferences("PREFS",0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("soundOn", isChecked);
                editor.apply();
                if(isChecked)
                {
                    ImageView img = view.findViewById(R.id.imageAudio);
                    img.setImageResource(R.drawable.ic_sound_on);
                }else
                {
                    ImageView img = view.findViewById(R.id.imageAudio);
                    img.setImageResource(R.drawable.ic_sound_off);
                }
            }
        });

    }

    private void showNotif(){

        textPerma.setText("");

        TranslateAnimation oa = new TranslateAnimation(0,0,0,0,
                TranslateAnimation.RELATIVE_TO_SELF,0,
                TranslateAnimation.RELATIVE_TO_SELF,2);
        oa.setDuration(500);
        oa.setInterpolator(new OvershootInterpolator(2));

        ObjectAnimator alpha = new ObjectAnimator();
        alpha.setDuration(500);
        alpha.setFloatValues(1, 0);
        alpha.setInterpolator(new AnticipateInterpolator());
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                layoutBottom.setAlpha(t);
            }
        });
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layoutBottom.setAlpha(0);
            }
        });

        alpha.start();
        layoutBottom.startAnimation(oa);
        layoutNotif.setVisibility(View.VISIBLE);
    }

    private void hideNotif(){

        TranslateAnimation oa = new TranslateAnimation(0,0,0,0,
                TranslateAnimation.RELATIVE_TO_SELF,2,
                TranslateAnimation.RELATIVE_TO_SELF,0);
        oa.setDuration(500);
        oa.setInterpolator(new OvershootInterpolator(2));

        ObjectAnimator alpha = new ObjectAnimator();
        alpha.setDuration(500);
        alpha.setFloatValues(0, 1);
        alpha.setInterpolator(new AnticipateInterpolator());
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                layoutBottom.setAlpha(t);
            }
        });
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                layoutBottom.setAlpha(1);
            }
        });

        alpha.start();
        layoutBottom.startAnimation(oa);
        layoutNotif.setVisibility(View.GONE);
    }

    private void initialiseViews() {

        // Initialise views
        imgUtter = view.findViewById(R.id.img_utter_0);
        imgBull = view.findViewById(R.id.img_bull_0);
        imgBubble = view.findViewById(R.id.img_bubble_0);
        imgBullPic = view.findViewById(R.id.img_bull_pic_0);
        imgSpinyBig = (ImageView) view.findViewById(R.id.img_spiny_long);
        imgSpinySml= (ImageView) view.findViewById(R.id.img_spiny_short);
        titleFrame = (CoordinatorLayout) view.findViewById(R.id.titleFrame_0);

        layoutBottom = view.findViewById(R.id.layoutMainMenuBottom);
        layoutTop = (ConstraintLayout) view.findViewById(R.id.layoutMainMenuTop);
        createGameLayout = (ConstraintLayout) view.findViewById(R.id.createGameLayout);
        joinGameLayout = (ConstraintLayout) view.findViewById(R.id.joinGameLayout);

        btnCreateGame = view.findViewById(R.id.button_create_clicker_0);
        btnJoinGame = view.findViewById(R.id.button_join_clicker_0);
        btnTutorial = (Button) view.findViewById(R.id.button_tutorial);
        btnInfo = (Button) view.findViewById(R.id.button_info_0);
        checkBoxAudio = (CheckBox) view.findViewById(R.id.button_audio_0);

        textCreate = view.findViewById(R.id.button_create_game_0);
        textJoin = view.findViewById(R.id.button_join_game_0);
        imgCreate = view.findViewById(R.id.image_create_0);
        imgJoin = view.findViewById(R.id.image_join_0);

        layoutNotif = (ConstraintLayout) view.findViewById(R.id.layoutNotif);
        textNotif = (TextView) view.findViewById(R.id.textNotifContent);
        notifDots = (LoadingDots) view.findViewById(R.id.dotsNotif);
        textPerma = (TextView) view.findViewById(R.id.textPermaNotif);

        view.findViewById(R.id.button_buy_ad_free_version_0).setVisibility(View.GONE);

        LayoutTransition lt = new LayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        lt.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        lt.enableTransitionType(LayoutTransition.CHANGING);

        layoutNotif.setLayoutTransition(lt);

        // Hide title frame views for now
        imgUtter.setVisibility(View.INVISIBLE);
        imgBubble.setVisibility(View.INVISIBLE);
        imgBull.setVisibility(View.INVISIBLE);
        imgBullPic.setVisibility(View.INVISIBLE);
        imgSpinyBig.setVisibility(View.INVISIBLE);
        imgSpinySml.setVisibility(View.INVISIBLE);

        layoutTop.setVisibility(View.INVISIBLE);
        createGameLayout.setVisibility(View.INVISIBLE);
        joinGameLayout.setVisibility(View.INVISIBLE);
        btnTutorial.setVisibility(View.INVISIBLE);

        // DEBUG ONLY
        /*b = true;
        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(v ->
        {
            if(b) showNotif();
            else hideNotif();

            b = !b;
        });*/
        // DEBUG ONLY

    }
    // DEBUG ONLY
    boolean b;
    // DEBUG ONLY

    /**
     * Runs thread that handles title animation
     */

    public final void updateUsableScreenSize() {
        final View vContent = view.findViewById(R.id.mainMenuRoot);
        vContent.post(new Runnable() {
            @Override
            public void run() {

                v = vContent.getHeight();
                h = vContent.getWidth();

                vContent.post(new Runnable() {
                    @Override
                    public void run() {
                        titleAnim();
                    }
                });
            }
        });
    }

    int v, h;

    private void titleAnim() {

        // imgUtter, imgBubble, imgBull, imgBullPic;

        if (showButtonsNow) showButtons();

        ObjectAnimator imgUtterAnim, imgBubbleAnim, imgBullAnim, imgBullPicAnim, spiny1, spiny2;

        int dur1, dur2, dur3, dur4, dur5, dur6;
        int del1, del2, del3, del4, del5, del6;
        float interp1, interp2, interp3, interp4,interp5,interp6;

        dur1 = 750;
        dur2 = 600;
        dur3 = 750;
        dur4 = 600;
        dur5 = 1000;
        dur6 = 1000;

        del1 = 200;
        del2 = del1 + 300;
        del3 = del2 + 200;
        del4 = del3 + 300;
        del5 = dur1 + dur2 + dur3 + dur4 - del1 - del2 - del3 - del4;
        del6 = del5 + 200;

        interp1 = 0.3f;
        interp2 = 0.3f;
        interp3 = 0.3f;
        interp4 = 0.3f;
        interp5 = 1f;
        interp6 = 1f;

        float imgUtterYInit = imgUtter.getY();

        imgUtterAnim = new ObjectAnimator();
        imgUtterAnim.setDuration(dur1);
        imgUtterAnim.setStartDelay(del1);
        imgUtterAnim.setFloatValues(imgUtter.getX() + h, imgUtter.getX());
        imgUtterAnim.setInterpolator(new CustomSpringInterpolator(interp1));
        imgUtterAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                imgUtter.setX(t);

                //float yIncr = (float) ( ((double) (t - imgUtter.getX())) * Math.tan(Math.toRadians(10)));
                //imgUtter.setY(imgUtterYInit + yIncr);
                //Log.i(TAG, "onAnimationUpdate: yIncr: "+yIncr);
            }
        });

        imgBubbleAnim = new ObjectAnimator();
        imgBubbleAnim.setDuration(dur2);
        imgBubbleAnim.setStartDelay(del2);
        imgBubbleAnim.setFloatValues(0, 1);
        imgBubbleAnim.setInterpolator(new CustomSpringInterpolator(interp2));
        imgBubbleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                imgBubble.setScaleX(t);
                imgBubble.setScaleY(t);
            }
        });

        imgBullAnim = new ObjectAnimator();
        imgBullAnim.setDuration(dur3);
        imgBullAnim.setStartDelay(del3);
        imgBullAnim.setFloatValues(imgBull.getY() + v, imgBull.getY());
        imgBullAnim.setInterpolator(new CustomSpringInterpolator(interp3));
        imgBullAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                imgBull.setY(t);
            }
        });

        imgBullPicAnim = new ObjectAnimator();
        imgBullPicAnim.setDuration(dur4);
        imgBullPicAnim.setStartDelay(del4);
        imgBullPicAnim.setFloatValues(0, 1);
        imgBullPicAnim.setInterpolator(new CustomSpringInterpolator(interp4));
        imgBullPicAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                imgBullPic.setScaleX(t);
                imgBullPic.setScaleY(t);
            }
        });

        spiny1 = new ObjectAnimator();
        spiny1.setDuration(dur5);
        spiny1.setStartDelay(del5);
        spiny1.setFloatValues(imgSpinyBig.getX() - h, imgSpinyBig.getX() - 20);
        spiny1.setInterpolator(new DecelerateInterpolator(interp5));
        spiny1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                imgSpinyBig.setX(t);
            }
        });
        spiny2 = new ObjectAnimator();
        spiny2.setDuration(dur6);
        spiny2.setStartDelay(del6);
        spiny2.setFloatValues(imgSpinySml.getX() - h, imgSpinySml.getX() - 20);
        spiny2.setInterpolator(new DecelerateInterpolator(interp6));
        spiny2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                imgSpinySml.setX(t);
            }
        });

        // Choreography
        imgUtterAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imgUtter.setVisibility(View.VISIBLE);
            }

        });
        imgBubbleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imgBubble.setVisibility(View.VISIBLE);
            }

        });
        imgBullAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imgBull.setVisibility(View.VISIBLE);
            }

        });
        imgBullPicAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imgBullPic.setVisibility(View.VISIBLE);
            }
        });
        spiny1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imgSpinyBig.setVisibility(View.VISIBLE);
            }
        });
        spiny2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imgSpinySml.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(!showButtonsNow) showButtons();
            }
        });

        imgUtterAnim.start();
        imgBubbleAnim.start();
        imgBullAnim.start();
        imgBullPicAnim.start();

        //imgBullPic.setPivotY(0);
        //imgBullPic.setPivotX(0.2f);

        rotateNum = 0;
        handler.post(spinyRotate);
        handler.post(bullBaying);
        handler.post(textPop);
        handler.post(bubbleRotate);

        spiny1.start();
        spiny2.start();
    }

    private void showButtons() {

        int dur = 1000;

        ObjectAnimator topBanner, create, join, tutorial;
        topBanner = new ObjectAnimator();
        create = new ObjectAnimator();
        join = new ObjectAnimator();
        tutorial = new ObjectAnimator();

        topBanner.setDuration(dur);
        create.setDuration(dur);
        join.setDuration(dur);
        tutorial.setDuration(dur);
        topBanner.setInterpolator(new OvershootInterpolator(2));
        create.setInterpolator(new OvershootInterpolator(2));
        join.setInterpolator(new OvershootInterpolator(2));
        tutorial.setInterpolator(new OvershootInterpolator(2));

        topBanner.setFloatValues(layoutTop.getY() - 100, layoutTop.getY());
        topBanner.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutTop.setY((float) animation.getAnimatedValue());
                layoutTop.setAlpha(animation.getAnimatedFraction());
            }
        });

        create.setFloatValues(createGameLayout.getY() + 200, createGameLayout.getY());
        create.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                createGameLayout.setY((float) animation.getAnimatedValue());
                createGameLayout.setAlpha(animation.getAnimatedFraction());
            }
        });

        join.setFloatValues(joinGameLayout.getY() + 100, joinGameLayout.getY());
        join.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                joinGameLayout.setY((float) animation.getAnimatedValue());
                joinGameLayout.setAlpha(animation.getAnimatedFraction());
            }
        });

        tutorial.setFloatValues(btnTutorial.getY() + 100, btnTutorial.getY());
        tutorial.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btnTutorial.setY((float) animation.getAnimatedValue());
                btnTutorial.setAlpha(animation.getAnimatedFraction());
            }
        });

        topBanner.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                layoutTop.setVisibility(View.VISIBLE);
            }
        });
        join.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                joinGameLayout.setVisibility(View.VISIBLE);
            }
        });
        create.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                createGameLayout.setVisibility(View.VISIBLE);
            }
        });
        tutorial.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                btnTutorial.setVisibility(View.VISIBLE);
            }
        });

        topBanner.start();
        create.start();
        join.start();
        tutorial.start();

        setButtons();
    }

    int rotateNum;
    Handler handler = new Handler();
    Runnable spinyRotate = new Runnable() {
        @Override
        public void run() {

            rotateNum++;
            rotateNum = rotateNum % 5;

            if(rotateNum % 5 == 0) imgSpinyBig.setRotation( (imgSpinyBig.getRotation() + 0.2f) % 360);
            if(rotateNum % 1 == 0) imgSpinySml.setRotation( (imgSpinySml.getRotation() + 0.2f) % 360);

            /*if(rotateNum > 900)
            {
                imgBullPic.setRotation(-50 + Math.abs(rotateNum - 950));
            }*/

            handler.postDelayed(spinyRotate, 1);
        }
    };
    Runnable bullBaying = new Runnable() {
        @Override
        public void run() {

            int l, m, s;
            l = 3000;
            m = 1500;
            s = 700;
            ObjectAnimator rotate1, rotate2, rotate3, rotate4, rotate5;

            rotate1 = new ObjectAnimator();
            rotate1.setDuration(s);
            rotate1.setStartDelay(0);
            rotate1.setFloatValues(0, 10);

            rotate2 = new ObjectAnimator();
            rotate2.setDuration(m);
            rotate2.setStartDelay(s);
            rotate2.setFloatValues(10, -20);

            rotate3 = new ObjectAnimator();
            rotate3.setDuration(m);
            rotate3.setStartDelay(s + m);
            rotate3.setFloatValues(-20, 0);

            rotate4 = new ObjectAnimator();
            rotate4.setDuration(l);
            rotate4.setStartDelay(s + 2*m);
            rotate4.setFloatValues(0, -20);

            rotate5 = new ObjectAnimator();
            rotate5.setDuration(l);
            rotate5.setStartDelay(s + 2*m + l);
            rotate5.setFloatValues(-20, 0);

            ValueAnimator.AnimatorUpdateListener va = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    imgBullPic.setRotation((float) animation.getAnimatedValue());
                }
            };

            rotate1.addUpdateListener(va);
            rotate2.addUpdateListener(va);
            rotate3.addUpdateListener(va);
            rotate4.addUpdateListener(va);
            rotate5.addUpdateListener(va);

            rotate1.start();
            rotate2.start();
            rotate3.start();
            rotate4.start();
            rotate5.start();

            handler.postDelayed(bullBaying, 2*l + 2*m + s);
        }
    };
    Runnable textPop = new Runnable() {
        @Override
        public void run() {

            ObjectAnimator scale = new ObjectAnimator();
            scale.setDuration(500);
            scale.setFloatValues(1, 1.25f, 1);
            scale.setInterpolator(new CustomSpringInterpolator(0.3f));
            scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    imgUtter.setScaleX((float) animation.getAnimatedValue());
                    imgUtter.setScaleY((float) animation.getAnimatedValue());
                }
            });

            ObjectAnimator scale2 = new ObjectAnimator();
            scale2.setDuration(500);
            scale2.setStartDelay(500);
            scale2.setFloatValues(1, 1.25f, 1);
            scale2.setInterpolator(new CustomSpringInterpolator(0.3f));
            scale2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    imgBull.setScaleX((float) animation.getAnimatedValue());
                    imgBull.setScaleY((float) animation.getAnimatedValue());
                }
            });

            scale.start();
            scale2.start();

            handler.postDelayed(textPop, 2000);
        }
    };
    Runnable bubbleRotate = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator rotate = new ObjectAnimator();
            rotate.setFloatValues(10,-10,10);
            rotate.setDuration(3000);
            rotate.setRepeatMode(ValueAnimator.RESTART);
            rotate.setRepeatCount(ObjectAnimator.INFINITE);
            rotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    imgBubble.setRotation((float) animation.getAnimatedValue());
                }
            });
            rotate.start();
        }
    };

    public class CustomSpringInterpolator implements Interpolator {

        private float factor = 0.3f; // default

        public CustomSpringInterpolator() {}

        public CustomSpringInterpolator(float factor) {
            this.factor = factor;
        }

        @Override
        public float getInterpolation(float input) {
            return (float) (Math.pow(2, (-10 * input)) * Math.sin(((2* Math.PI) * (input - (factor/4)))/factor) + 1);
        }
    }

    /**
     * Set ("", null, null, [boolean]) to just enable/disable buttons.
     *
     * Set ([string], false, null, true) to set perma-notif
     * @param statusUpdate
     * @param showNotif
     * @param showDots
     * @param enableButtonsAfter
     */
    @Override // from MainMenuInteraction
    public void changeUI(String statusUpdate, Boolean showNotif, Boolean showDots, Boolean enableButtonsAfter){

        btnCreateGame.setEnabled(enableButtonsAfter);
        btnJoinGame.setEnabled(enableButtonsAfter);

        if(statusUpdate.equals("") && showNotif == null && showDots == null)
        {

        }
        else if(!showNotif && showDots == null && enableButtonsAfter)
        {
            hideNotif();

            textPerma.setText("( ! ) "+statusUpdate);

        }
        else
        {
            textNotif.setText(statusUpdate);

            if(showDots != null && showDots) notifDots.setVisibility(View.VISIBLE);
            else notifDots.setVisibility(View.GONE);

            if(showNotif != null && showNotif) showNotif();
            else hideNotif();
        }


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }



    @Override // from MainMenuInteraction
    public void moveToWaitingRoom(){

        removeCallbacks();

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_mainMenu_to_waitingRoom);
    }


    public void moveToTutorial() {

        removeCallbacks();

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        MainMenuDirections.ActionMainMenuToTutorial action = MainMenuDirections.actionMainMenuToTutorial().setFromMainMenu(true);
        navController.navigate(action);
    }

    private void removeCallbacks() {

        handler.removeCallbacks(spinyRotate);
        handler.removeCallbacks(bullBaying);
    }

}
