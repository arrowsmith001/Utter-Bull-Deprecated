package com.arrowsmith.llv1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.Card;
import com.arrowsmith.llv1.classes.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.arrowsmith.llv1.MainActivity.TAG;


public class GamePhase1DelegateRoles extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View view;
    private MainActivity main;
    private ConstraintLayout mainLayout;

    ArrayList<Card> cardDeck;
    ArrayList<Card> trueCards;
    ArrayList<Card> lieCards;
    ArrayList<View> cardViews;
    ArrayList<View> trueCardViews;
    ArrayList<View> lieCardViews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.gp1_delegate_roles_fragment, container, false);
        main = (MainActivity) getActivity();

        main.printPlayers();

        mainLayout = (ConstraintLayout) view.findViewById(R.id.layoutDelegateRoles);

        createStateListener();

        // DEBUG FLAG
        //moveToTextEntry();

        view.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                view.setOnTouchListener(null);

                moveToTextEntry();

                return false;
            }
        });

        new CountDownTimer(750,750){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                initialise();
            }
        }.start();


        return view;
    }

    private void initialise() {

        cardDeck = new ArrayList<>();
        lieCards = new ArrayList<>();
        trueCards = new ArrayList<>();

        for(Player p: main.playersList)
        {
            if(!p.getName().equals(main.me.getName()))
            {
                Card newCard = new Card(p.getName(),false);
                cardDeck.add(newCard);
                lieCards.add(newCard);
            }
        }

        for(int i = 0;i < (main.playersList.size() - 1);i++)
        {
            Card newCard = new Card(main.me.getName(),true);
            cardDeck.add(newCard);
            trueCards.add(newCard);
        }


        updateUsableScreenSize();
        
    }
    
    ValueEventListener stateListener;

    private void createStateListener() {

        stateListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: STATE CHANGED");

                if(!((Boolean) dataSnapshot.child("inSession").getValue())){
                    main.myRoomRef.child("state").removeEventListener(this);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
                    navController.navigate(R.id.action_gamePhase1DelegateRoles_to_waitingRoom);
                    Toast toast = new Toast(getActivity());
                    toast.makeText(
                            getActivity(),"Player exited the round, kicked back to the lobby",Toast.LENGTH_SHORT)
                            .show();
                }

                if (dataSnapshot.child("phase").getValue() != null) {

                    String state = (String) dataSnapshot.child("phase").getValue();

                    if(!state.equals(main.currentState)) {
                        Log.i(TAG, "STATE CHANGE: "+main.currentState+" -> "+state);
                        main.currentState = state;

                        switch (state) {
                            case "nullCase":

                                break;
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    public final void updateUsableScreenSize() {
        final View vContent = view.findViewById(R.id.layoutDelegateRoles);
        vContent.post(new Runnable() {
            @Override
            public void run() {

                v = vContent.getHeight();
                h = vContent.getWidth();

                vContent.post(new Runnable() {
                    @Override
                    public void run() {
                        startShuffling();
                    }
                });
            }
        });
    }

    final int CARD_HEIGHT_ABS = 200;
    final int CARD_WIDTH_ABS = 125;

    int v;
    int h;
    int usableH;
    int usableV;
    int delay;

    private void startShuffling() {

        cardViews = new ArrayList<>();
        lieCardViews = new ArrayList<>();
        trueCardViews = new ArrayList<>();

        // DisplayMetrics displayMetrics = new DisplayMetrics();this.getWindowManager()
        //        .getDefaultDisplay()
        //        .getMetrics(displayMetrics);
        //h = Resources.getSystem().getDisplayMetrics().heightPixels;
        //w = Resources.getSystem().getDisplayMetrics().widthPixels;

        delay = 0;
        usableV = v - 32 - (2*CARD_HEIGHT_ABS); // assumes margin of 16
        usableH = h - 32 - (2*CARD_WIDTH_ABS); // assumes margin of 16

        Iterator<Card> iterator1 = ((Iterable<Card>) lieCards).iterator();
        Iterator<Card> iterator2 = ((Iterable<Card>) trueCards).iterator();

        boolean turn = true;

        for(Card c : cardDeck)
        {
            if(turn)
            {
                cardCreate(iterator1.next());
            }else
            {
                cardCreate(iterator2.next());
            }
            turn = !turn;
        }

        delay = 0;

        for(View cv : cardViews)
        {
            cardAnim1(cv);
        }


        /*for(View cv : trueCardViews)
        {
            cardAnim2(cv, true);
        }*/

    }

    /**
     * Creates the card views from deck
     * @param c
     */
    private void cardCreate(Card c)
    {

        ConstraintLayout card;

        LayoutInflater inflater = LayoutInflater.from(main);
        card = (ConstraintLayout) inflater.inflate(R.layout.gp1_role_card, mainLayout,false);

        cardViews.add(card);

        card.setVisibility(View.INVISIBLE);

        /*ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) card.getLayoutParams();
        params.height = 200;
        params.width = 125;
        card.setLayoutParams(params);*/

        // card = getLayoutInflater().inflate(R.layout.gp1_role_card,null);
        card.setId(View.generateViewId());
        mainLayout.addView(card);

        TextView cardName = (TextView) card.findViewById(R.id.text_role_card_victim);
        TextView corner1 = (TextView) card.findViewById(R.id.text_role_corner_1);
        TextView corner2 = (TextView) card.findViewById(R.id.text_role_corner_2);
        ConstraintLayout front = (ConstraintLayout) card.findViewById(R.id.layoutFront);
        ConstraintLayout back = (ConstraintLayout) card.findViewById(R.id.layoutBack);

        front.setVisibility(View.VISIBLE);
        front.bringToFront();
        back.setVisibility(View.INVISIBLE);
        cardName.setText(c.getPlayerName());

        if(c.isTruth())
        {
            corner1.setText("TRUE");
            corner2.setText("TRUE");

            // front.getBackground().setColorFilter(getResources().getColor(R.color.true_blue),PorterDuff.Mode.MULTIPLY);

            front.setBackground(getResources().getDrawable(R.drawable.role_card_true));

            /*int color = getResources().getColor(R.color.true_blue);
            Drawable drawable = front.getBackground();
            Drawable wrapped = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(wrapped, color);
            front.setBackground(wrapped);*/

            trueCardViews.add(card);
        }else
        {
            corner1.setText("BULL");
            corner2.setText("BULL");

            // front.getBackground().setColorFilter(getResources().getColor(R.color.bg_edges_light),PorterDuff.Mode.MULTIPLY);
            /*int color = getResources().getColor(R.color.bg_edges_light);
            Drawable drawable = front.getBackground();
            Drawable wrapped = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(wrapped, color);
            front.setBackground(wrapped);*/

            front.setBackground(getResources().getDrawable(R.drawable.role_card_lie));

            lieCardViews.add(card);
        }

        card.setX(h + CARD_WIDTH_ABS);
        card.setY(v + CARD_HEIGHT_ABS);

    }

    /**
     * Sets initial position (to be 2 skewed rows, top bottom)
     * @param cv
     */
    private void cardAnim1(View cv) {

        int vMargin, hMargin;

        if(trueCardViews.contains(cv))
        {
            int cardPosition = trueCardViews.indexOf(cv);
            int cardSize = trueCardViews.size() - 1;

            int allCardPosition = cardViews.indexOf(cv);
            int allCardSize = cardViews.size() - 1;

            vMargin =
                    (v/2) +
                            32 + (int) ((double) (usableV/2 - 2*CARD_HEIGHT_ABS) * ((double) cardPosition) / cardSize);
            hMargin = (32 + (int) (((double) usableH - 32) * ((double) (cardPosition) / cardSize)));

            //vMargin = lieCards.contains(c) ? h/4 - (CARD_HEIGHT_ABS/2) : (3*(h/4)) - (CARD_HEIGHT_ABS/2);
            //vMargin = 16;
            //hMargin = h - 250 - 16;

        }else
        {
            int cardPosition = lieCardViews.indexOf(cv);
            int cardSize = lieCardViews.size() - 1;

            int allCardPosition = cardViews.indexOf(cv);
            int allCardSize = cardViews.size() - 1;

            vMargin =
                    // (v/2) +
                    32 + (int) ((double) (usableV/2 - 2*CARD_HEIGHT_ABS) * ((double) cardPosition) / cardSize);
            hMargin = (32 + (int) (((double) usableH - 32) * ((double) (cardPosition) / cardSize)));
        }

        cv.setX(hMargin);
        cv.setY(vMargin);

        if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim1();

        /*ObjectAnimator oax = new ObjectAnimator();
        oax.setFloatValues(h + CARD_WIDTH_ABS,hMargin);
        oax.setStartDelay(delay);
        oax.setDuration(0);
        oax.setTarget(cv);
        oax.setInterpolator(new OvershootInterpolator(0.5f));
        oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });

        ObjectAnimator oay = new ObjectAnimator();
        oay.setFloatValues(v + CARD_HEIGHT_ABS,vMargin);
        oay.setStartDelay(delay);
        oay.setDuration(0);
        oay.setTarget(cv);
        oay.setInterpolator(new OvershootInterpolator(0.5f));
        oay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setY(progress);
            }
        });

        oay.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim1();
            }
        });

        oax.start();
        oay.start();

        cv.setVisibility(View.VISIBLE);

        delay += 0;*/

    }

    private void postAnim1() {

        delay = 0;

        for(View cv : cardViews)
        {
            /*cv.setAlpha(0.7f);
            TextView grey = (TextView) cv.findViewById(R.id.textGreyout);
            grey.setVisibility(View.VISIBLE);*/
        }

        for(View cv : cardViews)
        {
            cardAnim2(cv);
        }


        /*for(View cv : trueCardViews)
        {
            cv.setAlpha(0.7f);
            TextView grey = (TextView) cv.findViewById(R.id.textGreyout);
            grey.setVisibility(View.VISIBLE);
        }


        for(View cv : trueCardViews)
        {
            cardAnim2(cv);
        }*/


    }

    /**
     * Pops cards into initial configuration
     * @param cv
     */
    private void cardAnim2(View cv) {

        /*for(int i = cardViews.size() - 1; i>2; i--)
        {
            cardsReversedSkipTwo.add(cardViews.get(i));
        }*/

        /*ObjectAnimator oax = new ObjectAnimator();
        oax.setFloatValues(h + CARD_WIDTH_ABS,hMargin);
        oax.setStartDelay(delay);
        oax.setDuration(1000);
        oax.setTarget(cv);
        oax.setInterpolator(new OvershootInterpolator(0.5f));
        oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });*/


        AnimationSet set = new AnimationSet(false);

        ScaleAnimation scale = new ScaleAnimation(0.2f,1,0.2f,1,
                ScaleAnimation.ABSOLUTE,cv.getX()+(CARD_WIDTH_ABS/2),Animation.ABSOLUTE,cv.getY()+(CARD_HEIGHT_ABS/2));
        scale.setInterpolator(new OvershootInterpolator(2));
        scale.setDuration(100);
        scale.setStartOffset(delay);
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                cv.setScaleX(1);
                cv.setScaleY(1);

                if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim2();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AlphaAnimation alpha = new AlphaAnimation(0,1);
        alpha.setDuration(1);
        alpha.setStartOffset(scale.getStartOffset());

        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setFillAfter(false);

        cv.startAnimation(set);

        delay += 100;

    }

    private void postAnim2() {

        delay = 0;

        for(View cv : cardViews)
        {
            cardAnim3(cv);
        }

    }

    int i; // debug

    /**
     * Sends cards into mixed position (1 skewed row)
     * @param cv
     */
    private void cardAnim3(View cv) {

        int vMargin, hMargin;

        int allCardPosition = cardViews.indexOf(cv);
        int allCardSize = cardViews.size() - 1;

        vMargin = 32 + (int) ((double) (usableV - CARD_HEIGHT_ABS) * ((double) allCardPosition) / allCardSize);
        hMargin = (32 + (int) (((double) usableH - 32) * ((double) (allCardPosition) / allCardSize)));

        ObjectAnimator oax = new ObjectAnimator();
        oax.setFloatValues(cv.getX(),cv.getX()+1000,hMargin);
        oax.setStartDelay(delay);
        oax.setDuration(600);
        oax.setTarget(cv);
        oax.setInterpolator(new OvershootInterpolator(5));
        oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });
        oax.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                cv.setX(hMargin);

                if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim3();
            }
        });

        ObjectAnimator oay = new ObjectAnimator();
        oay.setFloatValues(cv.getY(),vMargin);
        oay.setStartDelay(delay);
        oay.setDuration(300);
        oay.setTarget(cv);
        oay.setInterpolator(new OvershootInterpolator(5));
        oay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setY(progress);
            }
        });
        oay.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                cv.setY(vMargin);
            }
        });

        oax.start();
        oay.start();

        delay += 100;

        /*AnimationSet set1 = new AnimationSet(false);
        ScaleAnimation scale1 = new ScaleAnimation(1,0.1f,1,1,ScaleAnimation.ABSOLUTE,cv.getX()+(CARD_WIDTH_ABS/2),0,0);
        scale1.setDuration(500);
        scale1.setStartOffset(0);
        scale1.setInterpolator(new DecelerateInterpolator(2));
        set1.addAnimation(scale1);

        AnimationSet set2 = new AnimationSet(false);
        ScaleAnimation scale2 = new ScaleAnimation(0.1f,1,1,1,ScaleAnimation.ABSOLUTE,cv.getX()+(CARD_WIDTH_ABS/2),0,0);
        scale2.setDuration(500);
        scale2.setStartOffset(0);
        scale2.setInterpolator(new DecelerateInterpolator(2));
        set2.addAnimation(scale2);

        ObjectAnimator oa1 = new ObjectAnimator();
        oa1.setFloatValues(newHeights.get(i),v/2 - CARD_HEIGHT_ABS);
        oa1.setDuration(1000);
        oa1.setTarget(cv);
        oa1.setInterpolator(new OvershootInterpolator(3));
        oa1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setY(progress);
            }
        });

        ObjectAnimator oa2 = new ObjectAnimator();
        oa2.setFloatValues(cv.getX(),h/2 - CARD_WIDTH_ABS);
        oa2.setDuration(1000);
        oa2.setTarget(cv);
        oa2.setInterpolator(new OvershootInterpolator(3));
        oa2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                cv.startAnimation(set1);

            }
        });

        set1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                cv.findViewById(R.id.text_q).setVisibility(View.VISIBLE);
                cv.startAnimation(set2);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        set2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                oa1.start();
                oa2.start();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        oa2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(cardViews.indexOf(cv) == (cardViews.size() - 1)) goToShuffling();

            }
        });
*/
        //oa.start();

    }

    private void postAnim3() {

        delay = 0;

        for(int i = (cardViews.size() - 1); i > -1; i--)
        {
            cardAnim4(cardViews.get(i));
        }

    }

    // Flips cards over
    private void cardAnim4(View cv){

       /* AnimationSet set = new AnimationSet(false);

        ScaleAnimation scale1 = new ScaleAnimation(1f,0.5f,1,1,
                ScaleAnimation.ABSOLUTE,cv.getX()+CARD_WIDTH_ABS/2,0,0);
                //ScaleAnimation.RELATIVE_TO_SELF,0.5f,0,0);

        scale1.setDuration(1000);
        scale1.setStartOffset(0);
        //scale1.setInterpolator(new DecelerateInterpolator(1));

        ScaleAnimation scale2 = new ScaleAnimation(0.5f,1f,1,1,
                ScaleAnimation.ABSOLUTE,cv.getX()+CARD_WIDTH_ABS/2,0,0);
                //ScaleAnimation.RELATIVE_TO_SELF,0.5f,0,0);

        scale2.setDuration(1000);
        scale2.setStartOffset(2000);
        //scale2.setInterpolator(new AccelerateInterpolator(1));

        set.addAnimation(scale1);
        set.addAnimation(scale2);*/

        ObjectAnimator oa1 = new ObjectAnimator();
        oa1.setFloatValues(1,0.1f);
        oa1.setStartDelay(delay);
        oa1.setDuration(300);
        oa1.setTarget(cv);
        oa1.setInterpolator(new AccelerateInterpolator(5));
        oa1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setScaleX(progress);
            }
        });
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                //if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim3();
            }
        });

        ObjectAnimator oa2 = new ObjectAnimator();
        oa2.setFloatValues(0.1f,1);
        oa2.setStartDelay(delay + 300);
        oa2.setDuration(300);
        oa2.setTarget(cv);
        oa2.setInterpolator(new DecelerateInterpolator(5));
        oa2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setScaleX(progress);
            }
        });
        oa2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                ConstraintLayout back = (ConstraintLayout) cv.findViewById(R.id.layoutBack);
                back.setVisibility(View.VISIBLE);
                back.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim4();
            }
        });

        oa1.start();
        oa2.start();

        delay += 10;

    }

    private void postAnim4() {

        for(View cv : cardViews)
        {
            cardAnim5(cv);
        }

    }

    // Centers cards into one deck
    private void cardAnim5(View cv) {

        ObjectAnimator oax = new ObjectAnimator();
        oax.setFloatValues(cv.getX(),usableH/2);
        oax.setStartDelay(200);
        oax.setDuration(300);
        oax.setTarget(cv);
        oax.setInterpolator(new OvershootInterpolator(5));
        oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });
        oax.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //if(cardViews.indexOf(cv) == (cardViews.size() - 1)) postAnim3();
            }
        });

        ObjectAnimator oay = new ObjectAnimator();
        oay.setFloatValues(cv.getY(),usableV/2);
        oay.setStartDelay(200);
        oay.setDuration(300);
        oay.setTarget(cv);
        oay.setInterpolator(new OvershootInterpolator(2));
        oay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setY(progress);
            }
        });
        oay.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(cardViews.indexOf(cv) == (cardViews.size() - 1)) goToShuffling();
            }
        });

        oax.start();
        oay.start();

    }

    private void goToShuffling() {

        ArrayList<View> cardViewsExtended = new ArrayList<>();


        while(cardViewsExtended.size() < 10)
        {
            cardViewsExtended.addAll(cardViews);
        }

        while(cardViewsExtended.size() > 10)
        {
            cardViewsExtended.remove(cardViewsExtended.get(cardViewsExtended.size() - 1));
        }

        Iterator<View> iterator = ((Iterable<View>) cardViewsExtended).iterator();
/*
        shuffle = new Runnable() {
            @Override
            public void run() {
                if(iterator.hasNext()) executeShuffle(iterator.next());

                if(iterator.hasNext())  handler1.postDelayed(shuffle,300);
            }
        };
        shuffle.run();*/

        main.playSound("shuffle");

        new CountDownTimer(10*200,200)
        {

            @Override
            public void onTick(long millisUntilFinished) {
                //Log.i(TAG, "onTick: millis: "+millisUntilFinished);
                if(iterator.hasNext()) executeShuffle(iterator.next());
            }

            @Override
            public void onFinish() {

                new CountDownTimer(500,500)
                {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {

                        revealCardPhase();

                    }
                }.start();

            }
        }.start();


    }

    private void executeShuffle(View cv)
    {
        //Log.i(TAG, "executeShuffle: SHUFFLE EXECUTED");

        int DURATION = 600;

        int randomSignX, randomSignY, randomX, randomY;

        double rand1 = Math.random();
        double rand2 = Math.random();
        double rand3 = Math.random();
        double rand4 = Math.random();

        if(rand1 < 0.5) randomSignX = 1;
        else randomSignX = -1;

        if(rand2 < 0.5) randomSignY = 1;
        else randomSignY = -1;

        randomX = 200 + (int) (((double)(h/2 - 200))*rand3);
        randomY = 200 + (int) (((double)(v/2 - 200))*rand4);

        ObjectAnimator oa11 = new ObjectAnimator();
        oa11.setFloatValues(cv.getX(),cv.getX()+(randomSignX*randomX),cv.getX());
        oa11.setDuration(DURATION);
        oa11.setTarget(cv);
        oa11.setInterpolator(new DecelerateInterpolator(2));
        oa11.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });

        ObjectAnimator oa12 = new ObjectAnimator();
        oa12.setFloatValues(cv.getY(),cv.getY()+(randomSignY*randomY),cv.getY());
        oa12.setDuration(DURATION);
        oa12.setTarget(cv);
        oa12.setInterpolator(new AccelerateDecelerateInterpolator());
        oa12.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setY(progress);
            }
        });

        oa11.start();
        oa12.start();
    }

    private void revealCardPhase() {

        ConstraintLayout card;

        LayoutInflater inflater = LayoutInflater.from(main);
        card = (ConstraintLayout) inflater.inflate(R.layout.gp1_role_card,mainLayout,false);

        // card = getLayoutInflater().inflate(R.layout.gp1_role_card,null);
        card.setId(View.generateViewId());
        mainLayout.addView(card);

        card.setVisibility(View.INVISIBLE);

        TextView cardName = (TextView) card.findViewById(R.id.text_role_card_victim);
        TextView corner1 = (TextView) card.findViewById(R.id.text_role_corner_1);
        TextView corner2 = (TextView) card.findViewById(R.id.text_role_corner_2);
        ConstraintLayout front = (ConstraintLayout) card.findViewById(R.id.layoutFront);
        ConstraintLayout back = (ConstraintLayout) card.findViewById(R.id.layoutBack);

        front.setVisibility(View.VISIBLE);
        front.bringToFront();
        back.setVisibility(View.INVISIBLE);

        card.setX(h/2 - CARD_WIDTH_ABS);
        card.setY(v/2 - CARD_HEIGHT_ABS);

        if(main.me.getTruth())
        {
            corner1.setText("TRUE");
            corner2.setText("TRUE");
            cardName.setText(main.me.getName());

            // front.getBackground().setColorFilter(getResources().getColor(R.color.true_blue),PorterDuff.Mode.MULTIPLY);

            front.setBackground(getResources().getDrawable(R.drawable.role_card_true));
        }else
        {
            corner1.setText("BULL");
            corner2.setText("BULL");
            cardName.setText(main.me.getTarget());

            // front.getBackground().setColorFilter(getResources().getColor(R.color.bg_edges_light), PorterDuff.Mode.MULTIPLY);

            front.setBackground(getResources().getDrawable(R.drawable.role_card_lie));
        }

        AnimationSet set = new AnimationSet(false);
        ScaleAnimation scale = new ScaleAnimation(0.7f,1,0.7f,1,ScaleAnimation.RELATIVE_TO_PARENT,0.5f,ScaleAnimation.RELATIVE_TO_PARENT,0.5f);
        scale.setDuration(500);
        scale.setStartOffset(0);
        scale.setInterpolator(new OvershootInterpolator(1));
        scale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                card.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        set.addAnimation(scale);

        card.startAnimation(set);

        for(View cv : cardViews)
        {
            fling(cv);
        }

        endSequence(card);

    }

    private void endSequence(View card) {

        new CountDownTimer(1000,1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                ObjectAnimator oax = new ObjectAnimator();
                oax.setFloatValues(card.getX(),h + (2*CARD_WIDTH_ABS) + 50);
                oax.setDuration(500);
                oax.setTarget(card);
                //oax.setInterpolator(new AnticipateInterpolator(2));
                oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float progress = (float) animation.getAnimatedValue();
                        card.setX(progress);
                    }
                });

                ObjectAnimator oa12 = new ObjectAnimator();
                oa12.setFloatValues(card.getRotation(),card.getRotation() + 360);
                oa12.setDuration(500);
                oa12.setTarget(card);
                oa12.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float progress = (float) animation.getAnimatedValue();
                        card.setRotation(progress);
                    }
                });
                oa12.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);

                        Log.i(TAG, "onAnimationStart: THIS CLASS: "+this.getClass().getName()+", FOREGROUND CLASS: "+main.getForegroundFragment().getClass().getSimpleName());

                        moveToTextEntry();
                    }
                });

                oax.start();
                oa12.start();

            }
        }.start();

    }

    private void fling(View cv) {

        double rand1, rand2, rand3, rand4;
        int randX, randY, randSignX, randSignY;

        rand1 = Math.random();
        rand2 = Math.random();
        rand3 = Math.random();
        rand4 = Math.random();

        randX = (int) (rand1 * 1000);
        randY = (int) (rand2 * 1000);

        if(rand3 < 0.5) randSignX = 1;
        else randSignX = -1;

        if(rand4 < 0.5) randSignY = 1;
        else randSignY = -1;

        ObjectAnimator oax = new ObjectAnimator();
        oax.setFloatValues(cv.getX(),randSignX*randX);
        oax.setDuration(2000);
        oax.setTarget(cv);
        oax.setInterpolator(new DecelerateInterpolator(1));
        oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setX(progress);
            }
        });

        ObjectAnimator oay = new ObjectAnimator();
        oay.setFloatValues(cv.getY(),randSignY*randY);
        oay.setDuration(1000);
        oay.setTarget(cv);
        oay.setInterpolator(new DecelerateInterpolator(1));
        oay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                cv.setY(progress);
            }
        });

        AlphaAnimation alpha = new AlphaAnimation(1,0);
        alpha.setDuration(1000);
        alpha.setFillAfter(true);

        oax.start();
        oay.start();

        cv.startAnimation(alpha);

    }
    
    private void moveToTextEntry() {

        if(this.getClass().getName().contains(main.getForegroundFragment().getClass().getSimpleName()))
        {
            NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
            navController.navigate(R.id.action_gamePhase1DelegateRoles_to_textEntryFragment);
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        main.myRoomRef.child("state").addValueEventListener(stateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        main.myRoomRef.child("state").removeEventListener(stateListener);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public GamePhase1DelegateRoles() {
        // Required empty public constructor
    }

    /*// TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // TODO: Rename and change types and number of parameters
    public static GamePhase1DelegateRoles newInstance(String param1, String param2) {
        GamePhase1DelegateRoles fragment = new GamePhase1DelegateRoles();
        Bundle args = new Bundle();
        *//*args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*//*
        fragment.setArguments(args);
        return fragment;
    }*/

}
