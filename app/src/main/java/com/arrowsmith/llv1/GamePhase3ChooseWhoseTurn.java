package com.arrowsmith.llv1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import static com.arrowsmith.llv1.MainActivity.TAG;


public class GamePhase3ChooseWhoseTurn extends Fragment {


    private View view;
    private MainActivity main;
    private static final long COUNTDOWN_TIME_1 = 1;
    private static final long COUNTDOWN_TIME_2 = 1;
    private static final long COUNTDOWN_TIME_3 = 1;

    private TextView textContent;
    private int timeToRead;
    private ValueEventListener stateListener;
    private ValueEventListener whoseTurnListener;

    private ConstraintLayout mainLayout;
    private ConstraintLayout layoutChoosChoosing;
    private TextView textChoosingPlayer;
    private TextView textEllipses;
    private Guideline guideLine;
    private ConstraintLayout layoutReadingOut;
    private ProgressBar readOutTimer;
    private TextView textSecondsLeft;
    private Button btnGo;
    private ConstraintLayout layoutMiddleBit;
    private ConstraintLayout arrowsLeft;
    private ConstraintLayout arrowsRight;
    private TextView textSaboInfo;
    private Button buttonDismissSabo;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.gp3_choose_whose_turn_fragment, container, false);

        main = (MainActivity) getActivity();

        initialiseViews();

        /*for(String s : main.playersText.keySet()){
            Log.i(TAG, "onCreateView: KEY: "+s+", VALUE: "+main.playersText.get(s));
        }*/

        // Create state listener
        stateListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!((Boolean) dataSnapshot.child("inSession").getValue())){
                    main.myRoomRef.child("state").removeEventListener(this);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
                    navController.navigate(R.id.action_chooseWhoseTurn_to_playFragment);
                    Toast toast = new Toast(getActivity());
                    toast.makeText(
                            getActivity(),"Player exited the round, kicked back to the lobby",Toast.LENGTH_SHORT)
                            .show();
                }

                if (dataSnapshot.child("phase").getValue() != null) {

                    String state = (String) dataSnapshot.child("phase").getValue();

                    // If state is new
                    if(!state.equals(main.currentState)){
                        Log.i(TAG, "STATE CHANGE: "+main.currentState+" -> "+state);
                        main.currentState = state;

                        switch (state) {
                            case "readWhoseTurn":

                                onReadWhoseTurn();

                                break;


                            case "beginChooseTimer":
                                Log.i(TAG, "onDataChange: END OF PLAY DETECTED");

                                chooseyTime();
                                // initialisePhase1();

                                break;
                            case "play":
                                Log.i(TAG, "onDataChange: END OF PLAY DETECTED");

                                moveToPlay();

                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // SET WHOSE TURN
        if(main.me.getHosting()) setWhoseTurn();
        else main.setReady(true);

        return view;
    }

    private void onReadWhoseTurn() {

        // If we're prompted for readWhoseTurn player phase...
        Log.i(TAG, "onDataChange: READ WHOSE TURN PHASE DETECTED");
        //

        if(main.me.getHosting())
        {
            main.setReady(true);
            main.checkReadyStatus("beginChooseTimer");
        }

        whoseTurnListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String playerWhoseTurn = (String) dataSnapshot.getValue();

                // Set local information
                main.whoseTurn = playerWhoseTurn;
                main.roundContent = main.playersText.get(playerWhoseTurn);
                Log.i(TAG, "onDataChange: READWHOSETURN: " + main.whoseTurn);

                // Remove player from players left to play
                // Add player to players who have played

                main.setReady(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        main.myRoomRef.child("whoseTurn").addListenerForSingleValueEvent(whoseTurnListener);
        //main.myRoomRef.child("whoseTurn").child("refresh").setValue(""); // Nudge


    }

    private void initialiseViews() {

        mainLayout = (ConstraintLayout) view.findViewById(R.id.layoutChooseTurn);

            layoutChoosChoosing = (ConstraintLayout) view.findViewById(R.id.layoutChooseChoosing);

                textChoosingPlayer = (TextView) view.findViewById(R.id.text_choosing_whose_turn);
                textEllipses = (TextView) view.findViewById(R.id.text_choosing_player_NAME);
                guideLine = (Guideline) view.findViewById(R.id.guideline);
                arrowsLeft = (ConstraintLayout) view.findViewById(R.id.layoutArrowsLeft);
                arrowsRight = (ConstraintLayout) view.findViewById(R.id.layoutArrowsRight);

            layoutMiddleBit = (ConstraintLayout) view.findViewById(R.id.layoutMiddleContentBit);

                textContent = (TextView) view.findViewById(R.id.text_content_or_waiting);
                textSaboInfo = (TextView) view.findViewById(R.id.textSaboteurInfo);
                buttonDismissSabo = (Button) view.findViewById(R.id.button_dismiss_sabo_inf);

            layoutReadingOut = (ConstraintLayout) view.findViewById(R.id.layoutChooseReadingOut);

                readOutTimer = (ProgressBar) view.findViewById(R.id.timer_read_out);
                textSecondsLeft = (TextView) view.findViewById(R.id.text_seconds_left_to_read);
                btnGo = (Button) view.findViewById(R.id.button_go);

        // Initialise views
        layoutReadingOut.setVisibility(View.GONE);
        layoutMiddleBit.setVisibility(View.GONE);
        arrowsLeft.setVisibility(View.GONE);
        arrowsRight.setVisibility(View.GONE);
        layoutChoosChoosing.setVisibility(View.VISIBLE);

    }


    int x, y;

    HashMap<Integer,Float> positions;
    HashMap<Integer,Float> factors;

    int numberInTicker;
    int v, h, usableV, usableH;
    long duration;
    boolean finalRun, playerLanded;
    private int tickCounter;

    ArrayList<String> listForTicker;
    Iterator<String> nameIterator;
    ArrayList<View> nameViews;

    private void chooseyTime() {

        LayoutTransition lt = new LayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        mainLayout.setLayoutTransition(lt);

        if(main.me.getName().equals(main.whoseTurn))
        {
            readOutTimer.setVisibility(View.VISIBLE);
            textSecondsLeft.setVisibility(View.VISIBLE);
            btnGo.setVisibility(View.VISIBLE);
            textContent.setText("Press GO! to reveal your statement");
            btnGo.setOnClickListener(v ->
            {
                onButtonGo();
            });
        }
        else
        {
            readOutTimer.setVisibility(View.GONE);
            textSecondsLeft.setVisibility(View.GONE);
            btnGo.setVisibility(View.GONE);

            if(main.whoseTurn.equals(main.me.getTarget()))
            {
                textSaboInfo.setVisibility(View.VISIBLE);
                buttonDismissSabo.setOnClickListener(v ->
                {
                    textSaboInfo.setVisibility(View.GONE);
                });
            }

            textContent.setText("Waiting for "+main.whoseTurn+" to read out statement . . .");
        }

        updateUsableScreenSize2();

    }

    private void updateUsableScreenSize2() {

        final View vContent = view.findViewById(R.id.layoutChooseTurn);
        vContent.post(new Runnable() {
            @Override
            public void run() {

                v = vContent.getHeight();
                h = vContent.getWidth();

                // Starting positions
                y = (int) (v*0.25);
                x = 0;

                usableV = v - y;
                usableH = h;

                vContent.post(new Runnable() {
                    @Override
                    public void run() {
                        preTicker();
                    }
                });
            }
        });
    }

    private void preTicker() {

        new CountDownTimer(500,500){

            @Override
            public void onTick(long millisUntilFinished) {


            }

            @Override
            public void onFinish() {

                ObjectAnimator oa = new ObjectAnimator();
                oa.setFloatValues(1,0.25f);
                oa.setInterpolator(new TimeInterpolator() {
                    @Override
                    public float getInterpolation(float input) {
                        return (float) sigmoid(input, 5);
                    }
                });
                oa.setDuration(500);
                oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        guideLine.setGuidelinePercent((float) animation.getAnimatedValue());
                    }
                });
                oa.start();

                beginTicker();
            }
        }.start();

    }

    private void beginTicker() {

        numberInTicker = 11;

        // Collections.shuffle(main.playersLeftToPlay);

        // Creates a new list of at least numberInTicker elements representing player names
        listForTicker = new ArrayList<>();
        while(listForTicker.size() < numberInTicker)
        {
            for(Player p : main.playersLeftToPlay)
            {
                listForTicker.add(p.getName());
            }
        }

        //for(String s : listForTicker) Log.i(TAG, "beginTicker: s : "+s);

        // Sets usable bounds - not confirmed

        int index = 0;

        nameIterator = ((Iterable<String>) listForTicker).iterator();
        nameViews = new ArrayList<>();

        nameIterator = ((Iterable<String>) listForTicker).iterator();

        positions = new HashMap<>();
        factors = new HashMap<>();

        for(int i = 0; i < numberInTicker; i++) {
            // Adds name view to layout
            LinearLayout name;

            LayoutInflater inflater = LayoutInflater.from(main);
            name = (LinearLayout) inflater.inflate(R.layout.gp3_ticker_item, mainLayout, false);
            name.setId(View.generateViewId());

            mainLayout.addView(name);
            nameViews.add(name);

            // Set width
            name.getLayoutParams().width = (int) (h*(((double) 7) / 8) - 50);

            // Set text
            TextView textName = name.findViewById(R.id.textTickerName);
            textName.setText(nameIterator.next());

            // Scale factor
            int mid = (int) ((double)numberInTicker) / 2;
            float factor = (((float)i - mid)) / mid;

            // Nudge factor
            int sign = - (int) Math.signum(factor);
            double nudge = Math.pow(Math.abs(factor),0.333)
                    * sign;

            // Position view
            int yMargin = 16*2;
            float newPos = yMargin +
                    (float) (y + ((int) (usableV-2*yMargin) * (double) i + 0.5)/numberInTicker);
            newPos = yMargin +
                    (float) (y/2 + ((int) (usableV-2*yMargin) * sigmoid(factor,5)));


            name.setY(newPos);
            positions.put(i,newPos);

            factors.put(i,(float) Math.sqrt(Math.abs(factor)) * sign);

            name.setPivotX(h/2);
            name.setScaleX(1 - Math.abs(factor));
            name.setPivotY(50);
            name.setScaleY((float) (1 - Math.abs(factor)));

            //Log.i(TAG, "beginTicker: i = "+i+", POS: "+newPos+", FACTOR: "+factor+", NUDGE: "
             //       +" SIGMOID: "+sigmoid(((double) i + 0.5)/numberInTicker,5));
        }

        nameIterator = ((Iterable<String>) listForTicker).iterator();

        postDelayedSequence();

        /*nameViews.get(0).setX(0);
        nameViews.get(0).setY((float) 0.25*v);
        nameViews.get(1).setX(0);
        nameViews.get(1).setY(v - 20);
        nameViews.get(2).setX(h - 20);
        nameViews.get(2).setY((float) 0.25*v);
        nameViews.get(3).setX(h - 20);
        nameViews.get(3).setY(v - 20);
*/
    }

    private void postDelayedSequence() {

        playerLanded = false;
        finalRun = false;

        tickCounter = 0;
        duration = 10;

        handler.postDelayed(tickOnce, duration);

    }

    public static double sigmoid(double x, double factor)
    {
        double exp = (double) Math.exp(- factor * x);
        return ((double) 1) / (1 + exp);
    }

    Handler handler = new Handler();
    Runnable tickOnce = new Runnable() {
        @Override
        public void run() {

            tickCounter++;

            HashMap<Integer,Float> posTemp = new HashMap<>();
            HashMap<Integer,Float> facTemp = new HashMap<>();

            nameViews.add(0, nameViews.get(nameViews.size() - 1));
            nameViews.remove(nameViews.get(nameViews.size() - 1));

            if(finalRun)
            {
                // Find position of zero factor
                int key = -1;
                for(Integer k : factors.keySet())
                {
                    if(Math.abs(factors.get(k)) < 0.01) key = k;
                }

                // Determine name of zero position
                TextView tv = nameViews.get(key).findViewById(R.id.textTickerName);
                String playerName = tv.getText().toString();
                if(playerName.equals(main.whoseTurn)) {
                    playerLanded = true;
                    onPlayerLanded(key);
                }
            }

            if(!playerLanded)
            {
                for(int i = 0; i < nameViews.size(); i++)
                {
                    View name = nameViews.get(i);

                    ObjectAnimator oaPos = new ObjectAnimator();
                    ObjectAnimator oaScale = new ObjectAnimator();
                    oaPos.setDuration(duration);
                    oaScale.setDuration(duration);

                    oaPos.setFloatValues(
                            positions.get(i),
                            positions.get((i + 1) % numberInTicker));
                    oaScale.setFloatValues(
                            Math.abs(factors.get(i)),
                            Math.abs(factors.get((i + 1) % numberInTicker)));

                    posTemp.put(i,positions.get((i + 1) % numberInTicker));
                    facTemp.put(i,factors.get((i + 1) % numberInTicker));

                    oaPos.setTarget(name);
                    oaScale.setTarget(name);

                /*oaPos.setInterpolator(new TimeInterpolator() {
                    @Override
                    public float getInterpolation(float t) {
                        float factor = 5;
                        float exp = (float) Math.exp(- factor * t);
                        return 1 / (1 + exp);
                    }
                });
                oaScale.setInterpolator(new TimeInterpolator() {
                    @Override
                    public float getInterpolation(float t) {
                        float factor = 5;
                        float exp = (float) Math.exp(- factor * t);
                        return 1 / (1 + exp);
                    }
                });*/
                    //oaPos.setInterpolator(new OvershootInterpolator(5));

                    oaPos.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float t = (float) animation.getAnimatedValue();
                            name.setY(t);
                        }
                    });

                    name.setPivotX(0);
                    name.setPivotY(50);
                    oaScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float t = (float) animation.getAnimatedValue();
                            name.setScaleX(1 - Math.abs(t));
                            name.setScaleY(1 - Math.abs(t));
                            name.setAlpha(1 - Math.abs(t));
                            // name.setX( (float) (h/2) *  (1 - Math.abs(t)) );
                            name.setX((float) (50 + (1 - Math.pow(Math.abs(t),2))*(h/8)));
                        }
                    });

                    if(factors.get(i) + 1 < 0.01)
                    {
                        if(!nameIterator.hasNext()) nameIterator = ((Iterable<String>) listForTicker).iterator();

                        TextView tv = name.findViewById(R.id.textTickerName);
                        tv.setText(nameIterator.next().toString());
                    }

                /*if(finalRun && Math.abs(factors.get(i))  < 0.01)
                {
                    TextView tv = name.findViewById(R.id.textTickerName);
                    playerHighlighted = tv.getText().toString();
                    Log.i(TAG, "run: PLAYERNAME HIGHLIGHT: "+playerHighlighted);
                    if(playerHighlighted.equals(playerWhoseTurn))
                    {
                        playerLanded = true;

                        onPlayerLanded();
                        //handler.removeCallbacks(tickOnce);
                    }
                }*/

                    oaPos.start();
                    oaScale.start();
                }


                positions = posTemp;
                factors = facTemp;

                int BOUND_1 = 20;
                int BOUND_12 = 30;
                int BOUND_2 = 33;
                int BOUND_23 = 36;
                int BOUND_3 = 38;

                int dur1 = 50;
                int dur2 = 150;
                int dur3 = 250;

                // DEBUG ONLY
                dur1 = 1;
                dur2 = 1;
                dur3 = 1;
                // DEBUG ONLY

                if(tickCounter < BOUND_1)
                {
                    duration = dur1;
                    //Log.i(TAG, "1 run: "+tickCounter+", "+"dur: "+duration);
                    handler.postDelayed(tickOnce, duration);
                }else
                if(tickCounter < BOUND_12)
                {
                    double durDiff = dur2 - dur1;
                    double boundDiff = BOUND_12 - BOUND_1;
                    duration =  (long) (dur2 - ((int) (durDiff)*( ((double)(BOUND_12 - tickCounter)) / boundDiff )));
                    //Log.i(TAG, "12 run: "+tickCounter+", "+"dur: "+duration);
                    handler.postDelayed(tickOnce, duration);
                }else
                if (tickCounter < BOUND_2)
                {
                    duration = dur2;
                    //Log.i(TAG, "2 run: "+tickCounter+", "+"dur: "+duration);
                    handler.postDelayed(tickOnce, duration);
                }else
                if (tickCounter < BOUND_23)
                {
                    double durDiff = dur3 - dur2;
                    double boundDiff = BOUND_23 - BOUND_2;
                    duration =  (long) (dur3 - ((int) (durDiff)*( ((double)(BOUND_23 - tickCounter)) / boundDiff )));
                    //Log.i(TAG, "23 run: "+tickCounter+", "+"dur: "+duration);

                    handler.postDelayed(tickOnce, duration);
                }
                else
                if (tickCounter < BOUND_3)
                {
                    duration = dur3;
                    Log.i(TAG, "3 run: "+tickCounter+", "+"dur: "+duration);
                    handler.postDelayed(tickOnce, duration);
                }
                else
                if(!playerLanded)
                {
                    if(tickCounter == BOUND_3) finalRun = true;
                    duration = dur3;
                    Log.i(TAG, "final run: "+tickCounter+", "+"dur: "+duration);
                    handler.postDelayed(tickOnce, duration);
                }

            }
        }

    };

    private void onPlayerLanded(int keyOfCenter) {

        for (Player p : main.playersList) {

            if (p.getName().equals(main.whoseTurn)) {

                main.playersWhovePlayed.add(p);
                main.playersLeftToPlay.remove(p);

                Log.i(TAG, "setWhoseTurn: ADDED " + p.getName() + " to players whove played");
            }

        }

        // textChoosingPlayer.setVisibility(View.INVISIBLE);

        for(View nv : nameViews)
        {
            if(nameViews.indexOf(nv) != keyOfCenter)
            {
                ObjectAnimator oa = new ObjectAnimator();
                oa.setFloatValues(nv.getX(), nv.getX() - 1000);
                oa.setDuration(750);
                oa.setTarget(nv);
                oa.setInterpolator(new AnticipateInterpolator(2));
                oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        nv.setX((float) animation.getAnimatedValue());
                    }
                });
                oa.start();
            }
            else
            {
                // TextView tv = nv.findViewById(R.id.textTickerName);
                float viewHeight = nv.getLayoutParams().height;
                float viewWidth = nv.getLayoutParams().width;

                /*TextView tv = nv.findViewById(R.id.textTickerName);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                tv.setLayoutParams(params);

                ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) nv.getLayoutParams();
                params2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                nv.setLayoutParams(params2);*/

                ObjectAnimator oax = new ObjectAnimator();
                oax.setFloatValues(nv.getX(), textEllipses.getX() - h/4);
                oax.setDuration(600);
                oax.setStartDelay(600);
                oax.setTarget(nv);
                oax.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        nv.setX((float) animation.getAnimatedValue());
                    }
                });

                ObjectAnimator oay = new ObjectAnimator();
                oay.setFloatValues(nv.getY(), textEllipses.getY());
                oay.setDuration(600);
                oay.setStartDelay(600);
                oay.setTarget(nv);
                oay.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        nv.setY((float) animation.getAnimatedValue());
                    }
                });
                oay.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);

                        // Set white line
                        layoutChoosChoosing.setBackground(getResources().getDrawable(R.drawable.bg_choosing));

                        // DONT CHANGE THIS ORDER
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layoutChoosChoosing.getLayoutParams();
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        layoutChoosChoosing.setLayoutParams(params);

                        layoutReadingOut.setVisibility(View.VISIBLE);
                        layoutMiddleBit.setVisibility(View.VISIBLE);

                        guideLine.setGuidelinePercent(1);
                        // DONT CHANGE THIS ORDER

                        textEllipses.setText(main.whoseTurn);
                        textChoosingPlayer.setText("");
                        textChoosingPlayer.setVisibility(View.INVISIBLE);

                        LayoutTransition lt = new LayoutTransition();
                        lt.enableTransitionType(LayoutTransition.CHANGING);
                        lt.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
                        lt.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
                        layoutChoosChoosing.setLayoutTransition(lt);
                        //layoutReadingOut.setLayoutTransition(lt);
                        layoutMiddleBit.setLayoutTransition(lt);
                        arrowsLeft.setLayoutTransition(lt);
                        arrowsRight.setLayoutTransition(lt);
                        mainLayout.setLayoutTransition(lt);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);


                    }
                });

                ObjectAnimator alpha = new ObjectAnimator();
                alpha.setFloatValues(1, 0);
                alpha.setDuration(500);
                alpha.setStartDelay(600);
                alpha.setTarget(nv);
                alpha.setTarget(textEllipses);
                alpha.setInterpolator(new DecelerateInterpolator(2));
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        nv.setScaleX((float) animation.getAnimatedValue());
                        nv.setScaleY((float) animation.getAnimatedValue());
                        textEllipses.setScaleX(1 - (float) animation.getAnimatedValue());
                        textEllipses.setScaleY(1 - (float) animation.getAnimatedValue());
                    }
                });

                oax.start();
                oay.start();
                alpha.start();


            }
        }

        if(!main.me.getName().equals(main.whoseTurn))
        {
            main.setReady(true);
            if(main.me.getHosting()) main.checkReadyStatus("play");
        }

    }

    private void onButtonGo() {

        leftArrowTop = view.findViewById(R.id.imageView4);
        leftArrowMid = view.findViewById(R.id.imageView5);
        leftArrowLow = view.findViewById(R.id.imageView6);
        rightArrowTop = view.findViewById(R.id.imageView);
        RightArrowMid = view.findViewById(R.id.imageView2);
        RightArrowLow = view.findViewById(R.id.imageView3);

        leftArrowTop.setImageAlpha(122);
        leftArrowMid.setImageAlpha(122);
        leftArrowLow.setImageAlpha(122);
        rightArrowTop.setImageAlpha(122);
        RightArrowMid.setImageAlpha(122);
        RightArrowLow.setImageAlpha(122);

        btnGo.setVisibility(View.GONE);

        textEllipses.setText("READ THIS OUT!");
        arrowsLeft.setVisibility(View.VISIBLE);
        arrowsRight.setVisibility(View.VISIBLE);

        arrowCycle = 0;

        handler.postDelayed(arrowFlash, 0);

        textChoosingPlayer.setVisibility(View.GONE);
        textContent.setText("");

        index = 0;
        text = main.playersText.get(main.whoseTurn);
        handler.postDelayed(setContentRunnable, 1000);
    }

    ImageView leftArrowTop;
    ImageView leftArrowMid;
    ImageView leftArrowLow;
    ImageView rightArrowTop;
    ImageView RightArrowMid;
    ImageView RightArrowLow;

    int arrowCycle, arrowCycleMod;

    Runnable arrowFlash = new Runnable() {
        @Override
        public void run() {

            arrowCycle ++;
            arrowCycleMod = arrowCycle % 3;

            //Log.i(TAG, "run: arrowCycle "+arrowCycle);

            switch(arrowCycleMod) {
                case 0:
                    leftArrowLow.setImageAlpha(122);
                    RightArrowLow.setImageAlpha(122);
                    leftArrowTop.setImageAlpha(255);
                    rightArrowTop.setImageAlpha(255);
                    break;
                case 1:
                    leftArrowTop.setImageAlpha(122);
                    rightArrowTop.setImageAlpha(122);
                    leftArrowMid.setImageAlpha(255);
                    RightArrowMid.setImageAlpha(255);
                    break;
                case 2:
                    leftArrowMid.setImageAlpha(122);
                    RightArrowMid.setImageAlpha(122);
                    leftArrowLow.setImageAlpha(255);
                    RightArrowLow.setImageAlpha(255);
                    break;
            }

            /*leftArrowTop.setImageAlpha((int) (     ((double)255) *   ( ( (double)  (3  +  ((arrowCycle+1) % 3) )   ) / 3 )    ) );
            rightArrowTop.setImageAlpha((int) (     ((double)255) *   ( ( (double)  (3  +  ((arrowCycle+1) % 3) )   ) / 3 )    ) );
            leftArrowMid.setImageAlpha((int) (     ((double)255) *   ( ( (double)  (2  +  ((arrowCycle+1) % 3) )   ) / 3 )    ) );
            RightArrowMid.setImageAlpha((int) (     ((double)255) *   ( ( (double)  (2  +  ((arrowCycle+1) % 3) )   ) / 3 )    ) );
            leftArrowLow.setImageAlpha((int) (     ((double)255) *   ( ( (double)  (1  +  ((arrowCycle+1) % 3) )   ) / 3 )    ) );
            RightArrowLow.setImageAlpha((int) (     ((double)255) *   ( ( (double)  (1  +  ((arrowCycle+1) % 3) )   ) / 3 )    ) );*/

            if(arrowCycle >= 3*4)
            {
                leftArrowTop.setImageAlpha(122);
                leftArrowMid.setImageAlpha(122);
                leftArrowLow.setImageAlpha(122);
                rightArrowTop.setImageAlpha(122);
                RightArrowMid.setImageAlpha(122);
                RightArrowLow.setImageAlpha(122);
            }
            else
            {
                handler.postDelayed(arrowFlash,150);
            }

            }

    };

    /**
     * HOST ONLY: Set next player turn
     */
    public void setWhoseTurn() {
        Log.i(TAG, "setWhoseTurn: CALLED");

        // Generate random index on remaining players
        int randomIndex = (int) (Math.random()*main.playersLeftToPlay.size());

        // Get random player, add to players played, and remove them from remaining players
        String playerWhoseTurn = main.playersLeftToPlay.get(randomIndex).getName();
        /*playersWhovePlayed.add(playersLeftToPlay.get(randomIndex));
        playersLeftToPlay.remove(randomIndex);*/

        // Save local information
        main.whoseTurn = playerWhoseTurn;

        // Set whose turn it is on the database
        main.myRoomRef.child("whoseTurn").setValue(playerWhoseTurn, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: WHOSETURN VALUE SET");

                // Upon value set completion
                main.setReady(true);
                main.checkReadyStatus("readWhoseTurn");
            }
        });

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

    int index;
    String text;
    Runnable setContentRunnable = new Runnable() {
        @Override
        public void run() {
            index++;
            textContent.setText(text.subSequence(0, index));
            if(index < text.length())
            {
                handler.postDelayed(this,5);
            }else if(index == text.length())
            {
                phase2();
            }
        }
    };

    int timeInSeconds;

    private void phase2() {

        // Our turn scenario
        if(main.me.getName().equals(main.whoseTurn))
        {

        }
        else // Not our turn scenario
        {
            //textName.setVisibility(View.GONE);
            //textMain.setText("Waiting for " + main.whoseTurn + " to read out statement...");
        }

        Log.i(TAG, "phase2: ROUNDCONTENT: "+main.roundContent);

        int maxTimerReadOut = readOutTimer.getMax();
        int lengthOfStatement = main.roundContent.length();

        Log.i(TAG, "phase2: LENGTH OF STATEMENT: "+lengthOfStatement);

        // Time to read out is at least 5, but can be up to number of letters divided by 8
        timeToRead = Math.max(
                5, (int) ( ((double)lengthOfStatement) / 8 )
        );
        timeToRead = 1; // DEBUG FLAG

        // Initialise timer text //
        Log.i(TAG, "phase2: TEXTIMER NULL = "+(textSecondsLeft==null ? "TRUE" : "FALSE"));

        textSecondsLeft.setText(Integer.toString(timeToRead));
        //textSecondsLeft.setPivotX(0.5f);
        //textSecondsLeft.setPivotY(0.5f);

        // Read out statement countdown
        new CountDownTimer(timeToRead*1000,1){

            @Override
            public void onTick(long millisUntilFinished) {
                readOutTimer.setProgress( (int)(maxTimerReadOut*((double) millisUntilFinished / (timeToRead*1000) )));

                 timeInSeconds = (int) Math.floor((double) (millisUntilFinished / 1000)) +1;

                 // If time in seconds has actually changed...
                 if(timeInSeconds != Integer.parseInt(textSecondsLeft.getText().toString()))
                 {
                     textSecondsLeft.setText(Integer.toString(timeInSeconds));
                     ObjectAnimator oa = new ObjectAnimator();
                     oa.setFloatValues(0.5f,1);
                     oa.setDuration(300);
                     oa.setTarget(textSecondsLeft);
                     oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                         @Override
                         public void onAnimationUpdate(ValueAnimator animation) {
                             textSecondsLeft.setScaleX((float)animation.getAnimatedValue());
                             textSecondsLeft.setScaleY((float)animation.getAnimatedValue());
                         }
                     });
                     oa.start();
                 }

            }

            @Override
            public void onFinish() {
                readOutTimer.setProgress(0);
                textSecondsLeft.setText("");

                main.setReady(true);
                if(main.me.getHosting()) main.checkReadyStatus("play");

            }
        }.start();

    }

    private void moveToPlay() {

        main.myRoomRef.child("state").removeEventListener(stateListener);

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_chooseWhoseTurn_to_playFragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


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

    private OnFragmentInteractionListener mListener;

    public GamePhase3ChooseWhoseTurn() {
        // Required empty public constructor
    }

    /*// TO-DO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GamePhase3ChooseWhoseTurn.
     *//*
    // TO-DO: Rename and change types and number of parameters
    public static GamePhase3ChooseWhoseTurn newInstance(String param1, String param2) {
        GamePhase3ChooseWhoseTurn fragment = new GamePhase3ChooseWhoseTurn();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    // TO-DO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    /*private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TO-DO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TO-DO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
