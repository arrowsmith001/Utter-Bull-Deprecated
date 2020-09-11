package com.arrowsmith.llv1;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.os.CountDownTimer;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.Achievement;
import com.arrowsmith.llv1.classes.FloatingToast;
import com.arrowsmith.llv1.classes.Manifest;
import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.RoleAssigner;
import com.arrowsmith.llv1.classes.SemiSwipeableViewPager;
import com.arrowsmith.llv1.classes.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.NullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.arrowsmith.llv1.MainActivity.TAG;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;


/**
 * A simple {@link Fragment} subclass.
 */
public class GamePhase6RevealsBaseFragment extends Fragment{


    private View view;
    private MainActivity main;
    SemiSwipeableViewPager viewPager;
    private TabLayout tabLayout;
    private ValueEventListener stateListener;
    private ConstraintLayout layoutButtons;
    private Button btnLeft;
    private Button btnCenter;
    private Button btnRight;

    private int caseCycler;
    private Button btnProgress;
    private ConstraintLayout mainLayout;


    public GamePhase6RevealsBaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.gp6_reveals_results_base_fragment, container, false);
        main = (MainActivity) getActivity();

        mainLayout = view.findViewById(R.id.layoutRevealsBase);

        viewPager = view.findViewById(R.id.pager);
        //tabLayout = (TabLayout) view.findViewById(R.id.tabs_container);

        // Button panel
        layoutButtons = view.findViewById(R.id.layoutButtonPanel);

            btnLeft = view.findViewById(R.id.button_panel_left);
            btnCenter = view.findViewById(R.id.button_panel_center);
            btnRight = view.findViewById(R.id.button_panel_right);

        //layoutButtons.setVisibility(View.GONE);

        btnLeft.setVisibility(View.GONE);
        btnCenter.setVisibility(View.GONE);
        btnRight.setVisibility(View.GONE);

        /*btnProgress = (Button) view.findViewById(R.id.button_progress_stage);
        btnProgress.setOnClickListener(v -> {

            progressCurrentPageStage();

        });*/

        // Constructs pager adapter
        main.pagerAdapter = new ViewPagerAdapter(
                getChildFragmentManager(),ViewPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                main.interpreter
        );

         // Constructs pager
        viewPager.setAdapter(main.pagerAdapter);
        viewPager.setOffscreenPageLimit(viewPager.getAdapter().getCount());
        main.pagerAdapter.notifyDataSetChanged();

        btnLeft.setOnClickListener(v -> {

            viewPager.setCurrentItem(max(
                    0,
                    viewPager.getCurrentItem() - 1));

        });

        btnCenter.setOnClickListener(v -> {

            viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);

        });

        btnRight.setOnClickListener(v -> {

            btnRight.setEnabled(false);
            btnRight.setText("Waiting for other players to ready up");
            btnRight.setTextSize(18);

            main.setReady(true);

            if(viewPager.getCurrentItem() == (viewPager.getAdapter().getCount() - 2)){

                if(main.me.getHosting()) main.checkReadyStatus("showResults");

            }else{

                if(main.me.getHosting()) main.checkReadyStatus("nextPage");
            }

        });

        caseCycler = 0;
        currentPage = 0;

        // Create state listener
        createStateListener();

        beginningUpToReveal();

        // DEBUG FLAG
        // viewPager.setSwipingUnlocked(true);

        // Set listener to respond to page changes -> button names/functions
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


                if(position == viewPager.getAdapter().getCount() - 1)
                {
                    btnRight.setEnabled(false);
                    btnCenter.getBackground().setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.MULTIPLY);
                    btnCenter.setText("NEW ROUND");
                    btnCenter.setOnClickListener(v -> {

                        btnCenter.setEnabled(false);
                        // viewPager.setSwipingUnlocked(false);
                        btnLeft.setVisibility(View.GONE);
                        btnRight.setVisibility(View.GONE);
                        btnCenter.setVisibility(View.GONE);

                        TextView textWaiting = (TextView) view.findViewById(R.id.textResultsButtonPanelWaiting);
                        textWaiting.setVisibility(View.VISIBLE);

                        main.setReady(true);
                        if(main.me.getHosting()) main.checkReadyStatus("newRound");

                    });
                }
                else
                {
                    btnRight.setEnabled(true);
                    btnCenter.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
                    btnCenter.setText("RESULTS");
                    btnCenter.setOnClickListener(v -> {

                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);

                    });

                    if(viewPager.isSwipingUnlocked()) {
                        ((ViewPagerAdapter)viewPager.getAdapter()).getFragment(position).setTrueWrittenBy();
                    }
                }

                if(position == 0)
                {
                    btnLeft.setEnabled(false);
                }
                else
                {
                    btnLeft.setEnabled(true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }


    public void setPage(int position)
    {
        viewPager.setCurrentItem(position);
    }

    int h, v;

    private void beginningUpToReveal() {

        // Introduced to page
        new CountDownTimer(2000,2000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                // Push views to top
                progressCurrentPageStage(1);

                new CountDownTimer(500,500){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {

                        // Middle bit appears
                        progressCurrentPageStage(2);

                        new CountDownTimer(1000,1000){

                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {

                                // Reveal button / status appears
                                progressCurrentPageStage(3);

                            }
                        }.start();

                    }
                }.start();

            }
        }.start();

    }

    GamePhase7RevealsFragment gp7;

    private void onRevealsNext() {

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        gp7 = (GamePhase7RevealsFragment) adapter.getFragment(viewPager.getCurrentItem());

        gp7.setRevealedValues();

        /*ArrayList<Achievement> achievementsOnReveal = gp7.getAchievedOnReveal();
        int numAchievementsOnReveal = achievementsOnReveal.size();

        Iterator<Achievement> iterator = ((Iterable<Achievement>) achievementsOnReveal).iterator();*/


        new CountDownTimer(1000,1000){

            @Override
            public void onTick(long millisUntilFinished) {

                // if(iterator.hasNext()) inflatePointsPopup(iterator.next(),5);

            }

            @Override
            public void onFinish() {

                // if(iterator.hasNext()) inflatePointsPopup(iterator.next(),5);

                // Hide reveal button / text, bring in votes
                progressCurrentPageStage(4);

               new CountDownTimer(500,500){

                   @Override
                   public void onTick(long millisUntilFinished) {

                   }

                   @Override
                   public void onFinish() {

                       progressCurrentPageStage(5);
                       // afterRevealsNext();

                   }
               }.start();

            }
        }.start();



    }

    private void afterRevealsNext() {

        ArrayList<Achievement> achievementsOnVotelist = gp7.getAchievedOnVotelist();
        int numAchievedOnVotelist = achievementsOnVotelist.size();

        Iterator<Achievement> iterator = ((Iterable<Achievement>) achievementsOnVotelist).iterator();

        new CountDownTimer(1000,1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {


                new CountDownTimer(2000*numAchievedOnVotelist,2000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                        //if(iterator.hasNext()) inflatePointsPopup(iterator.next(),6);

                    }

                    @Override
                    public void onFinish() {

                        if(iterator.hasNext()) {

                            new CountDownTimer(2000,2000){

                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {

                                    progressCurrentPageStage(6);
                                }
                            }.start();

                            //inflatePointsPopup(iterator.next(),6);
                        }else
                        {
                            progressCurrentPageStage(6);
                        }

                    }
                }.start();

            }
        }.start();

    }

    private void progressCurrentPageStage(int stage) {

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        GamePhase7RevealsFragment gp7 = (GamePhase7RevealsFragment) adapter.getFragment(viewPager.getCurrentItem());

        //caseCycler++;
        gp7.setViewVis(stage);

    }

    /*@Override
    public void togglePannelVis(boolean show){

        if(show) {
            layoutButtons.setVisibility(View.VISIBLE);
        }
        else{
            layoutButtons.setVisibility(View.GONE);

        }

    }

*/
    private void beginResultsPhase() {

        Log.i(TAG, "beginResultsPhase: CALLED");

        btnRight.setOnClickListener(v -> {

            viewPager.setCurrentItem(min(
                    viewPager.getAdapter().getCount() - 1,
                    viewPager.getCurrentItem() + 1));

        });
        btnRight.getLayoutParams().width = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        btnRight.setTextSize(16);
        btnRight.setText("NEXT");

        btnRight.setVisibility(View.VISIBLE);
        btnCenter.setVisibility(View.VISIBLE);
        btnLeft.setVisibility(View.VISIBLE);

        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);

        // Set list of view pager adapter
        ((ViewPagerAdapter) viewPager.getAdapter()).getResultsFragment().setLists();

        viewPager.setSwipingUnlocked(true);

    }

    private void proceedToNextPage() {

    }

    private void createStateListener() {
        stateListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: STATE CHANGED");

                if (!((Boolean) dataSnapshot.child("inSession").getValue())) {
                    main.myRoomRef.child("state").removeEventListener(this);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
                    navController.navigate(R.id.action_gamePhase6RevealsBaseFragment_to_waitingRoom);
                    Toast toast = new Toast(getActivity());
                    toast.makeText(
                            getActivity(), "Player exited the round, kicked back to the lobby", Toast.LENGTH_SHORT)
                            .show();
                }

                if (dataSnapshot.child("phase").getValue() != null) {

                    String state = (String) dataSnapshot.child("phase").getValue();

                    if (!state.equals(main.currentState)) {
                        Log.i(TAG, "STATE CHANGE: " + main.currentState + " -> " + state);
                        main.currentState = state;

                        switch (state) {

                            case "revealsNext":

                                onRevealsNext();

                                break;

                            case "allowReady":

                                onAllowReady();

                                break;
                            case "nextPage":

                                onNextPage();

                                break;
                            case "showResults":

                                beginResultsPhase();

                                break;
                            case "newRound":

                                onNewRound();

                                break;
                            case "downloadPlayers":

                                Log.i(TAG, "onDataChange: CASE "+state.toUpperCase()+" DETECTED");
                                onDownloadPlayers();

                                break;
                            case "loading":

                                Log.i(TAG, "onDataChange: CASE "+state.toUpperCase()+" DETECTED");
                                onLoading();

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

    private int currentPage;

    private void onNextPage() {

        btnRight.setEnabled(true);
        btnRight.setText("READY");
        btnRight.setTextSize(24);
        btnRight.setVisibility(View.GONE);

        caseCycler = 0;

        int nextPage = (viewPager.getCurrentItem()) + 1;

        Log.i(TAG, "onNextPage: NEXT PAGE SHOULD BE: "
                +min(
                viewPager.getAdapter().getCount(),
                nextPage));

        viewPager.setCurrentItem(min(
                viewPager.getAdapter().getCount() - 1,nextPage));

        /*currentPage++;
        viewPager.setCurrentItem(currentPage);*/

        viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                beginningUpToReveal();

                viewPager.getAdapter().unregisterDataSetObserver(this);
            }
        });
        viewPager.getAdapter().notifyDataSetChanged();


    }

    private void onAllowReady() {


        btnRight.setVisibility(View.VISIBLE);
        //togglePannelVis(true);

    }

    /**
     * Take actions to reset for a new round
     */
    private void onNewRound() {
        Log.i(TAG, "onNewRound: CALLED");

        // RESET LOCAL INFO
        main.resetLocalVarsForNewround();

        if(main.me.getHosting()){
            Log.i(TAG, "onNewRound: HOST SCENARIO EVOKED");

            RoleAssigner ra = new RoleAssigner(main.playersList);
            ra.setAllTrueAllowed(main.room.getSettings().isAllTrueEnabled());
            ra.assignRoles();
            main.playersList = ra.getPlayersList();

            upLoadPlayersWithRoles();

        }else{
            Log.i(TAG, "onNewRound: GUEST SCENARIO EVOKED");
            main.setReady(true);
        }
    }

    private void upLoadPlayersWithRoles() {

        Log.i(TAG, "upLoadPlayersWithRoles: CALLED");


        main.myRoomRef.child("players").setValue(main.playersList, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                purgeDatabase();

            }
        });
    }

    private void purgeDatabase() {

        Log.i(TAG, "purgeDatabase: CALLED");

        HashMap<String, Object> nullifier = new HashMap<>();

        nullifier.put("playersTextManifest", null);
        nullifier.put("playersTimesManifest", null);
        nullifier.put("playersTruthManifest", null);
        nullifier.put("playersVotesManifest", null);
        nullifier.put("whoseTurn", null);

        main.myRoomRef.updateChildren(nullifier, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                Log.i(TAG, "onComplete: CHECKING READY STATUS FOR DOWNLOAD PLAYERS");

                main.setReady(true);
                main.checkReadyStatus("downloadPlayers");
            }
        });

    }

    private void onDownloadPlayers() {

        if(main.me.getHosting()){

            main.setReady(true);
            main.checkReadyStatus("loading");

        }else{

            main.myRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener(){

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: PLAYERS DATA CHANGED");

                    // Gets Player objects in list
                    GenericTypeIndicator<ArrayList<Player>> t = new GenericTypeIndicator<ArrayList<Player>>() {};
                    ArrayList<Player> players = dataSnapshot.getValue(t);
                    main.playersList = players;

                    main.me.setTarget(null);
                    main.me.setTruth(null);
                    main.me.setText(null);
                    main.me.setVotes(null);

                    int meIndex = -1;
                    boolean meFound = false;
                    Iterator<Player> iterator = players.iterator();

                    while(iterator.hasNext() && !meFound)
                    {
                        meIndex++;
                        if(players.get(meIndex).getName().equals(main.me.getName())){
                            meFound = true;
                        }
                    }

                    // Set me player
                    main.me.setTruth(players.get(meIndex).getTruth());
                    if(players.get(meIndex).getTarget() != null){
                        main.me.setTarget(players.get(meIndex).getTarget());
                    }

                    main.room.setPlayers(players);

                    //main.playersList = null;
                    main.playersLeftToPlay.clear();
                    main.playersLeftToPlay.addAll(main.playersList);
                    main.playersWhovePlayed.clear();
                    main.playersText = null;
                    main.whoseTurn = null;
                    main.roundContent = null;
                    main.currentState = null;

                    main.setReady(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void onLoading()
    {
        moveToNewRound();
    }

    private void moveToNewRound() {

        main.myRoomRef.child("state").removeEventListener(stateListener);

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_gamePhase6RevealsBaseFragment_to_gamePhase1DelegateRoles);
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

    /////////////////////////////////////////////////////////////////////////////////////////////////
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
    /////////////////////////////////////////////////////////////////////////////////////////////////


}


