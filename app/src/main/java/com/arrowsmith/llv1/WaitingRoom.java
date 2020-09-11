package com.arrowsmith.llv1;

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
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.MemberListAdapter;
import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.RoleAssigner;
import com.arrowsmith.llv1.classes.Room;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.arrowsmith.llv1.MainActivity.TAG;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.random;


public class WaitingRoom extends Fragment implements WaitingRoomInteraction{

    private OnFragmentInteractionListener mListener;
    private View view;
    MainActivity main;
    private ChildEventListener playersManifestChildrenListener, settingsListener;
    private ValueEventListener playersListener, roomHostListener, stateListener;
    private TextView textGameCode;
    private ListView membersList;
    private TextView textWaitingForPlayers;
    private Button btnBeginGame;

    public Player dataMe;
    private boolean leavingRoom;
    private Toast beginGameToast;
    private ConstraintLayout optionsPanel;
    private ConstraintLayout optionsTimer;
    private ConstraintLayout optionsAngels;
    private ConstraintLayout optionsLewdness;
    private EditText timerText;
    private Button timerAdd;
    private Button timerMinus;
    private ImageView imgAngels;
    private Switch switchAngels;
    private TextView textAngels;
    private ImageView imgLewdness;
    private Switch switchLewdness;
    private TextView textLewdness;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.fragment_waiting_room, container, false);

        main = (MainActivity) getActivity();
        leavingRoom = false; // To stop list updating upon leaving

        // Initialise views
        textGameCode = (TextView) view.findViewById(R.id.text_game_code_1);
        textWaitingForPlayers = (TextView) view.findViewById(R.id.text_waiting_for_players);
        btnBeginGame = (Button) view.findViewById(R.id.button_begin);
        membersList = (ListView) view.findViewById(R.id.list_members);

        // Options panel views
        optionsPanel = (ConstraintLayout) view.findViewById(R.id.layoutOptionsPanel);

        optionsTimer = (ConstraintLayout) view.findViewById(R.id.layoutOptionsTimer);

        timerText = (EditText) view.findViewById(R.id.edit_timer_number);
        timerAdd = (Button) view.findViewById(R.id.button_timer_add);
        timerMinus = (Button) view.findViewById(R.id.button_timer_minus);

        optionsAngels = (ConstraintLayout) view.findViewById(R.id.layoutOptionsAngels);

        imgAngels = (ImageView) view.findViewById(R.id.img_angels);
        switchAngels = (Switch) view.findViewById(R.id.switch_angels);
        textAngels = (TextView) view.findViewById(R.id.text_options_angels_title);

        optionsLewdness = (ConstraintLayout) view.findViewById(R.id.layoutOptionsLewdness);

        imgLewdness = (ImageView) view.findViewById(R.id.img_bubble);
        switchLewdness = (Switch) view.findViewById(R.id.switch_bubble);
        textLewdness = (TextView) view.findViewById(R.id.text_options_lewd_title);

        // Set room code
        String codeDisplay = "";
        for(Character c : main.myCode.getCode().toCharArray())
        {
            codeDisplay = codeDisplay + c.toString() + " ";
        }
        SpannableString codeDisplaySpan = new SpannableString(codeDisplay.substring(0,9));
        codeDisplaySpan.setSpan(new UnderlineSpan(), 6, 7, 0);
        codeDisplaySpan.setSpan(new UnderlineSpan(), 8, 9, 0);
        textGameCode.setText(codeDisplaySpan);

        // Unlock/lock hosting privileges based on host status
        if(main.me.getHosting()) unlockHosting(true);
        else unlockHosting(false);

        setButtons();

        // GET ROOM in one-off listener
        main.myRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot != null && dataSnapshot.getValue() != null)
                {
                    // This line of code blows my mind!! Praise Firebase
                    main.room = (Room) dataSnapshot.getValue(Room.class);

                    // Apply settings real quick
                    timerText.setText(Integer.toString(main.room.getSettings().getRoundTimeMins()));

                    if(main.room.getSettings().isAllTrueEnabled())
                    {
                        switchAngels.setChecked(true);
                        imgAngels.setImageDrawable(getResources().getDrawable(R.drawable.ic_angels));
                    }else
                    {
                        switchAngels.setChecked(false);
                        imgAngels.setImageDrawable(getResources().getDrawable(R.drawable.ic_angel));
                    }

                    if(main.room.getSettings().isLewdnessEnabled())
                    {
                        switchLewdness.setChecked(true);
                        imgLewdness.setImageDrawable(getResources().getDrawable(R.drawable.ic_lewdness_v2));
                    }else
                    {
                        switchLewdness.setChecked(false);
                        imgLewdness.setImageDrawable(getResources().getDrawable(R.drawable.ic_lewdness_off));
                    }

                    // Lobby list of players
                    updateList(true);

                    main.myRoomRef.removeEventListener(this);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //main.myRoomRef.child("refresh").setValue("");

        // Set up separate listeners for:
        // PLAYERS
        // PLAYERS MANIFEST
        // ROOM HOST
        // SETTINGS
        // STATE

        createListeners();

        return view;


    }

    private void setButtons() {

        btnBeginGame.setOnClickListener(v ->{

            main.playersManifestRef.child("players").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            int playersNumber = (int) dataSnapshot.getChildrenCount();

                            final int PLAYER_MIN = 2;

                            // DEBUG FLAG - NUMBER OF PLAYERS
                            if(playersNumber < PLAYER_MIN){
                                // Reject request to start game
                                if(beginGameToast != null) beginGameToast.cancel();
                                if(playersNumber == (PLAYER_MIN - 1)) {
                                    beginGameToast = Toast.makeText(getActivity(),
                                            "You need 1 more player for a game",Toast.LENGTH_SHORT);
                                    beginGameToast.show();
                                }else {
                                    beginGameToast = Toast.makeText(getActivity(),
                                            "You need "+Integer.toString(PLAYER_MIN - playersNumber)+" more players for a game",Toast.LENGTH_SHORT);
                                    beginGameToast.show();
                                }
                            }else{
                                // Updates inSession to true
                                main.myRoomRef.child("state").child("inSession").setValue(true,
                                        new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                if(playersNumber == main.playersList.size()){
                                                    // Manifest matches players list. All okay
                                                    main.playSound("boop");

                                                    // Set playerScores object moving forward
                                                    main.playersScores = new HashMap<>();

                                                    // Clear roles from playersList, add scores
                                                    for (Player p : main.playersList)
                                                    {
                                                        p.setTarget(null);
                                                        p.setTruth(null);

                                                        main.playersScores.put(p.getName(),p.getPoints());
                                                    }

                                                    RoleAssigner ra = new RoleAssigner(main.playersList);
                                                    ra.setAllTrueAllowed(main.room.getSettings().isAllTrueEnabled());
                                                    ra.assignRoles();
                                                    main.playersList = ra.getPlayersList();

                                                    changeFragStateToLoading();

                                                }else{
                                                    Log.i(TAG, "onComplete: ERROR: Manifest and Players list mismatch");
                                                }

                                            }
                                        });

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    }
            );

        });

        timerAdd.setOnClickListener(v -> {

            main.playSound("click");
            int newVal = Integer.parseInt(timerText.getText().toString()) + 1;
            if(newVal <= 10)
            {
                timerText.setText(Integer.toString(newVal));
                main.myRoomRef.child("settings").child("roundTimeMins").setValue(newVal);
                main.room.getSettings().setRoundTimeMins(newVal);
            }
        });

        timerMinus.setOnClickListener(v -> {

            main.playSound("click");
            int newVal = Integer.parseInt(timerText.getText().toString()) - 1;
            if(newVal >= 1)
            {
                timerText.setText(Integer.toString(newVal));
                main.myRoomRef.child("settings").child("roundTimeMins").setValue(newVal);
                main.room.getSettings().setRoundTimeMins(newVal);
            }
        });

        switchAngels.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                main.myRoomRef.child("settings").child("allTrueEnabled").setValue(isChecked);
                switchAngels.setEnabled(false);
                handler.post(delaySwitch1);
            }
        });

        switchLewdness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                main.myRoomRef.child("settings").child("lewdnessEnabled").setValue(isChecked);
                switchLewdness.setEnabled(false);
                handler.post(delaySwitch2);
            }
        });

    }

    Handler handler = new Handler();
    Thread delaySwitch1 = new Thread(new Runnable(){

        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(switchAngels.isChecked())
            {
                imgAngels.setImageDrawable(getResources().getDrawable(R.drawable.ic_angels));
                textAngels.setText("\"ALL TRUE\" ALLOWED");
                main.room.getSettings().setAllTrueEnabled(true);
            }else
            {
                imgAngels.setImageDrawable(getResources().getDrawable(R.drawable.ic_angel));
                textAngels.setText("\"ALL TRUE\" DISALLOWED");
                main.room.getSettings().setAllTrueEnabled(false);
            }

            main.playSound("settings_click");
            if(main.me.getHosting()) switchAngels.setEnabled(true);

        }
    });
    Thread delaySwitch2 = new Thread(new Runnable(){

        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(switchLewdness.isChecked())
            {
                imgLewdness.setImageDrawable(getResources().getDrawable(R.drawable.ic_lewdness_v2));
                textLewdness.setText("LEWD HINTS ENABLED");
                main.room.getSettings().setLewdnessEnabled(true);
            }else
            {
                imgLewdness.setImageDrawable(getResources().getDrawable(R.drawable.ic_lewdness_off));
                textLewdness.setText("LEWD HINTS DISABLED");
                main.room.getSettings().setLewdnessEnabled(false);
            }

            main.playSound("settings_click");
            if(main.me.getHosting()) switchLewdness.setEnabled(true);
        }
    });

    private void createListeners(){

        // NECESSARY LISTENERS

        // To be applied to room/players ref - listens for all relevant updates
        playersListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: PLAYERS DATA CHANGED");

                // Gets Player objects in list (and self object)
                GenericTypeIndicator<ArrayList<Player>> t = new GenericTypeIndicator<ArrayList<Player>>() {};
                ArrayList<Player> players = dataSnapshot.getValue(t);

                // Test for increase / decrease / no change
                Boolean playerAdded = null;
                if(main.playersList != null && players != null)
                {
                    if(main.playersList.size() < players.size()) playerAdded = true;
                    else if(main.playersList.size() > players.size()) playerAdded = false;
                }

                // Set new players list
                main.playersList = players;

                // Update local object
                if(players != null && main != null && main.room != null){
                    main.room.setPlayers(players);
                    if(!leavingRoom) updateList(playerAdded);
                }

                // Update me object (for change in hosting)
                boolean foundMe = false;
                Iterator<Player> iterator = ((Iterable<Player>) main.playersList).iterator();
                while(iterator.hasNext() && !foundMe)
                {
                    Player p = iterator.next();
                    if(p.getName().equals(main.me.getName()))
                    {
                        main.me = p;
                        foundMe = true;
                    }
                }

                // Set score manifest
                main.playersScores = new HashMap<>();
                for(Player p : main.playersList)
                {
                    main.playersScores.put(p.getName(),
                            p.getPoints() != null ? p.getPoints() : 0);
                }


                unlockHosting(main.me.getHosting());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // To be applied to ...state node
        stateListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: STATE CHANGED");

                int rolesNumber = 0;

                // Check if role info exists in playersList object
                for(Player p : main.playersList){
                    if(p.getTruth() != null) rolesNumber++;
                }

                if(dataSnapshot.child("phase").getValue() != null){

                    // If state is loading
                    if(dataSnapshot.child("phase").getValue().equals("loading")
                            && rolesNumber == main.playersList.size()){

                        moveToLoading();

                    }else // State is loading, but players' role info not received!
                        if(dataSnapshot.child("phase").getValue().equals("loading")
                                && rolesNumber != main.playersList.size()){
                            Log.i(TAG, "onDataChange: STATE LISTENER: playerData not received");
                        }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // To be applied to ...settings node
        // Interest in child CHANGED
        settingsListener = new ChildEventListener(){

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildChanged: SETTING CHANGED: STRING:"+s+", SNAPSHOT KEY: "+dataSnapshot.getKey());

                if(dataSnapshot.getKey() != null)
                {
                    // No need to update this if host TODO: remove entire listener for host before the fact
                    if(!main.me.getHosting())
                    {
                        switch(dataSnapshot.getKey())
                        {
                            case "roundTimeMins":
                                timerText.setText(Long.toString((long) dataSnapshot.getValue()));
                                main.room.getSettings().setRoundTimeMins(((Long)dataSnapshot.getValue()).intValue());
                                break;
                            case "allTrueEnabled":
                                switchAngels.setChecked((boolean) dataSnapshot.getValue());
                                main.room.getSettings().setAllTrueEnabled((boolean) dataSnapshot.getValue());
                                break;
                            case "lewdnessEnabled":
                                switchLewdness.setChecked((boolean) dataSnapshot.getValue());
                                main.room.getSettings().setAllTrueEnabled((boolean) dataSnapshot.getValue());
                                break;
                            default:
                        }
                    }
                }

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

        // LISTENERS OF DUBIOUS VALUE TODO: Determine whether I actually need them

        // To be applied to "room/playersManifest/players" node
        // Interest in child ADDED and child REMOVED
        playersManifestChildrenListener = new ChildEventListener(){

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildAdded: PLAYER MANIFEST ADDED TO");
                // if(main.me.getHosting()) manageManifestToPlayers(dataSnapshot,'+');
                main.sounds.get("pop").start();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onChildAdded: PLAYER MANIFEST REMOVE FROM");
                // if(main.me.getHosting()) manageManifestToPlayers(dataSnapshot,'-');
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // To be applied to the ...roomHost node
        roomHostListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: ROOM HOST VALUE CHANGED");



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void removeListeners(){
        // Remove listeners from their particular nodes
        main.playersManifestRef.child("players").removeEventListener(playersManifestChildrenListener);
        main.myRoomRef.child("players").removeEventListener(playersListener);
        main.myRoomRef.child("settings").removeEventListener(settingsListener);
        main.myRoomRef.child("roomHost").removeEventListener(roomHostListener);
        main.myRoomRef.child("state").removeEventListener(stateListener);

    }

    private void moveToLoading() {

        removeListeners();

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_waitingRoom_to_gamePhase1DelegateRoles2);
        }

    private void updateList(Boolean playerAdded) {

        if(this.getClass().getName().contains(main.getForegroundFragment().getClass().getSimpleName()))

        {
            ArrayAdapter<Player> adapter =
                    new MemberListAdapter(main,0, (ArrayList<Player>) main.playersList,playerAdded);
            membersList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }

    private void manageManifestToPlayers(DataSnapshot playersData, char change) {

        ArrayList<Player> players = main.room.getPlayers();

        if(change == '+'){
            Log.i(TAG, "manageManifestToPlayers: player ADDED");
            players.add(new Player(playersData.getKey()));
        }else if(change == '-'){
            Log.i(TAG, "manageManifestToPlayers: player REMOVED");
            players.remove( (int) playersData.getValue());
        }

        main.myRoomRef.child("players").setValue(players, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                main.room.setPlayers(players);
            }
        });

    }

    private void changeFragStateToLoading() {
        main.myRoomRef.child("players").setValue(main.playersList, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: READY TO PROCEED");

                main.myRoomRef.child("state").child("phase").setValue("loading", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Log.i(TAG, "onComplete: FRAG set to LOADING");
                    }
                });

            }
        });
    }

    private void unlockHosting(boolean giveHostPrivileges) {

        if(giveHostPrivileges) {
            btnBeginGame.setVisibility(View.VISIBLE);
            timerAdd.setEnabled(true);
            timerMinus.setEnabled(true);
            switchAngels.setEnabled(true);
            switchLewdness.setEnabled(true);
        }
        else {
            btnBeginGame.setVisibility(View.GONE);
            timerAdd.setEnabled(false);
            timerMinus.setEnabled(false);
            switchAngels.setEnabled(false);
            switchLewdness.setEnabled(false);
        }

    }


    @Override
    public void onDetach() {

        Log.i(TAG, "onDetach: WAITING ROOM ON DETACH CALLED");
        
        Log.i(TAG, "onDetach: called");

        super.onDetach();
        mListener = null;

    }

    @Override // from WaitingRoomInteraction
    public void moveToMainMenu() {

        // Leave room
        if(main.playersList != null){
            Log.i(TAG, "moveToMainMenu: main.playersList exists (good)");
            leaveRoom();
        }else{
            Log.i(TAG, "moveToMainMenu: main.playersList NULL (not so good)");
         main.myRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener(){

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: PLAYERS DATA CHANGED");

                    // Gets Player objects in list
                    GenericTypeIndicator<ArrayList<Player>> t = new GenericTypeIndicator<ArrayList<Player>>() {};
                    ArrayList<Player> players = dataSnapshot.getValue(t);
                    main.playersList = players;

                    removeListeners();
                    leaveRoom();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }



    }

    private void leaveRoom() {

        leavingRoom = true; // Stops list from updating

        // Find self in room
        boolean selfFound = false;
        Iterator<Player> iterator = main.playersList.iterator();
        int index = -1;

        while(iterator.hasNext() && !selfFound){
            if(iterator.next().getName().equals(main.me.getName())) selfFound = true;
            index++;
        }

        removeListeners();

        // Remove self
        main.playersList.remove(index);

        // If we were the last player (playersList empty)
        if(main.playersList.size() == 0)
        {
            // Immediately remove room from manifest. This will prevent people joining.
            main.roomsManifestRef.child("rooms").child(main.myCode.getCode().toString()).setValue(null);
            // main.room.getRoomCode().getCode()
            // Then remove room
            main.myRoomRef.setValue(null);

            moveToMainFinal();
        }
        else
        {
            // Must pick a new host if hosting
            if(main.me.getHosting())
            {
                double randomIndex = Math.random() * main.playersList.size();
                main.playersList.get((int) randomIndex).setHosting(true);

                Log.i(TAG, "leaveRoom: NEW HOST SET TO "+main.playersList.get((int) randomIndex).getName());

            }

            main.myRoomRef.child("players").setValue(main.playersList, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    // Remove self from manifest
                    main.playersManifestRef.child("players").child(main.me.getName()).setValue(null,
                            new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    /*Bundle bundle = new Bundle();
                                    bundle.putBoolean("showButtonsNow",true);*/

                                    moveToMainFinal();
                                }
                            });

                }
            });
        }


    }

    private void moveToMainFinal() {

        // Return to Main Menu
        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        // bundle args

        WaitingRoomDirections.ActionWaitingRoomToMainMenu action = WaitingRoomDirections.actionWaitingRoomToMainMenu();
        action.setShowButtonsNow(true);
        navController.navigate(action);

    }

    @Override
    public void onResume() {
        super.onResume();

        addListeners();
    }

    private void addListeners() {
        // Attach listeners
        main.playersManifestRef.child("players").addChildEventListener(playersManifestChildrenListener);
        main.myRoomRef.child("players").addValueEventListener(playersListener);
        main.myRoomRef.child("settings").addChildEventListener(settingsListener);
        main.myRoomRef.child("roomHost").addValueEventListener(roomHostListener);
        main.myRoomRef.child("state").addValueEventListener(stateListener);

    }

    @Override
    public void onPause() {
        super.onPause();

        removeListeners();

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public WaitingRoom() {
        // Required empty public constructor
    }

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



    /*
    // TO-DO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TO-DO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    *//**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WaitingRoom.
     *//*
    // TO-DO: Rename and change types and number of parameters
    public static WaitingRoom newInstance(String param1, String param2) {
        WaitingRoom fragment = new WaitingRoom();
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
    }

    */




}