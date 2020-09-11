package com.arrowsmith.llv1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.arrowsmith.llv1.classes.Achievement;
import com.arrowsmith.llv1.classes.Player;
import com.arrowsmith.llv1.classes.ResultsData;
import com.arrowsmith.llv1.classes.Room;
import com.arrowsmith.llv1.classes.RoomCode;
import com.arrowsmith.llv1.classes.RoundDataInterpreter;
import com.arrowsmith.llv1.classes.TurnData;
import com.arrowsmith.llv1.classes.ViewPagerAdapter;
import com.arrowsmith.llv1.dialogs.CreateGameDialogFragment;
import com.arrowsmith.llv1.dialogs.InfoDialog;
import com.arrowsmith.llv1.dialogs.JoinGameDialogFragment;
import com.arrowsmith.llv1.dialogs.QuitFromGameRoundFragment;
import com.arrowsmith.llv1.dialogs.QuitGameDialogFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,

        Tutorial.OnFragmentInteractionListener,
        WaitingRoom.OnFragmentInteractionListener,
        GamePhase1DelegateRoles.OnFragmentInteractionListener,
        GamePhase2TextEntryFragment.OnFragmentInteractionListener,
        GamePhase3ChooseWhoseTurn.OnFragmentInteractionListener,
        GamePhase4PlayFragment.OnFragmentInteractionListener,
        GamePhase5RevealsIntro.OnFragmentInteractionListener,
        GamePhase6RevealsBaseFragment.OnFragmentInteractionListener,
        GamePhase7RevealsFragment.OnFragmentInteractionListener,
        GamePhase8ResultsFragment.OnFragmentInteractionListener,

        CreateGameDialogFragment.CreateGameListener,
        JoinGameDialogFragment.JoinGameListener,
        QuitGameDialogFragment.QuitListener,
        QuitFromGameRoundFragment.QuitFromGameListener,

        TextEntryInteraction

