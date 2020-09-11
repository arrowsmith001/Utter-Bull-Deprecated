package com.arrowsmith.llv1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arrowsmith.llv1.classes.Achievement;
import com.arrowsmith.llv1.classes.CustomExpandableListAdapter;
import com.arrowsmith.llv1.classes.MemberListAdapter;
import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.PlayerAndScore;
import com.arrowsmith.llv1.classes.ResultsData;
import com.arrowsmith.llv1.classes.RoleAssigner;
import com.arrowsmith.llv1.classes.Round;
import com.arrowsmith.llv1.classes.Utility;
import com.arrowsmith.llv1.classes.Voter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.arrowsmith.llv1.MainActivity.TAG;


public class GamePhase8ResultsFragment extends Fragment {

    private ResultsData resultsData;
    private View view;
    private MainActivity main;
    private ValueEventListener stateListener;

    ArrayList<HashMap<String,Object>> resultsInfo;

    public GamePhase8ResultsFragment(ResultsData resultsData){

        this.resultsData = resultsData;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.gp8_results_fragment, container, false);
        main = (MainActivity) getActivity();

        createStateListener();

        // constructRoundSummary();

        return view;
    }

    ArrayList<Round> roundsList;
    
    // Expandable list data
    List<Player> resultsListPlayers;
    HashMap<String,List<Round>> resultsListRounds;

    
    public void constructRoundSummary() {

        Log.i(TAG, "constructRoundSummary: CALLED");


        resultsListPlayers = new ArrayList<>();
        resultsListRounds = new HashMap<>();

        roundsList = new ArrayList<>();

        String output = "";
/*
        hm.put("name",name);
        hm.put("wasInFact",wasInFact);
        hm.put("writtenBy",writtenBy);
        hm.put("trueVoters",trueVoters);
        hm.put("lieVoters",lieVoters);
        hm.put("fastestPlayers",fastestPlayers);*/

        String name, content;
        String wasInFact;
        String writtenBy;
        ArrayList<Voter> trueVoters;
        ArrayList<Voter> lieVoters;
        ArrayList<Voter> xVoters;
        ArrayList<String> fastestPlayers;

        for (Player p : main.playersList) {

            Log.i(TAG, output);
            //textOutput.setText(output);

            // Get rid of unnecessary info
            for (Player p1 : main.playersList) {
                if (p.getName().equals(main.me.getName())) {
                    p.setVotes(null);
                    p.setVoteTimes(null);
                }
            }

            // main.playersList ready to set up UI

            // setLists();

            if (main.me.getHosting()) upLoadPlayersWithPoints();
            else main.setReady(true);
        }

    }

    private void upLoadPlayersWithPoints() {
        Log.i(TAG, "upLoadPlayers: CALLED");

        main.myRoomRef.child("players").setValue(main.playersList, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                Log.i(TAG, "onComplete: CHECKING READY STATUS FOR DOWNLOAD PLAYERS");

                main.setReady(true);
                main.checkReadyStatus("pointsUploaded");
            }
        });
    }


    public void setLists(){

        ExpandableListView expList = view.findViewById(R.id.expListView);

        // TODO: Pop list members in sequentially

        CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(
                getActivity(), resultsData, expList);
        expList.setAdapter(adapter);
        expList.setEnabled(true);

        expList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                try{
                    GamePhase6RevealsBaseFragment gp6 = ((GamePhase6RevealsBaseFragment) main.getForegroundFragment());
                    if(gp6.viewPager.isSwipingUnlocked()) gp6.setPage(childPosition);

                }catch(Exception e)
                {
                    Log.i(TAG, "onChildClick: ERROR: "+e.getMessage());
                }

                return false;
            }
        });

        Button btnExpand = view.findViewById(R.id.button_expand_all);
        Button btnCollapse = view.findViewById(R.id.button_collapse_all);

        btnExpand.setOnClickListener(v -> {
            for(int i = 0; i < adapter.getGroupCount(); i++)
            {
                expList.expandGroup(i,true);
            }
        });
        btnCollapse.setOnClickListener(v -> {
            for(int i = 0; i < adapter.getGroupCount(); i++)
            {
                expList.collapseGroup(i);
            }
        });

    }








    private void createStateListener() {

        stateListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: STATE CHANGED");

                if(!((Boolean) dataSnapshot.child("inSession").getValue())){
                    main.myRoomRef.child("state").removeEventListener(this);
                    NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
                    //navController.navigate(R.id.action_resultsFragment_to_waitingRoom);
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
                            case "newRound":

                                Log.i(TAG, "onDataChange: CASE "+state.toUpperCase()+" DETECTED");

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







    @Override
    public void onResume() {
        super.onResume();
        //main.myRoomRef.child("state").addValueEventListener(stateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        //main.myRoomRef.child("state").removeEventListener(stateListener);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


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


    /* // TO-DO: Rename parameter arguments, choose names that match
     // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
     private static final String ARG_PARAM1 = "param1";
     private static final String ARG_PARAM2 = "param2";

     // TO-DO: Rename and change types of parameters
     private String mParam1;
     private String mParam2;
 */
    private OnFragmentInteractionListener mListener;

    public GamePhase8ResultsFragment() {
        // Required empty public constructor
    }
   /*
    // TO-DO: Rename and change types and number of parameters
    public static GamePhase8ResultsFragment newInstance(String param1, String param2) {
        GamePhase8ResultsFragment fragment = new GamePhase8ResultsFragment();
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
    public interface OnFragmentInteractionListener {
        // TO-DO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
