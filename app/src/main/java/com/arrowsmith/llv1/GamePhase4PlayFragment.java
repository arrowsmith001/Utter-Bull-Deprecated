package com.arrowsmith.llv1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.ReadyListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class GamePhase4PlayFragment extends Fragment {

    View view;
    ChildEventListener readyStatusListener;
    private MainActivity main;

    // TODO: Trim fields list
    private String voteString;
    private Button voteTrue;
    private Button voteLie;
    private TextView textReady;
    private int maxProgress;
    private CountDownTimer countdownTimer;
    private Button btnContinue;
    private String currentWhoseTurn;
    private int timeToRead;
    private Boolean myVote;
    private CountDownTimer roundTimer;
    private ProgressBar readOutTimer;
    private int maxProgressToReadOut;
    private Button btnContinueToNextPlayer;
    private Toast youCannotVoteToast;
    String content;
    int progress;
    boolean timerRanOut;
    ProgressBar timer;
    TextView textPlayerWhoseTurn,textContent;
    ValueEventListener whoseTurnListener;
    private Thread timerThread;
    private TextView textMinutesLeft;
    private TextView textSecondsLeft;
    private TextView textMsLeft;
    private boolean haveVoted;
    private ChildEventListener childReadyStatusListener;
    private ValueEventListener stateListener;
    private ConstraintLayout layoutVoteButtons;
    private ConstraintLayout mainLayout;
    private ConstraintLayout layoutTextBelowButtons;
    private boolean truthIsHighlighted;
    private TextView textBelowButtons;
    private TextView textCastYourVote;

    private List<Player> playerUnReadyList;
    private List<Player> playerReadyList;
    private List<Player> playerFinalReadyList;
    private ConstraintLayout layoutReadyList;
    private ListView listReady;
    private Button btnHideShowList;
    private TextView textWaitingForPlayerX;
    private TextView numTextReady;
    private TextView numTextOutOf;
    private int numReady;
    private ConstraintLayout layoutBottomButtons;
    private Button btnConfirm;
    private TextView textColon2;
    private ConstraintLayout layoutReadyTracker;
    private CountDownTimer confirmationTimer;
    private Button btnCastYourVote;
    private ConstraintLayout layoutTextAboveButtons;
    private boolean timerReachedEnd;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.gp4_play_fragment, container, false);
        main = (MainActivity) getActivity();

       initialiseViews();

       timerReachedEnd = false;

        layoutReadyList.setVisibility(View.GONE);
        layoutVoteButtons.setVisibility(View.VISIBLE);
        btnContinueToNextPlayer.setVisibility(View.GONE);
        textWaitingForPlayerX.setVisibility(View.GONE);

        // Empty "waiting for players to vote" text
        textReady.setText("");
        textWaitingForPlayerX.setText("Waiting for "+main.whoseTurn+" to press continue...");
        numTextReady.setText("0");
        numTextOutOf.setText(Integer.toString(main.playersList.size() - 1));
        numReady = 0;

        // Initialise readiness lists
        playerUnReadyList = new ArrayList<>();
        for(Player p: main.playersList)
        {
            if(!p.getName().equals(main.whoseTurn)) {
                Player newPlayer = new Player(p.getName());
                newPlayer.setReady(false);
                playerUnReadyList.add(newPlayer);
            }
        }
        playerReadyList = new ArrayList<>();
        playerFinalReadyList = new ArrayList<>();
        playerFinalReadyList.addAll(playerUnReadyList);

        // Hide continue button & vote buttons
        layoutBottomButtons.setVisibility(View.GONE);

        btnContinueToNextPlayer.setEnabled(false);

        // Set button listener to ready-up
        btnContinueToNextPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnContinueToNextPlayer.setEnabled(false);

                newChooseOrReveals();

            }
        });
        btnConfirm.setOnClickListener(v ->
        {
            onButtonConfirm();
        });
        if(main.me.getName().equals(main.whoseTurn)) layoutBottomButtons.setVisibility(View.VISIBLE);


        // Set views with text
        textPlayerWhoseTurn.setText(main.whoseTurn);
        textContent.setText(main.roundContent);
        
        // Detect our turn / saboteur cases...
        // If our turn...
        if(main.me.getName().equals(main.whoseTurn))
        {
            // Set up OUR TURN case

            // Immediately vote "p" for "playing"
            vote("p", main.room.getSettings().getRoundTimeMins()*60);

            setUpButtonsForPlayer();
        }
        // If our target (we are saboteur)...
        else if(main.me.getTarget() != null
            && main.me.getTarget().equals(main.whoseTurn))
        {
            // Set up OUR TARGET case

            // Votes "s" for "saboteur"

            // Set buttons to ready up ONLY
            voteTrue.setOnClickListener(v ->{
                layoutVoteButtons.setVisibility(View.GONE);
                textCastYourVote.setText("You voted TRUE");
                if(numReady != main.playersList.size() - 2) layoutReadyList.setVisibility(View.VISIBLE);
                vote("s",(int)(((double)main.room.getSettings().getRoundTimeMins()*60)*((double)timer.getProgress()/timer.getMax())));
            });
            voteLie.setOnClickListener(v ->{
                layoutVoteButtons.setVisibility(View.GONE);
                textCastYourVote.setText("You voted BULL");
                if(numReady != main.playersList.size() - 2) layoutReadyList.setVisibility(View.VISIBLE);
                vote("s",(int)(((double)main.room.getSettings().getRoundTimeMins()*60)*((double)timer.getProgress()/timer.getMax())));
            });


        }else // We are simply playing
        {
            // Set up PLAYING case

            // Set up buttons to vote AND ready up
            voteTrue.setOnClickListener(v ->{
                layoutVoteButtons.setVisibility(View.GONE);
                textCastYourVote.setText("You voted TRUE");
                if(numReady != main.playersList.size() - 2) layoutReadyList.setVisibility(View.VISIBLE);
                vote("T",(int)(((double)main.room.getSettings().getRoundTimeMins()*60)*((double)timer.getProgress()/timer.getMax())));
            });
            voteLie.setOnClickListener(v ->{
                layoutVoteButtons.setVisibility(View.GONE);
                textCastYourVote.setText("You voted BULL");
                if(numReady != main.playersList.size() - 2) layoutReadyList.setVisibility(View.VISIBLE);
                vote("L",(int)(((double)main.room.getSettings().getRoundTimeMins()*60)*((double)timer.getProgress()/timer.getMax())));
            });

        }

        startPlayTimer();
        animateTimerSoft();

        // Create state listener
        stateListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: STATE CHANGED");

                if(!((Boolean) dataSnapshot.child("inSession").getValue())){
                    main.myRoomRef.child("state").removeEventListener(this);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
                    navController.navigate(R.id.action_playFragment_to_waitingRoom);
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
                            case "endOfPlay":

                                endRound();

                                break;
                            case "newChoose":

                                moveToChoose();

                                break;
                            case "reveals":

                                moveToReveals();

                                break;

                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // Create childReadyStatusListener
        childReadyStatusListener = new ChildEventListener(){

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildAdded: PLAYER READY STATUS ADDED");
                if(!dataSnapshot.getKey().equals(main.whoseTurn))
                {
                    numReady++;
                    numTextReady.setText(Integer.toString(numReady));
                    numTextReady.setScaleX(0.5f);
                    numTextReady.setScaleY(0.5f);
                    numTextReady.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(2))
                            .start();
                    addToReadyStatusList(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // Log.i(TAG, "onChildAdded: PLAYER READY STATUS AMENDED");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        setListAdapter();

        return view;
    }

    private void onButtonConfirm() {

        btnConfirm.setEnabled(false);
        confirmationTimer.cancel();

        main.myRoomRef.child("playersTruthManifest").child("players").child(main.me.getName()).setValue(truthIsHighlighted, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                btnConfirm.setVisibility(View.GONE);
                layoutVoteButtons.setVisibility(View.GONE);
                btnHideShowList.setVisibility(View.GONE);
                layoutReadyList.setVisibility(View.VISIBLE);

                //textCastYourVote.setVisibility(View.GONE);
                textCastYourVote.setText("Playing as " + (truthIsHighlighted ? "truth" : "lie") );
                textCastYourVote.setTextColor(main.getResources().getColor(R.color.light_grey));
                textWaitingForPlayerX.setText("TOUCH TO DISMISS");
                textWaitingForPlayerX.setVisibility(View.VISIBLE);

                btnCastYourVote.setOnClickListener(v -> {
                    layoutTextAboveButtons.setVisibility(View.GONE);
                });

                btnContinueToNextPlayer.setEnabled(true);
            }
        });

    }

    private void initialiseViews() {
        mainLayout = view.findViewById(R.id.constraintLayoutPlay);

        timer = (ProgressBar) view.findViewById(R.id.progress_bar);
        textPlayerWhoseTurn = (TextView) view.findViewById(R.id.player_whose_turn);
        textContent = (TextView) view.findViewById(R.id.player_whose_turn_statement);

        layoutVoteButtons = (ConstraintLayout) view.findViewById(R.id.layoutVoteButtons);

        voteTrue = (Button) view.findViewById(R.id.button_vote_true);
        voteLie = (Button) view.findViewById(R.id.button_vote_lie);

        layoutBottomButtons = (ConstraintLayout) view.findViewById(R.id.layoutBottomButtons);

        btnContinueToNextPlayer = (Button) view.findViewById(R.id.button_continue_after_eop);
        btnConfirm = (Button) view.findViewById(R.id.button_confirm);
        textReady = (TextView) view.findViewById(R.id.text_ready);

        layoutReadyTracker = (ConstraintLayout) view.findViewById(R.id.layoutReadyTracker);

        textMinutesLeft = (TextView) view.findViewById(R.id.text_clock_m);
        textSecondsLeft = (TextView) view.findViewById(R.id.text_clock_s);
        textMsLeft = (TextView) view.findViewById(R.id.text_clock_ms);
        textColon = view.findViewById(R.id.text_clock_colon1);
        textColon2 = view.findViewById(R.id.text_clock_colon2);

        layoutTextBelowButtons = (ConstraintLayout) view.findViewById(R.id.layoutVoteButtonsTruthSwitcher);
        textBelowButtons = (TextView) view.findViewById(R.id.text_below_buttons);
        layoutTextBelowButtons.setVisibility(View.GONE);

        layoutTextAboveButtons = (ConstraintLayout) view.findViewById(R.id.layoutTextAboveButtons);
        textCastYourVote = (TextView) view.findViewById(R.id.textCastYourVote);
        btnCastYourVote = (Button) view.findViewById(R.id.button_cast_your_vote);

        layoutReadyList = (ConstraintLayout) view.findViewById(R.id.layoutPlayerReadyList);
        listReady = (ListView) view.findViewById(R.id.listPlayerReadyList);
        btnHideShowList = (Button) view.findViewById(R.id.button_show_hide_ready_list);
        numTextReady = (TextView) view.findViewById(R.id.text_num_players_deciding);
        numTextOutOf = (TextView) view.findViewById(R.id.text_num_players_voted);

        textWaitingForPlayerX = (TextView) view.findViewById(R.id.text_waiting_for_player_X);

        LayoutTransition lt = new LayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        lt.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        lt.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);

        layoutVoteButtons.setLayoutTransition(lt);
        layoutReadyList.setLayoutTransition(lt);
        layoutTextBelowButtons.setLayoutTransition(lt);
        layoutBottomButtons.setLayoutTransition(lt);
    }

    boolean listShowing;

    private void setListAdapter() {

        listShowing = false;
        // Set ready list listener
        btnHideShowList.setOnClickListener(v -> {

            if(listShowing)
            {
                layoutReadyList.setVisibility(View.GONE);
                listShowing = false;
            }
            else
            {
                layoutReadyList.setVisibility(View.VISIBLE);
                listShowing = true;
            }
        });

        ReadyListAdapter adapter = new ReadyListAdapter(getActivity(),0,playerFinalReadyList,"VOTED");
        listReady.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void addToReadyStatusList(DataSnapshot dataSnapshot) {

        if(!dataSnapshot.getKey().equals(main.whoseTurn) && listReady.getAdapter() != null)
        {
            int index = -1;
            boolean playerFound = false;
            while(!playerFound)
            {
                index++;
                if(playerUnReadyList.get(index).getName().equals(dataSnapshot.getKey())) playerFound = true;
            }

            Player newReadyPlayer = playerUnReadyList.get(index);
            newReadyPlayer.setReady(true);

            playerUnReadyList.remove(newReadyPlayer);
            playerReadyList.add(newReadyPlayer);

            playerFinalReadyList.clear();

            playerFinalReadyList.addAll(playerReadyList);
            playerFinalReadyList.addAll(playerUnReadyList);

            ((ReadyListAdapter) listReady.getAdapter()).notifyDataSetChanged();
        }
    }

    private void setUpButtonsForPlayer() {

        textCastYourVote.setText("Confirm your statement");

        String truthIsTrue = "This truth you wrote for yourself is indeed true";
        String lieIsLie = "This lie written for you is indeed a lie";
        String truthIsLie = "This truth you wrote for yourself is, on second thoughts, a lie";
        String lieIsTrue = "This lie written for you happens to be true!";

        if(main.me.getTruth())
        {
            truthIsHighlighted = true;
            voteTrue.getBackground().setColorFilter(getResources().getColor(R.color.true_button_blue), PorterDuff.Mode.MULTIPLY);
            voteLie.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            textBelowButtons.setText(truthIsTrue);
        }
        else
        {
            truthIsHighlighted = false;
            voteLie.getBackground().setColorFilter(getResources().getColor(R.color.bull_button_red), PorterDuff.Mode.MULTIPLY);
            voteTrue.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
            textBelowButtons.setText(lieIsLie);
        }

        voteTrue.setOnClickListener(v -> {
            if(!truthIsHighlighted)
            {
                truthIsHighlighted = true;
                btnConfirm.setTextColor(getResources().getColor(R.color.text_blue));
                voteTrue.getBackground().setColorFilter(getResources().getColor(R.color.true_button_blue), PorterDuff.Mode.MULTIPLY);
                voteLie.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

                if(main.me.getTruth())  textBelowButtons.setText(truthIsTrue);
                else textBelowButtons.setText(lieIsTrue);
            }
        });

        voteLie.setOnClickListener(v -> {
            if(truthIsHighlighted)
            {
                truthIsHighlighted = false;
                btnConfirm.setTextColor(getResources().getColor(R.color.text_red));
                voteLie.getBackground().setColorFilter(getResources().getColor(R.color.bull_button_red), PorterDuff.Mode.MULTIPLY);
                voteTrue.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

                if(main.me.getTruth())  textBelowButtons.setText(truthIsLie);
                else textBelowButtons.setText(lieIsLie);
            }
        });

        btnConfirm.setTextColor(getResources().getColor(truthIsHighlighted ? R.color.text_blue : R.color.text_red));
        layoutTextBelowButtons.setVisibility(View.VISIBLE);

        startConfirmationTimer();
    }

    private void animateTimerHard() {

        // BAR FLASHING
        int colorFrom = 0x770000;
        int colorTo = getResources().getColor(R.color.red);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500); // milliseconds
        // colorAnimation.setStartDelay(10000); // will be useful for 1 min left variant
        colorAnimation.setInterpolator(new AccelerateInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                timer.setProgressBackgroundTintList( ColorStateList.valueOf((int) animator.getAnimatedValue()));; //#E9E9E9
            }

        });
        ValueAnimator colorAnimation2 = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
        colorAnimation2.setDuration(500); // milliseconds
        colorAnimation2.setInterpolator(new DecelerateInterpolator());
        // colorAnimation.setStartDelay(10000); // will be useful for 1 min left variant
        colorAnimation2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                timer.setProgressBackgroundTintList( ColorStateList.valueOf((int) animator.getAnimatedValue()));; //#E9E9E9
            }

        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                colorAnimation2.start();

            }
        });

        // TEXT FLASHING
        ValueAnimator colorAnimationText = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.white), colorTo);
        colorAnimationText.setDuration(150); // milliseconds
        // colorAnimation.setStartDelay(10000); // will be useful for 1 min left variant
        colorAnimationText.setInterpolator(new AccelerateInterpolator());
        colorAnimationText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                textSecondsLeft.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue())); //#E9E9E9
                textMsLeft.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue()));
                textColon2.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue()));
            }

        });
        ValueAnimator colorAnimationText2 = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, getResources().getColor(R.color.white));
        colorAnimationText2.setDuration(150); // milliseconds
        colorAnimationText2.setInterpolator(new DecelerateInterpolator());
        // colorAnimation.setStartDelay(10000); // will be useful for 1 min left variant
        colorAnimationText2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                textSecondsLeft.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue())); //#E9E9E9
                textMsLeft.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue()));
                textColon2.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue()));
            }

        });
        colorAnimationText.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                colorAnimationText2.start();

            }
        });

        handler.removeCallbacks(colorAnim);

        colorAnim = new Runnable() {
            @Override
            public void run() {

                colorAnimation.start();
                handler.postDelayed(colorAnim,1000);
            }
        };
        textColorAnim = new Runnable() {
            @Override
            public void run() {

                colorAnimationText.start();
                handler.postDelayed(textColorAnim,300);
            }
        };

        handler.postDelayed(colorAnim,0);
        handler.postDelayed(textColorAnim,0);
    }

    private void animateTimerSoft() {

            int colorFrom = 0x770000;
            int colorTo = getResources().getColor(R.color.white);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(1000); // milliseconds
            // colorAnimation.setStartDelay(10000); // will be useful for 1 min left variant
            colorAnimation.setInterpolator(new AccelerateInterpolator());
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    timer.setProgressBackgroundTintList( ColorStateList.valueOf((int) animator.getAnimatedValue()));; //#E9E9E9
                }

            });

            ValueAnimator colorAnimation2 = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
            colorAnimation2.setDuration(1000); // milliseconds
            colorAnimation2.setInterpolator(new DecelerateInterpolator());
            // colorAnimation.setStartDelay(10000); // will be useful for 1 min left variant
            colorAnimation2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    timer.setProgressBackgroundTintList( ColorStateList.valueOf((int) animator.getAnimatedValue()));; //#E9E9E9
                }

            });

            colorAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    colorAnimation2.start();

                }
            });

            colorAnim = new Runnable() {
                @Override
                public void run() {

                    colorAnimation.start();
                    handler.postDelayed(colorAnim,2000);
                }
            };

            handler.postDelayed(colorAnim,1000);
        }

    Runnable colorAnim, textColorAnim;
    Handler handler = new Handler();

    private void moveToChoose() {

        main.myRoomRef.child("state").removeEventListener(stateListener);
        main.playersReadyManifestRef.child("players").removeEventListener(childReadyStatusListener);

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_playFragment_to_chooseWhoseTurn);
    }

    private void moveToReveals() {

        main.myRoomRef.child("state").removeEventListener(stateListener);
        main.playersReadyManifestRef.child("players").removeEventListener(childReadyStatusListener);

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_playFragment_to_gamePhase5RevealsIntro);
    }

    private void endRound() {

        roundTimer.cancel();
        // layoutReadyTracker.setVisibility(View.GONE);

        // Remove readyStatus listener
        main.playersReadyManifestRef.child("players").removeEventListener(childReadyStatusListener);

        btnHideShowList.setOnClickListener(null);
        layoutReadyList.setVisibility(View.GONE);

        handler.removeCallbacks(colorAnim);
        if(textColorAnim != null) handler.removeCallbacks(textColorAnim);

        if(timerReachedEnd)
        {
            allowContinue();
        }
            else{
                bleedTimer();
        }

        // Set UI
        textReady.setText("ROUND OVER");
        // timer.setProgress(0);

        // We need:
        // New players turn to be set
        // New prompt to observe whoseTurn
        // Final prompt to proceed back to choose phase

        // If its our turn, offer button. Otherwise just ready up

        Log.i(TAG, "endRound: my name: "+main.me.getName()+", whoseTurn: "+main.whoseTurn);

    }

    private void allowContinue() {

        if(main.me.getName().equals(main.whoseTurn)){

                /*ViewGroup.LayoutParams params1 = (ViewGroup.LayoutParams) voteLie.getLayoutParams();
                params1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                voteLie.requestLayout();*/

                /*LayoutWrapContentUpdater lwcu = new LayoutWrapContentUpdater();

                ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) voteTrue.getLayoutParams();
                params2.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                lwcu.wrapContentAgain(layoutVoteButtons);
                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) layoutVoteButtons.getLayoutParams();
                params1.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                lwcu.wrapContentAgain(mainLayout);*/

                /*ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mainLayout.getLayoutParams();
                mainLayout.get*/
/*
            layoutVoteButtons.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT));*/

            btnContinueToNextPlayer.setVisibility(View.VISIBLE);

        } else{

            textWaitingForPlayerX.setVisibility(View.VISIBLE);
            layoutVoteButtons.setVisibility(View.GONE);

            newChooseOrReveals();
        }

    }

    private void bleedTimer() {

        minuteLeft = false;

        int progressLeft = timer.getProgress();

        double fractionLeft = ((double) progressLeft) / timer.getMax();

        int roundTime = main.room.getSettings().getRoundTimeMins()*60000;
        long msLeft = (long) (((double) roundTime) * fractionLeft);

        ObjectAnimator oa = new ObjectAnimator();
        oa.setIntValues(1000,0);
        oa.setDuration(1000);
        oa.setInterpolator(new DecelerateInterpolator(2));
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                double fracLeft = 1 - (double) (animation.getAnimatedFraction());
                timer.setProgress((int) (((double)progressLeft) * fracLeft));

                textMsLeft.setText(String.format("%02d",((int) (((double) msLeft) * fracLeft)) % 100));
                textSecondsLeft.setText(String.format("%02d",((int) (((double) msLeft) * fracLeft)) % 100));

                if(!minuteLeft){
                    textMinutesLeft.setText(Integer.toString((int) ( ((double) msLeft / 60000) * ((double) ((int) (((double) msLeft) * fracLeft)) / 1000))));
                    if(((int) ((double) ((int) (((double) msLeft) * fracLeft)) / 60000)) == 0) {
                        textMinutesLeft.setVisibility(View.GONE);
                        textColon.setVisibility(View.GONE);
                        minuteLeft = true;
                    }
                }
            }
        });
        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                main.playSound("timer_bleed");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                allowContinue();
            }
        });

        // Sounds
        main.playSound("round_end");
        int delay = main.getDurationLength(R.raw.timer_end_alarm, main);
        new CountDownTimer(delay, delay)
        {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                oa.start();
            }
        }.start();
        // Delay for length of sound




        /*new CountDownTimer(1000, 1)
        {

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setProgress((int) (progressLeft*((double) millisUntilFinished / (1000) )));
                textMsLeft.setText(String.format("%02d",millisUntilFinished % 100));
                textSecondsLeft.setText(String.format("%02d",millisUntilFinished % 100));

                if(!minuteLeft){
                    textMinutesLeft.setText(Integer.toString((int) ( ((double) msLeft / 60000) * ((double) millisUntilFinished / 1000))));
                    if(((int) ((double) millisUntilFinished / 60000)) == 0) {
                        textMinutesLeft.setVisibility(View.GONE);
                        textColon.setVisibility(View.GONE);
                        minuteLeft = true;
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();*/

    }

    private void newChooseOrReveals() {

        main.setReady(true);

        if(main.me.getHosting()){

            if(main.playersLeftToPlay.size() > 0){
                main.checkReadyStatus("newChoose");
            }else{
                main.checkReadyStatus("reveals");
            }
                // Check for continue

        }
    }

    private void setUpNewRound(){
        Log.i(TAG, "setUpNewRound: CALLED");

        // Start setting new turn (if host)
        if(main.me.getHosting()) main.setNewWhoseTurn();

            // If NOT host...
        else{
            main.setReady(true);
        }
    }




    private void vote(String voteChar, int voteTime) throws IllegalArgumentException {

        Log.i(TAG, "vote: VOTE: "+voteChar+", VOTE TIME: "+voteTime);

        // p = playing, s = saboteur, T = true vote, L = lie vote, X = did not vote in time
        if(voteChar != "p" && voteChar != "s" && voteChar != "T" && voteChar != "L" && voteChar != "X" ){
            throw new IllegalArgumentException("Error: unrecognised vote character passed");
        }

        haveVoted = true;

        main.me.addVote(voteChar);
        main.me.addTime(voteTime);
        
        main.setReady(true);
        if(main.me.getHosting()) main.checkReadyStatus("endOfPlay");

        Log.i(TAG, "vote: VOTED: "+voteChar+", VOTEARRAY NOW: "+main.me.getVotes());

    }

    boolean tenSecondsPassed = false;
    boolean minuteLeft = false;
    TextView textColon;

    private void startPlayTimer() {

        int roundTime = main.room.getSettings().getRoundTimeMins()*60000;
        maxProgress = timer.getMax();

        /*int secondNow = roundTime + 1; // Current second
        int minuteNow = ((int) ((double) roundTime / 60)) + 1;*/

        roundTimer = new CountDownTimer(roundTime,1){

            @Override
            public void onTick(long millisUntilFinished) {
                timer.setProgress((int) (maxProgress*((double) millisUntilFinished / (roundTime) )));
                textMsLeft.setText(String.format("%02d",millisUntilFinished % 100));
                textSecondsLeft.setText(String.format("%02d",(int) ((double) millisUntilFinished / 1000) % 60));

                if(!minuteLeft){
                    textMinutesLeft.setText(Integer.toString((int) ((double) millisUntilFinished / 60000)));
                    if(((int) ((double) millisUntilFinished / 60000)) == 0) {
                        textMinutesLeft.setVisibility(View.GONE);
                        textColon.setVisibility(View.GONE);
                        minuteLeft = true;
                    }
                }

                if(millisUntilFinished < 10000 && !tenSecondsPassed)
                {
                    animateTimerHard();
                    tenSecondsPassed = true;
                }

            }

            @Override
            public void onFinish() {

                timerReachedEnd = true;

                timer.setProgress(0);
                textMsLeft.setText("00");
                textSecondsLeft.setText("00");
                textMinutesLeft.setText("0");
                
                // If we have not even voted yet
                if(!haveVoted){

                    main.playSound("alarm");

                    // If saboteur
                    if(main.me.getTarget() != null
                            && main.me.getTarget().equals(main.whoseTurn)){

                        vote("s",0);
                        textCastYourVote.setText("You didn't vote in time!");
                    }else // We are voter
                        
                        if(!main.me.getName().equals(main.whoseTurn)
                                && !(main.me.getTarget() != null
                                && main.me.getTarget().equals(main.whoseTurn)))
                        {
                            vote("X",0);
                            textCastYourVote.setText("You didn't vote in time!");
                        }

                }

                layoutReadyTracker.setVisibility(View.GONE);
                btnHideShowList.setVisibility(View.GONE);

                Log.i(TAG, "onFinish: ");
            }
        };

        roundTimer.start();


    }

    private void startConfirmationTimer()
    {
        confirmationTimer = new CountDownTimer(30000,1000)
        {

            @Override
            public void onTick(long millisUntilFinished) {
                btnConfirm.setText("CONFIRM "+Integer.toString((int) Math.floor(((double) millisUntilFinished) / 1000)));
            }

            @Override
            public void onFinish() {

                onButtonConfirm();

            }
        }.start();
    }

    // TO-DO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        main.myRoomRef.child("state").addValueEventListener(stateListener);
        main.playersReadyManifestRef.child("players").addChildEventListener(childReadyStatusListener);

    }

    @Override
    public void onPause() {
        super.onPause();

        main.myRoomRef.child("state").removeEventListener(stateListener);
        main.playersReadyManifestRef.child("players").removeEventListener(childReadyStatusListener);
    }



    class VotingRegisterList extends ArrayAdapter<Player> {

        private Context context;
        private List<Player> list;

        public VotingRegisterList(@NonNull Context context,int textResourceId, ArrayList<Player> list) {
            super(context, 0, list);
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.wr_member_list_item,null);


/*
            textPlayerName.setText(list.get(position).getName());

            if(list.get(position).getName().equals(main.me.getName())) textIsYou.setText("You");
            else textIsYou.setVisibility(View.GONE);

            if(list.get(position).getHosting()) textIsHost.setText("HOST");
            else textIsHost.setVisibility(View.GONE);

            textPoints.setText(Integer.toString(list.get(position).getPoints()));*/

            return v;
        }
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////





    /*Button myButton;
    View myView;
    boolean isUp;

    // slide the view from below itself to the current position
    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void onSlideViewButtonClick(View view) {
        if (isUp) {
            slideDown(myView);
            myButton.setText("Slide up");
        } else {
            slideUp(myView);
            myButton.setText("Slide down");
        }
        isUp = !isUp;
    }*/





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

    public interface OnFragmentInteractionListener {
        // TO-DO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

     /*// TO-DO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TO-DO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;*/

    private OnFragmentInteractionListener mListener;

    public GamePhase4PlayFragment() {
        // Required empty public constructor
    }

   /*
    // TO-DO: Rename and change types and number of parameters
    public static GamePhase4PlayFragment newInstance(String param1, String param2) {
        GamePhase4PlayFragment fragment = new GamePhase4PlayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
*//*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/
}
