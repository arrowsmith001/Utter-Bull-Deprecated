package com.arrowsmith.llv1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.arrowsmith.llv1.classes.Achievement;
import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.TurnData;
import com.arrowsmith.llv1.classes.Voter;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.arrowsmith.llv1.MainActivity.TAG;


public class GamePhase7RevealsFragment extends Fragment {

    private TurnData turnData;
    private int roundNumber;
    private ArrayList<String> fastestPlayers;
    private String content;
    private String youVoted;
    private String wasInFact;
    private String writtenBy;
    private ArrayList<Voter> trueVoters;
    private ArrayList<Voter> lieVoters;
    // Constructor params
    private String identity;


    private static final long AFTER_REVEAL_TIME = 1000;
    private static final long POINTS_TIME = 1000;
    View view;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    private View layoutReveals, layoutResults;

    TextView textPlayerName, textContent, textIsInFact, textEllipses;
    Button btnReveal;
    private Player playerToReveal;
    Iterator playerIterator;

    private CountDownTimer introTimer;
    private TextView textTimeTo, textReveal, textTheTruth;
    private ConstraintLayout introLayout, mainLayout;
    private ValueEventListener stateListener;

    private MainActivity main;
    private int revealNumber;
    private int thisPlayerIndex;
    private TextView textTrueOrBull;
    private ListView listTrues;
    private ListView listLies;
    private TextView textWrittenBy;
    private TextView textVotedTrue;
    private TextView textVotedLie;
    private ConstraintLayout voteListsLayout;
    private String thisPlayerName;
    private TextView textNumberTrue;
    private TextView textNumberLie;
    private String tOrL;
    private ConstraintLayout mainTop;
    private ConstraintLayout mainMiddle;

    private String name;
    private TextView textYouHaveVoted;
    private ConstraintLayout layoutYouVoted;
    private Space kevin_bottom;
    private Space kevin_top;
    private ConstraintLayout layoutBtnReveal;
    private TextView textWaitingForXToReveal;
    private ConstraintLayout mainMiddleLeft;
    private ConstraintLayout mainMiddleRight;
    private TextView textScores;
    private TextView textYouHaveVotedText;
    private TextView textRoundXLeft;
    private TextView textRoundXTop;

    // Bring in constructor params
    public GamePhase7RevealsFragment(TurnData turnData){
        this.turnData = turnData;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.gp7_reveals_fragment, container, false);
        if(main == null) main = (MainActivity) getActivity();

        initialiseViews();

        updateUsableScreenSize();