{



    public static final String TAG = "debugtag";

    // DEBUG ONLY
    public final boolean adFree = true;


    public AdView adView;

    public Player me;
    public RoomCode myCode;
    public Room room;
    public ArrayList<Player> playersList;
    public HashMap<String,String> playersText;
    public ArrayList<Player> playersLeftToPlay;
    public ArrayList<Player> playersWhovePlayed;
    public HashMap<String,ArrayList<String>> playersVotes;
    public String whoseTurn;
    public String roundContent;
    public String currentState;
    public Player meTemp;

    public HashMap<String, ArrayList<Integer>> playersTimes;
    public HashMap<String, Boolean> playersTruth;
    public HashMap<String, Integer> playersScores;
    public List<TurnData> turnDataList;
    public ResultsData resultsData;

    public FirebaseDatabase database; // Database instance
    public DatabaseReference roomsRef; // Reference to all rooms
    public DatabaseReference roomsManifestRef; // Reference to all rooms
    public DatabaseReference myRoomRef; // Reference to my room
    public DatabaseReference playersManifestRef; // Reference to my room

    public NavController navController;
    public SharedPreferences prefs;
    public String playerNamePref;

    public String fragStateNow;
    public DatabaseReference playersReadyManifestRef;
    public ValueEventListener readyStatusListener;
    public DatabaseReference playersTextManifestRef;
    public RoundDataInterpreter interpreter;

    // Sounds;
    HashMap<String,MediaPlayer> sounds;

    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hides system bars
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility == 0)
                {
                    decorView.setSystemUiVisibility(hideSystemBars());
                }
            }
        });

        // Supposed to stop nav bar from showing up on pop up
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        if(!adFree) setUpAds();
        else findViewById(R.id.layoutAdview).setVisibility(View.GONE);

        // Set up database and references
        database = FirebaseDatabase.getInstance();
        roomsRef = database.getReference("rooms");
        roomsManifestRef = database.getReference("roomsManifest");

        // Get sounds
        sounds = new HashMap<>();
        sounds.put("boop",MediaPlayer.create(this, R.raw.create_or_join_game_button_tone));
        sounds.put("click",MediaPlayer.create(this, R.raw.minor_button_click));
        sounds.put("settings_click",MediaPlayer.create(this, R.raw.settings_button_click));
        sounds.put("pop",MediaPlayer.create(this, R.raw.waiting_room_player_join));
        sounds.put("draw_card",MediaPlayer.create(this, R.raw.role_selection_draw_card));
        sounds.put("shuffle",MediaPlayer.create(this, R.raw.role_selection_shuffle));
        sounds.put("alarm",MediaPlayer.create(this, R.raw.timer_end_alarm));
        sounds.put("round_end",MediaPlayer.create(this, R.raw.create_or_join_game_button_tone)); // Might want to separate these two
        sounds.put("timer_bleed",MediaPlayer.create(this, R.raw.create_or_join_game_button_tone)); // Might want to separate these two

        // Get shared preferences, if they exist
        prefs = getSharedPreferences("PREFS",0);
        playerNamePref = prefs.getString("playerName","");

        // Check if tutorial required
        boolean firstStart = prefs.getBoolean("firstStart",true);
        Log.i(TAG, "onCreateView: MAIN MENU: firstStart: "+firstStart);

        if(firstStart)
        {
            moveToTutorial();
        }
    }

    private void moveToTutorial() {

        NavController navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);
        navController.navigate(R.id.action_mainMenu_to_tutorial);

    }

    private void setUpAds() {

        adView = findViewById(R.id.adView);

        MobileAds.initialize(this,"ca-app-pub-5234994484736807~3449578979");

        // DEBUG ONLY
        List<String> testDeviceIds = Arrays.asList("A787FA9424EAEC1AE5E920573A7C4A42");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        // DEBUG ONLY

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static int getAdViewHeightInDP(Activity activity) {

        int adHeight = 0;

        int screenHeightInDP = getScreenHeightInDP(activity);
        if (screenHeightInDP < 400)
            adHeight = 32;
        else if (screenHeightInDP <= 720)
            adHeight = 50;
        else
            adHeight = 90;

        return adHeight;
    }

    public static int getScreenHeightInDP(Activity activity) {

        /*DisplayMetrics displayMetrics = ((Context) activity).getResources().getDisplayMetrics();
        float screenHeightInDP = displayMetrics.heightPixels
                /// displayMetrics.density
                ;*/

        int width = 0, height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();

        display.getRealMetrics(metrics);

        width = metrics.widthPixels;
        height = metrics.heightPixels;


        return Math.round(height);
    }

    public static int getScreenWidthInDP(Activity activity) {

        /*DisplayMetrics displayMetrics = ((Context) activity).getResources().getDisplayMetrics();
        float screenHeightInDP = displayMetrics.heightPixels
                /// displayMetrics.density
                ;*/

        int width = 0, height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();

        display.getRealMetrics(metrics);

        width = metrics.widthPixels;
        height = metrics.heightPixels;


        return Math.round(width);
    }

    public int hideSystemBars(){
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    public void playSound(String soundName)
    {
        if(prefs.getBoolean("soundOn", true))
        {
            sounds.get(soundName).seekTo(0);
            sounds.get(soundName).start();
        }
    }

    public int getDurationLength(int id, Activity activity) {
        MediaPlayer mp = MediaPlayer.create(activity, id);
        return mp.getDuration();
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
    private void changeMainMenuUI(String statusUpdate, Boolean showNotif, Boolean showDots, Boolean enableButtonsAfter)
    {
        // Get current fragment, hoping to god that it's the main menu
        MainMenu mainMenu;
        try {
            mainMenu = (MainMenu) getForegroundFragment();

            // Change UI
            mainMenu.changeUI(statusUpdate, showNotif, showDots, enableButtonsAfter);
        } catch (ClassCastException e) {
            Log.i(TAG, "changeMainMenuUI: ERROR: " + e.getMessage());
            return;
        } catch (Exception e) {
            Log.i(TAG, "changeMainMenuUI: ERROR: " + e.getMessage());
            return;
        }

    }

    @Override
    public void onHostNameEntered(String name) {
        Log.i(TAG, "onHostNameEntered: called");

        if(checkInternetConnection())
        {
            // Change UI
            changeMainMenuUI("Attempting to create room", true, true, false);

            // Updates preferred player name, based on returned name
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("playerName", name);
            editor.apply();

            playerNamePref = name;


            // Get roomsManifest and proceed to create room
            getRoomsManifest(null);
        }
        else
        {
            changeMainMenuUI("Internet connection required to play",false,null,true);
        }


    }

    @Override
    public void onJoinDetailsEntered(String name, String gameCode) {
        Log.i(TAG, "onJoinDetailsEntered: ");

        if(checkInternetConnection())
        {
            // Change UI
            changeMainMenuUI("Searching for room", true, true, false);

            // Updates preferred player name, based on returned name
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("playerName", name);
            editor.apply();

            playerNamePref = name;

            getRoomsManifest(new RoomCode(gameCode));
        }
        else
        {
            changeMainMenuUI("Internet connection required to play",false,null,true);
        }

    }

    private boolean checkInternetConnection() {

        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();

        for(NetworkInfo info : networkInfos)
        {
            if(info.getTypeName().equalsIgnoreCase("WIFI"))
                if(info.isConnected()) have_WIFI = true;
            if(info.getTypeName().equalsIgnoreCase("MOBILE"))
                if(info.isConnected()) have_MobileData = true;
        }

        return have_MobileData || have_WIFI;
    }


    private void getRoomsManifest(@Nullable RoomCode roomCode) {
        Log.i(TAG, "getRoomsManifest: called");

        roomsManifestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: getRoomsManifest");

                if(roomCode == null) {

                    try {
                        generateValidCode(dataSnapshot.child("rooms").getChildren(),100);
                    } catch (Exception e) {
                        changeMainMenuUI(e.getMessage(),false,null,true);
                    }

                }else{
                    checkRoomsForCode(dataSnapshot.child("rooms").getChildren(),roomCode);
                }

                roomsManifestRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: getRoomsManifest");
            }
        });
        //roomsManifestRef.child("refresh").setValue("");
    }


    private void generateValidCode (@Nullable Iterable<DataSnapshot> rooms, int attemptsAllowed)
    throws Exception{
        Log.i(TAG, "generateValidCode: called");

        List<String> existingRooms = new ArrayList<>();
        if(rooms != null)
        {
            for(DataSnapshot room : rooms)
            {
                existingRooms.add(room.getKey());
            }
        }

        // Initial strings
        int attempts = attemptsAllowed;
        String code = "";
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        boolean codeGot = false;
        double rand1,rand2,rand3,rand4,rand5;

        do{ //
            code = "";

            // Generates random code sequence
            while (code.length()!=5) {

                rand1 = Math.random();
                rand2 = Math.random();
                rand3 = Math.random();
                rand4 = Math.random();
                rand5 = Math.random();

                code = String.valueOf(alphabet.charAt((int)(rand1 * 26)))
                        + String.valueOf(alphabet.charAt((int) (rand2 * 26)))
                        + String.valueOf(alphabet.charAt((int) (rand3 * 26)))
                        + (int) (rand4 * 10)
                        + (int) (rand5 * 10);

                Log.i(TAG, "generateValidCode: random code generated: "+code);

                // DEBUG FLAG - FORCE CODE
                code = "ABC51";
            }

            if(rooms == null) {
                Log.i(TAG, "generateValidCode: ROOMS NULL");
                codeGot = true;
            }
            else {

                boolean conflictFound = false;

                //Iterator<DataSnapshot> roomsIterator = rooms.iterator();

                for(String roomCode : existingRooms){
                    if(roomCode.equals(code.toString())) conflictFound = true;
                }

                if(!conflictFound) codeGot = true;
            }

            attempts--;
            
            Log.i(TAG, "generateValidCode: attempts remaining:"+attempts);
        }while (!codeGot && attempts > 0);

        if(codeGot) {

            // Instantiate for later
            playersManifestRef = database.getReference(
                    "rooms/"
                            + code
                            + "/playersManifest");

            createRoom(new RoomCode(code));
        }

        if (attempts == 0 && !codeGot)
        {
            throw new Exception("Failed to create room! Try again later");
        }
    }

    private void checkRoomsForCode(Iterable<DataSnapshot> rooms, RoomCode roomCode) {
        Log.i(TAG, "checkRoomsForCode: called");

        Iterator<DataSnapshot> iterator = rooms.iterator();
        boolean codeFound = false;

        while(iterator.hasNext() && !codeFound){
            if(iterator.next().getKey().equals(roomCode.getCode())){
                codeFound = true;
            }
        }

        if(codeFound){

            changeMainMenuUI("Room found! Joining",null,true,false);

            // Attempt to add player to room
            playersManifestRef = database.getReference(
                    "rooms/"
                    + roomCode.getCode()
                    + "/playersManifest");
            playersManifestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> iterator = dataSnapshot.child("players").getChildren().iterator();

                    boolean repeatFound = false;

                    while (iterator.hasNext() && !repeatFound){

                        if (iterator.next().getKey().equals(playerNamePref)) repeatFound = true;

                    }

                    if (repeatFound) {

                        // Change UI
                        changeMainMenuUI(
                                "Sorry, there's already someone named " + playerNamePref + " in that room!",
                                false, null, true);
                    } else {

                        myRoomRef = roomsRef.child(roomCode.getCode());
                        joinGame(roomCode);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //playersManifestRef.child("refresh").setValue("");


        }
        else
        {
            // Change UI
            changeMainMenuUI(
                    "Could not find room " + roomCode.getCode(),
                    false, null, true);
        }



    }



    private void createRoom(RoomCode roomCode) {

        // First, establish ourselves as player
        me = new Player(playerNamePref);
        me.setHosting(true);
        me.setPoints(0);

        // Create room object
        room = new Room(roomCode, me);

        myRoomRef = roomsRef.child(roomCode.getCode());

        // Add room - automatically add us in manifests
        myRoomRef.setValue(room, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                // Put room name in room manifest
                roomsManifestRef.child("rooms").child(roomCode.getCode()).setValue("", new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        // Save room code
                        myCode = (RoomCode) roomCode;

                        // Move to room
                        moveToLobby();
                    }
                });

            }
        });


    }

    private void joinGame(RoomCode roomCode) {

        // First, establish ourselves as player
        me = new Player(playerNamePref);
        me.setHosting(false);
        me.setPoints(0);

        // Add self to room
        myRoomRef.child("players").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                GenericTypeIndicator<ArrayList<Player>> t = new GenericTypeIndicator<ArrayList<Player>>() {};
                ArrayList<Player> players = dataSnapshot.getValue(t);
                players.add(me);

                myRoomRef.child("players").setValue(players, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        // Add self to room manifest
                        myRoomRef = roomsRef.child(roomCode.getCode());
                        myRoomRef.child("playersManifest").child("players").child(me.getName()).setValue("",
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        // Save room code
                                        myCode = (RoomCode) roomCode;

                                        // Move to room
                                        moveToLobby();
                                    }
                                });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void moveToLobby() {

        MainMenu mainMenu;
        try {
            mainMenu = (MainMenu) getForegroundFragment();
        } catch (ClassCastException e) {
            Log.i(TAG, "onHostNameEntered: " + e.getMessage());
            return;
        } catch (Exception e) {
            Log.i(TAG, "onHostNameEntered: " + e.getMessage());
            return;
        }

        mainMenu.moveToWaitingRoom();
    }

    @Override
    public void onBackPressed() {

        // Get current fragment
        Fragment f = getForegroundFragment();

        // Get class name
        String fragClass = f.getClass().getSimpleName();

        // Decide what to do
        switch(fragClass){
            case "MainMenu":
                Log.i(TAG, "onBackPressed: MAIN MENU SCENARIO EVOKED");
                this.finish();
                break;
            case "WaitingRoom":
                Log.i(TAG, "onBackPressed: WAITING ROOM SCENARIO EVOKED");
                launchDialog("quit_fragment");
                break;
            default:
                if(fragClass.startsWith("GamePhase"))
                {
                    if( !(((Character) fragClass.charAt(9)).equals('1')))
                    {

                        Log.i(TAG, "onBackPressed: GAME SCENARIO EVOKED");
                        launchDialog("quit_from_game_fragment");
                    }

                }else
                {
                    Log.i(TAG, "onBackPressed: ERROR - invalid fragClass \""+fragClass+"\"");
                }

        }

    }

    /**
     * Launch dialog fragment that corresponds to a tag
     * @param tag Fragment tag
     */
    public void launchDialog(String tag){

        // Initialise fragment manager and transaction common to all DialogFragment objects
        FragmentManager sfm = getSupportFragmentManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = sfm.findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Launches correct DialogFragment based on tag provided
        switch(tag){
            case "create_game_fragment":
                DialogFragment createGameDialogFragment = new CreateGameDialogFragment();
                createGameDialogFragment.show(ft, tag);
                break;
            case "join_game_fragment":
                DialogFragment joinGameDialogFragment = new JoinGameDialogFragment();
                joinGameDialogFragment.show(ft, tag);
                break;
            case "quit_fragment":
                DialogFragment quitGameDialogFragment = new QuitGameDialogFragment();
                quitGameDialogFragment.show(ft, tag);
                break;
            case "quit_from_game_fragment":
                DialogFragment quitFromGameRoundFragment = new QuitFromGameRoundFragment();
                quitFromGameRoundFragment.show(ft, tag);
                break;
            case "info_fragment":
                InfoDialog infoDialog = new InfoDialog();
                infoDialog.show(ft,tag);
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    public Fragment getForegroundFragment(){
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.my_nav_host_fragment);
        return navHostFragment == null ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * Return to MainMenu from WaitingRoom
     */
    @Override // from QuitListener
    public void quit() {
        Log.i(TAG, "quit: BEGIN QUIT");

        WaitingRoom waitingRoom;
        try {
            waitingRoom = (WaitingRoom) getForegroundFragment();
        } catch (ClassCastException e) {
            Log.i(TAG, "onHostNameEntered: " + e.getMessage());
            return;
        } catch (Exception e) {
            Log.i(TAG, "onHostNameEntered: " + e.getMessage());
            return;
        }

        // TODO: Handle exceptions
        waitingRoom.moveToMainMenu();

    }

    /**
     * Return to WaitingRoom from any Game state
     */
    @Override // from QuitFromGameListener
    public void quitFromGame() {

        myRoomRef.child("state").child("phase").setValue("lobby", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                myRoomRef.child("state").child("inSession").setValue(false, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    }
                });
            }
        });


    }



    public void checkReadyStatus(String readyFor) {

        if(playersReadyManifestRef == null) playersReadyManifestRef = myRoomRef.child("playersReadyManifest");
        readyStatusListener = new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean proceeded = false;

                if(dataSnapshot.getValue() != null
                && dataSnapshot.getChildrenCount() !=0){

                    int readyCount = 0;
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while(iterator.hasNext()){
                        if((Boolean) iterator.next().getValue()) readyCount++;
                    }


                    if(playersList != null)
                    {
                        if(readyCount == playersList.size()){

                            playersReadyManifestRef.removeEventListener(this);
                            playersReadyManifestRef.setValue(null);

                            Log.i(TAG, "onDataChange: PLAYERS READY: COUNT: "+readyCount);

                            proceed(readyFor);
                            proceeded = true;
                        }
                    }
                    else
                    {
                        Log.i(TAG, "onDataChange: MAIN ACTIVITY CHECK READY STATUS: NULL PLAYERLIST");
                    }

                    // If number of ready players == number of players
                    // TODO: Send to textentry for UI update
                    // dataSnapshot.getValue(Manifest.class);

                }

                if(!proceeded)
                    playersReadyManifestRef.child("players").addListenerForSingleValueEvent(readyStatusListener);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        playersReadyManifestRef.child("players").addListenerForSingleValueEvent(readyStatusListener);

    }

    public void proceed(String readyFor) {
        Log.i(TAG, "proceed: PROCEEDING TO "+readyFor.toUpperCase());

        // Ensures we are moving to a NEW state
            myRoomRef.child("state").child("phase").setValue(readyFor, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Log.i(TAG, "onComplete: PHASE SET TO "+readyFor.toUpperCase());
                }
            });

