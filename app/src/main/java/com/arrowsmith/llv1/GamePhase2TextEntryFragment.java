package com.arrowsmith.llv1;

import android.animation.LayoutTransition;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.LayoutWrapContentUpdater;
import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.QueryHelper;
import com.arrowsmith.llv1.classes.ReadyListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class GamePhase2TextEntryFragment extends Fragment
implements QueryHelper.QueryRetriever {

    private OnFragmentInteractionListener mListener;
    private View view;
    private MainActivity main;
    private Button btnGenerate;
    private EditText editText;
    private FirebaseFirestore fs;
    private ValueEventListener stateChangeListener;
    private ValueEventListener whoseTurnListener;
    private CollectionReference categoriesRef;
    private CollectionReference ideasRef;

    Button btnSubmit;
    private ValueEventListener playersReadyListener;
    private ChildEventListener childReadyStatusListener;
    private ArrayList<Player> playerUnReadyList;
    private ArrayList<Player> playerReadyList;
    private ArrayList<Player> playerFinalReadyList;
    private int numReady;
    private ListView listReady;
    private boolean listShowing;
    private Button btnShowHideList;
    private ScrollView listReadyContainer;

    public GamePhase2TextEntryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.gp2_text_entry_fragment, container, false);

        main = (MainActivity) getActivity();

        // Get Firestore instance
        fs = FirebaseFirestore.getInstance();

        main.playersLeftToPlay = new ArrayList<>(main.playersList.size());
        main.playersLeftToPlay.addAll(main.playersList);

        main.playersWhovePlayed = new ArrayList<>();

        // Update "me" object
        boolean selfFound = false;
        Iterator<Player> iterator = main.playersList.iterator();
        int index = -1;
        while(iterator.hasNext() && !selfFound){
            index++;
            if(iterator.next().getName().equals(main.me.getName())){
                main.me = main.playersList.get(index);
                selfFound = true;
            }
        }

        // Initialise text views
        TextView text1 = view.findViewById(R.id.textTitle1); // Write a
        TextView text2 = view.findViewById(R.id.textTitle2); // Truth/Lie
        TextView text3 = view.findViewById(R.id.textTitle3); // About/For
        TextView text4 = view.findViewById(R.id.textTitle4); // Yourself/Target

        // Set title text
        if (main.me.getTruth()) {
            text2.setText("TRUTH");
            text3.setText("about");
            text4.setText("yourself!");
        } else{
            text2.setText("LIE");
            text3.setText("for");
            text4.setText(main.me.getTarget() + "!");
        }

        initialiseViews();

        // DEBUG FLAG - DEFAULT SUBMISSION
        String defaultText = (String) text2.getText()+" "+text3.getText()+" "+text4.getText();
        if(main.me.getTruth()) editText.setText(defaultText);
        else editText.setText(defaultText + " written by "+main.me.getName());
        // DEBUG FLAG - DEFAULT SUBMISSION

        // Initial layout state

        layoutPreSubmission.setVisibility(View.VISIBLE);
        layoutWhenSubmitted.setVisibility(View.GONE);

        layoutPreMainSubmission.setVisibility(View.VISIBLE);
        layoutPreMainSuggestion.setVisibility(View.GONE);

        btnNewIdea.setEnabled(false);
        btnNewIdea.setTextColor(getResources().getColor(R.color.text_disabled));
        btnUseIdea.setEnabled(false);
        btnUseIdea.setTextColor(getResources().getColor(R.color.text_disabled));

        // Disable submit button for now
        btnSubmit.setEnabled(false);
        btnSubmit.setTextColor(getResources().getColor(R.color.text_disabled));

        //DEBUG FLAG - ENABLE SUBMIT IMMEDIATELY
        btnSubmit.setEnabled(true);
        btnSubmit.setTextColor(getResources().getColor(R.color.text_red));
        //DEBUG FLAG

        setButtonListeners();

        setUpPlayerReadyListener();

        return view;
    }

    private void setUpPlayerReadyListener() {

        numReady = 0;
        numPlayersTotal.setText(Integer.toString(main.playersList.size()));

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

        setListAdapter();
    }

    private void setListAdapter() {

        listShowing = false;

        // Set ready list listener
        btnShowHideList.setOnClickListener(v -> {

            if(listShowing) // REVERT TO THESE SETTINGS IF UNREADY CLICKED DIRECTLY
            {
                onHideList();
            }
            else
            {
                listReadyContainer.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);

                // Make top layouts WRAP
                ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) layoutPreMainSubmission.getLayoutParams();
                params2.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                layoutPreMainSubmission.setLayoutParams(params2);
                ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) layoutPreMain.getLayoutParams();
                params3.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                layoutPreMain.setLayoutParams(params3);
                ConstraintLayout.LayoutParams params4 = (ConstraintLayout.LayoutParams) layoutPreSubmission.getLayoutParams();
                params4.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                layoutPreSubmission.setLayoutParams(params4);

                // Make parent layout MATCH
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layoutWhenSubmitted.getLayoutParams();
                params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
                layoutWhenSubmitted.setLayoutParams(params);

                listShowing = true;
            }
        });

        ReadyListAdapter adapter = new ReadyListAdapter(getActivity(),0,playerFinalReadyList, "READY");
        listReady.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void onHideList() {

        listReadyContainer.setVisibility(View.GONE);
        editText.setVisibility(View.VISIBLE);

        // Make parent layout WRAP
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layoutWhenSubmitted.getLayoutParams();
        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        layoutWhenSubmitted.setLayoutParams(params);

        // Make top layouts MATCH
        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) layoutPreMainSubmission.getLayoutParams();
        params2.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        layoutPreMainSubmission.setLayoutParams(params2);
        ConstraintLayout.LayoutParams params3 = (ConstraintLayout.LayoutParams) layoutPreMain.getLayoutParams();
        params3.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        layoutPreMain.setLayoutParams(params3);
        ConstraintLayout.LayoutParams params4 = (ConstraintLayout.LayoutParams) layoutPreSubmission.getLayoutParams();
        params4.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        layoutPreSubmission.setLayoutParams(params4);

        listShowing = false;

    }

    private void updateReadyStatusList(DataSnapshot dataSnapshot, boolean trueAddFalseRemove) {

        if(listReady.getAdapter() != null)
        {
            if(trueAddFalseRemove)
            {
                int index = -1;
                boolean playerFound = false;
                while(!playerFound)
                {
                    index++;
                    try{
                        if(playerUnReadyList.get(index).getName().equals(dataSnapshot.getKey())) playerFound = true;
                    }catch(IndexOutOfBoundsException e)
                    {
                        return;
                    }
                }

                Player newReadyPlayer = playerUnReadyList.get(index);
                newReadyPlayer.setReady(true);

                playerUnReadyList.remove(newReadyPlayer);
                playerReadyList.add(newReadyPlayer);
            }
            else
            {
                int index = -1;
                boolean playerFound = false;
                while(!playerFound)
                {
                    index++;
                    try{
                    if(playerReadyList.get(index).getName().equals(dataSnapshot.getKey())) playerFound = true;
                    }catch(IndexOutOfBoundsException e)
                    {
                        return;
                    }
                }

                Player newUnreadyPlayer = playerReadyList.get(index);
                newUnreadyPlayer.setReady(false);

                playerReadyList.remove(newUnreadyPlayer);
                playerUnReadyList.add(newUnreadyPlayer);
            }


            playerFinalReadyList.clear();

            playerFinalReadyList.addAll(playerReadyList);
            playerFinalReadyList.addAll(playerUnReadyList);

            ((ReadyListAdapter) listReady.getAdapter()).notifyDataSetChanged();
        }
    }

    private void setButtonListeners() {

        // Set listener on submit button
        btnSubmit.setOnClickListener(v -> {

            layoutPreButtons.setVisibility(View.GONE);
            layoutWhenSubmitted.setVisibility(View.VISIBLE);

            // TODO: Fix callback for text entry submission

            submitEntry(editText.getText().toString());
            textYouSubmittedContent.setText(editText.getText().toString());

        });

        // Set listener for empty EditText to disable/re-enable submit button
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()==0)
                {
                    btnSubmit.setEnabled(false);
                    btnSubmit.setTextColor(getResources().getColor(R.color.text_disabled));
                }
                else
                {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setTextColor(getResources().getColor(R.color.text_red));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set listener on hint generator
        btnGenerate.setOnClickListener(v ->
        {
            btnGenerate.setEnabled(false);
            btnGenerate.setTextColor(getResources().getColor(R.color.text_disabled));
            btnSubmit.setEnabled(false);
            btnSubmit.setTextColor(getResources().getColor(R.color.text_disabled));

            btnNewIdea.setEnabled(false);
            btnNewIdea.setTextColor(getResources().getColor(R.color.text_disabled));
            btnUseIdea.setEnabled(false);
            btnUseIdea.setTextColor(getResources().getColor(R.color.text_disabled));

            layoutPreMainSuggestion.setVisibility(View.VISIBLE);

            queryIdeas();
        });

        // Set suggestions sub pane button listeners
        btnNewIdea.setOnClickListener(v ->
        {
            btnNewIdea.setEnabled(false);
            btnNewIdea.setTextColor(getResources().getColor(R.color.text_disabled));
            btnUseIdea.setEnabled(false);
            btnUseIdea.setTextColor(getResources().getColor(R.color.text_disabled));
            handler.removeCallbacks(setTextRunnable);
            queryIdeas();
        });

        btnUseIdea.setOnClickListener(v ->
        {
            String text = textSuggestionContent.getText().toString();

            editText.setText("");
            editText.setHint("");

            if(main.me.getTruth())
            {
                Log.i(TAG, "setButtonListeners: main.me.truth: "+main.me.getTruth());
                editText.setHint(text);
            }
            else
            {
                Log.i(TAG, "setButtonListeners: main.me.truth: "+main.me.getTruth());
                editText.setText(text, TextView.BufferType.EDITABLE);
            }

            btnGenerate.setEnabled(true);
            btnGenerate.setTextColor(getResources().getColor(R.color.text_blue));
            layoutPreMainSuggestion.setVisibility(View.INVISIBLE);
            layoutPreMainSuggestion.setVisibility(View.GONE);
        });

        textDismissSuggestions.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                layoutPreMainSuggestion.setVisibility(View.INVISIBLE);
                layoutPreMainSuggestion.setVisibility(View.GONE);
                if(editText.getText().length() > 0)
                {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setTextColor(getResources().getColor(R.color.text_red));
                }

                btnGenerate.setEnabled(true);
                btnGenerate.setTextColor(getResources().getColor(R.color.text_blue));

                return false;
            }

        });

        btnUnready.setOnClickListener(v ->
        {
            main.setReady(null);

            onHideList();

            layoutWhenSubmitted.setVisibility(View.GONE);
            layoutPreButtons.setVisibility(View.VISIBLE);
        });
    }

    private ConstraintLayout layoutPreSubmission,layoutWhenSubmitted;
    private ConstraintLayout layoutPreMain, layoutPreButtons;
    private ConstraintLayout layoutPreMainSubmission, layoutPreMainSuggestion;
    private Button btnNewIdea, btnUseIdea, btnUnready;
    private TextView textSuggestionContent,textDismissSuggestions,textYouSubmittedContent,numPlayersReady,numPlayersTotal;

    private void initialiseViews() {

        // Initialise views

        layoutPreSubmission = (ConstraintLayout) view.findViewById(R.id.layoutPreSubmission);

            layoutPreMain = (ConstraintLayout) view.findViewById(R.id.layoutPreMain);

                layoutPreMainSubmission = (ConstraintLayout) view.findViewById(R.id.layoutPreMainSubmission);

                    editText = view.findViewById(R.id.editText_text);

                layoutPreMainSuggestion = (ConstraintLayout) view.findViewById(R.id.layoutPreMainSuggestion);

                    btnNewIdea = (Button) view.findViewById(R.id.button_entry_new_idea);
                    btnUseIdea = (Button) view.findViewById(R.id.button_entry_use_idea);
                    textSuggestionContent = (TextView) view.findViewById(R.id.text_entry_suggestion_content);
                    textDismissSuggestions = (TextView) view.findViewById(R.id.text_entry_dismiss_suggestions);

            layoutPreButtons = (ConstraintLayout) view.findViewById(R.id.layoutPreButtons);

                btnSubmit = view.findViewById(R.id.button_submit_entry);
                btnGenerate = view.findViewById(R.id.button_generate_hint);

        layoutWhenSubmitted = (ConstraintLayout) view.findViewById(R.id.layoutWhenSubmitted);

                textYouSubmittedContent = (TextView) view.findViewById(R.id.text_entry_you_have_submitted_content);
                btnUnready = (Button) view.findViewById(R.id.button_entry_unready);

            //layoutReadyTracker = (ConstraintLayout) view.findViewById(R.id.layoutReadyTracker);

                listReady = (ListView) view.findViewById(R.id.listPlayerReadyListTextEntry);
                listReadyContainer = (ScrollView) view.findViewById(R.id.scrollViewTextEntry);

                numPlayersReady = (TextView) view.findViewById(R.id.text_num_players_deciding);
                numPlayersTotal = (TextView) view.findViewById(R.id.text_num_players_voted);
                btnShowHideList = (Button) view.findViewById(R.id.button_show_hide_listview);

        // Set numPlayersTotal
        numPlayersTotal.setText(Integer.toString(main.playersList.size()));

        ArrayList<ConstraintLayout> layoutsToAnimate = new ArrayList<>();
        layoutsToAnimate.add(layoutPreSubmission);
        layoutsToAnimate.add(layoutPreMain);
        layoutsToAnimate.add(layoutPreMainSubmission);
        layoutsToAnimate.add(layoutPreMainSuggestion);
        layoutsToAnimate.add(layoutPreButtons);
        layoutsToAnimate.add(layoutWhenSubmitted);

        LayoutTransition lt;
        for(ConstraintLayout cl : layoutsToAnimate)
        {
            lt = new LayoutTransition();
            lt.enableTransitionType(LayoutTransition.CHANGING);
            lt.disableTransitionType(LayoutTransition.DISAPPEARING);
            lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            lt.setInterpolator(LayoutTransition.CHANGING,new TimeInterpolator(){
                @Override
                public float getInterpolation(float t) {
                    float T = 2;
                    return (float) ((T+1)*Math.pow(t-1,3) + T*Math.pow(t-1,2) + 1);
                }
            });
            cl.setLayoutTransition(lt);
        }

        /*layoutPreSubmission.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        layoutPreMain.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        layoutPreMainSubmission.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        layoutPreMainSuggestion.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        layoutPreButtons.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        layoutWhenSubmitted.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
*/
    }

    private void submitEntry(String text) {

        if(main.playersTextManifestRef == null) main.playersTextManifestRef = main.myRoomRef.child("playersTextManifest");

        // If player is truther-teller, set text to self in manifest
        if (main.me.getTruth()){
            main.me.setText(text);
            main.playersTextManifestRef.child("players").child(main.me.getName()).setValue(text, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    // Sets player status to "ready"
                    main.setReady(true);

                    // If player is hosting, they will begin checking for everybody's ready status
                    if(main.me.getHosting()) main.checkReadyStatus("getTextManifestInfo");

                    Log.i(TAG, "submitEntry: target is self");
                }
            });
        }
        // If player is liar, sets text of target to entered text
        else {
            main.playersTextManifestRef.child("players").child(main.me.getTarget()).setValue(text, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    // Sets player status to "ready"
                    main.setReady(true);

                    // If player is hosting, they will begin checking for everybody's ready status
                    if(main.me.getHosting()) main.checkReadyStatus("getTextManifestInfo");

                    Log.i(TAG, "submitEntry: target is "+main.me.getTarget());
                }
            });
        }
    }

    private void queryIdeas() {

        QueryHelper qh = new QueryHelper(main.me.getTruth(), fs, this);
        qh.setLewdnessAllowed(main.room.getSettings().isLewdnessEnabled());

        // DEBUG ONLY
        // qh.setCustomIdea(editText.getText().toString());
        // DEBUG ONLY

        qh.start();

    }

    private CharSequence text;
    private int index;
    private Handler handler = new Handler();

    private Runnable setTextRunnable = new Runnable() {
        @Override
        public void run() {
            index++;
            textSuggestionContent.setText(text.subSequence(0, index));
            if(index < text.length())
            {
                handler.postDelayed(this,5);
            }else if(index == text.length())
            {
                btnNewIdea.setEnabled(true);
                btnNewIdea.setTextColor(getResources().getColor(R.color.text_blue));
                btnUseIdea.setEnabled(true);
                btnUseIdea.setTextColor(getResources().getColor(R.color.text_red));
            }
        }
    };

    @Override
    public void setHintText(String text) {

        this.text = (CharSequence) text;
        index = 0;

        final Animation animation1 = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation1.setDuration(200); // duration - half a second
        animation1.setInterpolator(new AccelerateInterpolator()); // do not alter animation rate
        //animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        //animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

        final Animation animation2 = new AlphaAnimation(0, 1); // Change alpha from fully visible to invisible
        animation2.setDuration(200); // duration - half a second
        animation2.setInterpolator(new DecelerateInterpolator()); // do not alter animation rate
        //animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        //animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

        textSuggestionContent.startAnimation(animation1);
        handler.post(setTextRunnable);
        textSuggestionContent.startAnimation(animation2);
    }

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

        main = (MainActivity) getActivity();

        // Listens for end of "inSession" AND "textEntry_to_choose" transition phase
        stateChangeListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("inSession").getValue() != null)
                {
                    if(!((Boolean) dataSnapshot.child("inSession").getValue())){
                        main.myRoomRef.child("state").removeEventListener(this);
                        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
                        navController.navigate(R.id.action_textEntryFragment_to_waitingRoom);
                        Toast toast = new Toast(getActivity());
                        toast.makeText(
                                getActivity(),"Player exited the round, kicked back to the lobby",Toast.LENGTH_SHORT)
                                .show();
                    }
                }



                if (dataSnapshot.child("phase").getValue() != null) {

                    String state = (String) dataSnapshot.child("phase").getValue();

                    if(!state.equals(main.currentState)) {
                        Log.i(TAG, "STATE CHANGE: "+main.currentState+" -> "+state);
                        main.currentState = state;

                        switch (state) {
                            case "getTextManifestInfo":
                                // If we're prompted for getTextManifestInfo player phase...

                                // Take snapshot of current TEXT MANIFEST HASHMAP with all the information we'll need for
                                // play phase
                                main.myRoomRef.child("playersTextManifest").child("players").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        // Gets Text objects in a HashMap
                                        GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {
                                        };
                                        main.playersText = (HashMap<String, String>) dataSnapshot.getValue(t);

                                        Log.i(TAG, "onDataChange: PLAYERS TEXT MANIFEST LISTEN COMPLETE");
                                        Log.i(TAG, "onDataChange: main.me.getHosting(): " + main.me.getHosting());

                                        // Unless hosting, just ready up.
                                        // If host, whoseturn needs to be set and listen checker before readying up

                                        main.setReady(true);

                                        if (main.me.getHosting()) main.checkReadyStatus("choose");

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                break;

                            case "choose":

                                // If we're prompted for choose phase...

                                moveToChoose();

                   /* // Proceed to choose frag. If we're host, also clean up ready-ups
                    if(main.me.getHosting()){
                        main.myRoomRef.child("playersReadyManifest").setValue("", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                moveToChoose();
                            }
                        });

                    }else{
                        moveToChoose();
                    }*/
                                break;
                            default:
                                Log.i(TAG, "onDataChange: ERROR: Invalid state given to TextEntry state listener: "
                                        + (String) dataSnapshot.child("phase").getValue().toString());

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

                numReady++;
                numPlayersReady.setText(Integer.toString(numReady));
                numPlayersReady.setScaleX(0.5f);
                numPlayersReady.setScaleY(0.5f);
                numPlayersReady.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator(2))
                        .start();
                updateReadyStatusList(dataSnapshot, true);

                if(numReady == main.playersList.size())
                {
                    main.myRoomRef.child("playersReadyManifest").child("players").removeEventListener(this);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // Log.i(TAG, "onChildAdded: PLAYER READY STATUS AMENDED");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(numReady < main.playersList.size())
                {
                    numReady--;
                    numPlayersReady.setText(Integer.toString(numReady));
                    updateReadyStatusList(dataSnapshot, false);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();

        main.myRoomRef.child("state").addValueEventListener(stateChangeListener);
        main.myRoomRef.child("playersReadyManifest").child("players").addChildEventListener(childReadyStatusListener);
    }

    private void moveToChoose() {

        main.myRoomRef.child("state").removeEventListener(stateChangeListener);

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_textEntryFragment_to_chooseWhoseTurn);
    }

    @Override
    public void onPause() {
        super.onPause();

        main.myRoomRef.child("state").removeEventListener(stateChangeListener);
        main.myRoomRef.child("playersReadyManifest").child("players").removeEventListener(childReadyStatusListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;


    }


    /*// TO-DO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TO-DO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GamePhase2TextEntryFragment.
     *//*
    // TO-DO: Rename and change types and number of parameters
    public static GamePhase2TextEntryFragment newInstance(String param1, String param2) {
        GamePhase2TextEntryFragment fragment = new GamePhase2TextEntryFragment();
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
interface TextEntryInteraction {
    //void submitEntry(String text);
    void setReady(Boolean isReady);
}