        return view;
    }

    int v, h;

    private void updateUsableScreenSize()
    {

        final View vContent = mainLayout;
        vContent.post(new Runnable() {
            @Override
            public void run() {

                v = vContent.getHeight();
                h = vContent.getWidth();


                vContent.post(new Runnable() {
                    @Override
                    public void run() {

                        // Measure views
                        mainTop.measure(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
                        mainMiddle.measure(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,ConstraintLayout.LayoutParams.WRAP_CONTENT);

                        setViewVis(0);

                        setValues();
                        initialiseRevealViews();

                    }
                });
            }
        });
    }

    private void initialiseViews()
    {
        // Initialise main views
        mainLayout = view.findViewById(R.id.revealsLayout);

        // Name and Content (top bit)
        mainTop = (ConstraintLayout) view.findViewById(R.id.revealsLayoutTopBit);

            textPlayerName = view.findViewById(R.id.text_player_to_reveal);
            textContent = view.findViewById(R.id.text_reveal_content);
            textYouHaveVotedText = view.findViewById(R.id.text_you_have_voted);
            textYouHaveVoted = view.findViewById(R.id.text_you_have_voted2);
            textRoundXLeft = view.findViewById(R.id.text_round_x_left);
            textRoundXTop = view.findViewById(R.id.text_round_x_top);

        layoutYouVoted = (ConstraintLayout) view.findViewById(R.id.layout_you_voted_x);

        // Middle bit
        mainMiddle = (ConstraintLayout) view.findViewById(R.id.revealsLayoutMiddleBit);

            mainMiddleLeft = (ConstraintLayout) view.findViewById(R.id.layoutMiddleLeft);

                textIsInFact = view.findViewById(R.id.text_was_in_fact);
                textTrueOrBull = view.findViewById(R.id.text_big_reveal);

            mainMiddleRight = (ConstraintLayout) view.findViewById(R.id.layoutMiddleRight);

                textScores = view.findViewById(R.id.text_scores_sheet);

            layoutBtnReveal = view.findViewById(R.id.layout_reveal_button);

                btnReveal = view.findViewById(R.id.button_reveal);
                textWaitingForXToReveal = view.findViewById(R.id.text_waiting_for_x_to_reveal);

        // Vote Lists bit
        voteListsLayout = (ConstraintLayout) view.findViewById(R.id.constraint_layout_votes);

            listTrues = (ListView) view.findViewById(R.id.list_trues);
            listLies = (ListView) view.findViewById(R.id.list_lies);
            textWrittenBy = (TextView) view.findViewById(R.id.text_written_by);
            textVotedTrue = (TextView) view.findViewById(R.id.text_voted_true);
            textVotedLie = (TextView) view.findViewById(R.id.text_voted_bull);
            textNumberTrue = (TextView) view.findViewById(R.id.text_number_voted_true);
            textNumberLie = (TextView) view.findViewById(R.id.text_number_voted_lie);

        // Very bottom bit

        kevin_top = (Space) view.findViewById(R.id.kevin2);
        kevin_bottom = (Space) view.findViewById(R.id.kevin);

        LayoutTransition lt = new LayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        lt.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        lt.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);

        mainTop.setLayoutTransition(lt);
        // layoutYouVoted.setLayoutTransition(lt);
        layoutBtnReveal.setLayoutTransition(lt);
        voteListsLayout.setLayoutTransition(lt);
        mainMiddle.setLayoutTransition(lt);
        mainMiddleLeft.setLayoutTransition(lt);
        mainMiddleRight.setLayoutTransition(lt);


    }


    public void setRevealedValues(){

        textIsInFact.setVisibility(View.INVISIBLE);
        layoutBtnReveal.setVisibility(View.INVISIBLE);
        textIsInFact.setVisibility(View.GONE);
        layoutBtnReveal.setVisibility(View.GONE);

        textTrueOrBull.setText(turnData.getWasInFact().equals("T") ? "TRUE" : "BULL");

        if(turnData.getWrittenBy().equals(turnData.getName()))
        {
            textWrittenBy.setText("Written by "+ turnData.getWrittenBy());
        }
        else {
            textWrittenBy.setText("Written by TBA");
            textWrittenBy.setTextColor(getResources().getColor(R.color.light_grey));
        }

        textWrittenBy.setVisibility(View.VISIBLE);
    }

    public void setTrueWrittenBy(){

        textWrittenBy.setText("Written by "+ (turnData.getWrittenBy()));
        textWrittenBy.setTextColor(getResources().getColor(R.color.white));

    }

    private void setValues() {

        textRoundXLeft.setText(Integer.toString(turnData.getTurnNumber()));
        textRoundXTop.setText(Integer.toString(turnData.getTurnNumber()));

        // Set values
        textPlayerName.setText(turnData.getName());
        textContent.setText(turnData.getPlayersText().get(turnData.getName()));

        youVoted = main.meTemp.getVotes().get(turnData.getTurnNumber() - 1);

        if(youVoted.equals("s")){

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)textYouHaveVotedText.getLayoutParams();
            params.setMargins(0, 0, 0, 0); //substitute parameters for left, top, right, bottom
            textYouHaveVotedText.setLayoutParams(params);

            textYouHaveVoted.setText("");
            textYouHaveVotedText.setText("You wrote it");

        } else if (youVoted.equals("p")){

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)textYouHaveVotedText.getLayoutParams();
            params.setMargins(0, 0, 0, 0); //substitute parameters for left, top, right, bottom
            textYouHaveVotedText.setLayoutParams(params);

            textYouHaveVoted.setText("");
            textYouHaveVotedText.setText("You played it");

        } else if (youVoted.equals("X"))
        {
            textYouHaveVoted.setText("");
            textYouHaveVotedText.setText("You didn\'t vote in time!");
        }
        else
        {
            textYouHaveVoted.setText(youVoted.equals("T") ? "TRUE" : "BULL");
        }

        textNumberTrue.setText(Integer.toString(turnData.getTrueVoters().size()));
        textNumberLie.setText(Integer.toString(turnData.getLieVoters().size()));

        textTrueOrBull.setText(". . .");
        textWrittenBy.setVisibility(View.GONE);
        textWaitingForXToReveal.setText("Waiting for "+turnData.getName()+" to reveal . . .");

        // Set listViews
        truthAdapter =
                new VoteRegisterListAdapter(main, 0, turnData.getTrueVoters());
        listTrues.setAdapter(truthAdapter);

        liesAdapter =
                new VoteRegisterListAdapter(main, 0, turnData.getLieVoters());
        listLies.setAdapter(liesAdapter);

        truthAdapter.notifyDataSetChanged();
        liesAdapter.notifyDataSetChanged();
    }


    private void initialiseRevealViews() {

        btnReveal.setVisibility(View.INVISIBLE);

        btnReveal.setOnClickListener(v -> {
            main.setReady(true);
            if (main.me.getHosting()) main.checkReadyStatus("revealsNext");
            v.setVisibility(View.INVISIBLE);
        });
    }

    ArrayList<Achievement> achieved_on_votelist;

    public ArrayList<Achievement> getAchievedOnVotelist(){
        return achieved_on_votelist;
    }


    public void addToScoresText(String line){

        if(textScores.getText().equals("")) {
            mainMiddleRight.setVisibility(View.VISIBLE);
            textScores.setText(line);
        }else{
            textScores.setText(textScores.getText() + "\n" + line);
        }

    }

    ArrayAdapter<Voter> truthAdapter, liesAdapter;
    String scoreString;


    @Override
    public void onResume() {
        super.onResume();
//        main.myRoomRef.child("state").addValueEventListener(stateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
//        main.myRoomRef.child("state").removeEventListener(stateListener);
    }

    public void setViewVis(int stage) {

        Log.i(TAG, "setViewVis: STAGE: "+stage);

        switch(stage){

            case 0: // Hide panel & set initial view state

                mainLayout.setVisibility(View.VISIBLE);
                mainTop.setVisibility(View.VISIBLE);
                layoutYouVoted.setVisibility(View.VISIBLE);
                mainMiddle.setVisibility(View.GONE);
                mainMiddleRight.setVisibility(View.GONE);
                layoutBtnReveal.setVisibility(View.GONE);
                voteListsLayout.setVisibility(View.GONE);
                kevin_bottom.setVisibility(View.GONE);
                kevin_top.setVisibility(View.GONE);

                if(turnData.getWasInFact().equals("L")){

                    Log.i(TAG, "setViewVis: WAS IN FACT == L CONFIRMED");
                    ConstraintSet set1 = new ConstraintSet();

                    set1.clone(mainMiddle);/*
                    set1.clear(mainMiddleRight.getId(), ConstraintSet.END);
                    set1.clear(mainMiddleRight.getId(), ConstraintSet.START);*/

                    set1.connect(mainMiddleRight.getId(), ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START, 0);
                    set1.connect(mainMiddleRight.getId(), ConstraintSet.END,mainMiddleLeft.getId(),ConstraintSet.START, 0);
                    set1.connect(mainMiddleLeft.getId(), ConstraintSet.START,mainMiddleRight.getId(),ConstraintSet.END, 0);
                    set1.connect(mainMiddleLeft.getId(), ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END, 0);

                    set1.applyTo(mainMiddle);

                    textScores.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                }


                break;
            case 1: // Wrap two starting views so they squash to top

                textRoundXLeft.setVisibility(View.VISIBLE);
                textRoundXTop.setVisibility(View.GONE);

                ConstraintLayout.LayoutParams params1 = (ConstraintLayout.LayoutParams) mainTop.getLayoutParams();
                params1.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) layoutYouVoted.getLayoutParams();
                params3.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;

                kevin_bottom.setVisibility(View.VISIBLE);
                kevin_top.setVisibility(View.VISIBLE);

                break;

            case 2:

                mainMiddle.setVisibility(View.VISIBLE);
                mainMiddle.animate()
                        .translationY(150)
                        .setDuration(400)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mainMiddle.animate()
                                        .translationY(0)
                                        .setDuration(200)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);

                                            }
                                        });

                            }
                        });

                break;
            case 3:

                if(turnData.getName().equals(main.me.getName())) {
                    textWaitingForXToReveal.setVisibility(View.GONE);
                    btnReveal.setVisibility(View.VISIBLE);
                }
                else{
                    textWaitingForXToReveal.setVisibility(View.VISIBLE);
                    btnReveal.setVisibility(View.GONE);

                    main.setReady(true);
                    if (main.me.getHosting()) main.checkReadyStatus("revealsNext");
                }

                layoutBtnReveal.setVisibility(View.VISIBLE);
                break;

            case 4:

                /*textIsInFact.setVisibility(View.INVISIBLE);
                layoutBtnReveal.setVisibility(View.INVISIBLE);

                textIsInFact.setVisibility(View.GONE);
                layoutBtnReveal.setVisibility(View.GONE);*/


                break;

            case 5:

                voteListsLayout.setVisibility(View.VISIBLE);
                voteListsLayout.animate()
                        .translationY(1000)
                        .setDuration(0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                kevin_bottom.setVisibility(View.GONE);
                                kevin_top.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                                voteListsLayout.animate()
                                        .translationY(0)
                                        .setDuration(1000)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);

                                                if(turnData.getWasInFact().equals("T")) {
                                                    listLies.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey), android.graphics.PorterDuff.Mode.MULTIPLY);
                                                    listLies.setAlpha(0.8f);
                                                    textNumberLie.setTextColor(getResources().getColor(R.color.grey));
                                                    textVotedLie.setTextColor(getResources().getColor(R.color.grey));
                                                }
                                                else {
                                                    listTrues.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey), android.graphics.PorterDuff.Mode.MULTIPLY);
                                                    listTrues.setAlpha(0.8f);
                                                    textNumberTrue.setTextColor(getResources().getColor(R.color.grey));
                                                    textVotedTrue.setTextColor(getResources().getColor(R.color.grey));
                                                }

                                                readOutAchievements(turnData.getAchievementsUnlocked().get(main.me.getName()), 6);

                                            }
                                        });

                            }
                        });

                break;

            case 6:

                if(textScores.getText().equals("")){

                    if(main.meTemp.getVotes().get(turnData.getTurnNumber() - 1).equals("X"))
                    {
                        textScores.setText("You didn't vote in time and scored nothing this turn");
                    }
                    else
                    {
                        textScores.setText("You scored nothing this turn");
                    }
                    mainMiddleRight.setVisibility(View.VISIBLE);
                }

                main.setReady(true);
                if(main.me.getHosting()) main.checkReadyStatus("allowReady");

                break;

            default: // caseCycler = -1;

        }
            

        }


    private void readOutAchievements(ArrayList<Achievement> achievements, int gravity) {

        int numberOfAchievements = achievements.size();
        Iterable<Achievement> iterable = (Iterable<Achievement>) achievements;

        iterator = iterable.iterator();

        if (numberOfAchievements == 0) {
            setViewVis(6);
        } else {

            handler.postDelayed(postPopup, 0);
        }

    }

    Iterator<Achievement> iterator;
    Handler handler = new Handler();
    Runnable postPopup = new Runnable() {
        @Override
        public void run() {

            inflatePointsPopup(iterator.next(),6);

            if(iterator.hasNext())
            {
                handler.postDelayed(postPopup, 2000);
            }
            else
            {
                setViewVis(6);
            }

        }
    };


    private void inflatePointsPopup(Achievement a,int yOffsetFactor) {

        addToScoresText(
                (a.toSimpleString()));
        addToScoresText("");

        Log.i(TAG, "inflatePointsPopup: MAKING POPUP: "+a.getPointsWorth()+a.getName()+a.getMessage());

        int offset;

        if(yOffsetFactor >= 1 && yOffsetFactor <= 7){

            double yFactor = ((double)(yOffsetFactor)) / 8;
            // int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
            offset = (int) (((double)v) * yFactor);
        }else
        {
            offset = 0;
        }

        String pointsText, nameText, msgText;
        pointsText = a.getPointsString();
        nameText = a.getName();
        msgText = a.getMessage();

        int yOffsetPoints, yOffsetName, yOffsetMsg;
        yOffsetPoints = -135 + offset;
        yOffsetName = -55 + offset;
        yOffsetMsg = 0 + offset;

        RelativeLayout toastPoints, toastName, toastMsg;
        TextView textViewPoints, textViewName, textViewMsg;

        Typeface pointsFont = ResourcesCompat.getFont(main, R.font.showg);
        Typeface nameFont = ResourcesCompat.getFont(main, R.font.brlnsdb);
        Typeface msgFont = ResourcesCompat.getFont(main, R.font.lapsus_pro_bold);

        LayoutInflater inflater = LayoutInflater.from(main);
        toastPoints = (RelativeLayout) inflater.inflate(R.layout.layout_toast, mainLayout, false);
        toastName = (RelativeLayout) inflater.inflate(R.layout.layout_toast, mainLayout, false);
        toastMsg = (RelativeLayout) inflater.inflate(R.layout.layout_toast, mainLayout, false);
        toastPoints.setId(View.generateViewId());
        toastName.setId(View.generateViewId());
        toastMsg.setId(View.generateViewId());

        textViewPoints = (TextView) toastPoints.findViewById(R.id.messageTextView);
        textViewName = (TextView) toastName.findViewById(R.id.messageTextView);
        textViewMsg = (TextView) toastMsg.findViewById(R.id.messageTextView);
        textViewPoints.setText(pointsText);
        textViewName.setText(nameText);
        textViewMsg.setText(msgText);
        textViewPoints.setTextColor(getResources().getColor(R.color.white));
        textViewName.setTextColor(getResources().getColor(R.color.white));
        textViewMsg.setTextColor(getResources().getColor(R.color.white));
        textViewPoints.setTextSize(36);
        textViewName.setTextSize(24);
        textViewMsg.setTextSize(18);
        textViewPoints.setShadowLayer(5, 0, 0, Color.parseColor("#000000"));
        textViewName.setShadowLayer(5, 0, 0, Color.parseColor("#000000"));
        textViewMsg.setShadowLayer(5, 0, 0, Color.parseColor("#000000"));
        textViewPoints.setTypeface(pointsFont);
        textViewName.setTypeface(nameFont);
        textViewMsg.setTypeface(msgFont);

        formatBlurViews(toastPoints);
        formatBlurViews(toastName);
        formatBlurViews(toastMsg);

        mainLayout.addView(toastPoints);
        mainLayout.addView(toastName);
        mainLayout.addView(toastMsg);

        toastPoints.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        toastName.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        toastMsg.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        toastPoints.setY(yOffsetPoints);
        toastName.setY(yOffsetName);
        toastMsg.setY(yOffsetMsg);

        animatePointsView(toastPoints, 0);
        animatePointsView(toastName, 100);
        animatePointsView(toastMsg, 200);
    }

    private void formatBlurViews(RelativeLayout toast) {

        TextView text = (TextView) toast.findViewById(R.id.messageTextView);
        TextView blurViewLeft = (TextView) toast.findViewById(R.id.blurViewLeft);
        TextView blurViewRight = (TextView) toast.findViewById(R.id.blurViewRight);

        blurViewLeft.setText(text.getText());
        blurViewRight.setText(text.getText());

        blurViewLeft.setTextColor(text.getTextColors().getDefaultColor());
        blurViewRight.setTextColor(text.getTextColors().getDefaultColor());

        blurViewLeft.setTypeface(text.getTypeface());
        blurViewRight.setTypeface(text.getTypeface());

        blurViewLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, text.getTextSize());
        blurViewRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, text.getTextSize());

    }

    private void animatePointsView(RelativeLayout toast, int delay) {

        final int DURATION = 2000;

        ObjectAnimator pop, rise, exit, fade;

        pop = new ObjectAnimator();
        pop.setFloatValues(0.5f, 1);
        pop.setDuration(300);
        pop.setStartDelay(0 + delay);
        pop.setInterpolator(new OvershootInterpolator(2));
        /*pop.addListener(new AnimatorListenerAdapter() {

        });*/
        pop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                toast.setScaleX(t);
                toast.setScaleY(t);
            }
        });

        rise = new ObjectAnimator();
        rise.setIntValues( (int) toast.getY(), ((int) toast.getY()) - 50);
        rise.setDuration(DURATION);
        rise.setStartDelay(0 + delay);
        rise.setInterpolator(null);
        rise.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int t = (int) animation.getAnimatedValue();
                toast.setY(t);
            }
        });

        exit = new ObjectAnimator();
        exit.setIntValues((int) toast.getX(), (int) (toast.getX() + h));
        exit.setDuration(400);
        exit.setStartDelay(DURATION - 400 + delay);
        exit.setInterpolator(new AnticipateInterpolator(1));
        exit.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int t = (int) animation.getAnimatedValue();
                toast.setX(t);
            }
        });

        fade = new ObjectAnimator();
        fade.setFloatValues(1,0);
        fade.setDuration(400);
        fade.setStartDelay(DURATION - 400 + delay);
        fade.setInterpolator(new AccelerateInterpolator(3));
        fade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = (float) animation.getAnimatedValue();
                toast.setAlpha(t);
            }
        });
        fade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mainLayout.removeView(toast);
            }
        });

        pop.start();
        rise.start();
        exit.start();
        fade.start();
    }


    class VoteRegisterListAdapter extends ArrayAdapter<Voter> {

        public VoteRegisterListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Voter> voters) {
            super(context, resource, voters);
            this.context = context;
            this.list = voters;
        }

        private Context context;
        private List<Voter> list;

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = inflater.inflate(R.layout.gp7_reveals_list_item,null);

            TextView textName = (TextView) v.findViewById(R.id.text_player_name_reveals_list);
            // TextView textTorL = (TextView) v.findViewById(R.id.text_true_or_lie_reveals_item);
            TextView textInTime = (TextView) v.findViewById(R.id.text_in_time);
            TextView textFastestSymbol = (TextView) v.findViewById(R.id.text_fast_symbol);

            //Log.i(TAG, "getView: NAME: "+list.get(position).getName());
            //Log.i(TAG, "getView: TorF: "+(list.get(position).getTruth() ? "T" : "F"));

            textName.setText(list.get(position).getName());

            // Only set fastest if multiple
            textFastestSymbol.setVisibility(View.GONE);
            if(list.size() > 1){
                if(list.get(position).getFastestAndCorrect()){
                    textFastestSymbol.setVisibility(View.VISIBLE);
                }
            }

            textInTime.setText("in "
                    +((main.room.getSettings().getRoundTimeMins()*60) - list.get(position).getTimeLeft())
                    +"s ");

            /*
            TextView textPlayerName = (TextView) v.findViewById(R.id.text_player_name_here);
            TextView textIsYou = (TextView) v.findViewById(R.id.text_you_or_not);
            TextView textIsHost = (TextView) v.findViewById(R.id.text_host_or_not);
            TextView textPoints = (TextView) v.findViewById(R.id.text_points);

            textPlayerName.setText(list.get(position).getName());

            if(list.get(position).getName().equals(main.me.getName())) textIsYou.setText("You");
            else textIsYou.setVisibility(View.GONE);

            if(list.get(position).getHosting()) textIsHost.setText("HOST");
            else textIsHost.setVisibility(View.GONE);

            textPoints.setText(Integer.toString(list.get(position).getPoints()));*/

            return v;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public GamePhase7RevealsFragment() {
        // Required empty public constructor
    }

    /*// TO-DO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TO-DO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;*/

    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    private OnFragmentInteractionListener mListener;

// TO-DO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TO-DO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}interface TogglePanel{
    void togglePannelVis(boolean show);
}