/*
        "getTextManifestInfo":
                // Once everyone has declared they are ready after having submitted text entries for play
                // Next steps:
                //  Get all players to take snapshot of players list and save it
                //  Wait for result
                //  Randomly choose a player to play and set them as WHOSETURN

            "readWhoseTurn"
                // Once everyone has successfully got the data, host will set whoseturn value
                // Host will alert everyone to acknowledge whose turn it is
                // Then we will proceed to CHOOSE phase

            "choose"
                // Once everyone has received and processed whose turn it is, it's time to finally start a round
                // Beginning with the fake "choose" phase. Set phase accordingly.

                // Once everyone has received and processed whose turn it is, it's time to finally start a round
                // Beginning with the fake "choose" phase. Set phase accordingly.

            "endOfPlay"
                // Once everyone has received and processed whose turn it is, it's time to finally start a round
                // Beginning with the fake "choose" phase. Set phase accordingly.


            "continue"
                    // Proceed from endOfPlay*/

        }

    @Override // From TextEntryInteraction
    public void setReady(Boolean isReady) {

        if(playersReadyManifestRef == null) playersReadyManifestRef = myRoomRef.child("playersReadyManifest");
        playersReadyManifestRef.child("players").child(me.getName()).setValue(isReady);

    }



    /**
     * HOST ONLY: Set next player turn
     */
    public void setNewWhoseTurn() {
        Log.i(TAG, "setWhoseTurn: CALLED");

        // Generate random index on remaining players
        int randomIndex = (int) (Math.random()*playersLeftToPlay.size());

        // Get random player and remove them from remaining players
        String playerWhoseTurn = playersLeftToPlay.get(randomIndex).getName();

        // Save local information
        whoseTurn = playerWhoseTurn;

        // Set whose turn it is on the database
/*
        myRoomRef.child("whoseTurn").setValue(playerWhoseTurn, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: WHOSETURN VALUE SET");

                // Upon value set completion
                setReady(true);

                checkReadyStatus("readNewWhoseTurn");

            }
        });
*/

    }



    ViewPagerAdapter pagerAdapter;



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {

            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void resetLocalVarsForNewround() {
/*
        // This should also reset "me"
        for(Player p : playersList){
            p.setText(null);
            p.setTruth(null);
            p.setTarget(null);
            p.setVotes(null);
            p.setReady(null);
        }

        *//*public Player me;
        public RoomCode myCode;
        public Room room;*//*

        playersText.clear();
        playersWhovePlayed.clear();
        playersTimes.clear();
        playersTruth.clear();
        // playersScores.clear();

        playersLeftToPlay.clear();
        playersLeftToPlay.addAll(playersList);

        playersVotes.clear();
        whoseTurn = null;
        roundContent = null;
        currentState = null;

        turnDataList = null;
        resultsData = null;

        // Tidy room
        room.setPlayers(playersList);*/
    }

    public void printPlayers() {

        for (Player p : playersList)
        {
            Log.i(TAG, "printPlayers: from "+getForegroundFragment().getClass().toString()
                    +" - "+p.getName()
                    +" - "+p.getHosting()
                    +" - "+p.getTruth()
                    +" - "+p.getPoints()
                    +" - "+p.getText()
                    +" - "+p.getVotes()
                    +" - "+p.getVoteTimes()
                    +" - "+p.getTarget());
        }

    }
}
interface MainMenuInteraction{
    void changeUI(String statusUpdate, Boolean showNotif, Boolean showDots, Boolean enableButtonsAfter);
    void moveToWaitingRoom();
}
interface WaitingRoomInteraction{
    // void changeUI(Boolean enableButtons,String statusUpdate);
    void moveToMainMenu();
}