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
import android.widget.Toast;

import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.ResultsData;
import com.arrowsmith.llv1.classes.RoundData;
import com.arrowsmith.llv1.classes.RoundDataInterpreter;
import com.arrowsmith.llv1.classes.TurnData;
import com.arrowsmith.llv1.classes.ViewPagerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.arrowsmith.llv1.MainActivity.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class GamePhase5RevealsIntro extends Fragment {


    private View view;
    private Button buttonProceed;
    private MainActivity main;
    private GamePhase6RevealsBaseFragment revealsBaseFrag;
    private ValueEventListener stateListener;

    public GamePhase5RevealsIntro() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.gp5_reveals_intro_fragment, container, false);
        main = (MainActivity) getActivity();

        main.printPlayers();

        addStateListener();

        buttonProceed = view.findViewById(R.id.button_escape_intro);
        buttonProceed.setVisibility(View.GONE);
        buttonProceed.setOnClickListener(v ->{

            main.setReady(true);
            if (main.me.getHosting()) main.checkReadyStatus("firstPage");

        });

        uploadVotes();

        return view;

    }

    private void addStateListener() {
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
                            case "downloadVotes":

                                onDownloadVotes();

                                break;
                            case "pointsUploaded":

                                readyUpForRevealsPager();

                                break;
                            case "firstPage":

                                onFirstPage();

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

    private void onFirstPage() {

        moveToReveals();
    }

    private void uploadVotes() {
        Log.i(TAG, "onUploadVotes: CALLED");

        ArrayList<String> votesUpload = new ArrayList<>();

        for(String v : main.me.getVotes()){
            votesUpload.add(v.toString());
        }

        main.myRoomRef.child("playersVotesManifest").child("players").child(main.me.getName()).setValue(votesUpload, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                uploadVoteTimes();
            }
        });

    }

    private void uploadVoteTimes() {

        ArrayList<Integer> voteTimesUpload = new ArrayList<>();

        for(Integer t : main.me.getVoteTimes()){
            voteTimesUpload.add(t);
        }

        main.myRoomRef.child("playersTimesManifest").child("players").child(main.me.getName()).setValue(voteTimesUpload, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                main.setReady(true);
                if(main.me.getHosting()) main.checkReadyStatus("downloadVotes");
            }
        });

    }

    private void onDownloadVotes() {
        Log.i(TAG, "onDownloadVotes: CALLED");

        main.myRoomRef.child("playersVotesManifest").child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                GenericTypeIndicator<HashMap<String,ArrayList<String>>> t = new GenericTypeIndicator<HashMap<String,ArrayList<String>>>() {};
                HashMap<String,ArrayList<String>> votes = dataSnapshot.getValue(t);
                main.playersVotes = votes;

                onDownloadVoteTimes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void onDownloadVoteTimes() {
        Log.i(TAG, "onDownloadVotesTimes: CALLED");

        main.myRoomRef.child("playersTimesManifest").child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                GenericTypeIndicator<HashMap<String,ArrayList<Integer>>> t = new GenericTypeIndicator<HashMap<String,ArrayList<Integer>>>() {};
                HashMap<String,ArrayList<Integer>> times = dataSnapshot.getValue(t);
                main.playersTimes = times;

                onDownloadTruth();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void onDownloadTruth() {
        Log.i(TAG, "onDownloadTruth: CALLED");

        main.myRoomRef.child("playersTruthManifest").child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                GenericTypeIndicator<HashMap<String,Boolean>> t = new GenericTypeIndicator<HashMap<String,Boolean>>() {};
                HashMap<String,Boolean> truth = dataSnapshot.getValue(t);

                main.playersTruth = truth;

                commissionViewPagerCreation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void commissionViewPagerCreation() {

        Log.i(TAG, "commissionViewPagerCreation: CALLED");

        HashMap<String,String> playersToTargets = new HashMap<>();
        for (Player p: main.playersList)
        {
            playersToTargets.put(p.getName(),
                    (p.getTarget() != null ? p.getTarget() : p.getName()));
        }

        printRoundDataInput(main.me,
                main.playersWhovePlayed,
                main.playersTruth,
                main.playersVotes,
                main.playersTimes,
                main.playersScores,
                main.playersText);

        RoundData data = new RoundData(main.me,
                main.playersWhovePlayed,
                main.playersTruth,
                main.playersVotes,
                main.playersTimes,
                main.playersScores,
                main.playersText);

        RoundDataInterpreter interpreter = new RoundDataInterpreter(data);
        interpreter.interpret();

        main.interpreter = interpreter;

        main.turnDataList = interpreter.getTurnData();
        main.resultsData = interpreter.getResultsData();
        main.playersScores = interpreter.getResultsData().getNewScores();

        main.meTemp = new Player(main.me.getName());
        main.meTemp.setVotes(main.me.getVotes());
        main.meTemp.setVoteTimes(main.me.getVoteTimes());

        if(main.me.getHosting()) updatePlayers();
        else main.setReady(true);

    }

    private void printRoundDataInput(Player me,
                                     ArrayList<Player> playersWhovePlayed,
                                     HashMap<String, Boolean> playersTruth,
                                     HashMap<String, ArrayList<String>> playersVotes,
                                     HashMap<String, ArrayList<Integer>> playersTimes,
                                     HashMap<String, Integer> playersScores,
                                     HashMap<String, String> playersText) {
        Log.i(TAG, "printRoundDataInput: me: "+me.getName()
                +" - "+me.getText()
                +" - "+me.getPoints()
                +" - "+me.getVotes()
                +" - "+me.getVoteTimes());
        for(int i = 0; i < playersWhovePlayed.size(); i++)
        {
            Log.i(TAG, "printRoundDataInput: playersWhovePlayed: "+playersWhovePlayed.get(i));
            Log.i(TAG, "playersTruth: "+playersTruth.get(playersWhovePlayed.get(i).getName()));
            Log.i(TAG, "playersVotes: "+playersVotes.get(playersWhovePlayed.get(i).getName()));
            Log.i(TAG, "playersTimes: "+playersTimes.get(playersWhovePlayed.get(i).getName()));
            Log.i(TAG, "playersScores: "+playersScores.get(playersWhovePlayed.get(i).getName()));
        }

    }

    private void updatePlayers() {

        ArrayList<Player> newPlayersList = new ArrayList<>();

        for(String playerName : main.resultsData.getPlayersOrdered())
        {
            // Find that player in player list
            int index = -1;
            boolean playerFound = false;
            while(!playerFound)
            {
                index++;
                if(main.playersList.get(index).getName().equals(playerName)) playerFound = true;
            }

            // Get player and set score
            Player p = main.playersList.get(index);
            p.setPoints(main.resultsData.getNewScores().get(playerName));

            p.setVotes(null);
            p.setVoteTimes(null);
            p.setTarget(null);
            p.setTruth(null);

            newPlayersList.add(p);
        }

        // Set new player list
        main.playersList = newPlayersList;

        main.printPlayers();

        main.myRoomRef.child("players").setValue(main.playersList, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                main.setReady(true);
                main.checkReadyStatus("pointsUploaded");
            }
        });
    }


    private void moveToReveals() {
        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_gamePhase5RevealsIntro_to_gamePhase6RevealsBaseFragment);
    }


    public void readyUpForRevealsPager() {

        showButton();

    }

    public void showButton() {
        buttonProceed.setVisibility(View.VISIBLE);
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
    private GamePhase6RevealsBaseFragment.OnFragmentInteractionListener mListener;

    // TO-DO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GamePhase6RevealsBaseFragment.OnFragmentInteractionListener) {
            mListener = (GamePhase6RevealsBaseFragment.OnFragmentInteractionListener) context;
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
